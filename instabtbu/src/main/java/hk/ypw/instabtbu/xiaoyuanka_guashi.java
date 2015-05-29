package hk.ypw.instabtbu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class xiaoyuanka_guashi extends SwipeBackActivity {
    Activity thisActivity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xiaoyuanka_guashi);
        myui();
    }

    @SuppressWarnings("unused")
    public void myui() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;
        float density = mDisplayMetrics.density;
        double w = width / 720.0;

        setView(R.id.xiaoyuanka_buban, 0, 0, width, width * 1115 / 720);
        setView(R.id.xiaoyuanka_dianhua, (int) (160 * w), (int) (465 * w),
                (int) (520 * w), (int) (79 * w));

    }

    public void dianhua(View v) {
        Intent intent = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:01081353262"));
        try {
            startActivity(intent);
        } catch (Exception e) {
            new AlertDialog.Builder(thisActivity).setTitle("提示")
                    .setMessage("您的设备暂时不支持拨打电话。").setPositiveButton("确定", null)
                    .show();
        }
    }

    public mypoint setView(int id, int x, int y, int wid, int hei) {
        View myView = findViewById(id);
        LayoutParams myParams = new LayoutParams(wid, hei);
        myParams.setMargins(x, y, 0, 0);
        myView.setLayoutParams(myParams);
        return new mypoint(x, y, wid, hei);
    }

    public class mypoint {
        int x;
        int y;
        int width;
        int height;

        public mypoint(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
