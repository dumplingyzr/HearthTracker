package dumplingyzr.hearthtracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogReaderRunnable implements Runnable {
    private final InputStream mInputStream = null;
    private final String mLogType;
    private boolean mCanceled;

    final RunnableLogReaderMethods mLogParserTask;

    interface RunnableLogReaderMethods {
        void setLogReaderThread(Thread currentThread);

        /**
         * Pass line up to LogParserTask
         * @param line
         */
        void setLogReaderLine(String line);

        /**
         * Pass information to LogParserTask whether line is valid
         * @param isValidLine
         */
        void handleValidLine(boolean isValidLine);
    }

    public LogReaderRunnable(RunnableLogReaderMethods parseLogTask, String logType){
        mLogType = logType;
        mLogParserTask = parseLogTask;
        mCanceled = false;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mLogParserTask.setLogReaderThread(Thread.currentThread());
        File file = new File(MainActivity.HEARTHSTONE_FILES_DIR + "Logs/" + mLogType + ".log");

        long lastSize;
        BufferedReader br;
        while (!mCanceled) {
            try {
                if (mInputStream != null) {
                    br = new BufferedReader(new InputStreamReader(mInputStream));
                } else {
                    br = new BufferedReader(new FileReader(file));
                }
            } catch (FileNotFoundException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    //Timber.e(e1);
                }
                continue;
            }

            String line;
            lastSize = file.length();

            while (!mCanceled) {

                long size = file.length();
                if (size < lastSize) {
                    String w = String.format("truncated file ? (%s).log [%d -> %d]", mLogType, lastSize, size);
                    break;
                }
                try {
                    line = br.readLine();
                } catch (IOException e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        //Timber.e(e1);
                    }
                    //Timber.e("cannot read log file file" + file);
                    //Utils.reportNonFatal(e);
                    break;
                }
                if (line == null) {
                    //wait until there is more of the file for us to read
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //Timber.e(e);
                    }
                } else {
                    String finalLine = line;
                    mLogParserTask.setLogReaderLine(finalLine);
                    mLogParserTask.handleValidLine(true);
                    //mHandler.post(() -> mListener.onLine(finalLine));
                }
            }

            try {
                br.close();
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }
}
