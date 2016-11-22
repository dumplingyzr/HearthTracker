package dumplingyzr.hearthtracker;

import android.support.annotation.NonNull;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import dumplingyzr.hearthtracker.LogReaderRunnable.RunnableLogReaderMethods;
/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParserTask implements RunnableLogReaderMethods {
    private Queue<String> mStringQueue;
    private Thread mCurrentThread;
    private Runnable mLogReaderRunnable;
    private WeakReference<TextView> mTextWeakRef;
    private WeakReference<ScrollView> mScrollWeakRef;

    private static LogParser sLogParser;

    LogParserTask(String logType) {
        mLogReaderRunnable = new LogReaderRunnable(this, logType);
        sLogParser = LogParser.getInstance();
    }

    void handleState(int state) {
        sLogParser.handleState(this, state);
    }

    @Override
    public void setLogReaderThread(Thread thread) { mCurrentThread = thread; }

    @Override
    public void handleLogReaderState(int state) { handleState(state); }

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

    Runnable getLogReaderRunnable() {
        return mLogReaderRunnable;
    }
}
