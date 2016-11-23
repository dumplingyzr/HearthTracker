package dumplingyzr.hearthtracker;

import android.os.Process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogReaderPower implements Runnable {
    private static final int POWER_TASK = 1;
    private static final int DISPLAY_LINE = 1;
    private static final int READ_FILE_FINISH = 2;

    private static final int WAITING_FOR_LOG = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_CREATE_GAME = 1;
    private static final int STATE_INITIAL_HAND = 2;
    private static final int STATE_SWITCH_CARD = 3;
    private static final int STATE_PLAYER_TURN = 4;
    private static final int STATE_OPPONENT_TURN = 5;
    private static final int STATE_FINISH = 6;

    private Integer mTurn = 1;
    private int mState = WAITING_FOR_LOG;
    private int mPrevState = STATE_IDLE;
    private long mPosition = 0;
    private long mPrevPosition = 0;

    private ArrayList<String> mPlayerNames = new ArrayList<>();
    private class Player {
        String name;
        boolean isFirstPlayer = false;
        Player(String _name){
            name = _name;
        }
    }
    private Player mPlayer;
    private Player mOpponent;

    private boolean mCanceled;
    private ByteBuffer mByteBuffer = ByteBuffer.allocate(1000);
    private String mLine = "";

    final RunnablePowerLogReaderMethods mLogParserTask;

    interface RunnablePowerLogReaderMethods {
        void setPowerThread(Thread currentThread);
        /**
         * Pass string to be displayed up to LogParserTask
         * @param line
         */
        void setLogReaderLine(String line);
        void handlePowerState(int state, int task);
    }

    LogReaderPower(RunnablePowerLogReaderMethods parseLogTask){
        mLogParserTask = parseLogTask;
        mCanceled = false;
    }

    @Override
    public void run() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            mLogParserTask.setPowerThread(Thread.currentThread());
            File file = new File(MainActivity.HEARTHSTONE_FILES_DIR + "Logs/Power.log");
            FileInputStream fs = new FileInputStream(file);
            FileChannel fc = fs.getChannel();

            while (!mCanceled) {
                switch (mState) {
                    case STATE_FINISH:
                        mState = STATE_IDLE;
                        break;
                    case WAITING_FOR_LOG:
                        mByteBuffer.clear();
                        //mPrevPosition = mPosition;
                        //mPosition =
                        if(fc.read(mByteBuffer) <= 0){
                            Thread.sleep(1000);
                            continue;
                        } else {
                            mByteBuffer.flip();
                            mState = mPrevState;
                        }
                        //mLogParserTask.handleLogReaderState(READ_FILE_FINISH);
                        break;
                    default:
                        parsePowerLine();
                }
            }
        } catch (IOException|InterruptedException e){
            System.out.println("Failed to open file: Power.log");
            e.printStackTrace();
        }
    }
    private boolean readLine () {
        StringBuilder sb = new StringBuilder(256);
        while (mByteBuffer.hasRemaining()) {
            char c = (char)mByteBuffer.get();
            if(c == '\n' | c == '\r'){
                mLine += sb.toString();
                return true;
            }
            sb.append(c);
        }
        mLine = sb.toString();
        return false;
    }

    private String checkAndStripLine(String line) {
        if (!line.contains("PowerTaskList.DebugPrintPower")) {
            return null;
        }

        int i = line.indexOf("-");
        if (i < 0) {
            Timber.e("bad line: " + line);
            return null;
        }

        /**
         * skip spaces
         */
        int start = i + 1;
        while (start < line.length() && line.charAt(start) == ' ') {
            start++;
        }

        if (start == line.length()) {
            return null;
        }

        line = line.substring(start);

        return line;
    }

    private void parsePowerLine() {
        if(!readLine()) {
            mPrevState = mState;
            mState = WAITING_FOR_LOG;
            return;
        }
        String line = checkAndStripLine(mLine);
        mLine = "";
        if (line == null) {
            return;
        }

        switch (mState) {
            case STATE_IDLE:
                if (line.startsWith("CREATE_GAME")) {
                    mState = STATE_CREATE_GAME;
                    mLogParserTask.setLogReaderLine("Game Started!");
                    mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                }
                break;
            case STATE_CREATE_GAME:
                if (line.startsWith("BLOCK_START BlockType=TRIGGER Entity=GameEntity")) {
                    mPlayer = new Player(mPlayerNames.get(0));
                    mOpponent = new Player(mPlayerNames.get(1));
                    mState = STATE_INITIAL_HAND;
                    mLogParserTask.setLogReaderLine("Player initial hand:\n");
                    mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                } else if (line.startsWith("TAG_CHANGE")) {
                    Pattern p = Pattern.compile(".*Entity=(.*) tag=PLAYSTATE value=PLAYING");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mPlayerNames.add(m.group(1));
                    }
                }
                break;
            case STATE_INITIAL_HAND:
                if (line.startsWith("BLOCK_START BlockType=TRIGGER Entity=GameEntity")) {
                    mState = STATE_SWITCH_CARD;
                    mLogParserTask.setLogReaderLine("Player switch card:\n");
                    mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                } else if(line.startsWith("SHOW_ENTITY")){
                    Pattern p = Pattern.compile(".*Updating Entity=.* CardID=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mPlayer.name + " draws:\n    " + CardAPI.getCard(m.group(1)).name + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                } else if (line.startsWith("TAG_CHANGE")) {
                    Pattern p = Pattern.compile(".*Entity=(.*) tag=FIRST_PLAYER value=(.)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        if(mOpponent.name.equals(m.group(1))) {
                            String tmp = mPlayer.name;
                            mPlayer.name = mOpponent.name;
                            mOpponent.name = tmp;
                        }
                        if(m.group(2).equals("1")) mPlayer.isFirstPlayer = true;
                        else mOpponent.isFirstPlayer = true;
                    }
                }
                break;
            case STATE_SWITCH_CARD:
                if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
                    mState = mPlayer.isFirstPlayer ? STATE_PLAYER_TURN : STATE_OPPONENT_TURN;
                    mLogParserTask.setLogReaderLine("Turn " + mTurn.toString() + "\n");
                    mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                } else if(line.startsWith("SHOW_ENTITY")){
                    Pattern p = Pattern.compile(".*Updating Entity=.* CardID=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mPlayer.name + " draws:\n    " + CardAPI.getCard(m.group(1)).name + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                } else if(line.startsWith("HIDE_ENTITY")){
                    Pattern p = Pattern.compile(".*name=(.*) id=.*");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mPlayer.name + " switches out:\n    " + m.group(1) + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                }
                break;
            case STATE_PLAYER_TURN:
                if(line.startsWith("TAG_CHANGE Entity=GameEntity tag=NEXT_STEP value=FINAL_GAMEOVER")){
                    mState = STATE_FINISH;
                } else if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
                    mState = STATE_OPPONENT_TURN;
                    mTurn++;
                } else if(line.startsWith("SHOW_ENTITY")){
                    Pattern p = Pattern.compile(".*Updating Entity=.*zone=DECK.*CardID=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mPlayer.name + " draws:\n    " + CardAPI.getCard(m.group(1)).name + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                } else if(line.startsWith("BLOCK_START BlockType=POWER")){
                    Pattern p = Pattern.compile(".*name=(.*) id=.*Target.*");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mPlayer.name + " plays:\n    " + m.group(1) + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                }
                break;
            case STATE_OPPONENT_TURN:
                if(line.startsWith("TAG_CHANGE Entity=GameEntity tag=NEXT_STEP value=FINAL_GAMEOVER")){
                    mState = STATE_FINISH;
                } else if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
                    mState = STATE_PLAYER_TURN;
                    mLogParserTask.setLogReaderLine("Turn " + mTurn.toString() + "\n");
                    mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                } else if(line.startsWith("SHOW_ENTITY")) {
                    Pattern p = Pattern.compile(".*Updating Entity=.*zone=DECK.*CardID=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mOpponent.name + " draws:\n    " + CardAPI.getCard(m.group(1)).name + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                } else if(line.startsWith("BLOCK_START BlockType=POWER")){
                    Pattern p = Pattern.compile(".*name=(.*) id=.*Target.*");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        mLogParserTask.setLogReaderLine("  " + mOpponent.name + " plays:\n    " + m.group(1) + "\n");
                        mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                    }
                }
                break;
        }
    }
}
