package dumplingyzr.hearthtracker;

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

public class TrackerWindow extends Service {
    private static Context sContext = HearthTrackerUtils.getContext();
    private WindowManager mWindowManager;
    private View mView;
    private View mButtonView;
    private View mDeckListView;
    private int mWindowWidth = dp2Pixel(150);

    WindowManager.LayoutParams mDeckListParams;

    private static final int WITH_COUNT = 0;
    private static final int WITHOUT_COUNT = 1;

    private CardListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static SharedPreferences sSharedPref;
    private static SharedPreferences.Editor sEditor;

    private Intent mIntent;

    @Override
    public IBinder onBind(Intent intent) {
        mIntent = intent;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPref = sContext.getSharedPreferences("HearthTrackerSharedPreferences", Context.MODE_PRIVATE);
        sEditor = sSharedPref.edit();
        mWindowManager = (WindowManager)  getSystemService(WINDOW_SERVICE);
        Point screenSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(screenSize);

        final LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        WindowManager.LayoutParams logWindowParams = new WindowManager.LayoutParams(
                mWindowWidth, //width
                screenSize.x - dp2Pixel(70), //height
                WindowManager.LayoutParams.TYPE_PHONE, //type
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //flag
                PixelFormat.TRANSLUCENT);
        logWindowParams.gravity = Gravity.TOP | Gravity.START;
        logWindowParams.x = 0;
        logWindowParams.y = dp2Pixel(70);

        WindowManager.LayoutParams buttonParams = new WindowManager.LayoutParams(
                mWindowWidth, //width
                dp2Pixel(35), //height
                WindowManager.LayoutParams.TYPE_PHONE, //type
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //flag
                PixelFormat.TRANSLUCENT);
        buttonParams.gravity = Gravity.TOP | Gravity.START;
        buttonParams.x = 0;
        buttonParams.y = dp2Pixel(35);

        mDeckListParams = new WindowManager.LayoutParams(
                mWindowWidth, //width
                dp2Pixel(37)*(HearthTrackerUtils.sUserDecks.size()+1), //height
                WindowManager.LayoutParams.TYPE_PHONE, //type
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, //flag
                PixelFormat.TRANSLUCENT);
        mDeckListParams.gravity = Gravity.TOP | Gravity.START;
        mDeckListParams.x = 0;
        mDeckListParams.y = dp2Pixel(70);

        mDeckListView = layoutInflater.inflate(R.layout.deck_list_recycler, null);
        RecyclerView deckListRecyclerView = (RecyclerView) mDeckListView.findViewById(R.id.recycler_view);
        deckListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mDeckListView.setVisibility(View.GONE);

        mView = layoutInflater.inflate(R.layout.log_window, null);
        mButtonView = layoutInflater.inflate(R.layout.control_buttons, null);
        mView.setVisibility(View.GONE);
        mButtonView.setVisibility(View.GONE);

        mWindowManager.addView(mButtonView, buttonParams);
        mWindowManager.addView(mView, logWindowParams);
        mWindowManager.addView(mDeckListView, mDeckListParams);

        final ImageButton buttonStop = (ImageButton) mButtonView.findViewById(R.id.stop);
        final ImageButton buttonHide = (ImageButton) mButtonView.findViewById(R.id.hide);
        final ImageButton buttonMenu = (ImageButton) mButtonView.findViewById(R.id.menu);
        final RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        final ImageView imageView = (ImageView) mView.findViewById(R.id.hero_image);
        final TextView textView = (TextView) mView.findViewById(R.id.deck_info);

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        HearthTrackerUtils.username = sSharedPref.getString("UserName", "");
        String activeDeckName = sSharedPref.getString("ActiveDeckName", "");
        mAdapter = new CardListAdapter();
        if(!activeDeckName.equals("")){
            Deck activeDeck = new Deck();
            activeDeck.createFromXml(activeDeckName);
            if(activeDeck.classIndex >= 0 && activeDeck.classIndex < 9){
                String heroId = Card.classIndexToHeroId(activeDeck.classIndex);
                int drawableId;
                Context context = HearthTrackerUtils.getContext();
                drawableId = context.getResources().getIdentifier(heroId.toLowerCase(), "drawable", context.getPackageName());
                imageView.setBackground(context.getDrawable(drawableId));
            }
            mAdapter.setActiveDeck(activeDeck);
        }
        recyclerView.setAdapter(mAdapter);
        deckListRecyclerView.setAdapter(
                new DeckListAdapter(HearthTrackerUtils.sUserDecks, mAdapter, mDeckListView, imageView));

        LogParserTask mLogReaderThread;
        mLogReaderThread = LogParser.init(mAdapter, imageView, textView);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
                saveUserMetrics();
                System.exit(0);
            }
        });
        buttonHide.setOnClickListener(new View.OnClickListener() {
            //WindowManager.LayoutParams updatedParameters = mParams;
            @Override
            public void onClick(View v) {
                if(mView.getVisibility() == View.GONE) {
                    mView.setVisibility(View.VISIBLE);
                    buttonHide.setImageDrawable(getDrawable(R.drawable.ic_hide_black));
                }
                else if(mView.getVisibility() == View.VISIBLE) {
                    mView.setVisibility(View.GONE);
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

    public void saveUserMetrics() {
        mAdapter.getDeck().saveCards();
        sEditor.putString("ActiveDeckName", mAdapter.getDeck().path);
        sEditor.putString("UserName", HearthTrackerUtils.username);
        final int num = HearthTrackerUtils.sUserDeckNames.size();
        Set<String> deckName = new HashSet<>();
        for(int i=0;i<num;i++){
            deckName.add(HearthTrackerUtils.sUserDeckNames.get(i));
        }
        sEditor.putStringSet("UserDeckNames", deckName);
        sEditor.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration c) {
        super.onConfigurationChanged(c);
        Point screenSize = new Point();
        mWindowManager.getDefaultDisplay().getSize(screenSize);
        if (screenSize.x > screenSize.y) {
            mView.setVisibility(View.VISIBLE);
            mButtonView.setVisibility(View.VISIBLE);
        } else {
            mView.setVisibility(View.GONE);
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
        saveUserMetrics();
    }
}