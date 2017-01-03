package dumplingyzr.hearthtracker.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import dumplingyzr.hearthtracker.MainActivity;
import dumplingyzr.hearthtracker.R;
import dumplingyzr.hearthtracker.activities.ClassSelectActivity;

/**
 * Created by dumplingyzr on 2016/12/21.
 */

public class DeckListFragment extends Fragment{
    private Context mContext;
    private DeckListAdapter mAdapter;
    private MainActivity mActivity;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.new_deck) ImageButton newDeck;
    @BindView(R.id.start_tracker) ImageButton startTracker;

    public static DeckListFragment newInstance() {
        return new DeckListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deck_list_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        recyclerView.setAdapter(mAdapter);
        newDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.LaunchClassSelectActivity();
            }
        });
        startTracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.LaunchLogWindow();
            }
        });
    }

    public void setContext(MainActivity activity){
        mContext = activity;
        mActivity = activity;
        mAdapter = new DeckListAdapter(mContext);
    }

    public DeckListAdapter getAdapter(){
        return mAdapter;
    }
}
