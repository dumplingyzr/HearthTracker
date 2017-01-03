package dumplingyzr.hearthtracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.MainActivity;
import dumplingyzr.hearthtracker.R;
import dumplingyzr.hearthtracker.Utils;
import dumplingyzr.hearthtracker.fragments.CardSearchDialog;

/**
 * Created by dumplingyzr on 2016/11/27.
 */

public class DeckCreateActivity extends AppCompatActivity
        implements CardSearchDialog.NoticeDialogListener{
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;

    private DeckEditAdapter mCurrDeckAdapter;
    private DeckCreateAdapter mCardPoolAdapter;

    private int mFilterCost;
    private int mFilterSet;
    private int mFilterClass;
    private String mSearchWord = "";

    private Deck mDeck = new Deck();

    @BindView(R.id.num_of_cards) TextView mNumOfCardsTextView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view_curr_deck) RecyclerView mCurrDeckView;
    @BindView(R.id.recycler_view_card_pool) RecyclerView mCardPoolView;
    @BindView(R.id.filter_class) Spinner mClassFilterSpinner;
    @BindView(R.id.filter_cost) Spinner mCostFilterSpinner;
    @BindView(R.id.filter_set) Spinner mSetFilterSpinner;

    @BindView(R.id.CoordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.search) ImageButton searchButton;
    @BindView(R.id.search_content) TextView editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);
        ButterKnife.bind(this);

        int deckIndex = getIntent().getIntExtra("deckIndex", -1);
        int classIndex = getIntent().getIntExtra("classIndex", 1);
        int deckType = getIntent().getIntExtra("deckType", 0);
        String deckName = getIntent().getStringExtra("deckName");

        if(deckIndex == -1) {
            mDeck.classIndex = classIndex;
            mDeck.name = deckName;
            mDeck.type = deckType;
        }
        else {
            mDeck = Utils.sUserDecks.get(deckIndex);
            updateNumOfCards();
        }

        toolbar.setTitle(mDeck.name);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mCurrDeckAdapter = new DeckEditAdapter(this, mDeck, deckIndex == -1);
        mCardPoolAdapter = new DeckCreateAdapter(this, mDeck, mCurrDeckAdapter);
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
        if(deckType == STANDARD_DECK) {
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

        setupSpinner(mCostFilterSpinner, R.id.filter_cost, costFilterAdapter.getCount()-1);
        setupSpinner(mSetFilterSpinner, R.id.filter_set, setFilterAdapter.getCount()-1);
        setupSpinner(mClassFilterSpinner, R.id.filter_class, classFilterAdapter.getCount()-1);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                mSearchWord = "";
                mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass, mSearchWord);
            }
        });

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new CardSearchDialog();
                dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_deck, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        switch(item.getItemId()) {
            case R.id.done:
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
            default:
                return false;
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

    private void setupSpinner(Spinner spinner, final int spinnerId, final int size){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                switch (spinnerId){
                    case R.id.filter_class:
                        if(pos == size){
                            mFilterClass = 0;
                        } else {
                            mFilterClass = pos;
                        }
                        break;
                    case R.id.filter_cost:
                        if(pos == size){
                            mFilterCost = 0;
                        } else {
                            mFilterCost = pos;
                        }
                        break;
                    case R.id.filter_set:
                        if(pos == size){
                            mFilterSet = 0;
                        } else {
                            mFilterSet = pos;
                        }
                }
                mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass, mSearchWord);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void onDialogPositiveClick(CardSearchDialog dialog){
        editText.setText(dialog.mSearchContent);
        mSearchWord = dialog.mSearchContent;
        mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass, mSearchWord);
    }
}
