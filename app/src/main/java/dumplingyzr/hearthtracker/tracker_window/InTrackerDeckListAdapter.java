package dumplingyzr.hearthtracker.tracker_window;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import dumplingyzr.hearthtracker.Card;
import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.HearthTrackerUtils;
import dumplingyzr.hearthtracker.R;


/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class InTrackerDeckListAdapter extends RecyclerView.Adapter<InTrackerDeckListAdapter.ViewHolder>{
    private ArrayList<Deck> mDecks;
    private CardListAdapter mCardListAdapter;
    private View mParent;
    private ImageView mHeroImageView;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView mImageView;
        public TextView mTextView;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.class_image);
            mTextView = (TextView) view.findViewById(R.id.deck_name);
        }
    }

    @Override
    public InTrackerDeckListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list_item_in_tracker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        View view = viewHolder.mView;
        ImageView imageView = viewHolder.mImageView;
        TextView textView = viewHolder.mTextView;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewHolder.getAdapterPosition();
                if(pos == 0){
                    mCardListAdapter.setActiveDeck(new Deck());
                    mHeroImageView.setBackgroundColor(Color.BLACK);
                } else {
                    mCardListAdapter.setActiveDeck(mDecks.get(pos - 1));
                    int drawableId;
                    Context context = HearthTrackerUtils.getContext();
                    String heroId = Card.classIndexToHeroId(mDecks.get(pos - 1).classIndex);
                    drawableId = context.getResources().getIdentifier(heroId.toLowerCase(), "drawable", context.getPackageName());
                    mHeroImageView.setBackground(context.getDrawable(drawableId));
                }
                mParent.setVisibility(View.GONE);
            }
        });

        if(position == 0){
            textView.setText("Auto detect");
            return;
        }
        Deck deck = mDecks.get(position - 1);
        Context context = HearthTrackerUtils.getContext();

        switch (deck.classIndex){
            case 0: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_warrior_64)); break;
            case 1: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_shaman_64)); break;
            case 2: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_rogue_64)); break;
            case 3: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_paladin_64)); break;
            case 4: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_hunter_64)); break;
            case 5: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_druid_64)); break;
            case 6: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_warlock_64)); break;
            case 7: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_mage_64)); break;
            case 8: imageView.setImageDrawable(context.getDrawable(R.drawable.icon_priest_64)); break;
        }
        textView.setText(deck.name);
    }

    @Override
    public int getItemCount() {
        return mDecks.size()+1;
    }

    public InTrackerDeckListAdapter(
            ArrayList<Deck> decks,
            CardListAdapter adapter,
            View parent,
            ImageView heroImageView) {
        mDecks = decks;
        mCardListAdapter = adapter;
        mParent = parent;
        mHeroImageView = heroImageView;
    }

}
