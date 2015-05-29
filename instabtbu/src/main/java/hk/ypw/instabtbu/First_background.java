package hk.ypw.instabtbu;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;

public class First_background extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_first_background);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        Double width = (double) mDisplayMetrics.widthPixels;
        System.out.println("宽度:" + width);
        ImageView myImageView = (ImageView) findViewById(R.id.first_img);
        if (width >= 1000)
            myImageView.setBackgroundResource(R.drawable.first_start_1080);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(500);
                SystemClock.sleep(500);
                finish();
            }
        }).start();

        myImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}