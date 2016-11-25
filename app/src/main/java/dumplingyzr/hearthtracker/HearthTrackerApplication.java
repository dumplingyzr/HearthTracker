package dumplingyzr.hearthtracker;

import android.app.Application;
import android.content.Context;
import android.speech.tts.TextToSpeech;

/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class HearthTrackerApplication extends Application {

    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

}
