package dumplingyzr.hearthtracker.tracker_window;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import dumplingyzr.hearthtracker.Card;
import dumplingyzr.hearthtracker.Deck;
import dumplingyzr.hearthtracker.Utils;
import dumplingyzr.hearthtracker.R;
import dumplingyzr.hearthtracker.parsers.LogParser;
import dumplingyzr.hearthtracker.parsers.LogParserTask;

public class TrackerWindow extends Service {
    private WindowManager mWindowManager;
    private View mTrackerWindowView;
    private View mButtonView;
    private View mDeckListView;
    private int mWindowWidth = dp2Pixel(150);


    private static final int WITH_COUNT = 0;
    private static final int WITHOUT_COUNT = 1;

    private CardListAdapter mAdapter;
    private LogParserTask mLogReaderThread;

    private Context mContext = this;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Point screenSize = new Point();
        mWindowManager = (WindowManager)  getSystemService(WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getSize(screenSize);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mDeckListView = layoutInflater.inflate(R.layout.deck_list_recycler, null);
        mTrackerWindowView = layoutInflater.inflate(R.layout.log_window, null);
        mButtonView = layoutInflater.inflate(R.layout.control_buttons, null);

        mDeckListView.setVisibility(View.GONE);
        mTrackerWindowView.setVisibility(View.GONE);
        mButtonView.setVisibility(View.GONE);

        RecyclerView deckListRecyclerView = (RecyclerView) mDeckListView.findViewById(R.id.recycler_view);
        deckListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        addView(screenSize.x - dp2Pixel(70),
                mWindowWidth, 0, dp2Pixel(70), mTrackerWindowView);
        addView(dp2Pixel(35),
                mWindowWidth, 0, dp2Pixel(35), mButtonView);
        addView(dp2Pixel(37)*(Utils.sUserDecks.size()+1),
                mWindowWidth, 0, dp2Pixel(70), mDeckListView);

        final ImageButton buttonStop = (ImageButton) mButtonView.findViewById(R.id.stop);
        final ImageButton buttonHide = (ImageButton) mButtonView.findViewById(R.id.hide);
        final ImageButton buttonMenu = (ImageButton) mButtonView.findViewById(R.id.menu);
        final RecyclerView recyclerView = (RecyclerView) mTrackerWindowView.findViewById(R.id.recycler_view);
        final ImageView imageView = (ImageView) mTrackerWindowView.findViewById(R.id.hero_image);
        final TextView textView = (TextView) mTrackerWindowView.findViewById(R.id.deck_info);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new CardListAdapter(this);
        Deck activeDeck = Utils.sActiveDeck;
        mAdapter.setActiveDeck(activeDeck);
        if(activeDeck.classIndex >= 0 && activeDeck.classIndex < 9){
            String heroId = Card.classIndexToHeroId(activeDeck.classIndex);
            int drawableId;
            drawableId = getResources().getIdentifier(heroId.toLowerCase(), "drawable", getPackageName());
            imageView.setBackground(getDrawable(drawableId));
        }
        recyclerView.setAdapter(mAdapter);
        deckListRecyclerView.setAdapter(
                new InTrackerDeckListAdapter(Utils.sUserDecks, mAdapter, mDeckListView, imageView, this));

        LogParser.getInstance().setContext(this);

        mLogReaderThread = LogParser.init(mAdapter, imageView, textView);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogReaderThread.getCurrentThread().interrupt();
                mWindowManager.removeView(mTrackerWindowView);
                mWindowManager.removeView(mButtonView);
                mWindowManager.removeView(mDeckListView);
                stopSelf();
                Utils.saveUserMetrics(mContext);
            }
        });
        buttonHide.setOnClickListener(new View.OnClickListener() {
            //WindowManager.LayoutParams updatedParameters = mParams;
            @Override
            public void onClick(View v) {
                if(mTrackerWindowView.getVisibility() == View.GONE) {
                    mTrackerWindowView.setVisibility(View.VISIBLE);
                    buttonHide.setImageDrawable(getDrawable(R.drawable.ic_hide_black));
                }
                else if(mTrackerWindowView.getVisibility() == View.VISIBLE) {
                    mTrackerWindowView.setVisibility(View.GONE);
                    buttonHide.setImageDrawable(getDrawable(R.drawable.ic_show_black));
                }
            }
        });

        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDeckListView.getVisibility() == View.GONE) {
                    mDeckListView.setVisibility(View.VISIBLE);
                }
                else if(mDeckListView.getVisibility() == View.VISIBLE) {
                    mDeckListView.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        Point screenSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(screenSize);
        if (screenSize.x > screenSize.y) {
            mTrackerWindowView.setVisibility(View.VISIBLE);
            mButtonView.setVisibility(View.VISIBLE);
        } else {
            mTrackerWindowView.setVisibility(View.GONE);
            mDeckListView.setVisibility(View.GONE);
            mButtonView.setVisibility(View.GONE);
        }
    }

    public int dp2Pixel(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Utils.saveUserMetrics(mContext);
    }

    private void addView(int height, int width, int x, int y, View view){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width, //width
                height, //height
                WindowManager.LayoutParams.TYPE_PHONE, //type
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //flag
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = x;
        params.y = y;
        mWindowManager.addView(view, params);
    }
}