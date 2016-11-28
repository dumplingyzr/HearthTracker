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
    private Queue<Card> mCardQueue;
    private Thread mCurrentThread;
    private Runnable mPowerReaderRunnable;
    private Runnable mLoadingScreenReaderRunnable;
    private WeakReference<CardListAdapter> mCardListAdapterWeakRef;
    //private WeakReference<ScrollView> mScrollWeakRef;

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

    @Override
    public void setLogReaderCard(Card card) { mCardQueue.add(card); }

    public String getLine() { return mStringQueue.poll(); }
    public Card getCard() { return mCardQueue.poll(); }

    public void init(
            LogParser logParser,
            CardListAdapter cardListAdapter) {

        sLogParser = logParser;
        mStringQueue = new LinkedList<>();
        mCardQueue = new LinkedList<>();
        mCardListAdapterWeakRef = new WeakReference<>(cardListAdapter);
        //mScrollWeakRef = new WeakReference<>(scrollView);
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
    public CardListAdapter getCardListAdapter() {
        if (mCardListAdapterWeakRef != null) {
            return mCardListAdapterWeakRef.get();
        }
        return null;
    }

    /*public TextView getTextView() {
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
    }*/

    Runnable getPowerRunnable() {
        return mPowerReaderRunnable;
    }

    Runnable getLoadingScreenRunnable() {
        return mLoadingScreenReaderRunnable;
    }
}
