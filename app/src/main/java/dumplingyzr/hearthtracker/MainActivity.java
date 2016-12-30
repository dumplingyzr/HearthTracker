package dumplingyzr.hearthtracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import dumplingyzr.hearthtracker.activities.ClassSelectActivity;
import dumplingyzr.hearthtracker.fragments.DeckListFragment;
import dumplingyzr.hearthtracker.tracker_window.TrackerWindow;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_GET_OVERLAY_PERMISSIONS = 2;
    public static final String HEARTHSTONE_FILES_DIR = Environment.getExternalStorageDirectory().getPath()+
            "/Android/data/com.blizzard.wtcg.hearthstone/files/";
    public static final String HEARTHSTONE_PACKAGE_ID = "com.blizzard.wtcg.hearthstone";
    private File mFile = new File(HEARTHSTONE_FILES_DIR + "log.config");
    private ActionBarDrawerToggle mDrawerToggle;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.drawer) NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        setupDrawer();

        if (savedInstanceState == null){
            new CardAPI().init(this);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, DeckListFragment.newInstance())
                    .commit();
        }

    }

    /*public boolean hasAllPermissions() {
        boolean has = checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            has &= android.provider.Settings.canDrawOverlays(this);
        }
        return has && mFile.exists();
    }*/

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!android.provider.Settings.canDrawOverlays(this)) {
            Toast.makeText(MainActivity.this, "Please Enable Permissions", Toast.LENGTH_LONG).show();
        } else {
            LaunchLogWindow();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Please Enable Permissions", Toast.LENGTH_LONG).show();
        } else {
            LaunchLogWindow();
        }
    }

    private void LaunchLogWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
                return;
            } else if (!android.provider.Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_GET_OVERLAY_PERMISSIONS);
                return;
            }
        }
        if (!mFile.exists()){
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.config);
                FileOutputStream outputStream = new FileOutputStream(mFile);

                byte buffer[] = new byte[8192];

                while (true) {
                    int read = inputStream.read(buffer);
                    if (read == -1) {
                        break;
                    } else if (read > 0) {
                        outputStream.write(buffer, 0, read);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "HearthTracker setup completed. Please kill and restart HearthStone.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "HearthTracker is started.\nPlease open Hearthstone game. Enjoy!", Toast.LENGTH_LONG).show();
        }

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(MainActivity.this, TrackerWindow.class);
        startService(serviceIntent);

    }

    private void LaunchClassSelectActivity() {
        Intent newIntent = new Intent();
        newIntent.setClass(this, ClassSelectActivity.class);
        startActivity(newIntent);
    }

    public void onCardsReady(){
        Utils.getUserMetrics(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,          /* DrawerLayout object */
                R.string.open_drawer,         /* "open drawer" description */
                R.string.close_drawer         /* "close drawer" description */
        );

        drawerLayout.setDrawerListener(mDrawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.start_tracker:
                        drawerLayout.closeDrawers();
                        LaunchLogWindow();
                        return true;
                    case R.id.new_deck:
                        drawerLayout.closeDrawers();
                        LaunchClassSelectActivity();
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Utils.saveUserMetrics(this);
    }
}