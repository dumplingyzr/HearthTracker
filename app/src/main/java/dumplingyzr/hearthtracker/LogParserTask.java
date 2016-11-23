package dumplingyzr.hearthtracker;

import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

import dumplingyzr.hearthtracker.LogReaderPower.RunnablePowerLogReaderMethods;
import dumplingyzr.hearthtracker.LogReaderLoadingScreen.RunnableLoadingScreenLogReaderMethods;
/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParserTask implements
        RunnablePowerLogReaderMethods, RunnableLoadingScreenLogReaderMethods {
    private Queue<String> mStringQueue;
    private Thread mCurrentThread;
    private Runnable mPowerReaderRunnable;
    private Runnable mLoadingScreenReaderRunnable;
    private WeakReference<TextView> mTextWeakRef;
    private WeakReference<ScrollView> mScrollWeakRef;

    private static LogParser sLogParser;

    LogParserTask() {
        mPowerReaderRunnable = new LogReaderPower(this);
        mLoadingScreenReaderRunnable = new LogReaderLoadingScreen(this);
        sLogParser = LogParser.getInstance();
    }

    void handleState(int state, int task) {
        sLogParser.handleState(this, state, task);
    }

    @Override
    public void setPowerThread(Thread thread) { setCurrentThread (thread); }

    @Override
    public void handlePowerState(int state, int task) { handleState(state, task); }

    @Override
    public void setLoadingScreenThread(Thread thread) { setCurrentThread (thread); }

    @Override
    public void handleLoadingScreenState(int state, int task) { handleState(state, task); }

    @Override
    public void setLogReaderLine(String line) { mStringQueue.add(line); }

    public String getLine() { return mStringQueue.poll(); }

    public void init(
            LogParser logParser,
            TextView textView,
            ScrollView scrollView) {

        sLogParser = logParser;
        mStringQueue = new LinkedList<>() ;
        mTextWeakRef = new WeakReference<>(textView);
        mScrollWeakRef = new WeakReference<>(scrollView);
    }

    public Thread getCurrentThread() {
        synchronized(sLogParser) {
            return mCurrentThread;
        }
    }

    public void setCurrentThread(Thread thread) {
        synchronized(sLogParser) {
            mCurrentThread = thread;
        }
    }

    public TextView getTextView() {
        if (mTextWeakRef != null) {
            return mTextWeakRef.get();
        }
        return null;
    }

    public ScrollView getScrollView() {
        if (mScrollWeakRef != null) {
            return mScrollWeakRef.get();
        }
        return null;
    }

    Runnable getPowerRunnable() {
        return mPowerReaderRunnable;
    }

    Runnable getLoadingScreenRunnable() {
        return mLoadingScreenReaderRunnable;
    }
}
