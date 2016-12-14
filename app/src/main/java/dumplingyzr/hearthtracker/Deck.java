package dumplingyzr.hearthtracker;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.util.SortedList;
import android.util.Xml;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by dumplingyzr on 2016/11/23.
 */

public class Deck implements Parcelable{
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;

    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private SortedList<Card> mCards;
    private int[] mManaCurve = new int[8];
    private static int DECK_SIZE = 30;

    public int numOfCards = 0;
    public String name = "custom";
    public int classIndex = -1;
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
        if(this.isComplete()) return;

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
            if(this.name.equals("AUTO_DETECT")) {
                FileUtils.writeStringToFile(new File(path + this.name + ".deck.xml"), writer.toString());
            } else {
                DateFormat df = new SimpleDateFormat("_yyyyMMddHHmmss");
                Date date = new Date();
                String currTime = df.format(date);
                FileUtils.writeStringToFile(new File(path + this.name + currTime + ".deck.xml"), writer.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createFromXml(String deckName){

        try {
            String path = Environment.getExternalStorageDirectory().getPath()
                    + "/Android/data/dumplingyzr.hearthtracker/files/" + deckName + ".deck.xml";
            File file = new File(path);
            InputStream inputStream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder deck = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                deck.append(line);
            }

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(deck.toString()));
            int eventType = xpp.getEventType();

            String cardId = "unknown";
            String tagName = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {

                } else if (eventType == XmlPullParser.START_TAG) {
                    tagName = xpp.getName();
                } else if (eventType == XmlPullParser.END_TAG) {

                } else if (eventType == XmlPullParser.TEXT) {
                    if (tagName.equals("Id")){
                        cardId = xpp.getText();
                    } else if (tagName.equals("Count")){
                        for(int i=0;i<Integer.parseInt(xpp.getText());i++){
                            addCard(CardAPI.getCardById(cardId));
                        }
                    } else if (tagName.equals("Name")){
                        name = xpp.getText();
                    } else if (tagName.equals("Class")){
                        classIndex = Card.playerClassToClassIndex(xpp.getText());
                    }
                }
                eventType = xpp.next();
            }
        } catch (Exception e){

        }
    }

    public boolean isComplete() {
        return numOfCards == DECK_SIZE;
    }

    public SortedList<Card> getCards(){ return mCards; }

    public HashMap<String, Integer> getCardCount(){ return mCardCount; }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int arraySize = mCards.size();
        String[] cardId = new String[arraySize];
        int[] cardCount = new int[arraySize];
        for(int i=0;i<arraySize;i++){
            String id = mCards.get(i).id;
            cardId[i] = id;
            cardCount[i] = mCardCount.get(id);
        }
        out.writeString(name);
        out.writeInt(classIndex);
        out.writeInt(arraySize);
        out.writeStringArray(cardId);
        out.writeIntArray(cardCount);
    }

    public static final Parcelable.Creator<Deck> CREATOR
            = new Parcelable.Creator<Deck>() {
        public Deck createFromParcel(Parcel in) {
            Deck deck = new Deck();
            deck.name = in.readString();
            deck.classIndex = in.readInt();
            int arraySize = in.readInt();
            String[] cardId = new String[arraySize];
            int[] cardCount = new int[arraySize];
            in.readStringArray(cardId);
            in.readIntArray(cardCount);
            for(int i=0;i<arraySize;i++){
                for(int j=0;j<cardCount[i];j++){
                    deck.addCard(CardAPI.getCardById(cardId[i]));
                }
            }
            return deck;
        }
        public Deck[] newArray(int size) {
            return new Deck[size];
        }
    };
}
