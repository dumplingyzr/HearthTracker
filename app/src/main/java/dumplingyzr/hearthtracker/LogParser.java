package dumplingyzr.hearthtracker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParser {
    static final int DISPLAY_LINE = 1;
    static final int READ_FILE_FINISH = 2;

    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int CORE_POOL_SIZE = 4;
    private static final int MAXIMUM_POOL_SIZE = 4;

    private final BlockingQueue<Runnable> mLogReaderThreadQueue;
    private final BlockingQueue<LogParserTask> mLogParserTaskQueue;
    private final ThreadPoolExecutor mLogReaderThreadPool;

    private Handler mHandler;
    private TextView mTextView;
    private ScrollView mScrollView;
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
        mLogReaderThreadQueue = new LinkedBlockingQueue<>();
        mLogParserTaskQueue = new LinkedBlockingQueue<>();
        mLogReaderThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mLogReaderThreadQueue);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                LogParserTask logParserTask = (LogParserTask) inputMessage.obj;
                mTextView = logParserTask.getTextView();
                mScrollView = logParserTask.getScrollView();
                if(inputMessage.what == DISPLAY_LINE) {
                    mTextView.append(logParserTask.getLine());
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        };
    }

    public static LogParserTask init(
            ScrollView scrollView,
            TextView textView,
            String logType) {

        logParserTask = sInstance.mLogParserTaskQueue.poll();
        if (null == logParserTask) {
            logParserTask = new LogParserTask(logType);
        }

        logParserTask.init(LogParser.sInstance, textView, scrollView);

        sInstance.mLogReaderThreadPool.execute(logParserTask.getLogReaderRunnable());

        return logParserTask;
    }

    public void handleState(LogParserTask logParserTask, int state) {
        Message completeMessage = mHandler.obtainMessage(state, logParserTask);
        completeMessage.sendToTarget();
    }

}
