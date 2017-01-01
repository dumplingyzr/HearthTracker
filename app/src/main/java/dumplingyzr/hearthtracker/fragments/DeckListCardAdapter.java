package dumplingyzr.hearthtracker.fragments;

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
import dumplingyzr.hearthtracker.R;
import dumplingyzr.hearthtracker.activities.DeckCreateActivity;


/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class DeckListCardAdapter extends RecyclerView.Adapter<DeckListCardAdapter.ViewHolder>{
    private Context mContext;
    private SortedList<Card> mCards;
    private Deck mDeck;
    private HashMap<String, Integer> mCardCount = new HashMap<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
    public DeckListCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

        Card card = mCards.get(position);

        try {
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
            textViewCount.setText(String.format("%d", mCardCount.get(card.id)));
            textViewName.setText(card.name);
            int drawableId;
            drawableId = mContext.getResources().getIdentifier(card.id.toLowerCase(), "drawable", mContext.getPackageName());
            imageView.setBackground(mContext.getDrawable(drawableId));
        } catch (Resources.NotFoundException e) {
            System.out.println(card.name);
        }

    }

    @Override
    public int getItemCount() {
        if(mCards.get(mCards.size()-1).name.equals("unknown")){
            return mCards.size() - 1;
        }
        return mCards.size();
    }

    public DeckListCardAdapter(Context context, Deck deck) {
        mContext = context;
        mDeck = deck;
        mCards = deck.getCards();
        mCardCount = deck.getCardCount();
    }

}
