package hk.ypw.instabtbu;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ypw
 */

public class jiaowuchaxun extends Activity {
    static boolean wificonnected = false;

    String filepath = "";
    Random random = new Random();
    Leftmenu Leftmenu;
    SlidingMenu menu;
    Activity thisActivity = this;
    Bitmap yzmBitmap;
    String gengxinString = "";
    Toast toast;
    String showString = "";
    private ExecutorService executorService = Executors.newCachedThreadPool();
    // 线程池
    private ProgressDialog dialog2;

    Runnable chengjiRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                yzmBitmap = Common.GET("http://jwgl.btbu.edu.cn/verifycode.servlet");
                gengxin("获取验证码完毕");
                // Message message = new Message();
                // message.what=1;
                // handler.sendMessage(message);
                EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
                EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
                yzmBitmap = im2bw(yzmBitmap);
                String yzm = shibie(yzmBitmap);
                gengxin(yzm);
                String numString = numEditText.getText().toString();
                if (numString.contains("P"))
                    jiaowu_chengji.pangtingsheng = true;
                String resultString = POST("http://jwgl.btbu.edu.cn/Logon.do",
                        "method=logon&USERNAME=" + numEditText.getText()
                                + "&PASSWORD=" + pswEditText.getText()
                                + "&RANDOMCODE=" + yzm);
                random = new Random();
                Thread.sleep(random.nextInt(120));
                if (find(resultString,
                        "http://jwgl.btbu.edu.cn/framework/main.jsp")) {
                    gengxin("登录成功，获取权限……");
                    POST("http://jwgl.btbu.edu.cn/Logon.do?method=logonBySSO",
                            "");
                    // 登录成功
                    if (dialog2.isShowing())
                        dialog2.dismiss();
                    loadchengji();
                } else if (find(resultString, "验证码错误")) {
                    gengxin("验证码错误，重新登录……");
                    run();
                } else if (find(resultString, "errorinfo")) {
                    if (dialog2.isShowing())
                        dialog2.dismiss();
                    show(zhongjian(resultString, "errorinfo\">", "</span>")
                            + "\n默认密码为学号或身份证后六位。");
                } else {
                    if (dialog2.isShowing())
                        dialog2.dismiss();
                    show("登录失败");
                }
            } catch (Exception e) {
                if (dialog2.isShowing())
                    dialog2.dismiss();
                e.printStackTrace();
            }
            if (dialog2.isShowing())
                dialog2.dismiss();
        }
    };
    Runnable kebiaoRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                yzmBitmap = Common.GET("http://jwgl.btbu.edu.cn/verifycode.servlet");
                gengxin("获取验证码完毕");
                EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
                EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
                yzmBitmap = im2bw(yzmBitmap);
                String yzm = shibie(yzmBitmap);
                gengxin(yzm);
                String resultString = POST("http://jwgl.btbu.edu.cn/Logon.do",
                        "method=logon&USERNAME=" + numEditText.getText()
                                + "&PASSWORD=" + pswEditText.getText()
                                + "&RANDOMCODE=" + yzm);
                // System.out.println(resultString);
                if (dialog2.isShowing())
                    dialog2.dismiss();
                if (find(resultString,
                        "http://jwgl.btbu.edu.cn/framework/main.jsp")) {
                    gengxin("登录成功，获取权限……");
                    POST("http://jwgl.btbu.edu.cn/Logon.do?method=logonBySSO",
                            "");
                    // 登录成功
                    loadkebiao();
                } else if (find(resultString, "验证码错误")) {
                    gengxin("验证码错误，重新登录……");
                    run();
                } else if (find(resultString, "errorinfo")) {
                    if (dialog2.isShowing())
                        dialog2.dismiss();
                    show(zhongjian(resultString, "errorinfo\">", "</span>")
                            + "\n默认密码为学号或身份证后六位。");
                } else
                    show("登录失败");
            } catch (Exception e) {
                if (dialog2.isShowing())
                    dialog2.dismiss();
            }
            if (dialog2.isShowing())
                dialog2.dismiss();
        }
    };
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (dialog2.isShowing()) {
                        dialog2.setMessage(gengxinString);
                    }
                } else if (msg.what == 2) {
                    if (toast == null)
                        toast = Toast.makeText(getApplicationContext(),
                                showString, Toast.LENGTH_SHORT);
                    else
                        toast.setText(showString);
                    toast.show();
                }
            } catch (Exception ignored) {
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiaowu);

        SharedPreferences sp2 = getSharedPreferences("data", 0);
        String num2 = sp2.getString("num", "");
        String psw2 = sp2.getString("psw", "");
        // 获取上网登录密码
        Common.dengluVPN(num2, psw2);

        try {
            Leftmenu = new Leftmenu(thisActivity, 2);
            menu = Leftmenu.menu;

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
            EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
            // MobclickAgent.onEvent(thisActivity,"jiaowu");
            filepath = getFilesDir().toString() + "/";

            SharedPreferences sp = getSharedPreferences("data", 0);
            String num = sp.getString("num_jiaowu", null);
            String psw = sp.getString("psw_jiaowu", null);
            numEditText.setText(num);
            pswEditText.setText(psw);

            TextView left_userTextView = (TextView) findViewById(R.id.leftmenu_textview_user);
            TextView left_user2TextView = (TextView) findViewById(R.id.leftmenu_textview_dianzheli);
            if (numEditText.getText().toString().length() == 0)
                left_userTextView.setText("还没有登录");
            else
                left_userTextView.setText(numEditText.getText().toString());
            left_user2TextView.setText("好好学习~");

        } catch (Exception ignored) {
        }

