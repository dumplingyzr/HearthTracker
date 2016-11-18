package dumplingyzr.hearthtracker;

import android.os.AsyncTask;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by dumplingyzr on 2016/11/16.
 */

public class CardAPI {
    private static String locale;
    private static String cardsJSON;
    private static Object lock = new Object();
    private static ArrayList<Card> cards;
    private static boolean cardsReady;
    
    public void init(){
        locale = "enUS";
        new GetCardsTask().execute();
    }

    public static boolean isReady(){
        return cardsReady;
    }

    public static Card getCard(String key) {
        synchronized (lock) {
            if (cards == null) {
                return Card.unknown();
            }
            int index = Collections.binarySearch(cards, key);
            if (index < 0) {
                return Card.unknown();
            } else {
                return cards.get(index);
            }
        }
    }

    private class GetCardsTask extends AsyncTask<Void, Void, String> {
        String endpoint = "https://api.hearthstonejson.com/v1/latest/" + locale + "/cards.collectible.json";
        Request request = new Request.Builder().url(endpoint).get().build();
        Response response = null;

        @Override
        protected String doInBackground(Void... voids) {
            try {
                response = new OkHttpClient().newCall(request).execute();
                return new String(response.body().bytes());
            } catch (IOException e) {
                return "Unable to retrieve cards";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            cardsJSON = result;
            storeCards();
        }
    }

    private static void storeCards() {
        synchronized (lock) {
            ArrayList<Card> list = new Gson().fromJson(cardsJSON, new TypeToken<ArrayList<Card>>() {
            }.getType());
            Collections.sort(list, new Comparator<Card>(){
                public int compare(Card c1, Card c2)
                {
                    return c1.id.compareTo(c2.id);
                }
            });
            cards = list;
        }
        cardsReady = true;
    }
}
