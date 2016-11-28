package dumplingyzr.hearthtracker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
    private Spinner mCostFilterSpinner;
    private Spinner mSetFilterSpinner;

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

        mCostFilterSpinner = (Spinner) findViewById(R.id.filter_cost);
        ArrayAdapter<CharSequence> costFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_cost, android.R.layout.simple_spinner_item);
        costFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCostFilterSpinner.setAdapter(costFilterAdapter);

        mSetFilterSpinner = (Spinner) findViewById(R.id.filter_set);
        ArrayAdapter<CharSequence> setFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_set, android.R.layout.simple_spinner_item);
        setFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSetFilterSpinner.setAdapter(setFilterAdapter);
    }
}
