package dumplingyzr.hearthtracker.fragments;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

import dumplingyzr.hearthtracker.R;

/**
 * Created by dumplingyzr on 2016/12/21.
 */

public class DeckListAdapter extends RecyclerView.Adapter<DeckListAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ViewHolder(View view) {
            super(view);
            mView = view;
        }
    }

    @Override
    public DeckListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.deck_list_item_fragment, parent, false);
        return new DeckListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        View view = viewHolder.mView;
        TextView textView = (TextView) view.findViewById(R.id.text);

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        textView.setBackgroundColor(color);
        textView.setHeight(rnd.nextInt(800)+200);
        textView.setText(String.format("%d",position));
    }

    @Override
    public int getItemCount() {
        return 30;
    }
}