//        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
//        Double width = (double) mDisplayMetrics.widthPixels;
//        Double height = (double) mDisplayMetrics.heightPixels;

//         System.out.println("宽高比:" + height / width);

        // if(height/width!=16.0/9.0){
        // ImageView xiaoliImageView =
        // (ImageView)findViewById(R.id.jiaowu_xiaoli);
        // xiaoliImageView.setVisibility(View.INVISIBLE);
        // ImageView qingImageView =
        // (ImageView)findViewById(R.id.jiaowu_qingkongziliao);
        // qingImageView.setVisibility(View.INVISIBLE);
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        Leftmenu.leftmenu_ui(1);
    }

    public void xiaoli(View v) {
        Intent intent = new Intent();
        intent.setClass(thisActivity, jiaowu_xiaoli.class);
        startActivity(intent);
    }

    public void chengji(View v) {
        if (Common.isWifiConnected(thisActivity)) {
            EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
            EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
            SharedPreferences.Editor editor = getSharedPreferences("data", 0)
                    .edit();
            editor.putString("num_jiaowu", numEditText.getText().toString());
            editor.putString("psw_jiaowu", pswEditText.getText().toString());
            editor.apply();

            dialog2 = ProgressDialog.show(jiaowuchaxun.this, "正在登录", "正在登录中……",
                    true, true);
            Common.resetClient();
            executorService.submit(chengjiRunnable);
        }else {
            SharedPreferences sp = getSharedPreferences("data", 0);
            String zidongvpn = sp.getString("zidongvpn", "");
            if(zidongvpn.contains("z")){

            }else{
                show("请连接BTBU或打开");
            }
        }
    }

    public void kebiao(View v) {
        if (Common.isWifiConnected(thisActivity)) {
            EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
            EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
            SharedPreferences.Editor editor = getSharedPreferences("data", 0)
                    .edit();
            editor.putString("num_jiaowu", numEditText.getText().toString());
            editor.putString("psw_jiaowu", pswEditText.getText().toString());
            editor.apply();

            dialog2 = ProgressDialog.show(thisActivity, "正在登录", "正在登录中……",
                    true, true);
            Common.resetClient();
            executorService.submit(kebiaoRunnable);
            wificonnected = true;
        } else {
            wificonnected = false;
            Intent intent = new Intent();
            intent.setClass(thisActivity, jiaowu_kebiao.class);
            startActivity(intent);
        }

    }

    void gengxin(String gx) {
        gengxinString = gx;
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }

    public boolean find(String text, String w) {
        return text.contains(w);
    }

    protected void loadchengji() {
        Intent intent = new Intent();
        intent.setClass(this, jiaowu_chengji.class);
        startActivity(intent);
    }

    protected void loadkebiao() {
        Intent intent = new Intent();
        intent.setClass(this, jiaowu_kebiao.class);
        startActivity(intent);
    }

    public String zhongjian(String text, String textl, String textr) {
        return zhongjian(text, textl, textr, 0);
    }

    // final Handler handler = new Handler(){
    // @Override
    // public void handleMessage(Message msg){
    // try{
    // super.handleMessage(msg);
    // //ImageView asdImageView= (ImageView)findViewById(R.id.jiaowu_kebiao);
    // if(msg.what == 1){
    //
    // }
    // }catch(Exception e)
    // {
    //
    // }
    // }
    // };

    public String zhongjian(String text, String textl, String textr, int start) {
        int left = text.indexOf(textl, start);
        int right = text.indexOf(textr, left + textl.length());
        return text.substring(left + textl.length(), right);
    }

    public String shibie(Bitmap myBitmap) {
        String yzm = "";
        String[] myyzm = {"1", "2", "3", "b", "c", "m", "n", "v", "x", "z"};
        try {
            int qietu, duibi;
            int x, y;
            for (qietu = 0; qietu < 4; qietu++) {
                int errnum[] = {100, 100, 100, 100, 100, 100, 100, 100, 100,
                        100};
                for (duibi = 0; duibi < 10; duibi++) {
                    int errpixel = 0;
                    InputStream is = getResources().getAssets().open(
                            myyzm[duibi] + ".bmp");
                    Bitmap rawBitmap = BitmapFactory.decodeStream(is);
                    for (y = 0; y < 12; y++) {
                        for (x = 0; x < 9; x++) {
                            int col = myBitmap.getPixel(x + 3 + 10 * qietu,
                                    y + 4);
                            int col2 = rawBitmap.getPixel(x, y);
                            if ((col2 & 0xFF) > (col & 0xFF))
                                errpixel++;
                        }
                        System.out.print("nn");
                    }
                    errnum[duibi] = errpixel;
                }
                int wz = 0, min = 100, i;
                for (i = 0; i < 10; i++)
                    if (errnum[i] < min) {
                        min = errnum[i];
                        wz = i;
                    }
                yzm += myyzm[wz];
            }
        } catch (Exception ignored) {
        }
        System.out.println(yzm);
        return yzm;
    }

    public Bitmap im2bw(Bitmap myBitmap) {
        Bitmap bwBitmap;
        int x = myBitmap.getWidth();
        int y = myBitmap.getHeight();

        bwBitmap = myBitmap.copy(Config.ARGB_8888, true);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                int col = bwBitmap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                if (gray <= 128) {
                    gray = 255;
                } else {
                    gray = 0;
                }
                // 新的ARGB
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                // 设置新图像的当前像素值
                bwBitmap.setPixel(i, j, newColor);
            }
        }

        return bwBitmap;
    }

    public void qingkongziliao(View v) {
        EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
        EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
        numEditText.setText("");
        pswEditText.setText("");
        SharedPreferences.Editor editor = getSharedPreferences("data", 0)
                .edit();
        editor.putString("num_jiaowu", "");
        editor.putString("psw_jiaowu", "");
        editor.apply();
        File file = new File(filepath);
        file.delete();
        try {
            File[] childFiles = file.listFiles();
            int i;
            for (i = 0; i < childFiles.length; i++)
                childFiles[i].delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        show("已经清空保存的信息。");
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

    public void show(String str) {
        show(str, 0);
    }

    public void show(String str, int d) {
        if (d > 0)
            str = str.substring(0, str.length() - 1);
        showString = str;
        Message message = new Message();
        message.what = 2;
        handler.sendMessage(message);
    }

    public String POST(String url, String postdata) {
        String result = "";
        try {
            result = Common.commonPOST(url, postdata);

        } catch (Exception e) {
            if (dialog2.isShowing())
                dialog2.dismiss();
            show(getString(R.string.postFail));
        }
        return result;
    }

}
