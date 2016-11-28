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
    private static final int CLEAR_WINDOW = 2;
    private static final int DISPLAY_CARD = 3;
    private static final int REMOVE_CARD = 4;

    private static final int WAITING_FOR_LOG = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_CREATE_GAME = 1;
    private static final int STATE_INITIAL_HAND = 2;
    private static final int STATE_SWITCH_CARD = 3;
    private static final int STATE_PLAYER_TURN = 4;
    private static final int STATE_OPPONENT_TURN = 5;
    private static final int STATE_FINISH = 6;

    private Integer mTurn = 1;
    private int mLineNumber = 0;
    private int mState = WAITING_FOR_LOG;
    private int mPrevState = STATE_IDLE;

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
        void setLogReaderCard(Card card);
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
                        if(fc.read(mByteBuffer) <= 0){
                            Thread.sleep(1000);
                            continue;
                        } else {
                            mByteBuffer.flip();
                            mState = mPrevState;
                        }
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
        StringBuilder sb = new StringBuilder(512);
        while (mByteBuffer.hasRemaining()) {
            char c = (char)mByteBuffer.get();
            if(c == '\n' | c == '\r'){
                mLine += sb.toString();
                mLineNumber += 1;
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

        if (line.startsWith("CREATE_GAME")) {
            mState = STATE_CREATE_GAME;
            mTurn = 1;
            mPlayerNames.clear();
            mLogParserTask.handlePowerState(CLEAR_WINDOW, POWER_TASK);
            mLogParserTask.setLogReaderLine("Game Started!\n");
            mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
            return;
        }

        switch (mState) {
            case STATE_CREATE_GAME:
                if (line.startsWith("BLOCK_START BlockType=TRIGGER Entity=GameEntity")) {
                    /**
                     * Handle corrupted log file
                     */
                    if(mPlayerNames.size()<2){
                        mState = STATE_IDLE;
                        break;
                    }
                    mPlayer = new Player(mPlayerNames.get(0));
                    mOpponent = new Player(mPlayerNames.get(1));
                    mState = STATE_INITIAL_HAND;
                    mLogParserTask.setLogReaderLine("Initial hand:\n");
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
                        //mLogParserTask.setLogReaderLine("  " + mPlayer.name + " draws:\n    " + CardAPI.getCardById(m.group(1)).name + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        mLogParserTask.setLogReaderCard(CardAPI.getCardById(m.group(1)));
                        mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                    }
                } else if (line.startsWith("TAG_CHANGE")) {
                    Pattern p = Pattern.compile(".*Entity=(.*) tag=FIRST_PLAYER value=1");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        if(mPlayer.name.equals(m.group(1)))
                            mPlayer.isFirstPlayer = true;
                        else
                            mOpponent.isFirstPlayer = true;
                    }
                } else if(line.startsWith("TAG_CHANGE")){
                    Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
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
                        //mLogParserTask.setLogReaderLine("  " + mPlayer.name + " draws:\n    " + CardAPI.getCardById(m.group(1)).name + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        mLogParserTask.setLogReaderCard(CardAPI.getCardById(m.group(1)));
                        mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                    }
                } else if(line.startsWith("HIDE_ENTITY")){
                    Pattern p = Pattern.compile(".*name=(.*) id=.*");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        //mLogParserTask.setLogReaderLine("  " + mPlayer.name + " drops:\n    " + m.group(1) + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        mLogParserTask.setLogReaderCard(CardAPI.getCardByName(m.group(1)));
                        mLogParserTask.handlePowerState(REMOVE_CARD, POWER_TASK);
                    }
                } else if(line.startsWith("TAG_CHANGE")){
                    Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
                    }
                }
                break;
            case STATE_PLAYER_TURN:
                if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
                    mState = STATE_OPPONENT_TURN;
                    mTurn++;
                } else if(line.startsWith("SHOW_ENTITY")){
                    Pattern p = Pattern.compile(".*Updating Entity=.*zone=DECK.*CardID=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        //mLogParserTask.setLogReaderLine("  " + mPlayer.name + " draws:\n    " + CardAPI.getCardById(m.group(1)).name + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        mLogParserTask.setLogReaderCard(CardAPI.getCardById(m.group(1)));
                        mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                    }
                } else if(line.startsWith("BLOCK_START BlockType=POWER")){
                    Pattern p = Pattern.compile(".*name=(.*) id=.*Target.*");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        //mLogParserTask.setLogReaderLine("  " + mPlayer.name + " plays:\n    " + m.group(1) + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        //mLogParserTask.setLogReaderCard(CardAPI.getCardById(m.group(1)));
                        //mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                    }
                } else if(line.startsWith("TAG_CHANGE")){
                    Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
                    }
                }
                break;
            case STATE_OPPONENT_TURN:
                if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
                    mState = STATE_PLAYER_TURN;
                    mLogParserTask.setLogReaderLine("Turn " + mTurn.toString() + "\n");
                    mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                } else if(line.startsWith("SHOW_ENTITY")) {
                    Pattern p = Pattern.compile(".*Updating Entity=.*zone=DECK.*CardID=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        //mLogParserTask.setLogReaderLine("  " + mOpponent.name + " draws:\n    " + CardAPI.getCardById(m.group(1)).name + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        mLogParserTask.setLogReaderCard(CardAPI.getCardById(m.group(1)));
                        mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                    }
                } else if(line.startsWith("BLOCK_START BlockType=POWER")){
                    Pattern p = Pattern.compile(".*name=(.*) id=.*Target.*");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        //mLogParserTask.setLogReaderLine("  " + mOpponent.name + " plays:\n    " + m.group(1) + "\n");
                        //mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
                        //mLogParserTask.setLogReaderCard(CardAPI.getCardById(m.group(1)));
                        //mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                    }
                } else if(line.startsWith("TAG_CHANGE")){
                    Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
                    Matcher m = p.matcher(line);
                    if (m.matches()) {
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                        if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
                    }
                }
                break;
        }
    }
}
