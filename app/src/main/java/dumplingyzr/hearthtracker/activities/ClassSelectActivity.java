package dumplingyzr.hearthtracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import dumplingyzr.hearthtracker.Card;
import dumplingyzr.hearthtracker.HearthTrackerUtils;
import dumplingyzr.hearthtracker.R;

/**
 * Created by dumplingyzr on 2016/11/28.
 */

public class ClassSelectActivity extends AppCompatActivity {
    private int mClassIndex = -1;
    private String mDeckName = "";
    private int mDeckType = 0;
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.deck_name) EditText editText;
    @BindView(R.id.class_grid) GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_select_grid);
        ButterKnife.bind(this);

        toolbar.setTitle("Create Deck");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ClassSelectAdapter classSelectAdapter = new ClassSelectAdapter();
        gridView.setAdapter(classSelectAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(mClassIndex != -1) {
                    View prevView = gridView.getChildAt(mClassIndex);
                    TextView prevTextView = (TextView) prevView.findViewById(R.id.class_name);
                    prevView.setBackgroundColor(Color.TRANSPARENT);
                    prevTextView.setTextColor(Color.BLACK);
                }
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                TextView textView = (TextView) view.findViewById(R.id.class_name);
                textView.setTextColor(Color.WHITE);
                mClassIndex = position;
                editText.setText("Custom " + StringUtils.capitalize(Card.classIndexToPlayerClass(mClassIndex).toLowerCase()));
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.deck_type);
        mDeckType = radioGroup.getCheckedRadioButtonId() == R.id.standard ? STANDARD_DECK : WILD_DECK;
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                mDeckType = radioGroup.getCheckedRadioButtonId() == R.id.standard ? STANDARD_DECK : WILD_DECK;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_class_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item){
        if (item.getItemId() != R.id.next) return false;
        if(mClassIndex == -1){
            Toast.makeText(this, "Please select a class!", Toast.LENGTH_LONG).show();
            return false;
        } else {
            LaunchDeckCreateActivity();
            return true;
        }
    }

    private void LaunchDeckCreateActivity() {
        Intent newIntent = new Intent();
        newIntent.setClass(HearthTrackerUtils.getContext(), DeckCreateActivity.class);
        newIntent.putExtra("classIndex", mClassIndex);

        mDeckName = editText.getText().toString();
        newIntent.putExtra("deckName", mDeckName);
        newIntent.putExtra("deckType", mDeckType);
        startActivity(newIntent);
    }
}
