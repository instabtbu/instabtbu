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

}
