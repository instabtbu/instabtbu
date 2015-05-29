package hk.ypw.instabtbu;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.umeng.analytics.MobclickAgent;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

@SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
public class Ditie extends SwipeBackActivity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ditie);

        getSwipeBackLayout().setEdgeSize(2);

        WebView webView = (WebView) findViewById(R.id.ditie_webview);

        WebSettings ws = webView.getSettings();
        try {
            ws.setSupportZoom(true);
            ws.setUseWideViewPort(true);
            ws.setBuiltInZoomControls(true);
            System.out.println(android.os.Build.VERSION.SDK_INT);
            if (android.os.Build.VERSION.SDK_INT > 15)
                ws.setDisplayZoomControls(false);
            /**
             * 安卓4.0以下是不存在这个命令的(setDisplayZoomControls)
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                WebView webView = (WebView) findViewById(R.id.ditie_webview);
                webView.setInitialScale(25);
                webView.loadUrl("file:///android_asset/ditie_bjsubway.png");
            }
        });
    }

    public void ditie(View v) {
        MobclickAgent.onEvent(this, "ditie");
        ImageView ditie = (ImageView) findViewById(R.id.ditie_ditie);
        ditie.setImageResource(R.drawable.ditie_ditie1);
        ImageView gongjiao = (ImageView) findViewById(R.id.ditie_gongjiao);
        gongjiao.setImageResource(R.drawable.ditie_gongjiao0);

        WebView webView = (WebView) findViewById(R.id.ditie_webview);
        webView.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                WebView webView = (WebView) findViewById(R.id.ditie_webview);
                webView.setInitialScale(35);
                webView.loadUrl("file:///android_asset/ditie_bjsubway.png");
            }
        });
    }

    public void gongjiao(View v) {
        MobclickAgent.onEvent(this, "gongjiao");
        ImageView ditie = (ImageView) findViewById(R.id.ditie_ditie);
        ditie.setImageResource(R.drawable.ditie_ditie0);
        ImageView gongjiao = (ImageView) findViewById(R.id.ditie_gongjiao);
        gongjiao.setImageResource(R.drawable.ditie_gongjiao1);
        WebView webView = (WebView) findViewById(R.id.ditie_webview);
        webView.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                WebView webView = (WebView) findViewById(R.id.ditie_webview);
                DisplayMetrics mDisplayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(
                        mDisplayMetrics);
                int width = mDisplayMetrics.widthPixels;
                webView.setInitialScale(width / 12);
                webView.loadUrl("file:///android_asset/ditie_gongjiao.png");
            }
        });
    }
}