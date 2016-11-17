package dumplingyzr.hearthtracker;

import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.concurrent.Future;

import com.google.gson.Gson;


/**
 * Created by dumplingyzr on 2016/11/16.
 */

public class CardAPI {
    private static String locale;
    private static JsonNode cardsJSON;
    private static Object lock = new Object();
    private static ArrayList<Card> cards;
    private static boolean cardsReady;
    
    public static void init(){
        locale = "enUS";
    }

    public static void getCards(){
        String endpoint = "https://omgvamp-hearthstone-v1.p.mashape.com/cards";
        Future<HttpResponse<JsonNode>> future = Unirest.post(endpoint)
                .header("X-Mashape-Key", "BqZIaED48jmshtVQ2eqTu3lXup11p1rEs85jsnKl2ZUKSUbPr3")
                .field("collectible", 1)
                .field("locale", locale)
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed");
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        cardsJSON = response.getBody();
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                    }

                });

        synchronized (lock) {
            ArrayList<Card> list = new Gson().fromJson(cardsJSON.toString(), new TypeToken<ArrayList<Card>>() {
            }.getType());
            for(Card x:list){
                System.out.println(x);
            }
            cards = list;
            cardsReady = true;
        }
    }
}
