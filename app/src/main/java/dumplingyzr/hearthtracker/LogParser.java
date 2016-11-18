package dumplingyzr.hearthtracker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

/**
 * Created by dumplingyzr on 2016/11/17.
 */

public class LogParser {
    private Handler mHandler;
    private TextView mTextView;
    private static LogParser sInstance = null;

    /**
     * Returns the LogParser object
     * @return The global LogParser object
     */
    public static LogParser getInstance() {
        return sInstance;
    }

    private LogParser(final TextView textView) {
        mTextView = textView;
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                LogParserTask logParserTask = (LogParserTask) inputMessage.obj;
                if(logParserTask.isValidLine()){
                    textView.append(logParserTask.getLine());
                }
            }
        };
    }
}
