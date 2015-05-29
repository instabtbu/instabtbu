package hk.ypw.instabtbu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;

import java.net.URLEncoder;

public class Guancang extends Activity {
    Leftmenu Leftmenu;
    SlidingMenu menu;
    Activity thisActivity = this;
    long uiId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guancang);

        Leftmenu = new Leftmenu(thisActivity, 6);
        menu = Leftmenu.menu;
        MobclickAgent.updateOnlineConfig(this);
        uiId = Thread.currentThread().getId();
    }

    @Override
    public void onResume() {
        super.onResume();
        Leftmenu.leftmenu_ui(6);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.btbu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Public_menu menu = new Public_menu();
        menu.thisActivity = thisActivity;
        menu.select(item);
        return false;
    }

    public void search(View view) {
        EditText kwEditText = (EditText) findViewById(R.id.guancang_edittext);
        String kw = kwEditText.getText().toString();
        if (kw.length() == 0) {
            Toast.makeText(thisActivity, "请输入查询内容", Toast.LENGTH_SHORT).show();
        } else {
            try {
                Guancang_web.urlString = "http://211.82.113.138:8080/search?xc=4&kw="
                        + URLEncoder.encode(kw, "UTF-8");
                Intent intent = new Intent();
                intent.setClass(thisActivity, Guancang_web.class);
                thisActivity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void scan(View v) {
        Intent intent = new Intent();
        intent.setClass(Guancang.this, Guancang_scan.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String isbnString = bundle.getString("result");
                    Toast.makeText(thisActivity, "ISBN:" + isbnString,
                            Toast.LENGTH_SHORT).show();
                    Guancang_web.urlString = "http://211.82.113.138:8080/search?xc=4&isbnstr="
                            + isbnString;
                    Intent intent = new Intent();
                    intent.setClass(thisActivity, Guancang_web.class);
                    thisActivity.startActivity(intent);
                }
        }
    }

}
