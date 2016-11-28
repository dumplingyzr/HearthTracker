package dumplingyzr.hearthtracker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by dumplingyzr on 2016/11/27.
 */

public class DeckCreateActivity extends Activity {
    private RecyclerView mCurrDeckView;
    private RecyclerView mCardPoolView;
    private DeckEditAdapter mCurrDeckAdapter;
    private DeckCreateAdapter mCardPoolAdapter;
    private RecyclerView.LayoutManager mCurrDeckLayoutManager;
    private RecyclerView.LayoutManager mCardPoolLayoutManager;

    private Deck mDeck = new Deck();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_deck);

        mCurrDeckView = (RecyclerView) findViewById(R.id.recycler_view_curr_deck);
        mCardPoolView = (RecyclerView) findViewById(R.id.recycler_view_card_pool);
        mCurrDeckAdapter = new DeckEditAdapter(mDeck);
        mCardPoolAdapter = new DeckCreateAdapter(CardAPI.getsCardsCollectible(), mDeck, mCurrDeckAdapter);
        mCurrDeckLayoutManager = new LinearLayoutManager(this);
        mCardPoolLayoutManager = new LinearLayoutManager(this);
        mCurrDeckView.setLayoutManager(mCurrDeckLayoutManager);
        mCardPoolView.setLayoutManager(mCardPoolLayoutManager);
        mCurrDeckView.setAdapter(mCurrDeckAdapter);
        mCardPoolView.setAdapter(mCardPoolAdapter);
    }
}
