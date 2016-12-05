package dumplingyzr.hearthtracker;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created by dumplingyzr on 2016/11/27.
 */

public class DeckCreateActivity extends AppCompatActivity {
    private RecyclerView mCurrDeckView;
    private RecyclerView mCardPoolView;
    private DeckEditAdapter mCurrDeckAdapter;
    private DeckCreateAdapter mCardPoolAdapter;
    private RecyclerView.LayoutManager mCurrDeckLayoutManager;
    private RecyclerView.LayoutManager mCardPoolLayoutManager;
    private Spinner mCostFilterSpinner;
    private Spinner mSetFilterSpinner;
    private Spinner mClassFilterSpinner;

    private int mClassIndex;
    private int mDeckType;
    private String mDeckName;
    private int mFilterCost;
    private int mFilterSet;
    private int mFilterClass;

    private Deck mDeck = new Deck();
    private TextView mNumOfCardsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);

        mClassIndex = getIntent().getIntExtra("classIndex", 1);
        mDeckName = getIntent().getStringExtra("deckName");
        mDeckType = getIntent().getIntExtra("deckType", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mDeckName);
        setSupportActionBar(toolbar);

        mDeck.classIndex = mClassIndex;
        mDeck.name = mDeckName;
        mDeck.type = mDeckType;

        mCurrDeckView = (RecyclerView) findViewById(R.id.recycler_view_curr_deck);
        mCardPoolView = (RecyclerView) findViewById(R.id.recycler_view_card_pool);
        mCurrDeckAdapter = new DeckEditAdapter(this, mDeck);
        mCardPoolAdapter = new DeckCreateAdapter(this, mClassIndex, mDeck, mCurrDeckAdapter);
        mCurrDeckLayoutManager = new LinearLayoutManager(this);
        mCardPoolLayoutManager = new LinearLayoutManager(this);
        mCurrDeckView.setLayoutManager(mCurrDeckLayoutManager);
        mCardPoolView.setLayoutManager(mCardPoolLayoutManager);
        mCurrDeckView.setAdapter(mCurrDeckAdapter);
        mCardPoolView.setAdapter(mCardPoolAdapter);

        mCostFilterSpinner = (Spinner) findViewById(R.id.filter_cost);
        ArrayAdapter<CharSequence> costFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_cost_filter, android.R.layout.simple_spinner_item);
        costFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCostFilterSpinner.setAdapter(costFilterAdapter);

        mSetFilterSpinner = (Spinner) findViewById(R.id.filter_set);
        ArrayAdapter<CharSequence> setFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_set_filter, android.R.layout.simple_spinner_item);
        setFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSetFilterSpinner.setAdapter(setFilterAdapter);

        mClassFilterSpinner = (Spinner) findViewById(R.id.filter_class);
        ArrayAdapter<CharSequence> classFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.card_class_filter, android.R.layout.simple_spinner_item);
        classFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mClassFilterSpinner.setAdapter(classFilterAdapter);

        mCostFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mFilterCost = pos;
                mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSetFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mFilterSet = pos;
                mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mClassFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                mFilterClass = pos;
                mCardPoolAdapter.filter(mFilterCost, mFilterSet, mFilterClass);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button button = (Button) findViewById(R.id.ok_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeck.isComplete()) {
                    mDeck.saveCards();
                    CardListAdapter.setActiveDeck(mDeck);
                    Toast toast = Toast.makeText(HearthTrackerApplication.getContext(), "Deck Saved and activated", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        mNumOfCardsTextView = (TextView) findViewById(R.id.num_of_cards);

    }
    public void updateNumOfCards(){
        mNumOfCardsTextView.setText(String.format("%d/30",mDeck.numOfCards));
    }
}
