package dumplingyzr.hearthtracker.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.R;
import dumplingyzr.hearthtracker.Utils;
import dumplingyzr.hearthtracker.activities.DeckCreateActivity;

/**
 * Created by dumplingyzr on 2016/12/21.
 */

public class DeckListAdapter extends RecyclerView.Adapter<DeckListAdapter.ViewHolder> {
    private Context mContext;

    public DeckListAdapter(Context context){
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView mClassImageView;
        public TextView mDeckNameTextView;
        public RecyclerView mRecyclerView;
        public Spinner mSpinner;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mClassImageView = (ImageView) view.findViewById(R.id.class_image);
            mDeckNameTextView = (TextView) view.findViewById(R.id.deck_name);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
            mSpinner = (Spinner) view.findViewById(R.id.edit_deck);
        }
    }

    @Override
    public DeckListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list_item_fragment, parent, false);
        return new DeckListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final Deck deck = Utils.sUserDecks.get(position);
        Drawable classImageDrawable;

        switch (deck.classIndex){
            case 0: classImageDrawable = mContext.getDrawable(R.drawable.icon_warrior_64); break;
            case 1: classImageDrawable = mContext.getDrawable(R.drawable.icon_shaman_64); break;
            case 2: classImageDrawable = mContext.getDrawable(R.drawable.icon_rogue_64); break;
            case 3: classImageDrawable = mContext.getDrawable(R.drawable.icon_paladin_64); break;
            case 4: classImageDrawable = mContext.getDrawable(R.drawable.icon_hunter_64); break;
            case 5: classImageDrawable = mContext.getDrawable(R.drawable.icon_druid_64); break;
            case 6: classImageDrawable = mContext.getDrawable(R.drawable.icon_warlock_64); break;
            case 7: classImageDrawable = mContext.getDrawable(R.drawable.icon_mage_64); break;
            case 8: classImageDrawable = mContext.getDrawable(R.drawable.icon_priest_64); break;
            default: classImageDrawable = mContext.getDrawable(R.drawable.icon_mage_64);
        }
        viewHolder.mClassImageView.setImageDrawable(classImageDrawable);
        viewHolder.mDeckNameTextView.setText(deck.name);

        RecyclerView recyclerView = viewHolder.mRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(new DeckListCardAdapter(mContext, deck));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.deck_edit_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.mSpinner.setAdapter(adapter);
        viewHolder.mSpinner.setSelection(3, false);
        viewHolder.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if(pos == 0){
                    Utils.sActiveDeck = deck;
                    Utils.sActiveDeckName = deck.path;
                    Toast.makeText(mContext, deck.name + " is set as active deck",Toast.LENGTH_LONG).show();
                } else if (pos == 1){
                    deck.isModified = true;
                    LaunchDeckCreateActivity(viewHolder.getAdapterPosition());
                } else if (pos == 2){
                    Utils.deleteUserMetrics(mContext, viewHolder.getAdapterPosition());
                    Utils.saveUserMetrics(mContext);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return Utils.sUserDecks.size();
    }

    public void updateDeckList(){
        notifyDataSetChanged();
    }

    private void LaunchDeckCreateActivity(int pos) {
        Intent newIntent = new Intent();
        newIntent.setClass(mContext, DeckCreateActivity.class);
        newIntent.putExtra("deckIndex", pos);
        mContext.startActivity(newIntent);
    }
}
