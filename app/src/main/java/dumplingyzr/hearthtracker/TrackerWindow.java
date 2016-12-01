package dumplingyzr.hearthtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class TrackerWindow extends Service {
    private Context mContext;
    private WindowManager mWindowManager;
    private ArrayList<View> mViews = new ArrayList<>();
    private int mWindowWidth = dp2Pixel(150);
    private int mLogHeight;
    private int mButtonHeight;

    private static final int WITH_COUNT = 0;
    private static final int WITHOUT_COUNT = 1;

    private CardListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager)  getSystemService(WINDOW_SERVICE);
        Point screenSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(screenSize);
        mLogHeight = (int) (screenSize.x * 0.8);
        mButtonHeight =(int) (screenSize.x * 0.1);

        final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        WindowManager.LayoutParams logWindowParams = new WindowManager.LayoutParams(
                mWindowWidth, //width
                mLogHeight, //height
                WindowManager.LayoutParams.TYPE_PHONE, //type
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //flag
                PixelFormat.TRANSLUCENT);
        logWindowParams.gravity = Gravity.TOP | Gravity.START;
        logWindowParams.x = 0;
        logWindowParams.y = mButtonHeight * 2;

        WindowManager.LayoutParams buttonParams = new WindowManager.LayoutParams(
                mWindowWidth, //width
                mButtonHeight, //height
                WindowManager.LayoutParams.TYPE_PHONE, //type
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //flag
                PixelFormat.TRANSLUCENT);
        buttonParams.gravity = Gravity.TOP | Gravity.START;
        buttonParams.x = 0;
        buttonParams.y = mButtonHeight;

        final View logView = layoutInflater.inflate(R.layout.log_window, null);
        logView.setVisibility(View.GONE);
        final View buttonView = layoutInflater.inflate(R.layout.button_window, null);
        buttonView.setVisibility(View.GONE);
        mViews.add(logView);
        mViews.add(buttonView);

        //mWindowManager.addView(logView, logWindowParams);
        mWindowManager.addView(buttonView, buttonParams);

        ImageButton buttonStop = (ImageButton) buttonView.findViewById(R.id.stop);
        final ImageButton buttonHide = (ImageButton) buttonView.findViewById(R.id.hide);
        ImageButton buttonMenu = (ImageButton) buttonView.findViewById(R.id.menu);

        final TextView tv = (TextView) logView.findViewById(R.id.textView);
        final ScrollView sv = (ScrollView) logView.findViewById(R.id.scrollView);


        final View recyclerView = layoutInflater.inflate(R.layout.card_list_recycler, null);
        recyclerView.setVisibility(View.GONE);
        mViews.add(recyclerView);
        mRecyclerView = (RecyclerView) recyclerView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CardListAdapter();
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setItemAnimator(RecyclerView.ItemAnimator);

        mWindowManager.addView(recyclerView, logWindowParams);

        LogParserTask mLogReaderThread;
        mLogReaderThread = LogParser.init(mAdapter);
        //mLogReaderThread = LogParser.init(sv, tv);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mWindowManager.removeView(logView);
                mWindowManager.removeView(buttonView);
                mWindowManager.removeView(recyclerView);
                stopSelf();
                System.exit(0);
            }
        });
        buttonHide.setOnClickListener(new View.OnClickListener() {
            //WindowManager.LayoutParams updatedParameters = mParams;
            @Override
            public void onClick(View v) {
                if(recyclerView.getVisibility() == View.GONE) {
                    //logView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    buttonHide.setImageDrawable(getDrawable(R.drawable.ic_hide_black));
                }
                else if(recyclerView.getVisibility() == View.VISIBLE) {
                    //logView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    buttonHide.setImageDrawable(getDrawable(R.drawable.ic_show_black));
                }
            }
        });

        buttonMenu.setOnClickListener(new View.OnClickListener() {
            //WindowManager.LayoutParams updatedParameters = mParams;
            @Override
            public void onClick(View v) {
                mAdapter.onCardDraw(CardAPI.getRandomCard());
                Toast toast = Toast.makeText(getApplicationContext(), "meow", Toast.LENGTH_SHORT);
                toast.show();
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        Point screenSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(screenSize);
        if (screenSize.x > screenSize.y) {
            showAllViews();
        } else {
            hideAllViews();
        }
    }

    public void hideAllViews() {
        for (View v:mViews) {
           v.setVisibility(View.GONE);
        }
    }

    public void showAllViews() {
        for (View v:mViews) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public int dp2Pixel(int pixel) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, Resources.getSystem().getDisplayMetrics());
    }

}