package hk.ypw.instabtbu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.UserInfo;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import java.util.HashMap;
import java.util.Map;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

@SuppressLint("ClickableViewAccessibility")
public class About extends SwipeBackActivity {
    Activity thisActivity = this;
    int click = 0;
    Activity mContext = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void caidan(View v) {
        click++;
        if (click >= 5) {
            Intent intent = new Intent();
            intent.setClass(thisActivity, First_jieshao.class);
            startActivity(intent);
            click = 0;
        }
    }

    public void feedback(View v) {
        FeedbackAgent agent = new FeedbackAgent(thisActivity);
        UserInfo info = agent.getUserInfo();
        if (info == null)
            info = new UserInfo();
        Map<String, String> contact = info.getContact();
        if (contact == null)
            contact = new HashMap<String, String>();
        SharedPreferences sp = thisActivity.getSharedPreferences("data", 0);
        String num = sp.getString("num", "");
        contact.put("num", num);
        info.setContact(contact);
        agent.setUserInfo(info);
        agent.startFeedbackActivity();
    }

    public void checknew(View v) {
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int arg0, UpdateResponse arg1) {
                // TODO Auto-generated method stub
                switch (arg0) {
                    case UpdateStatus.Yes: // has update
                        UmengUpdateAgent.showUpdateDialog(mContext, arg1);
                        break;
                    case UpdateStatus.No: // has no update
                        Toast.makeText(mContext, "您使用的已经是最新版本。", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case UpdateStatus.NoneWifi: // none wifi
                        Toast.makeText(mContext, "没有连接wifi。", Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case UpdateStatus.Timeout: // time out
                        Toast.makeText(mContext, "请求超时。", Toast.LENGTH_SHORT)
                                .show();
                        break;
                }
            }
        });

        UmengUpdateAgent.forceUpdate(this);
    }
}
