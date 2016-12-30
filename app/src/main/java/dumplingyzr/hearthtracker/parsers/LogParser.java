package dumplingyzr.hearthtracker.parsers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import dumplingyzr.hearthtracker.Card;
import dumplingyzr.hearthtracker.Utils;
import dumplingyzr.hearthtracker.tracker_window.CardListAdapter;

/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParser {
    private static final int POWER_TASK = 1;
    private static final int DISPLAY_LINE = 1;
    private static final int CLEAR_WINDOW = 2;
    private static final int DISPLAY_CARD = 3;
    private static final int REMOVE_CARD = 4;
    private static final int ADD_CARD_TO_DECK = 5;
    private static final int SET_PLAYER_HERO = 6;

    private static final int LOADING_SCREEN_TASK = 2;
    private static final int STATE_GAME_START = 1;
    private static final int STATE_GAME_END = 2;

    private static final int UI_DISPLAY_LINE = 1;
    private static final int UI_CLEAR_WINDOW = 2;
    private static final int UI_DISPLAY_CARD = 3;
    private static final int UI_REMOVE_CARD = 4;
    private static final int UI_ADD_CARD_TO_DECK = 5;
    private static final int UI_SET_PLAYER_HERO = 6;

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 4;

    private final BlockingQueue<Runnable> mPowerThreadQueue;
    private final BlockingQueue<Runnable> mLoadingScreenThreadQueue;
    private final BlockingQueue<LogParserTask> mLogParserTaskQueue;
    private final ThreadPoolExecutor mPowerThreadPool;
    private final ThreadPoolExecutor mLoadingScreenThreadPool;

    private Context mContext;
    private Handler mHandler;
    private CardListAdapter mCardListAdapter;
    private static LogParserTask logParserTask;
    private static LogParser sInstance = null;
    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new LogParser();
    }

    /**
     * Returns the LogParser object
     * @return The global LogParser object
     */
    public static LogParser getInstance() {
        return sInstance;
    }

    private LogParser() {
        mLogParserTaskQueue = new LinkedBlockingQueue<>();
        mPowerThreadQueue = new LinkedBlockingQueue<>();
        mLoadingScreenThreadQueue = new LinkedBlockingQueue<>();
        mPowerThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mPowerThreadQueue);
        mLoadingScreenThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mLoadingScreenThreadQueue);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                LogParserTask logParserTask = (LogParserTask) inputMessage.obj;
                mCardListAdapter = logParserTask.getCardListAdapter();
                if(inputMessage.what == UI_CLEAR_WINDOW) {
                    mCardListAdapter.startNewGame();
                }
                if(inputMessage.what == UI_DISPLAY_CARD) {
                    mCardListAdapter.onCardDraw(logParserTask.getCard());
                }
                if(inputMessage.what == UI_REMOVE_CARD) {
                    mCardListAdapter.onCardDrop(logParserTask.getCard());
                }
                if(inputMessage.what == UI_ADD_CARD_TO_DECK) {
                    mCardListAdapter.addCardToDeck(logParserTask.getCard(), logParserTask.getCardCount());
                }
                if(inputMessage.what == UI_SET_PLAYER_HERO) {
                    String heroId = logParserTask.getPlayerHeroId();
                    int classIndex = Card.heroIdToClassIndex(heroId);
                    if (classIndex != mCardListAdapter.getDeck().classIndex){
                        mCardListAdapter.startNewDeck(classIndex);
                        mCardListAdapter.startNewGame();
                    }
                    int drawableId;
                    drawableId = mContext.getResources().getIdentifier(heroId.toLowerCase(), "drawable", mContext.getPackageName());
                    logParserTask.getHeroImageView().setBackground(mContext.getDrawable(drawableId));
                }
            }
        };
    }

    public static LogParserTask init(
            CardListAdapter cardListAdapter,
            ImageView imageView,
            TextView textView) {

        logParserTask = sInstance.mLogParserTaskQueue.poll();
        if (null == logParserTask) {
            logParserTask = new LogParserTask();
        }

        logParserTask.init(LogParser.sInstance, cardListAdapter, imageView, textView);

        sInstance.mPowerThreadPool.execute(logParserTask.getPowerRunnable());
        sInstance.mLoadingScreenThreadPool.execute(logParserTask.getLoadingScreenRunnable());

        return logParserTask;
    }

    public void handleState(LogParserTask logParserTask, int state, int task) {
        int managerState = -1;
        if(state == DISPLAY_LINE && task == POWER_TASK) managerState = UI_DISPLAY_LINE;
        if(state == DISPLAY_CARD && task == POWER_TASK) managerState = UI_DISPLAY_CARD;
        if(state == REMOVE_CARD && task == POWER_TASK) managerState = UI_REMOVE_CARD;
        if(state == CLEAR_WINDOW && task == POWER_TASK) managerState = UI_CLEAR_WINDOW;
        if(state == ADD_CARD_TO_DECK && task == POWER_TASK) managerState = UI_ADD_CARD_TO_DECK;
        if(state == SET_PLAYER_HERO && task == POWER_TASK) managerState = UI_SET_PLAYER_HERO;
        Message completeMessage = mHandler.obtainMessage(managerState, logParserTask);
        completeMessage.sendToTarget();
    }

    public void setContext(Context context){
        mContext = context;
    }
}
