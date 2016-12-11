package dumplingyzr.hearthtracker;

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
import java.util.concurrent.locks.Lock;


/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class DeckEditAdapter extends RecyclerView.Adapter<DeckEditAdapter.ViewHolder>{

    private SortedList<Card> mCards;
    private Deck mDeck;
    private HashMap<String, Integer> mCardCount = new HashMap<>();
    private DeckCreateActivity mDeckCreateActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ImageView mImageView;
        public TextView mTextViewName;
        public TextView mTextViewCost;
        public TextView mTextViewCount;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.card_image);
            mTextViewName = (TextView) view.findViewById(R.id.card);
            mTextViewCost = (TextView) view.findViewById(R.id.cost);
            mTextViewCount = (TextView) view.findViewById(R.id.count);
        }
    }

    @Override
    public DeckEditAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        View view = viewHolder.mView;
        ImageView imageView = viewHolder.mImageView;
        TextView textViewName = viewHolder.mTextViewName;
        TextView textViewCost = viewHolder.mTextViewCost;
        TextView textViewCount = viewHolder.mTextViewCount;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewHolder.getAdapterPosition();
                if (pos < 0) return;
                Card c = mCards.get(pos);
                if (mDeck.removeCard(c)) {
                    removeCard(c);
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
        Context context = HearthTrackerApplication.getContext();

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
            textViewCount.setText(String.format("%d", mCardCount.get(card.id)));
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

    public DeckEditAdapter(DeckCreateActivity parent, Deck deck) {
        mDeckCreateActivity = parent;
        mDeck = deck;
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
                    mCards.removeItemAt(i);
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

}
