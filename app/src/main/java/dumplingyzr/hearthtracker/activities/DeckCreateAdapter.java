package dumplingyzr.hearthtracker.activities;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import dumplingyzr.hearthtracker.Card;
import dumplingyzr.hearthtracker.CardAPI;
import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.HearthTrackerUtils;
import dumplingyzr.hearthtracker.R;


/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class DeckCreateAdapter extends RecyclerView.Adapter<DeckCreateAdapter.ViewHolder>{
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;
    private static final String[] STANDARD_SETS = {
            "",
            "GANGS",
            "KARA",
            "OG",
            "LOE",
            "TGT",
            "BRM",
            "EXPERT1",
            "CORE"
    };
    private static final String[] WILD_SETS = {
            "",
            "GANGS",
            "KARA",
            "OG",
            "LOE",
            "TGT",
            "BRM",
            "EXPERT1",
            "CORE",
            "NAXX",
            "GVG",
            "REWARD"
    };

    private SortedList<Card> mCards;
    private SortedList<Card> mFilteredCards;
    private DeckEditAdapter mDeckEditAdapter;
    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private Deck mDeck;
    private int mClassIndex;
    private String mClassName;
    private DeckCreateActivity mDeckCreateActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ImageView mImageView;
        public TextView mTextViewName;
        public TextView mTextViewCost;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.card_image);
            mTextViewName = (TextView) view.findViewById(R.id.card);
            mTextViewCost = (TextView) view.findViewById(R.id.cost);
        }
    }

    @Override
    public DeckCreateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_item_no_count, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        View view = viewHolder.mView;
        ImageView imageView = viewHolder.mImageView;
        TextView textViewName = viewHolder.mTextViewName;
        TextView textViewCost = viewHolder.mTextViewCost;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Card c = mCards.get(viewHolder.getAdapterPosition());
                if(mDeck.addCard(c)){
                    mDeckEditAdapter.addCard(c);
                    mDeckCreateActivity.updateNumOfCards();
                }
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getAction();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        Card c = mCards.get(viewHolder.getAdapterPosition());
                        HashMap<String, Integer> cardCount = mDeck.getCardCount();
                        if(cardCount.containsKey(c.id)) {
                            if (cardCount.get(c.id) == 2) break;
                            if (cardCount.get(c.id) == 1 && c.rarity.equals("LEGENDARY")) break;
                        }
                        view.setAlpha((float)0.5);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        view.setAlpha(1);
                    default:
                }
                return false;
            }
        });

        Card card = mCards.get(position);
        Context context = HearthTrackerUtils.getContext();

        try {
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

            textViewName.setText(card.name);
            int drawableId;
            drawableId = context.getResources().getIdentifier(card.id.toLowerCase(), "drawable", context.getPackageName());
            imageView.setBackground(context.getDrawable(drawableId));
            //view.getBackground().setAlpha(191);
        } catch (Resources.NotFoundException e) {
            System.out.println(card.name);
        }

    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public DeckCreateAdapter(DeckCreateActivity parent, int classIndex, Deck deck, DeckEditAdapter deckEditAdapter) {
        mDeckCreateActivity = parent;
        mDeckEditAdapter = deckEditAdapter;
        mDeck = deck;
        mClassIndex = classIndex;
        mFilteredCards = new SortedList<>(Card.class, new SortedList.Callback<Card>() {
            @Override
            public int compare(Card c1, Card c2) {
                int res = c1.cost.compareTo(c2.cost);
                return res == 0 ? c1.name.compareTo(c2.name) : res;
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
        init();
    }

    public void addCard(Card card){
        if(mCardCount.containsKey(card.id)) {
            mCardCount.put(card.id, mCardCount.get(card.id) + 1);
            //mAnimatePosition = findCardById(card.id);
            notifyDataSetChanged();
        } else {
            mCardCount.put(card.id, 1);
            mCards.add(card);
        }
    }

    public void removeCard(Card card) {
        for(int i=0;i<mCards.size();i++){
            if(mCards.get(i).id.equals(card.id)){
                int count = mCardCount.get(card.id);
                if(count > 1){
                    mCardCount.put(card.id, count - 1);
                    notifyDataSetChanged();
                    return;
                } else {
                    mCardCount.remove(card.id);
                    mCards.removeItemAt(findCardById(card.id));
                    return;
                }
            }
        }
    }

    public void clearAll(){
        int size = mCards.size();
        mCards.clear();
        mCardCount.clear();
        notifyItemRangeRemoved(0, size);
    }

    public int findCardById(String key){
        for(int i=0;i<mCards.size();i++){
            if(mCards.get(i).id.equals(key)) return i;
        }
        return -1;
    }

    public void filter(int cost, int set, int _class) {
        mCards = CardAPI.getCardsByClass(mClassIndex, mDeck.type);
        if(cost == 0 && set == 0 && _class == 0) {
            notifyDataSetChanged();
            return;
        }
        mFilteredCards.clear();
        for(int i=0;i<mCards.size();i++) {
            Card c = mCards.get(i);
            if((cost != 0 && cost != 8 && c.cost != cost-1) || (cost == 8 && c.cost < 7)) continue;
            if(set != 0 && !c.set.equals(setIndex2String(set))) continue;
            if(_class == 1 && !c.playerClass.equals(Card.classIndexToPlayerClass(mClassIndex))) continue;
            if(_class == 2 && !c.playerClass.equals("NEUTRAL")) continue;
            mFilteredCards.add(c);
        }
        mCards = mFilteredCards;
        notifyDataSetChanged();
    }

    public void init() {
        mCards = CardAPI.getCardsByClass(mClassIndex, mDeck.type);
        notifyDataSetChanged();
    }

    private String setIndex2String (int index) {
        if(mDeck.type == STANDARD_DECK) {
            return STANDARD_SETS[index];
        } else {
            return WILD_SETS[index];
        }
    }
}
