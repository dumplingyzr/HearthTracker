package dumplingyzr.hearthtracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.MainActivity;
import dumplingyzr.hearthtracker.R;
import dumplingyzr.hearthtracker.Utils;

/**
 * Created by dumplingyzr on 2016/11/27.
 */

public class DeckCreateActivity extends AppCompatActivity {
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;

    private DeckEditAdapter mCurrDeckAdapter;
    private DeckCreateAdapter mCardPoolAdapter;

    private int mFilterCost;
    private int mFilterSet;
    private int mFilterClass;

    private Deck mDeck = new Deck();

    @BindView(R.id.num_of_cards) TextView mNumOfCardsTextView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view_curr_deck) RecyclerView mCurrDeckView;
    @BindView(R.id.recycler_view_card_pool) RecyclerView mCardPoolView;
    @BindView(R.id.filter_class) Spinner mClassFilterSpinner;
    @BindView(R.id.filter_cost) Spinner mCostFilterSpinner;
    @BindView(R.id.filter_set) Spinner mSetFilterSpinner;

    @BindView(R.id.CoordinatorLayout) CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);
        ButterKnife.bind(this);

        int index = getIntent().getIntExtra("deckIndex", -1);
        int mClassIndex = getIntent().getIntExtra("classIndex", 1);
        int mDeckType = getIntent().getIntExtra("deckType", 0);
        String mDeckName = getIntent().getStringExtra("deckName");

        toolbar.setTitle(mDeckName);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(index == -1) {
            mDeck.classIndex = mClassIndex;
            mDeck.name = mDeckName;
            mDeck.type = mDeckType;
        }
        else {
            mDeck = Utils.sUserDecks.get(index);
        }

        mCurrDeckAdapter = new DeckEditAdapter(this, mDeck, index == -1);
        mCardPoolAdapter = new DeckCreateAdapter(this, mClassIndex, mDeck, mCurrDeckAdapter);
        LinearLayoutManager mCurrDeckLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager mCardPoolLayoutManager = new LinearLayoutManager(this);
        mCurrDeckView.setLayoutManager(mCurrDeckLayoutManager);
        mCardPoolView.setLayoutManager(mCardPoolLayoutManager);
        mCurrDeckView.setAdapter(mCurrDeckAdapter);
        mCardPoolView.setAdapter(mCardPoolAdapter);

        ArrayAdapter<CharSequence> costFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_cost_filter, android.R.layout.simple_spinner_item);
        costFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCostFilterSpinner.setAdapter(costFilterAdapter);

        ArrayAdapter<CharSequence> setFilterAdapter;
        if(mDeckType == STANDARD_DECK) {
            setFilterAdapter = ArrayAdapter.createFromResource(this,
                    R.array.card_set_standard_filter, android.R.layout.simple_spinner_item);
        } else {
            setFilterAdapter = ArrayAdapter.createFromResource(this,
                    R.array.card_set_wild_filter, android.R.layout.simple_spinner_item);
        }
        setFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSetFilterSpinner.setAdapter(setFilterAdapter);

        ArrayAdapter<CharSequence> classFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_class_filter, android.R.layout.simple_spinner_item);
        classFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mClassFilterSpinner.setAdapter(classFilterAdapter);

        setupSpinner(mCostFilterSpinner, R.id.filter_cost);
        setupSpinner(mSetFilterSpinner, R.id.filter_set);
        setupSpinner(mClassFilterSpinner, R.id.filter_class);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_deck, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        if (item.getItemId() != R.id.done) return false;
        if (mDeck.isComplete()) {
            mDeck.saveCards();
            activityFinish();
            return true;
        } else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Deck is not completed", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.YELLOW)
                    .setAction("Save Anyway", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDeck.saveCards();
                            activityFinish();
                        }
                    });
            snackbar.show();
            return true;
        }
    }

    private void activityFinish(){
        Toast.makeText(this, "Deck is saved as \"" + mDeck.name + "\"", Toast.LENGTH_LONG).show();
        finish();
        Utils.saveUserMetrics(this);
        Intent home = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(home);
    }

    public void updateNumOfCards(){
        mNumOfCardsTextView.setText(String.format("%d/30",mDeck.numOfCards));
    }

    private void setupSpinner(Spinner spinner, final int spinnerId){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                switch (spinnerId){
                    case R.id.filter_class:
                        mFilterClass = pos;
                        break;
                    case R.id.filter_cost:
                        mFilterCost = pos;
                        break;
                    case R.id.filter_set:
                        mFilterSet = pos;
                }
                mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
