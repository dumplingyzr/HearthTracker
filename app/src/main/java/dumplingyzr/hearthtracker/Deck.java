package dumplingyzr.hearthtracker;

import android.os.Environment;
import android.support.v7.util.SortedList;
import android.util.Xml;

/*import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;*/

//import org.apache.commons.io.FileUtils;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.HashMap;

/**
 * Created by dumplingyzr on 2016/11/23.
 */

public class Deck {
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;

    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private SortedList<Card> mCards;
    private int[] mManaCurve = new int[8];
    private static int DECK_SIZE = 30;

    public int numOfCards = 0;
    public String name = "custom";
    public int classIndex;
    public int type = 0;

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
        if(c.id.equals("unknown")) {
            numOfCards++;
            if(mCardCount.containsKey(c.id)){
                mCardCount.put(c.id, mCardCount.get(c.id) + 1);
            } else {
                mCardCount.put(c.id, 1);
                mCards.add(c);
            }
            return true;
        }

        if(c.cost != null){
            int cost = c.cost;
            if (cost >= 7) mManaCurve[7]++;
            else mManaCurve[cost]++;
        } else {
            return false;
        }

        if(mCardCount.containsKey(c.id)){
            int count = mCardCount.get(c.id);
            if(count == 1 && !c.rarity.equals("LEGENDARY")) {
                mCardCount.put(c.id, 2);
                numOfCards++;
                return true;
            } else { return false; }
        } else {
            mCards.add(c);
            mCardCount.put(c.id, 1);
            numOfCards++;
            return true;
        }
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

    public void saveCards(){
        String path = Environment.getExternalStorageDirectory().getPath()+"/Android/data/dumplingyzr.hearthtracker/files/";
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", null);
                serializer.startTag("", "Deck");
                    for (HashMap.Entry<String, Integer> entry: mCardCount.entrySet()){
                    serializer.startTag("", "Card");
                    serializer.startTag("", "Id");
                    serializer.text(entry.getKey());
                    serializer.endTag("", "Id");
                    serializer.startTag("", "Count");
                    serializer.text(entry.getValue().toString());
                    serializer.endTag("", "Count");
                    serializer.endTag("", "Card");
                }
                serializer.startTag("", "Class");
                serializer.text(Card.classIndexToPlayerClass(classIndex));
                serializer.endTag("", "Class");
                serializer.startTag("", "Name");
                serializer.text(name);
                serializer.endTag("", "Name");
            serializer.endTag("", "Deck");
            serializer.endDocument();
            FileUtils.writeStringToFile(new File(path + this.name + ".deck.xml"), writer.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isComplete() {
        return numOfCards == DECK_SIZE;
    }

    public SortedList<Card> getCards(){ return mCards; }

    public HashMap<String, Integer> getCardCount(){ return mCardCount; }
}
