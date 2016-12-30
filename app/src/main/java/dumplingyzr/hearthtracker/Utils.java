package dumplingyzr.hearthtracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class Utils extends Application {
    public static String sUserName = "";
    public static Set<String> sUserDeckNames = new HashSet<>();
    public static ArrayList<Deck> sUserDecks = new ArrayList<>();
    public static String sActiveDeckName;
    public static Deck sActiveDeck = new Deck();

    public static void addDeck(String deckName){
        sUserDeckNames.add(deckName);
        Deck deck = new Deck();
        deck.createFromXml(deckName);
        sUserDecks.add(deck);
    }

    public static void getUserMetrics(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("HearthTrackerSharedPreferences", Context.MODE_PRIVATE);
        Set<String> userDeckNames = sharedPref.getStringSet("UserDeckNames", null);
        sActiveDeckName = sharedPref.getString("ActiveDeckName", "");
        sUserName = sharedPref.getString("UserName", "");
        if(userDeckNames != null) {
            for (String s : userDeckNames) {
                Deck deck = new Deck();
                deck.createFromXml(s);
                sUserDecks.add(deck);
                sUserDeckNames.add(s);
                if(s.equals(sActiveDeckName)){
                    sActiveDeck = deck;
                }
            }
        }
    }

    public static void saveUserMetrics(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("HearthTrackerSharedPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("ActiveDeckName", sActiveDeckName);
        editor.putString("UserName", sUserName);
        editor.putStringSet("UserDeckNames", sUserDeckNames);
        editor.apply();
    }
}
