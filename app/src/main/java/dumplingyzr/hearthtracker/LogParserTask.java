package dumplingyzr.hearthtracker;

import dumplingyzr.hearthtracker.LogReaderRunnable.RunnableLogReaderMethods;
/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParserTask implements RunnableLogReaderMethods {
    private String mLine;
    private boolean mIsValidLine;
    private Thread mCurrentThread;
    private Runnable mLogReaderRunnable;

    private static LogParser sLogParser;

    LogParserTask(String logType) {
        mLine = null;
        mIsValidLine = false;
        mLogReaderRunnable = new LogReaderRunnable(this, logType);
    }

    @Override
    public void setLogReaderThread(Thread thread) { mCurrentThread = thread; }

    @Override
    public void handleValidLine(boolean isValidLine) { mIsValidLine = isValidLine; }

    @Override
    public void setLogReaderLine(String line) { mLine = line; }

    public String getLine() { return mLine; }
    public boolean isValidLine() {return mIsValidLine; }

    public void init() {
        sLogParser = LogParser.getInstance();
    }
}
