package dumplingyzr.hearthtracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;

/**
 * Created by dumplingyzr on 2016/11/28.
 */

public class ClassSelectActivity extends AppCompatActivity {
    private int mClassIndex = -1;
    private String mDeckName = "";
    private int mDeckType = 0;
    private static final int STANDARD_DECK = 0;
    private static final int WILD_DECK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_select_grid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Create Deck");
        setSupportActionBar(toolbar);

        final GridView gridView = (GridView) this.findViewById(R.id.class_grid);
        ClassSelectAdapter classSelectAdapter = new ClassSelectAdapter();
        gridView.setAdapter(classSelectAdapter);

        final Button button = (Button) this.findViewById(R.id.ok_button);
        button.setEnabled(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LaunchDeckCreateActivity();
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(mClassIndex != -1) {
                    View prevView = gridView.getChildAt(mClassIndex);
                    prevView.setBackgroundColor(Color.TRANSPARENT);
                }
                view.setBackgroundColor(Color.GRAY);
                mClassIndex = position;
                button.setEnabled(true);
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

    private void LaunchDeckCreateActivity() {
        Intent newIntent = new Intent();
        newIntent.setClass(HearthTrackerUtils.getContext(), DeckCreateActivity.class);
        newIntent.putExtra("classIndex", mClassIndex);
        EditText editText = (EditText) findViewById(R.id.deck_name);
        mDeckName = editText.getText().toString();
        if(mDeckName.equals("")) { mDeckName = "custom " + Card.classIndexToPlayerClass(mClassIndex).toLowerCase(); }
        newIntent.putExtra("deckName", mDeckName);
        newIntent.putExtra("deckType", mDeckType);
        startActivity(newIntent);
    }
}
