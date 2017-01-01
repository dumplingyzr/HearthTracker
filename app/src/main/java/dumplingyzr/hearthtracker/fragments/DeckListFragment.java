package dumplingyzr.hearthtracker.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import dumplingyzr.hearthtracker.R;

/**
 * Created by dumplingyzr on 2016/12/21.
 */

public class DeckListFragment extends Fragment{
    private Context mContext;
    private DeckListAdapter mAdapter;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    public static DeckListFragment newInstance() {
        return new DeckListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.deck_list_recycler, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, 1));
        recyclerView.setAdapter(mAdapter);
    }

    public void setContext(Context context){
        mContext = context;
        mAdapter = new DeckListAdapter(mContext);
    }

    public DeckListAdapter getAdapter(){
        return mAdapter;
    }
}
