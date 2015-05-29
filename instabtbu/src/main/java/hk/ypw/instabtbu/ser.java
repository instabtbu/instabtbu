package hk.ypw.instabtbu;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("HandlerLeak")
public class Ser extends Service {
    static boolean always = false;
    String num = "", psw = "";
    Thread main = null;
    long nowtime = System.currentTimeMillis();
    long lasttime = System.currentTimeMillis();
    PowerManager.WakeLock wakeLock = null;
    int DELAYTIME = 45;
    int delaytime = 45;
    long last = System.currentTimeMillis();
    DatagramSocket datagramSocket;
    Socket client = null;
    OutputStream outputStream;
    InputStream inputStream;
    String toaststr = "";
    Runnable rDenglu = new Runnable() {
        @Override
        public void run() {
            print("开始连接");
            if (connect()) {
                char[] buf = {0x7E, 0x11, 0x00, 0x00, 0x54, 0x01, 0x7E};
                send(buf);
                char[] rec = read();
                rec = Common.fanzhuan(rec);
                if (rec.length == 23) {
                    Common.verify = new char[16];
                    System.arraycopy(rec, 4, Common.verify, 0, 16);
                    char[] msg = Common.user(num, psw);
                    msg = Common.feng(msg, 0x01);
                    msg = Common.zhuan(msg);
                    send(msg);
                    rec = read();
                    rec = Common.jiefeng(rec);
                    if (rec != null) {
                        show("登录成功");
                    } else
                        print(Common.recString);
                } else {
                    delaytime = 2;
                }
            } else {
                delaytime = 2;
            }
        }
    };

    Toast toast = null;
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (toast == null)
                        toast = Toast.makeText(getApplicationContext(),
                                toaststr, Toast.LENGTH_SHORT);
                    else
                        toast.setText(toaststr);
                    toast.show();
                }
            } catch (Exception ignored) {
            }
        }
    };
    private ExecutorService executorService = Executors.newFixedThreadPool(16);
    Runnable mainThread = new Runnable() {
        @Override
        public void run() {
            SystemClock.sleep(5000);
            try {
                datagramSocket = new DatagramSocket();
                print("建立保持端口");
            } catch (Exception e) {
                e.printStackTrace();
                print("建立保持端口出错:" + e.getMessage());
            }

            while (always) {
                if (always) {
                    last = SystemClock.currentThreadTimeMillis();
                    InetAddress ip;
                    try {
                        ip = InetAddress.getByName("192.168.8.8");
                        char[] data = Common.getcmd(0);
                        print("保持:发送数据:" + Common.charsToHexString(data));
                        DatagramPacket datagramPacket = new DatagramPacket(
                                Common.charToByte(data), Common.remain.length + 7, ip, 21099);
                        datagramSocket.send(datagramPacket);
                        datagramSocket.setSoTimeout(15000);
                        datagramSocket.receive(datagramPacket);
                        // print("保持返回数据:"
                        // + bytesToHexString(datagramPacket.getData()));
                        delaytime = DELAYTIME;
                    } catch (Exception e) {
                        e.printStackTrace();
                        print("保持:出错:" + e.getMessage());
                        delaytime = 2;
                        executorService.submit(rDenglu);
                    }
                    shangwang.delaytime = SystemClock.currentThreadTimeMillis()
                            - last;
                }
                while (nowtime - lasttime < delaytime * 1000) {
                    SystemClock.sleep(100);
                    nowtime = System.currentTimeMillis();
                }
                lasttime = nowtime;
            }

        }
    };



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 当创建一个Servcie对象之后，会首先调用这个函数
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        Common.ip = Common.getIp(this);
        
        print("ser建立");
        
        String stopString = MobclickAgent.getConfigParams(this, "use");
        if (stopString.indexOf("Stop") > 0) {
            System.exit(0);
        }

        always = true;
        SharedPreferences sp = getSharedPreferences("data", 0);
        num = sp.getString("num", "");
        psw = sp.getString("psw", "");
        main = new Thread(mainThread);
        main.start();
        MobclickAgent.onResume(this);
        MobclickAgent.onEventBegin(this, "online");
        UmengUpdateAgent.update(this);

        if (always) {
            MobclickAgent.updateOnlineConfig(this);

            Notification notification = new Notification(R.drawable.ser_logo,
                    "instabtbu", System.currentTimeMillis());
            Intent notificationIntent = new Intent(this, shangwang.class);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            notification.icon = R.drawable.ser_logo;
            notification.tickerText = "保持在线服务已开启";
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);
            MobclickAgent.updateOnlineConfig(this);
            notification.setLatestEventInfo(this, "instabtbu",
                    MobclickAgent.getConfigParams(this, "t"), pendingIntent);
            // 公告

            startForeground(1, notification);
            try {
                PowerManager powerManager = (PowerManager) (this
                        .getSystemService(Context.POWER_SERVICE));
                if (wakeLock == null)
                    wakeLock = powerManager.newWakeLock(
                            PowerManager.PARTIAL_WAKE_LOCK, "instabtbu_online");
                wakeLock.acquire();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
        // START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
        // START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
        // START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
        // START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
    }

    @Override
    public void onDestroy() {
        always = false;
        try {
            wakeLock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MobclickAgent.onEventEnd(this, "online");
        MobclickAgent.onPause(this);
        try {
            stopForeground(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("ser结束");
        super.onDestroy();
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
        // print("发送数据:" + charsToHexString(buf) + ",长度:" + buf.length);
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
        // print("读到数据:" + charsToHexString(read2) + ",长度:" + read2.length);
        return read2;
    }
    
    public void show(String str) {
        show(str, 0);
    }

    public void show(String str, int d) {
        // d大于0则去尾部最后一个字
        if (d > 0)
            str = str.substring(0, str.length() - 1);
        toaststr = str;
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);
    }
    
}