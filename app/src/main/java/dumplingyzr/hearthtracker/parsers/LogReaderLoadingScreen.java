package dumplingyzr.hearthtracker.parsers;

import android.os.Process;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dumplingyzr.hearthtracker.MainActivity;

/**
 * Created by dumplingyzr on 2016/11/22.
 */

public class LogReaderLoadingScreen implements Runnable{
    private static final int LOADING_SCREEN_TASK = 2;
    private static final int WAITING_FOR_LOG = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_GAME_START = 1;
    private static final int STATE_GAME_END = 2;

    private int mState = WAITING_FOR_LOG;
    private int mPrevState = STATE_IDLE;
    private boolean mCanceled;
    private ByteBuffer mByteBuffer = ByteBuffer.allocate(1000);
    private String mLine = "";

    final RunnableLoadingScreenLogReaderMethods mLogParserTask;

    interface RunnableLoadingScreenLogReaderMethods {
        void setLoadingScreenThread(Thread currentThread);
        void handleLoadingScreenState(int state, int task);
    }
    LogReaderLoadingScreen(RunnableLoadingScreenLogReaderMethods parseLogTask){
        mLogParserTask = parseLogTask;
        mCanceled = false;
    }

    @Override
    public void run() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            mLogParserTask.setLoadingScreenThread(Thread.currentThread());
            File file = new File(MainActivity.HEARTHSTONE_FILES_DIR + "Logs/LoadingScreen.log");
            FileInputStream fs = new FileInputStream(file);
            FileChannel fc = fs.getChannel();

            while (!mCanceled) {
                switch (mState) {
                    case STATE_GAME_END:
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
                        //mLogParserTask.handleLogReaderState(READ_FILE_FINISH);
                        break;
                    default:
                        parseLine();
                }
            }
        } catch (IOException |InterruptedException e){
            System.out.println("Failed to open file: Power.log");
            e.printStackTrace();
        }
    }

    private boolean readLine () {
        StringBuilder sb = new StringBuilder(256);
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

    private void parseLine() {
        if(!readLine()) {
            mPrevState = mState;
            mState = WAITING_FOR_LOG;
            return;
        }

        Pattern p;
        Matcher m;

        switch (mState) {
            case STATE_IDLE:
                p = Pattern.compile("Gameplay.Start");
                m = p.matcher(mLine);
                if (m.matches()) {
                    mState = STATE_GAME_START;
                    mLogParserTask.handleLoadingScreenState(mState, LOADING_SCREEN_TASK);
                }
                break;
            case STATE_GAME_START:
                p = Pattern.compile("Gameplay.Unload");
                m = p.matcher(mLine);
                if (m.matches()) {
                    mState = STATE_GAME_END;
                    mLogParserTask.handleLoadingScreenState(mState, LOADING_SCREEN_TASK);
                }
                break;
            default:
        }
    }
}
