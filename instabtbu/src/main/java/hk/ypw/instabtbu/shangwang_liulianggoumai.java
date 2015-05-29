package hk.ypw.instabtbu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class shangwang_liulianggoumai extends SwipeBackActivity {
    String psw = "";
    Activity thisActivity = this;
    String showString = "";
    String alertString = "";
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    new AlertDialog.Builder(thisActivity).setTitle("")
                            .setMessage(alertString)
                            .setPositiveButton("确定", null).show();
                } else if (msg.what == 3) {
                    Toast.makeText(getApplicationContext(), showString,
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ignored) {
            }
        }
    };
    private ProgressDialog dialog2;
    Runnable rbao8g = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sp = getSharedPreferences("data", 0);
            String num = sp.getString("num", "");

            String result;
            result = POST("https://self.btbu.edu.cn/cgi-bin/nacgi.cgi",
                    "textfield=" + num + "&textfield2=" + psw
                            + "&jsidx=1&radio=2&Submit=%CC%E1%BD%BB&nacgicmd=2");

            if (dialog2.isShowing())
                dialog2.dismiss();

            Pattern p = Pattern.compile("align=\"center\">(.*)<br>");
            Matcher m = p.matcher(result);
            if (m.find()) {
                String result2 = m.group(1).replace("<br>", "\r\n");
                System.out.println(m.group(1));
                show(result2);
            } else
                show("申请包月失败，请检查当前网络状态。");

        }
    };
    Runnable rbao20g = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sp = getSharedPreferences("data", 0);
            String num = sp.getString("num", "");
            String result;
            result = POST("https://self.btbu.edu.cn/cgi-bin/nacgi.cgi",
                    "textfield=" + num + "&textfield2=" + psw
                            + "&jsidx=2&radio=2&Submit=%CC%E1%BD%BB&nacgicmd=2");
//            System.out.println(result);
            if (dialog2.isShowing())
                dialog2.dismiss();
            Pattern p = Pattern.compile("align=\"center\">(.*)<br>");
            Matcher m = p.matcher(result);
            if (m.find()) {
                String result2 = m.group(1).replace("<br>", "\r\n");
                /**
                 * 成功申请包月(当月流量用完停止上网)方式
                 * 您没有输入上网密码
                 * 申请包月(当月流量用完停止上网)方式失败<br>原因：无效帐号或密码
                 */
                System.out.println(m.group(1));
                show(result2);
            } else if(result.length() == 0)
                show("申请包月失败，请检查当前网络状态。");
            else show(result);

            // if(result.indexOf("成功申请包月(当月流量用完停止上网)方式")!=-1)show("成功申请包月(当月流量用完停止上网)方式");
            // else
            /**
             * <td width="90%" height="388" class="STYLE11" align="center">
             * 您没有输入上网密码<br>
             */

        }
    };
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liulianggoumai);

        SharedPreferences sp = getSharedPreferences("data", 0);
        String yueString = sp.getString("yue", "");

        TextView yueTextView = (TextView) findViewById(R.id.liuliang_yue);
        yueTextView.setText(yueString);
    }

    public void bao8G(View v) {
        AlertDialog.Builder builder = new Builder(this);
        final EditText password = new EditText(this);
        password.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(password);
        builder.setMessage("确认包8GB流量吗？\n请输入您的密码：");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                psw = password.getText().toString();
                dialog2 = ProgressDialog.show(shangwang_liulianggoumai.this,
                        "正在申请", "正在申请流量包……", true, true);
                executorService.submit(rbao8g);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    public void bao20G(View v) {
        AlertDialog.Builder builder = new Builder(this);
        final EditText password = new EditText(this);
        password.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(password);
        builder.setMessage("确认包20GB流量吗？\n请输入您的密码：");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                psw = password.getText().toString();
                dialog2 = ProgressDialog.show(shangwang_liulianggoumai.this,
                        "正在申请", "正在申请流量包……", true, true);
                executorService.submit(rbao20g);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    public String POST(String url, String postdata) {
        String result = "";
        if (Common.isWifiConnected(this)) {
            try{
                result = Common.SSLPOST(url, postdata);
                result = new String(result.getBytes("ISO_8859_1"),"gbk");
            } catch (Exception e) {
                e.printStackTrace();
                show("连接BTBU失败。\n请确认信号良好再操作。");
            }
        } else {
            result = "wifi未连接!";
        }
        return result;
    }

    public void show(String str) {
        show(str, 0);
    }

    public void show(String str, int d) {
        // ==================================================================
        // 函数名：show
        // 作者：ypw
        // 功能：创建一个toast,显示提示,如果d大于0就把str最后一个字去掉,
        // 目的是把学校返回的最后一个换行符删去,以免toast只显示一行字,但是宽却占据两行
        // 输入参数：String str,int d
        // 返回值：void
        // ==================================================================
        if (d > 0)
            str = str.substring(0, str.length() - 1);
        showString = str;
        Message message = new Message();
        message.what = 3;
        handler.sendMessage(message);
    }

}
