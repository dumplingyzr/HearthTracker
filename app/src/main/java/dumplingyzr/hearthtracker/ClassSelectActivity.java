package dumplingyzr.hearthtracker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

/**
 * Created by dumplingyzr on 2016/11/28.
 */

public class ClassSelectActivity extends Activity {
    private int mClassIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_select_grid);
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

    }

    private void LaunchDeckCreateActivity() {
        Intent newIntent = new Intent();
        newIntent.setClass(HearthTrackerApplication.getContext(), DeckCreateActivity.class);
        newIntent.putExtra("classIndex", mClassIndex);
        startActivity(newIntent);
    }
}
