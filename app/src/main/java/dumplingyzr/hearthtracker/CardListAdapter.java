package dumplingyzr.hearthtracker;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


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
    private static Deck sActiveDeck = new Deck();
    private static Deck sAddedCards = new Deck();
    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private int mAnimatePosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
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
        TextView textViewName = (TextView) view.findViewById(R.id.card);
        TextView textViewCost = (TextView) view.findViewById(R.id.cost);
        TextView textViewCount = (TextView) view.findViewById(R.id.count);
        textViewCost.setVisibility(View.VISIBLE);
        textViewCount.setVisibility(View.VISIBLE);

        Card card = mCards.get(position);
        Context context = HearthTrackerApplication.getContext();

        if(card.id.equals("unknown")){
            textViewName.setText(mCardCount.get(card.id).toString() + " unknown");
            textViewCost.setVisibility(View.INVISIBLE);
            textViewCount.setVisibility(View.INVISIBLE);
            return;
        }

        try {
            int drawableId;
            drawableId = context.getResources().getIdentifier(card.id.toLowerCase(), "drawable", context.getPackageName());
            view.setBackground(context.getDrawable(drawableId));
            view.getBackground().setAlpha(191);

            textViewName.setText(card.name);

            if(card.cost == null) { textViewCost.setText("0"); }
            else {
                textViewCost.setText(String.format("%d", card.cost));
                switch (card.rarity) {
                    case "RARE":
                        textViewCost.setBackgroundColor(context.getResources().getColor(R.color.rare));
                        break;
                    case "EPIC":
                        textViewCost.setBackgroundColor(context.getResources().getColor(R.color.epic));
                        break;
                    case "LEGENDARY":
                        textViewCost.setBackgroundColor(context.getResources().getColor(R.color.legendary));
                        break;
                    default:
                        textViewCost.setBackgroundColor(context.getResources().getColor(R.color.common));
                        break;
                }
            }

            if(mCardCount.containsKey(card.id) && mCardCount.get(card.id) > 0) {
                textViewCount.setText(String.format("%d", mCardCount.get(card.id)));
            }
            else {
                textViewCount.setVisibility(View.GONE);
                view.getBackground().setAlpha(63);
            }


        } catch (Resources.NotFoundException e) {
            System.out.println(card.name);
        }

    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public CardListAdapter() {
        mCards = new SortedList<>(Card.class, new SortedList.Callback<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                int res = c1.cost.compareTo(c2.cost);
                return res == 0 ? c1.name.compareTo(c2.name) : res;
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Card c1, Card c2) {
                // return whether the items' visual representations are the same or not.
                return c1.id.equals(c2.id);
            }

            @Override
            public boolean areItemsTheSame(Card c1, Card c2) {
                return c1.id.equals(c2.id);
            }
        });
        while(!sActiveDeck.isComplete()){
            sActiveDeck.addCard(Card.unknown());
        }
        mCards = sActiveDeck.getCards();
        mCardCount.putAll(sActiveDeck.getCardCount());
    }

    public void onCardDraw(Card card){
        if(sAddedCards.removeCard(card)) {
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
                sActiveDeck.removeCard(card.unknown());
                sActiveDeck.addCard(card);
                mCards.add(card);
                mCardCount.put(card.id, 0);
                if(mCardCount.get("unknown") == 0) { mCards.remove(card.unknown());}
                notifyDataSetChanged();
            } else {
                Toast toast = Toast.makeText(HearthTrackerApplication.getContext(),"Error Detected",Toast.LENGTH_SHORT);
                toast.show();
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

    public void resetAll(){
        while(!sActiveDeck.isComplete()){
            sActiveDeck.addCard(Card.unknown());
        }
        mCards = sActiveDeck.getCards();
        mCardCount.putAll(sActiveDeck.getCardCount());
        notifyDataSetChanged();
    }

    public static void setActiveDeck(Deck deck){
        sActiveDeck = deck;
    }

}
