package dumplingyzr.hearthtracker;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by dumplingyzr on 2016/11/24.
 */

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ViewHolder>{
    private ArrayList<Card> mCards;

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
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        View view = viewHolder.mView;
        TextView textView = (TextView) view.findViewById(R.id.card);
        Card card = mCards.get(position);
        Context context = HearthTrackerApplication.getContext();

        textView.setText(card.name);
        int drawableId;
        try{
            drawableId = context.getResources().getIdentifier(card.id.toLowerCase(), "drawable", context.getPackageName());
        } catch (Resources.NotFoundException e){
            drawableId = context.getResources().getIdentifier("hexfrog", "drawable", context.getPackageName());
        }
        try {
            textView.setBackground(context.getDrawable(drawableId));
        } catch (Resources.NotFoundException e){
            System.out.println(card.name);
        }

    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public CardListAdapter(ArrayList<Card> cards){
        mCards = cards;
    }

    public void addCard(Card card){
        Context context = HearthTrackerApplication.getContext();
        try {
            int drawableId = context.getResources().getIdentifier(card.id.toLowerCase(), "drawable", context.getPackageName());
            context.getDrawable(drawableId);
            mCards.add(card);
            notifyItemInserted(mCards.size() - 1);
        } catch (Resources.NotFoundException e){
            System.out.println(card.name);
        }
    }


}
