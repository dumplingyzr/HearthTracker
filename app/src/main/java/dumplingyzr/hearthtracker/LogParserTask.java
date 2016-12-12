package dumplingyzr.hearthtracker;

import android.widget.ImageView;
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
    private Queue<String> mCardIdQueue;
    private Queue<Integer> mCardCountQueue;
    private String mPlayerHero;
    private Thread mCurrentThread;
    private Runnable mPowerReaderRunnable;
    private Runnable mLoadingScreenReaderRunnable;
    private WeakReference<CardListAdapter> mCardListAdapterWeakRef;
    private WeakReference<ImageView> mHeroImageWeakRef;
    private WeakReference<TextView> mDeckInfoWeakRef;

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
    public void setLogReaderCard(String cardId) { mCardIdQueue.add(cardId); }

    @Override
    public void setLogReaderCardCount(int count) { mCardCountQueue.add(count); }

    @Override
    public void setLogReaderPlayerClass(String heroId) { mPlayerHero = heroId; }

    public String getLine() { return mStringQueue.poll(); }
    public String getPlayerHeroId() { return mPlayerHero; }
    public Card getCard() { return CardAPI.getCardById(mCardIdQueue.poll()); }
    public int getCardCount() { return mCardCountQueue.poll(); }

    public void init(
            LogParser logParser,
            CardListAdapter cardListAdapter,
            ImageView imageView,
            TextView textView) {

        sLogParser = logParser;
        mPlayerHero = "";
        mStringQueue = new LinkedList<>();
        mCardIdQueue = new LinkedList<>();
        mCardCountQueue = new LinkedList<>();
        mCardListAdapterWeakRef = new WeakReference<>(cardListAdapter);
        mHeroImageWeakRef = new WeakReference<>(imageView);
        mDeckInfoWeakRef = new WeakReference<>(textView);
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

    public ImageView getHeroImageView() {
        if (mHeroImageWeakRef != null) {
            return mHeroImageWeakRef.get();
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
