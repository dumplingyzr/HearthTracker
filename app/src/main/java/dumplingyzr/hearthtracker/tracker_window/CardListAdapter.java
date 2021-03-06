package dumplingyzr.hearthtracker.tracker_window;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import dumplingyzr.hearthtracker.Card;
import dumplingyzr.hearthtracker.CardAPI;
import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.R;


/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder>{
    private static final int IDLE = 0;
    private static final int SLIDE_IN = 1;
    private static final int SLIDE_OUT = 2;
    private static final int SLIDE_DOWN = 3;
    private static final int FLASH = 4;

    private SortedList<Card> mCards;
    private Deck mActiveDeck = new Deck();
    private Deck mAddedCards = new Deck();
    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ImageView mCardImageView;
        public ImageView mGradientView;
        public TextView mTextViewName;
        public TextView mTextViewCost;
        public TextView mTextViewCount;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCardImageView = (ImageView) view.findViewById(R.id.card_image);
            mGradientView = (ImageView) view.findViewById(R.id.gradient_image);
            mTextViewName = (TextView) view.findViewById(R.id.card);
            mTextViewCost = (TextView) view.findViewById(R.id.cost);
            mTextViewCount = (TextView) view.findViewById(R.id.count);
        }
    }

    @Override
    public CardListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        View view = viewHolder.mView;
        ImageView imageView = viewHolder.mCardImageView;
        ImageView gradientView = viewHolder.mGradientView;
        TextView textViewName = viewHolder.mTextViewName;
        TextView textViewCost = viewHolder.mTextViewCost;
        TextView textViewCount = viewHolder.mTextViewCount;

        Card card = mCards.get(position);

        if(card.id.equals("unknown")){
            textViewName.setText(mCardCount.get(card.id).toString() + " unknown");
            textViewName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textViewCost.setVisibility(View.INVISIBLE);
            textViewCount.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            gradientView.setVisibility(View.INVISIBLE);
            return;
        } else {
            textViewName.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textViewCost.setVisibility(View.VISIBLE);
            textViewCount.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            gradientView.setVisibility(View.VISIBLE);
        }

        try {
            int drawableId;
            drawableId = mContext.getResources().getIdentifier(card.id.toLowerCase(), "drawable", mContext.getPackageName());
            imageView.setBackground(mContext.getDrawable(drawableId));
            imageView.getBackground().setAlpha(255);

            textViewName.setText(card.name);

            if(card.cost == null) { textViewCost.setText("0"); }
            else {
                textViewCost.setText(String.format("%d", card.cost));
                switch (card.rarity) {
                    case "RARE":
                        textViewCost.setBackgroundColor(mContext.getResources().getColor(R.color.rare));
                        break;
                    case "EPIC":
                        textViewCost.setBackgroundColor(mContext.getResources().getColor(R.color.epic));
                        break;
                    case "LEGENDARY":
                        textViewCost.setBackgroundColor(mContext.getResources().getColor(R.color.legendary));
                        break;
                    default:
                        textViewCost.setBackgroundColor(mContext.getResources().getColor(R.color.common));
                        break;
                }
            }

            if(mCardCount.containsKey(card.id) && mCardCount.get(card.id) > 0) {
                textViewCount.setText(String.format("%d", mCardCount.get(card.id)));
            }
            else {
                textViewCount.setVisibility(View.GONE);
                imageView.getBackground().setAlpha(95);
            }


        } catch (Resources.NotFoundException e) {
            System.out.println(card.name);
        }

    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public CardListAdapter(Context context) {
        mContext = context;
        mCards = CardAPI.newSortedList();
        while(!mActiveDeck.isComplete()){
            mActiveDeck.addCard(Card.unknown());
        }
        SortedList<Card> temp = mActiveDeck.getCards();
        for(int i=0;i<temp.size();i++){
            mCards.add(temp.get(i));
        }
        mCardCount.putAll(mActiveDeck.getCardCount());
    }

    public void onCardDraw(Card card){
        if(mAddedCards.removeCard(card)) {
            int count = mCardCount.get(card.id);
            mCardCount.put(card.id, count - 1);
            notifyDataSetChanged();
            return;
        }

        if(mCardCount.containsKey(card.id) && mCardCount.get(card.id) > 0) {
            int count = mCardCount.get(card.id);
            mCardCount.put(card.id, count - 1);
            notifyDataSetChanged();
        } else {
            if (mCardCount.containsKey("unknown") && mCardCount.get("unknown") > 0){
                mCardCount.put("unknown", mCardCount.get("unknown") - 1);
                mActiveDeck.removeCard(card.unknown());
                mActiveDeck.addCard(card);
                mCards.add(card);
                mCardCount.put(card.id, 0);
                if(mCardCount.get("unknown") == 0) {
                    mCards.remove(card.unknown());
                    mActiveDeck.name = "custum " + Card.classIndexToPlayerClass(mActiveDeck.classIndex).toLowerCase();
                    Toast toast = Toast.makeText(mContext, "Deck completed and saved as: " + mActiveDeck.name,Toast.LENGTH_LONG);
                    toast.show();
                    mActiveDeck.saveCards();
                }
                notifyDataSetChanged();
            } else {
                System.out.println("Card Missed " + card.id);
            }
        }
    }

    public void onCardDrop(Card card) {
        for(int i=0;i<mCards.size();i++){
            if(mCards.get(i).id.equals(card.id)){
                int count = mCardCount.get(card.id);
                mCardCount.put(card.id, count + 1);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void addCardToDeck(Card card, int count){
        for(int i=0;i<mCards.size();i++){
            if(mCards.get(i).id.equals(card.id)){
                int c = mCardCount.get(card.id);
                mCardCount.put(card.id, c + count);
                notifyDataSetChanged();
                return;
            }
        }
        mCards.add(card);
        mCardCount.put(card.id, count);
        notifyDataSetChanged();
    }

    public void startNewDeck(int classIndex){
        mActiveDeck = new Deck();
        mActiveDeck.classIndex = classIndex;
        mActiveDeck.name = "AUTO_DETECT";
    }

    public void startNewGame(){
        while(!mActiveDeck.isComplete()){
            mActiveDeck.addCard(Card.unknown());
        }
        mAddedCards = new Deck();
        mCards.clear();
        mCardCount.clear();
        SortedList<Card> temp = mActiveDeck.getCards();
        for(int i=0;i<temp.size();i++){
            mCards.add(temp.get(i));
        }
        mCardCount.putAll(mActiveDeck.getCardCount());
        notifyDataSetChanged();
    }

    public void setActiveDeck(Deck deck){
        mActiveDeck = deck;
        startNewGame();
    }

    public Deck getDeck(){ return mActiveDeck; }

}
