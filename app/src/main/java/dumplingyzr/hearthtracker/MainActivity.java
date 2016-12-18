package dumplingyzr.hearthtracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_GET_OVERLAY_PERMISSIONS = 2;
    public static final String HEARTHSTONE_FILES_DIR = Environment.getExternalStorageDirectory().getPath()+
            "/Android/data/com.blizzard.wtcg.hearthstone/files/";
    public static final String HEARTHSTONE_PACKAGE_ID = "com.blizzard.wtcg.hearthstone";
    private File mFile = new File(HEARTHSTONE_FILES_DIR + "log.config");
    private Intent mServiceIntent = new Intent();

    public Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HearthTrackerUtils.setContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonStart = (Button) findViewById(R.id.start);

        Button buttonNewDeck = (Button) findViewById(R.id.new_deck);

        if(savedInstanceState == null) {
            buttonStart.setText("Loading Card Database");
            buttonStart.setEnabled(false);
            new CardAPI().init(this);
        } else {
            onCardsReady();
        }

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchLogWindow();
            }
        });

        buttonNewDeck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                LaunchClassSelectActivity();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean hasAllPermissions() {
        boolean has = checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            has &= android.provider.Settings.canDrawOverlays(this);
        }
        return has && mFile.exists();
    }

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
            buttonStart.setText("HearthTracker is running");
            buttonStart.setEnabled(false);


            mServiceIntent.setClass(MainActivity.this, TrackerWindow.class);
            startService(mServiceIntent);

            Toast.makeText(this, "HearthTracker setup completed. Please kill and restart HearthStone.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(MainActivity.this, TrackerWindow.class);
        startService(serviceIntent);

        buttonStart.setText("HearthTracker is running");
        buttonStart.setEnabled(false);
        Toast toast = Toast.makeText(this, "HearthTracker is started.\nPlease open Hearthstone game. Enjoy!", Toast.LENGTH_LONG);
        toast.show();
    }

    private void LaunchClassSelectActivity() {
        Intent newIntent = new Intent();
        newIntent.setClass(this, ClassSelectActivity.class);
        startActivity(newIntent);
    }

    public void onCardsReady(){
        buttonStart.setEnabled(true);
        if (hasAllPermissions()) {
            buttonStart.setText("Start HearthTracker");
        } else {
            buttonStart.setText("Authorize and start HearthTracker");
        }
        HearthTrackerUtils.loadUserDecks();
    }
}