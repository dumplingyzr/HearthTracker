package dumplingyzr.hearthtracker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_GET_OVERLAY_PERMISSIONS = 2;
    public static final String HEARTHSTONE_FILES_DIR = Environment.getExternalStorageDirectory().getPath()+
            "/Android/data/com.blizzard.wtcg.hearthstone/files/";
    public static final String HEARTHSTONE_PACKAGE_ID = "com.blizzard.wtcg.hearthstone";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HearthTrackerApplication.setContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button buttonStart = (Button) findViewById(R.id.start);
        Button buttonNewDeck = (Button) findViewById(R.id.new_deck);

        if (hasAllPermissions()) {
            buttonStart.setText("Start HearthTracker");
        } else {
            buttonStart.setText("Authorize and start HearthTracker");
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

        new CardAPI().init();

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private boolean hasAllPermissions() {
        boolean has = checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            has &= android.provider.Settings.canDrawOverlays(this);
        }
        return has;
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
        Intent serviceIntent = new Intent();
        serviceIntent.setClass(MainActivity.this, TrackerWindow.class);
        startService(serviceIntent);

        Toast toast = Toast.makeText(this, "HearthTracker is started.\nPlease open Hearthstone game.", Toast.LENGTH_LONG);
        toast.show();
    }

    private void LaunchClassSelectActivity() {
        Intent newIntent = new Intent();
        newIntent.setClass(this, ClassSelectActivity.class);
        startActivity(newIntent);
    }
}