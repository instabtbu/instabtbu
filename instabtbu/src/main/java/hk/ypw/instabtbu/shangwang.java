package hk.ypw.instabtbu;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class shangwang extends Activity {
    static long delaytime = 0;
    /**
     * @author ypw
     * @time 2014-09-17
     */
    long uiId = 0;
    Leftmenu Leftmenu;
    SlidingMenu menu;
    Activity thisActivity = this;
    String liuliangString = "";
    String shebeiString = "";
    String showString = "";
    Toast toast;
    int liuliangColor = 0;
    boolean always = true;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);

                EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
                EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
                TextView userTextView = (TextView) findViewById(R.id.leftmenu_textview_user);
                TextView user2TextView = (TextView) findViewById(R.id.leftmenu_textview_dianzheli);
                if (msg.what == 1) {
                    editText_num.setEnabled(false);
                    editText_psw.setEnabled(false);
                    userTextView.setText(editText_num.getText());
                    user2TextView.setText("登录成功");
                    denglubutton(0);
                } else if (msg.what == 2) {
                    editText_num.setEnabled(true);
                    editText_psw.setEnabled(true);
                    userTextView.setText("还没有登录");
                    user2TextView.setText("请登录");
                    denglubutton(1);
                } else if (msg.what == 3) {
                    if (toast == null)
                        toast = Toast.makeText(getApplicationContext(),
                                showString, Toast.LENGTH_SHORT);
                    else
                        toast.setText(showString);
                    toast.show();
                } else if (msg.what == 4) {
                    TextView liuliang = (TextView) findViewById(R.id.shangwang_textliuliang);
                    liuliang.setText(liuliangString + " MB");
                    TextView shebei = (TextView) findViewById(R.id.shangwang_textshebei);
                    shebei.setText(shebeiString);
                // } else if (msg.what == 5) {
                    // if(dialog2.isShowing())
                    // {
                    // dialog2.setMessage(gengxinString);
                    // }
                } else if (msg.what == 6) {
                    TextView liuliang = (TextView) findViewById(R.id.shangwang_textliuliang);
                    TextView shibie = (TextView) findViewById(R.id.shangwang_textshebei);
                    liuliang.setTextColor(liuliangColor);
                    shibie.setTextColor(liuliangColor);
                }

            } catch (Exception ignored) {

            }
        }
    };

    Runnable bianseRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 255; i += 5) {
                liuliangColor = Color.argb(255, 255, 255, i);
                Message message = new Message();
                message.what = 6;
                handler.sendMessage(message);
                SystemClock.sleep(20);
            }

        }
    };
    Socket client = null;
    OutputStream outputStream;
    InputStream inputStream;
    String gengxinString = "";
    Runnable rDenglu = new Runnable() {
        @Override
        public void run() {
            EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
            EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
            gengxinString = "";
            gengxin("开始连接服务器...");
            print("开始连接");
            try {
                if (connect()) {
                    char[] buf = {0x7E, 0x11, 0x00, 0x00, 0x54, 0x01, 0x7E};
                    gengxin("服务器连接成功,正在发送请求...");
                    send(buf);
                    gengxin("发送请求成功,正在等待验证...");
                    char[] rec = read();
                    rec = Common.fanzhuan(rec);
                    if (rec.length == 23) {
                        Common.verify = new char[16];
                        gengxin("服务器验证成功,正在发送登录请求...");
                        System.arraycopy(rec, 4, Common.verify, 0, 16);
                        char[] msg = Common.user(editText_num.getText().toString(),
                                editText_psw.getText().toString());
                        msg = Common.feng(msg, 0x01);
                        msg = Common.zhuan(msg);
                        send(msg);
                        gengxin("发送登录请求成功,正在等待验证...");
                        rec = read();
                        gengxin("接收返回成功,正在处理...");
                        rec = Common.jiefeng(rec);
                        try {
                            client.close();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        if (rec != null) {
                            SharedPreferences.Editor editor = getSharedPreferences(
                                    "data", 0).edit();
                            editor.putString("num", editText_num.getText()
                                    .toString());
                            editor.putString("psw", editText_psw.getText()
                                    .toString());
                            editor.apply();
                            // print("登录成功:" + charsToHexString(rec));
                            show("登录成功");
                            Ser.always = true;
                            startService();
                        } else {
                            print(Common.recString);
                            show(Common.recString);
                        }
                    } else
                        show("获取数据出错");
                } else {
                    print("连接BTBU服务器出错");
                }
            } catch (Exception e) {
                show("连接失败\n服务器已断开连接");
            }
            // dialog2.dismiss();
        }
    };
    Runnable rDuankailianjie = new Runnable() {
        @Override
        public void run() {
            /**
             * 这个很简单,我们把帐号和密码post到这个接口上就可以强制断开
             * 地址:http://self.btbu.edu.cn/cgi-bin/nacgi.cgi
             * 数据:textfield=你的学号&textfield2
             * =你的密码&Submit=%CC%E1%BD%BB&nacgicmd=9&radio=1&jsidx=1
             * 如果返回文本里带这句话"成功断开本帐号的当前的所有连接"那就是成功了,否则失败
             */
            String result = "";
            try {
                EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
                EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
                result = POST(
                        "http://self.btbu.edu.cn/cgi-bin/nacgi.cgi",
                        "textfield="
                                + editText_num.getText()
                                + "&textfield2="
                                + editText_psw.getText()
                                + "&Submit=%CC%E1%BD%BB&nacgicmd=9&radio=1&jsidx=1");

                result = new String(result.getBytes("ISO_8859_1"), "gbk");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (find(result, "成功断开本帐号的当前的所有连接"))
                show("成功断开本帐号的当前的所有连接");
            else
                show("断开失败");

        }
    };
    Runnable duankaiRunnable = new Runnable() {
        @Override
        public void run() {
            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket();
                print("建立断开socket");
            } catch (Exception e) {
                e.printStackTrace();
            }
            InetAddress ip;
            try {
                ip = InetAddress.getByName("192.168.8.8");
                char[] data = Common.getcmd(1);
                // print("断开发送数据:" + charsToHexString(data));
                DatagramPacket datagramPacket = new DatagramPacket(
                        Common.charToByte(data), Common.remain.length + 7, ip, 21099);

                assert datagramSocket != null;

                datagramSocket.send(datagramPacket);
                datagramSocket.send(datagramPacket);
                datagramSocket.send(datagramPacket);
                Common.remain = null;
                show("断开成功");
            } catch (NullPointerException e) {
                show("你还没有登录");
            } catch (Exception e) {
                e.printStackTrace();
                print("断开出错:" + e.getMessage());
            }
        }
    };
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    Runnable rChaliuliang = new Runnable() {
        @Override
        public void run() {
            EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
            EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);

            print("开始连接");
            connect();
            char[] buf = {0x7E, 0x11, 0x00, 0x00, 0x54, 0x01, 0x7E};
            send(buf);
            try {
                char[] rec = read();

                rec = Common.fanzhuan(rec);
                if (rec.length == 23) {
                    Common.verify = new char[16];
                    System.arraycopy(rec, 4, Common.verify, 0, 16);
                    char[] msg = Common.user_noip(editText_num.getText().toString(),
                            editText_psw.getText().toString());
                    msg = Common.feng(msg, 0x03);
                    msg = Common.zhuan(msg);
                    send(msg);
                    rec = read();
                    Common.jiefeng(rec);
                    try {
                        client.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    String result = Common.recString;
                    //print(result);
                    String yueString = "";
                    if (!find(result, "流量"))
                        show(result, 1);
                    else {
                        try {
                            Pattern p = Pattern.compile(getString(R.string.shangwang_liuliang));
                            Matcher m = p.matcher(result);
                            int liuliang = 0;
                            while (m.find()) {
                                String tempString = m.group(1);
                                liuliang += Integer.valueOf(tempString);
                            }
                            liuliangString = String.valueOf(liuliang);
                            p = Pattern.compile(getString(R.string.regexWangfei));
                            m = p.matcher(result);

                            if (m.find()) {
                                yueString = m.group(1);
                            }
                        } catch (Exception e) {
                            liuliangString += "err";
                            e.printStackTrace();
                        }

                        try {
                            Pattern p = Pattern.compile(getString(R.string.regexZaixian));
                            Matcher m = p.matcher(result);
                            if (m.find()) {
                                shebeiString = m.group(1);
                            }
                        } catch (Exception ignored) {

                        }

                        SharedPreferences.Editor editor = getSharedPreferences(
                                "data", 0).edit();
                        editor.putString("num", editText_num.getText()
                                .toString());
                        editor.putString("psw", editText_psw.getText()
                                .toString());
                        editor.putString("liuliang", liuliangString);
                        editor.putString("shebei", shebeiString);
                        editor.putString("yue", yueString);
                        editor.apply();

                        Message message = new Message();
                        message.what = 4;
                        handler.sendMessage(message);
                        executorService.submit(bianseRunnable);
                        // show("你还有" + liuliangString + "MB流量。");
                    }
                }
            } catch (Exception e) {
                show(getString(R.string.connectFail));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.ip = Common.getIp(this);
        setContentView(R.layout.activity_shangwang);
        if (getIntent().getIntExtra("menu", 0) == 0) {
            Intent intent = new Intent();
            intent.setClass(this, First_background.class);
            startActivity(intent);
        }

        // WifiManager wifi = (WifiManager)
        // getSystemService(Context.WIFI_SERVICE);
        // WifiConfiguration wifiConfiguration = new WifiConfiguration();
        // wifiConfiguration.SSID = "BTBU";
        // wifiConfiguration.allowedKeyManagement.clear();
        // wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
        // wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);

        uiId = Thread.currentThread().getId();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        ImageView zidongdengluImageView = (ImageView) findViewById(R.id.shangwang_zidongdenglu);
        ImageView zidongchaImageView = (ImageView) findViewById(R.id.shangwang_zidongcha);
        EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
        EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
        TextView liuliangTextView = (TextView) findViewById(R.id.shangwang_textliuliang);
        TextView shebeiTextView = (TextView) findViewById(R.id.shangwang_textshebei);
        /**
         * 在这个函数中找到界面中的元素的实例,没有实例就无法操作界面上的元素 举个例子,你需要把textview流量上面的字改成12345
         * MB,你需要这样做: TextView liuliangTextView =
         * (TextView)findViewById(R.id.shangwang_textliuliang);
         * liuliangTextView.setText("12345 MB");
         * 第一行从R文件中寻找到了Id,然后从Id寻找到了View,再将View强制类型转换到TextView
         * 为什么要强制类型转换呢,因为View没有setText这个方法 所以我们要这样写
         */

        try {
            SharedPreferences sp = getSharedPreferences("data", 0);
            String num = sp.getString("num", "");
            String psw = sp.getString("psw", "");
            String liuliang = sp.getString("liuliang", "");
            String shebei = sp.getString("shebei", "");
            String zidong = sp.getString("zidong", "");
            String zidongcha = sp.getString("zidongcha", "");
            /**
             * SharedPreferences是一种轻型的数据存储方式,它的本质是基于XML文件存储key-value键值对数据
             * 通常用来存储一些简单的配置信息,我们在这里存储帐号密码,因为没有比这个更方便的方法了.
             * 如果有人觉得这个不安全,那么请他不要下载盗版软件和病毒,因为除非是被修改过的instabtbu
             * 或者root过的app,不然没有人能读到instabtbu创建的SharedPreferences.
             * 我们现在在SharedPreferences的data中读取我们的num(学号),psw(密码)
             * liuliang(流量),为什么读流量呢?因为有人可能在没连wifi或者wifi质量特别差的情况下打开instabtbu
             * 我们为了让他能够显示出流量来,默认储存上一次查过的流量,这样能给用户带来比较好的体验.
             * zidong(自动登录),zidongcha(自动查流量),自动查流量这个勾勾是为了避免包流量又没交钱的用户掉线.
             */

            editText_num.setText(num);
            editText_psw.setText(psw);
            if (liuliang != null) {
                if (liuliang.length() != 0)
                    liuliangTextView.setText(liuliang + " MB");
            }
            if (shebei != null) {
                if (shebei.length() != 0)
                    shebeiTextView.setText(shebei);
            }
            if (find(zidongcha, "z"))
                zidongchaImageView
                        .setBackgroundResource(R.drawable.shangwang_zidongcha1);
            else
                zidongchaImageView
                        .setBackgroundResource(R.drawable.shangwang_zidongcha0);

            /**
             * 这里我们让num和psw的编辑框显示上次登录成功之后保存的帐号和密码,
             * 然后如果上次保存了流量(判断它的长度是否等于0),我们才显示流量,避免出现null这样的字眼.
             * 然后zidongchaImageView(自动查流量)设置勾上与否,如果在zidongcha(刚才获取的保存信息)找到了z这个字
             * 那我们就给zidongchaImageView赋予绿色图标,否则就是灰色.
             *
             * 其中,find函数是我自己定义的一个函数,用法在定义处有.功能是寻找有没有某字符串.
             */

            if (find(zidongcha, "z") && Common.isWifiConnected(this))
                executorService.submit(rChaliuliang);
            /**
             * 如果自动查流量的功能开启了,那我们就把chaliuliang2这个任务投入线程池中 线程池的语法: private
             * ExecutorService executorService =
             * Executors.newCachedThreadPool();
             * executorService.submit(rChaliuliang2); 第一句定义了线程池,一般放在外面当作全局变量
             * (如果你想知道它在哪里被定义了,请点击executorService按F3) 第二句就是把一个Runnable投入到线程池中.
             * 一个Runnable相当于一个任务(task) 之后会有Runnable的写法.
             */

            if (find(zidong, "z")) {
                zidongdengluImageView
                        .setBackgroundResource(R.drawable.shangwang_zidongdenglu1);
                if (!checkser()) {
                    if (Common.isWifiConnected(this))
                        denglu(null);
                }
                /**
                 * 如果自动,那么就给checkbox1(自动登录)设置勾勾 然后如果服务没有被开启,我们就进行查流量
                 */
            } else
                zidongdengluImageView
                        .setBackgroundResource(R.drawable.shangwang_zidongdenglu0);

        } catch (Exception ex) {
            SharedPreferences.Editor editor = getSharedPreferences("data", 0)
                    .edit();
            editor.putString("num", "");
            editor.putString("psw", "");
            editor.putString("liuliang", "");
            editor.putString("zidong", "f");
            editor.putString("zidongcha", "f");
            editor.apply();
        }
        /**
         * 如果出现错误,那么表示从未点过自动这个勾勾,我们就给这里的数据赋值
         * 因为如果有用户从不点这个勾勾,那它就永远不会查流量(即使自动查流量点了勾勾)
         */

        try {
            SharedPreferences sp = getSharedPreferences("data", 0);
            String banben = sp.getString("banben", "");
            PackageInfo pi = this.getPackageManager().getPackageInfo(
                    this.getPackageName(), 0);
            System.out.println(banben);

            if (banben != null) {
                if (!banben.contains(pi.versionName)) {
                       System.out.println("新版本");
                       Intent intent1 = new Intent();
                       intent1.setClass(thisActivity, First_jieshao.class);
                       startActivity(intent1);
                       SharedPreferences.Editor editor = getSharedPreferences("data",
                               0).edit();
                       editor.putString("banben", banben + "\n" + pi.versionName);
                       editor.apply();
               }
            }else{
                //第一次运行
                try {
                    Intent intent1 = new Intent();
                    intent1.setClass(thisActivity, First_jieshao.class);
                    startActivity(intent1);
                    show("欢迎使用instabtbu，您可以在登录成功后启用自动登录功能。");
                    SharedPreferences.Editor editor = getSharedPreferences("data",
                            0).edit();
                    editor.putString("banben", pi.versionName);
                    editor.apply();
                } catch (Exception ignored) {

                }
            }
        } catch (Exception ignored) {

        }

        zidongdengluImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView zidongdengluImageView = (ImageView) findViewById(R.id.shangwang_zidongdenglu);
                SharedPreferences sp = getSharedPreferences("data", 0);
                String zidong = sp.getString("zidong", "");
                SharedPreferences.Editor editor = getSharedPreferences("data",
                        0).edit();
                System.out.println(zidong);
                if (!find(zidong, "z")) {
                    editor.putString("zidong", "z");
                    zidongdengluImageView
                            .setBackgroundResource(R.drawable.shangwang_zidongdenglu1);
                } else {
                    editor.putString("zidong", "f");
                    zidongdengluImageView
                            .setBackgroundResource(R.drawable.shangwang_zidongdenglu0);
                }
                editor.apply();
            }
        });

        zidongchaImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView) findViewById(R.id.shangwang_zidongcha);
                SharedPreferences sp = getSharedPreferences("data", 0);
                String zidongcha = sp.getString("zidongcha", "");
                SharedPreferences.Editor editor = getSharedPreferences("data",
                        0).edit();
                System.out.println(zidongcha);
                if (!find(zidongcha, "z")) {
                    editor.putString("zidongcha", "z");
                    imageView
                            .setBackgroundResource(R.drawable.shangwang_zidongcha1);
                } else {
                    editor.putString("zidongcha", "f");
                    imageView
                            .setBackgroundResource(R.drawable.shangwang_zidongcha0);
                }
                editor.apply();
            }
        });

        try {
            Leftmenu = new Leftmenu(thisActivity, 1);
            menu = Leftmenu.menu;

            TextView left_userTextView = (TextView) thisActivity
                    .findViewById(R.id.leftmenu_textview_user);
            TextView left_user2TextView = (TextView) thisActivity
                    .findViewById(R.id.leftmenu_textview_dianzheli);
            left_userTextView.setText("还没有登录");
            left_user2TextView.setText("请登录");

        } catch (Exception ignored) {
        }

        final ImageView dengluImageView = (ImageView) findViewById(R.id.shangwang_deglu);
        dengluImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dengluImageView.getBackground().setAlpha(128);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    dengluImageView.getBackground().setAlpha(255);
                    if (!checkser())
                        denglu(null);
                }
                return true;
            }
        });

        final ImageView duankaiImageView = (ImageView) findViewById(R.id.shangwang_duankai);
        duankaiImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    duankaiImageView.getBackground().setAlpha(128);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    duankaiImageView.getBackground().setAlpha(255);
                    duankai(null);
                }

                return true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (always) {
                    try {
                        checkser();
                    } catch (Exception ignored) {}
                    SystemClock.sleep(500);
                }
            }
        }).start();

        UmengUpdateAgent.forceUpdate(this);
        UmengUpdateAgent.update(this);
        MobclickAgent.updateOnlineConfig(this);

        FeedbackAgent agent = new FeedbackAgent(this);
        agent.sync();

        // DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        // Double width = (double) mDisplayMetrics.widthPixels;
        // Double height = (double) mDisplayMetrics.heightPixels;
        //
        // System.out.println("宽高比:"+height/width);

        // if(height/width!=16.0/9.0){
        // ImageView forceImageView =
        // (ImageView)findViewById(R.id.shangwang_imgforce);
        // forceImageView.setVisibility(View.INVISIBLE);
        // }
    }

    @Override
    public void onResume() {
        super.onResume();
        Leftmenu.leftmenu_ui(0);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        always = false;
    }
    public void liulianggoumai(View v) {
        MobclickAgent.onEvent(thisActivity, getString(R.string.umeng_liulianggoumai));
        Intent intent = new Intent();
        intent.setClass(thisActivity, shangwang_liulianggoumai.class);
        startActivity(intent);
    }

    public boolean checkser() {
        boolean boolser = false;
        try {
            boolser = Ser.always;
            Boolean isRunning = false;
            ActivityManager activityManager = (ActivityManager) this
                    .getApplicationContext().getSystemService(
                            Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                    .getRunningServices(100);
            for (int i = 0; i < serviceList.size(); i++) {
                if (find(serviceList.get(i).service.getClassName(),
                        "hk.ypw.instabtbu.Ser")) {
                    isRunning = true;
                    break;
                }
            }
            if (isRunning && !boolser)
                stopService();

            EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
            EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);

            try {
                if (Thread.currentThread().getId() != uiId) {
                    Message message = new Message();
                    if (boolser)
                        message.what = 1;
                    else
                        message.what = 2;
                    handler.sendMessage(message);
                } else {
                    if (boolser) {
                        editText_num.setEnabled(false);
                        editText_psw.setEnabled(false);
                        denglubutton(0);
                    } else {
                        editText_num.setEnabled(true);
                        editText_psw.setEnabled(true);
                        denglubutton(1);
                    }
                }
            } catch (Exception ignored) {
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return boolser;
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

    public String POST(String url, String postdata) {
        long last = System.currentTimeMillis();
        String result = "";
        if (Common.isWifiConnected(this)) {
            // System.out.println("cmd="+zhongjian(postdata,
            // "netlogincmd=","&"));
            try {
                // System.out.println(url);
                result = Common.SSLPOST(url, postdata);
                delaytime = System.currentTimeMillis() - last;
            } catch (Exception e) {
                e.printStackTrace();
                show("连接BTBU失败。\n请确认信号良好再操作。");
            }
        } else {
            result = "wifi未连接!!";
            delaytime = 30000;
        }
        return result;
    }

    /**
     * delaytime表示的是当前的延迟毫秒数
     */

    public void denglubutton(int a) {
        // ==================================================================
        // 函数名：denglubutton
        // 作者：ypw
        // 功能：下面全部是用来更新UI的,由于几个地方都要用到,所以我写成了一个函数
        // 输入参数：a,a=1代表没有登录,否则就是登录成功
        // 返回值：void
        // ==================================================================

        ImageView bdenglu = (ImageView) findViewById(R.id.shangwang_deglu);
        ImageView zhuangtaImageView = (ImageView) findViewById(R.id.shangwang_zhuangtai);
        if (a == 1) {
            bdenglu.setBackgroundResource(R.drawable.shangwang_denglu1);
            zhuangtaImageView
                    .setBackgroundResource(R.drawable.shangwang_zhuangtai0);
        } else {
            bdenglu.setBackgroundResource(R.drawable.shangwang_denglu0);
            // System.out.println("延迟:"+delaytime);
            // if(delaytime<300)
            // zhuangtaImageView.setBackgroundResource(R.drawable.shangwang_yilianjie5);
            // else
            // if(delaytime<800)zhuangtaImageView.setBackgroundResource(R.drawable.shangwang_yilianjie4);
            // else
            // if(delaytime<1500)zhuangtaImageView.setBackgroundResource(R.drawable.shangwang_yilianjie3);
            // else
            // if(delaytime<3000)zhuangtaImageView.setBackgroundResource(R.drawable.shangwang_yilianjie2);
            // else
            // zhuangtaImageView.setBackgroundResource(R.drawable.shangwang_yilianjie1);
            zhuangtaImageView
                    .setBackgroundResource(R.drawable.shangwang_zhuangtai1);
        }
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

    public boolean find(String text, String w) {
        // ==================================================================
        // 函数名：find
        // 作者：ypw
        // 功能：在一个字符串(text)当中寻找另一个字符串(w)
        // 输入参数：String text,String w
        // 返回值： boolean 找到返回true,没找到返回false
        // ==================================================================
        return text.contains(w);
    }

    public void print(String p) {
        System.out.println("OUT:"+p);
        // show(p);
        // SystemClock.sleep(200);
    }

    public boolean connect() {
        boolean f = false;
        try {
            client = new Socket("192.168.8.8", 21098);
            outputStream = client.getOutputStream();
            inputStream = client.getInputStream();
            client.setSoTimeout(30000);
            f = true;
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        return f;
    }

    public boolean send(char[] buf) {
        boolean f = false;
        try {
            byte[] send = Common.charToByte(buf);
            outputStream.write(send);
            outputStream.flush();
            f = true;
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        print("发送数据:" + Common.charsToHexString(buf) + ",长度:" + buf.length);
        return f;
    }

    public char[] read() {
        char[] read2 = null;
        try {
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            read2 = new char[len];
            int j;
            for (j = 0; j < len; j++)
                read2[j] = (char) buffer[j];
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        if (read2 != null) {
            print("读到数据:" + Common.charsToHexString(read2) + ",长度:" + read2.length);
        }
        return read2;
    }

    protected void startService() {
        Intent intent = new Intent(this, Ser.class);
        startService(intent);
        checkser();
    }

    protected void stopService() {
        Intent intent = new Intent(this, Ser.class);
        stopService(intent);
        checkser();
    }

    public void denglu(View v) {
        // ==================================================================
        // 函数名：denglu
        // 作者：ypw
        // 功能：将登录任务投入线程池中,再对UI进行更新,
        // 输入参数：View v(如果在layout里用onClick,就必须要写View v)
        // 返回值：void
        // ==================================================================
        if (Common.isWifiConnected(this)) {
            // dialog2 = ProgressDialog.show(this, "正在登录", "正在登录中……",true,true);
            executorService.submit(rDenglu);
            EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
            EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
            editText_num.setEnabled(false);
            editText_psw.setEnabled(false);
            // denglubutton(0);
        } else {
            show(getString(R.string.shangwang_wifi));
        }

    }

    void gengxin(String gx) {
        System.out.println(gx);
        // gengxinString+=gx+"\n";
        // Message message = new Message();
        // message.what=5;
        // handler.sendMessage(message);
        // show(gx);
    }

    public void force(View v) {
        // ==================================================================
        // 函数名：fotce
        // 作者：ypw
        // 功能：强制断开,由于没有统计的必要,我们没有加友盟统计
        // 输入参数：View v(如果在layout里用onClick,就必须要写View v)
        // 返回值：void
        // ==================================================================
        executorService.submit(rDuankailianjie);
        stopService();
    }

    public void chaliuliang(View v) {
        // ==================================================================
        // 函数名：chaliuliang
        // 作者：ypw
        // 功能：将查流量任务投入线程池
        // 输入参数：View v(如果在layout里用onClick,就必须要写View v)
        // 返回值：void
        // ==================================================================
        executorService.submit(rChaliuliang);
        MobclickAgent.onEvent(this, "Chaliuliang");
        // 上面一句话是友盟的统计
    }

    public void duankai(View v) {
        // ==================================================================
        // 函数名：duankai
        // 作者：ypw
        // 功能：将断开任务投入线程池,并且将编辑框设置为可编辑(Enabled)
        // 输入参数：View v(如果在layout里用onClick,就必须要写View v)
        // 返回值：void
        // ==================================================================
        denglubutton(0);
        stopService();
        EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
        EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
        editText_num.setEnabled(true);
        editText_psw.setEnabled(true);
        executorService.submit(duankaiRunnable);
    }

}