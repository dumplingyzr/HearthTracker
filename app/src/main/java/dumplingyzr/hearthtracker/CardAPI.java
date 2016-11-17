package dumplingyzr.hearthtracker;

import android.os.AsyncTask;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by dumplingyzr on 2016/11/16.
 */

public class CardAPI {
    private static String locale;
    private static String cardsJSON;
    private static Object lock = new Object();
    private static ArrayList<Card> cards;
    private static boolean cardsReady;
    
    public static void init(){
        locale = "enUS";
    }

    private class GetCardsTask extends AsyncTask<String, Void, String> {
        String endpoint = "https://api.hearthstonejson.com/v1/latest/" + locale + "/cards.json";
        Request request = new Request.Builder().url(endpoint).get().build();
        Response response = null;

        @Override
        protected String doInBackground(String... urls) {
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

    public void getCards(){
        new GetCardsTask().execute();
    }

    private static void storeCards() {
        synchronized (lock) {
            ArrayList<Card> list = new Gson().fromJson(cardsJSON, new TypeToken<ArrayList<Card>>() {
            }.getType());
            for(Card x:list){
                System.out.println(x);
            }
            cards = list;
            cardsReady = true;
        }
        cardsReady = true;
    }
}
