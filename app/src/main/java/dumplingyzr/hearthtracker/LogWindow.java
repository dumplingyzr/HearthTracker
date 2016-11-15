package dumplingyzr.hearthtracker;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class LogWindow extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final WindowManager  wm = (WindowManager)  getSystemService(WINDOW_SERVICE);
        final LayoutInflater ll = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                height, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.CENTER;
        params.x = 0;
        params.y = 0;

        final ViewGroup mv = (ViewGroup) ll.inflate(R.layout.log_window, null);
        wm.addView(mv, params);

        Button stop = (Button) mv.findViewById(R.id.stop);
        final TextView tv = (TextView) mv.findViewById(R.id.tv);

        mv.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatedParameters = params;
            double x;
            double y;
            double pressedX;
            double pressedY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        x = updatedParameters.x;
                        y = updatedParameters.y;

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x = (int) (x + (event.getRawX() - pressedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - pressedY));

                        wm.updateViewLayout(mv, updatedParameters);

                    default:
                        break;
                }

                return false;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wm.removeView(mv);
                stopSelf();
                System.exit(0);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

}