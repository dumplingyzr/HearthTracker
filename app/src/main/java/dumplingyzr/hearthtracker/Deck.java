package dumplingyzr.hearthtracker;

import android.support.v7.util.SortedList;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dumplingyzr on 2016/11/23.
 */

public class Deck {
    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private SortedList<Card> mCards;
    private int[] mManaCurve = new int[8];
    private static int DECK_SIZE = 30;
    public int numOfCards = 0;
    public String name;
    public int classIndex;

    public Deck(){
        numOfCards = 0;
        mCards = new SortedList<>(Card.class, new SortedList.Callback<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                return c1.cost.compareTo(c2.cost);
            }

            @Override
            public void onInserted(int position, int count) {}

            @Override
            public void onRemoved(int position, int count) {}

            @Override
            public void onMoved(int fromPosition, int toPosition) {}

            @Override
            public void onChanged(int position, int count) {}

            @Override
            public boolean areContentsTheSame(Card c1, Card c2) { return c1.id.equals(c2.id); }

            @Override
            public boolean areItemsTheSame(Card c1, Card c2) {
                return c1.id.equals(c2.id);
            }
        });
    }

    public boolean addCard(Card c){
        if(numOfCards == DECK_SIZE) return false;

        if(c.cost != null){
            int cost = c.cost;
            if (cost >= 7) mManaCurve[7]++;
            else mManaCurve[cost]++;
        } else {
            return false;
        }

        if(mCardCount.containsKey(c.id)){
            int count = mCardCount.get(c.id);
            if(count == 1) {
                mCardCount.put(c.id, 2);
                numOfCards++;
                return true;
            } else if(count == 2) { return false; }
        } else {
            mCards.add(c);
            mCardCount.put(c.id, 1);
            numOfCards++;
            return true;
        }
        return false;
    }

    public boolean removeCard(Card c) {
        for(int i=0;i<mCards.size();i++){
            if(mCards.get(i).id.equals(c.id)){
                if(c.cost != null){
                    int cost = c.cost;
                    if (cost >= 7) mManaCurve[7]--;
                    else mManaCurve[cost]--;
                } else {
                    return false;
                }

                int count = mCardCount.get(c.id);
                if(count > 1){
                    mCardCount.put(c.id, count - 1);
                    numOfCards--;
                    return true;
                } else {
                    mCardCount.remove(c.id);
                    mCards.removeItemAt(i);
                    numOfCards--;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isComplete() {
        return numOfCards == DECK_SIZE;
    }
}
