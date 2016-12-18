package dumplingyzr.hearthtracker;

import android.os.Process;
import android.util.SparseArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final int ADD_CARD_TO_DECK = 5;
    private static final int SET_PLAYER_HERO = 6;

    private static final int WAITING_FOR_LOG = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_CREATE_GAME = 1;
    private static final int STATE_FIND_FIRSTPLAYER = 2;
    private static final int STATE_INITIAL_HAND = 3;
    private static final int STATE_SWITCH_CARD = 4;
    private static final int STATE_PLAYER_TURN = 5;
    private static final int STATE_OPPONENT_TURN = 6;
    private static final int STATE_JOUST = 7;
    private static final int STATE_FINISH = 8;

    private Integer mTurn = 1;
    private int mState = WAITING_FOR_LOG;
    private int mPrevState = STATE_IDLE;
    private int mStatePriorJoust = STATE_IDLE;
    private boolean mDoomcallerPlayed = false;

    private ArrayList<String> mPlayerNames = new ArrayList<>();
    private SparseArray<String> mPlayerHeroes = new SparseArray<>();
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
    private int mPlayerIndex = 0;
    private HashMap<String, Integer> mNumCardsDrawn;
    private ByteBuffer mByteBuffer = ByteBuffer.allocate(1000);
    private String mLine = "";

    private final RunnablePowerLogReaderMethods mLogParserTask;

    interface RunnablePowerLogReaderMethods {
        void setPowerThread(Thread currentThread);
        void setLogReaderLine(String line);
        void setLogReaderCard(String cardId);
        void setLogReaderCardCount(int count);
        void setLogReaderPlayerClass(String heroId);
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
            new PrintWriter(MainActivity.HEARTHSTONE_FILES_DIR + "Logs/Power.log").close();
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
            mPlayerHeroes.clear();
            mLogParserTask.handlePowerState(CLEAR_WINDOW, POWER_TASK);
            mLogParserTask.setLogReaderLine("Game Started!\n");
            mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
            return;
        }

        switch (mState) {
            case STATE_CREATE_GAME:
                onCreateGame(line);
                break;
            case STATE_FIND_FIRSTPLAYER:
                onFindFirstPlayer(line);
                break;
            case STATE_INITIAL_HAND:
                onInitialHand(line);
                break;
            case STATE_SWITCH_CARD:
                onSwitchCard(line);
                break;
            case STATE_PLAYER_TURN:
                onPlayerTurn(line);
                break;
            case STATE_OPPONENT_TURN:
                onOpponentTurn(line);
                break;
            case STATE_JOUST:
                onJoust(line);
        }
    }

    private void addKnownCardToDeck(String cardId, int count){
        mLogParserTask.setLogReaderCard(cardId);
        mLogParserTask.setLogReaderCardCount(count);
        mLogParserTask.handlePowerState(ADD_CARD_TO_DECK, POWER_TASK);
    }

    private void addTargetCardToDeck(String target, int count){
        Pattern p = Pattern.compile(".*cardId=(.*) player.*");
        Matcher m = p.matcher(target);
        if(m.matches()){
            mLogParserTask.setLogReaderCard(m.group(1));
            mLogParserTask.setLogReaderCardCount(count);
            mLogParserTask.handlePowerState(ADD_CARD_TO_DECK, POWER_TASK);
        }
    }

    private void onCreateGame(String line){
        if (line.startsWith("BLOCK_START BlockType=TRIGGER Entity=GameEntity")) {
            if(mPlayerNames.size()<2){
                mState = STATE_IDLE;
                return;
            }
            String username = HearthTrackerUtils.username;
            if(username.equals("")) {
                mState = STATE_FIND_FIRSTPLAYER;
            } else {
                mPlayerIndex = mPlayerNames.get(0).equals(username) ? 0 : 1;
                mPlayer = new Player(mPlayerNames.get(mPlayerIndex));
                mOpponent = new Player(mPlayerNames.get(1-mPlayerIndex));
                mLogParserTask.setLogReaderPlayerClass(mPlayerHeroes.get(mPlayerIndex));
                mLogParserTask.handlePowerState(SET_PLAYER_HERO, POWER_TASK);
                mState = STATE_INITIAL_HAND;
            }
        } else if (line.startsWith("TAG_CHANGE")) {
            Pattern p = Pattern.compile(".*Entity=(.*) tag=PLAYSTATE value=PLAYING");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mPlayerNames.add(m.group(1));
            }
        } else if (line.startsWith("FULL_ENTITY")) {
            Pattern p = Pattern.compile(".*player=(.).*CardID=(HERO_.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mPlayerHeroes.put(Integer.parseInt(m.group(1)) - 1, m.group(2));
            }
        }
    }
    private void onFindFirstPlayer(String line){
        if(line.startsWith("SHOW_ENTITY")){
            Pattern p = Pattern.compile(".*Updating Entity=.*player=(.).*CardID=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mPlayer = new Player(mPlayerNames.get(0));
                mOpponent = new Player(mPlayerNames.get(1));
                mLogParserTask.setLogReaderPlayerClass(mPlayerHeroes.get(0));
                mLogParserTask.handlePowerState(SET_PLAYER_HERO, POWER_TASK);

                mLogParserTask.setLogReaderCard(m.group(2));
                mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);

                HearthTrackerUtils.username = mPlayerNames.get(0);
                mState = STATE_INITIAL_HAND;
            }
        } else if (line.startsWith("TAG_CHANGE")) {
            Pattern p = Pattern.compile("Entity=.name=UNKNOWN ENTITY .cardType=INVALID.*player=1.*");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mPlayer = new Player(mPlayerNames.get(1));
                mOpponent = new Player(mPlayerNames.get(0));
                mLogParserTask.setLogReaderPlayerClass(mPlayerHeroes.get(1));
                mLogParserTask.handlePowerState(SET_PLAYER_HERO, POWER_TASK);
                HearthTrackerUtils.username = mPlayerNames.get(1);
                mState = STATE_INITIAL_HAND;
            }
        }
    }
    private void onInitialHand(String line){
        if (line.startsWith("BLOCK_START BlockType=TRIGGER Entity=GameEntity")) {
            mState = STATE_SWITCH_CARD;
        } else if(line.startsWith("SHOW_ENTITY")){
            Pattern p = Pattern.compile(".*Updating Entity=.*player=(.).*CardID=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mLogParserTask.setLogReaderCard(m.group(2));
                mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
            }
        } else if (line.startsWith("TAG_CHANGE")) {
            Pattern p = Pattern.compile(".*Entity=(.*) tag=NUM_CARDS_DRAWN_THIS_TURN value=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                //mNumCardsDrawn.put(m.group(1),Integer.parseInt(m.group(2)));
            }
        } else if(line.startsWith("TAG_CHANGE")){
            Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
            }
        }
    }
    private void onSwitchCard(String line){
        if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
            mState = mPlayerIndex == 0 ? STATE_PLAYER_TURN : STATE_OPPONENT_TURN;
            mLogParserTask.setLogReaderLine("Turn " + mTurn.toString() + "\n");
            mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
        } else if(line.startsWith("SHOW_ENTITY")){
            Pattern p = Pattern.compile(".*Updating Entity=.* CardID=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mLogParserTask.setLogReaderCard(m.group(1));
                mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
            }
        } else if(line.startsWith("HIDE_ENTITY")){
            Pattern p = Pattern.compile(".*cardId=(.*) player.*");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                mLogParserTask.setLogReaderCard(m.group(1));
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
    }
    private void onPlayerTurn(String line){
        if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
            mState = STATE_OPPONENT_TURN;
            mTurn++;
        } else if(line.startsWith("BLOCK_END") && mDoomcallerPlayed) {
            mDoomcallerPlayed = false;
        } else if(line.startsWith("SHOW_ENTITY")) {
            Pattern p = Pattern.compile(".*Updating Entity=.*zone=DECK.*CardID=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                if(m.group(1).equals("LOE_110t")) {
                    return; //ignore SHOW_ENTITY for BattleCry ancient curse
                }
                mLogParserTask.setLogReaderCard(m.group(1));
                mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
            }
        } else if(line.startsWith("FULL_ENTITY") && mDoomcallerPlayed){
            addKnownCardToDeck("OG_280", 1);
        } else if(line.startsWith("BLOCK_START BlockType=POWER")){
            Pattern p = Pattern.compile(".*name=(.*) id=.*EffectIndex=(.*) Target=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String s = m.group(1);
                if(s.equals("Forgotten Torch")) {
                    addKnownCardToDeck("LOE_002t", 1);
                }
                if(s.equals("Ancient Shade")) {
                    addKnownCardToDeck("LOE_110t", 1);
                }
                if(s.equals("Elise Starseeker")) {
                    addKnownCardToDeck("LOE_019t", 1);
                }
                if(s.equals("Map to the Golden Monkey")) {
                    addKnownCardToDeck("LOE_019t2", 1);
                }
                if(s.equals("Entomb") || s.equals("Manic Soulcaster")){
                    addTargetCardToDeck(m.group(3), 1);
                }
                if(s.equals("Gang Up")){
                    addTargetCardToDeck(m.group(3), 3);
                }
                if(s.equals("Jade Idol") && (m.group(2).equals("1") || m.group(2).equals("-1"))){
                    addKnownCardToDeck("CFM_602", 3);
                }
                if(s.equals("Doomcaller")){
                    mDoomcallerPlayed = true;
                }
            }
        } else if(line.startsWith("BLOCK_START BlockType=TRIGGER")){
            Pattern p = Pattern.compile(".*name=(.*) id=.*cardId=(.*) player=(.).*Target=.*");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String s = m.group(1);
                if(s.equals("Ancient Curse")) {
                    mLogParserTask.setLogReaderCard(m.group(2));
                    mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                }
                if(s.equals("Burrowing Mine")) {
                    mLogParserTask.setLogReaderCard(m.group(2));
                    mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                }
                if(s.equals("Malorne") && m.group(3).equals("1")){
                    addKnownCardToDeck("GVG_035", 1);
                }
                if(s.equals("Weasel Tunneler") && m.group(3).equals("2")){
                    addKnownCardToDeck("CFM_095", 1);
                }
                if(s.equals("White Eyes") && m.group(3).equals("1")){
                    addKnownCardToDeck("CFM_324t", 1);
                }
            }
        } else if(line.startsWith("TAG_CHANGE")){
            Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
            }
        } else if(line.startsWith("BLOCK_START BlockType=JOUST")){
            mStatePriorJoust = STATE_PLAYER_TURN;
            mState = STATE_JOUST;
        }
    }
    private void onOpponentTurn(String line){
        if (line.equals("TAG_CHANGE Entity=GameEntity tag=STEP value=MAIN_START")) {
            mState = STATE_PLAYER_TURN;
            mLogParserTask.setLogReaderLine("Turn " + mTurn.toString() + "\n");
            mLogParserTask.handlePowerState(DISPLAY_LINE, POWER_TASK);
        } else if(line.startsWith("SHOW_ENTITY")) {
            Pattern p = Pattern.compile(".*Updating Entity=.*zone=DECK.*player=(.).*CardID=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                //Check if it is player, was failing for patches the pirate
                if(Integer.parseInt(m.group(1))==mPlayerIndex) {
                    mLogParserTask.setLogReaderCard(m.group(2));
                    mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                }
            }
        } else if(line.startsWith("BLOCK_START BlockType=POWER")){
            Pattern p = Pattern.compile(".*name=(.*) id=.*EffectIndex=(.*) Target=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String s = m.group(1);
                if(s.equals("Excavated Evil")) {
                    addKnownCardToDeck("LOE_111", 1);
                }
                if(s.equals("Beneath the Grounds")) {
                    addKnownCardToDeck("AT_035t", 3);
                }
                if(s.equals("Iron Juggernaut")) {
                    addKnownCardToDeck("GVG_056t", 1);
                }
                if(s.equals("Recycle")){
                    addTargetCardToDeck(m.group(3), 1);
                }
            }
        } else if(line.startsWith("BLOCK_START BlockType=TRIGGER")){
            Pattern p = Pattern.compile(".*name=(.*) id=.*cardId=(.*) player=(.).*Target=.*");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                String s = m.group(1);
                if(s.equals("Ancient Curse")) {
                    mLogParserTask.setLogReaderCard(m.group(2));
                    mLogParserTask.handlePowerState(DISPLAY_CARD, POWER_TASK);
                }
                if(s.equals("Malorne") && m.group(3).equals("1")){
                    addKnownCardToDeck("GVG_035", 1);
                }
                if(s.equals("Weasel Tunneler") && m.group(3).equals("2")){
                    addKnownCardToDeck("CFM_095", 1);
                }
                if(s.equals("White Eyes") && m.group(3).equals("1")){
                    addKnownCardToDeck("CFM_324t", 1);
                }
            }
        } else if(line.startsWith("TAG_CHANGE")){
            Pattern p = Pattern.compile("TAG_CHANGE Entity=(.*) tag=PLAYSTATE value=(.*)");
            Matcher m = p.matcher(line);
            if (m.matches()) {
                if (m.group(1).equals(mPlayer.name) && m.group(2).equals("WON")) mState = STATE_FINISH;
                if (m.group(1).equals(mPlayer.name) && m.group(2).equals("LOST")) mState = STATE_FINISH;
            }
        }  else if(line.startsWith("BLOCK_START BlockType=JOUST")){
            mStatePriorJoust = STATE_OPPONENT_TURN;
            mState = STATE_JOUST;
        }
    }
    private void onJoust(String line){
        if (line.equals("BLOCK_END")) {
            mState = mStatePriorJoust;
        }
    }
}
