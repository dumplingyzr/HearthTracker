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
import android.provider.ContactsContract;
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
import android.widget.Toast;

public class TrackerWindow extends Service {
    private static Context sContext = HearthTrackerApplication.getContext();
    private WindowManager mWindowManager;
    private View mView;
    private View mButtonView;
    private int mWindowWidth = dp2Pixel(150);

    private static final int WITH_COUNT = 0;
    private static final int WITHOUT_COUNT = 1;

    private CardListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private static SharedPreferences sSharedPref = sContext.getSharedPreferences("HearthTrackerSharedPreferences", Context.MODE_PRIVATE);
    private static SharedPreferences.Editor sEditor = sSharedPref.edit();

    private Intent mIntent;

    @Override
    public IBinder onBind(Intent intent) {
        mIntent = intent;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        mView = layoutInflater.inflate(R.layout.log_window, null);
        mButtonView = layoutInflater.inflate(R.layout.control_buttons, null);
        mView.setVisibility(View.GONE);
        mButtonView.setVisibility(View.GONE);

        mWindowManager.addView(mButtonView, buttonParams);
        mWindowManager.addView(mView, logWindowParams);

        final ImageButton buttonStop = (ImageButton) mButtonView.findViewById(R.id.stop);
        final ImageButton buttonHide = (ImageButton) mButtonView.findViewById(R.id.hide);
        final ImageButton buttonMenu = (ImageButton) mButtonView.findViewById(R.id.menu);
        final RecyclerView recyclerView = (RecyclerView) mView.findViewById(R.id.recycler_view);
        final ImageView imageView = (ImageView) mView.findViewById(R.id.hero_image);
        final TextView textView = (TextView) mView.findViewById(R.id.deck_info);

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        String activeDeckName = sSharedPref.getString("ActiveDeckName", "");
        if(!activeDeckName.equals("")){
            Deck activeDeck = new Deck();
            if(activeDeck.classIndex >= 0 && activeDeck.classIndex < 9){
                String heroId = Card.classIndexToHeroId(activeDeck.classIndex);
                int drawableId;
                Context context = HearthTrackerApplication.getContext();
                drawableId = context.getResources().getIdentifier(heroId.toLowerCase(), "drawable", context.getPackageName());
                imageView.setBackground(context.getDrawable(drawableId));
            }
            activeDeck.createFromXml(activeDeckName);
            CardListAdapter.setActiveDeck(activeDeck);
        }
        mAdapter = new CardListAdapter();
        recyclerView.setAdapter(mAdapter);

        LogParserTask mLogReaderThread;
        mLogReaderThread = LogParser.init(mAdapter, imageView, textView);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveActiveDeck();
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
            //WindowManager.LayoutParams updatedParameters = mParams;
            @Override
            public void onClick(View v) {
                mAdapter.onCardDraw(CardAPI.getRandomCard());
                Toast toast = Toast.makeText(getApplicationContext(), "meow", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }

    public static void saveActiveDeck() {
        CardListAdapter.getDeck().saveCards();
        sEditor.putString("ActiveDeckName", CardListAdapter.getDeck().name);
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
            mButtonView.setVisibility(View.GONE);
        }
    }

    public int dp2Pixel(int pixel) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, Resources.getSystem().getDisplayMetrics());
    }

}