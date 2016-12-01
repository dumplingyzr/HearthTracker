package dumplingyzr.hearthtracker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParser {
    private static final int POWER_TASK = 1;
    private static final int DISPLAY_LINE = 1;
    private static final int CLEAR_WINDOW = 2;
    private static final int DISPLAY_CARD = 3;
    private static final int REMOVE_CARD = 4;

    private static final int LOADING_SCREEN_TASK = 2;
    private static final int STATE_GAME_START = 1;
    private static final int STATE_GAME_END = 2;

    private static final int UI_DISPLAY_LINE = 1;
    private static final int UI_CLEAR_WINDOW = 2;
    private static final int UI_DISPLAY_CARD = 3;
    private static final int UI_REMOVE_CARD = 4;

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 4;

    private final BlockingQueue<Runnable> mPowerThreadQueue;
    private final BlockingQueue<Runnable> mLoadingScreenThreadQueue;
    private final BlockingQueue<LogParserTask> mLogParserTaskQueue;
    private final ThreadPoolExecutor mPowerThreadPool;
    private final ThreadPoolExecutor mLoadingScreenThreadPool;

    private Handler mHandler;
    private CardListAdapter mCardListAdapter;
    //private TextView mTextView;
    //private ScrollView mScrollView;
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
                //mTextView = logParserTask.getTextView();
                //mScrollView = logParserTask.getScrollView();
                if(inputMessage.what == UI_DISPLAY_LINE) {
                    //mTextView.append(logParserTask.getLine());
                    //mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
                if(inputMessage.what == UI_CLEAR_WINDOW) {
                    //mTextView.setText("");
                    //mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    mCardListAdapter.resetAll();
                }
                if(inputMessage.what == UI_DISPLAY_CARD) {
                    mCardListAdapter.onCardDraw(logParserTask.getCard());
                }
                if(inputMessage.what == UI_REMOVE_CARD) {
                    mCardListAdapter.onCardDrop(logParserTask.getCard());
                }
            }
        };
    }

    public static LogParserTask init(
            CardListAdapter cardListAdapter) {

        logParserTask = sInstance.mLogParserTaskQueue.poll();
        if (null == logParserTask) {
            logParserTask = new LogParserTask();
        }

        logParserTask.init(LogParser.sInstance, cardListAdapter);

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
        Message completeMessage = mHandler.obtainMessage(managerState, logParserTask);
        completeMessage.sendToTarget();
    }

}
