package dumplingyzr.hearthtracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class HearthTrackerUtils extends Application {
    public static ArrayList<String> sUserDeckNames = new ArrayList<>();
    public static ArrayList<Deck> sUserDecks = new ArrayList<>();
    public static String username = "";
    private static Context sContext;

    private static SharedPreferences sSharedPref;
    private static SharedPreferences.Editor sEditor;

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static void addDeck(String deckName){
        sUserDeckNames.add(deckName);
        Deck deck = new Deck();
        deck.createFromXml(deckName);
        sUserDecks.add(deck);
    }

    public static void loadUserDecks(){
        sSharedPref = sContext.getSharedPreferences("HearthTrackerSharedPreferences", Context.MODE_PRIVATE);
        sEditor = sSharedPref.edit();
        Set<String> userDeckNames = sSharedPref.getStringSet("UserDeckNames", null);
        if(userDeckNames != null) {
            for (String s : userDeckNames) {
                Deck deck = new Deck();
                deck.createFromXml(s);
                sUserDecks.add(deck);
                sUserDeckNames.add(s);
            }
        }
    }
}
