package hk.ypw.instabtbu;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;

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
    String mainresult = "";
    long last = System.currentTimeMillis();
    DatagramSocket datagramSocket;
    Socket client = null;
    OutputStream outputStream;
    InputStream inputStream;
    char[] verify = null;
    char[] remain = null;
    String recString = "";
    String toaststr = "";
    Runnable rDenglu = new Runnable() {
        @Override
        public void run() {
            print("开始连接");
            if (connect()) {
                char[] buf = {0x7E, 0x11, 0x00, 0x00, 0x54, 0x01, 0x7E};
                send(buf);
                char[] rec = read();
                rec = fanzhuan(rec);
                if (rec.length == 23) {
                    verify = new char[16];
                    for (int i = 0; i < 16; i++) {
                        verify[i] = rec[i + 4];
                    }
                    shangwang.verify = verify;
                    char[] msg = user(num, psw);
                    msg = feng(msg, 0x01);
                    msg = zhuan(msg);
                    send(msg);
                    rec = read();
                    rec = jiefeng(rec);
                    shangwang.remain = remain;
                    if (rec != null) {
                        show("登录成功");
                    } else
                        print(recString);
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
            } catch (Exception e) {
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
                    InetAddress ip = null;
                    try {
                        ip = InetAddress.getByName("192.168.8.8");
                        char[] data = getcmd(0);
                        // print("保持发送数据:" + charsToHexString(data));
                        DatagramPacket datagramPacket = new DatagramPacket(
                                charToByte(data), remain.length + 7, ip, 21099);
                        datagramSocket.send(datagramPacket);
                        datagramSocket.setSoTimeout(15000);
                        datagramSocket.receive(datagramPacket);
                        // print("保持返回数据:"
                        // + bytesToHexString(datagramPacket.getData()));
                        delaytime = DELAYTIME;
                    } catch (Exception e) {
                        e.printStackTrace();
                        print("保持出错:" + e.getMessage());
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

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static PublicKey getPublicKey() {
        PublicKey publicKey = null;
        String modulus = "EA32BA96FCC395CC766EAFFEBC8EFE1F0886E99504CB7C3877548698793446BA7BA07CF915DBB5BE69337A3697B4DC354DA78ABAE17ED33EDAD87674D0D0D2B54D549E566AF0C016C276F327ADC3D4EE06E64EBC608E4AC9E3CE63416C246FD57DBEA8ADA036AA683F9A812CD8ECA705E019D6A943121CDDB2CF9BF1BCD0F5F9";
        String publicExponent = "65537";
        BigInteger m = new BigInteger(modulus, 16);
        BigInteger e = new BigInteger(publicExponent);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return publicKey;
    }

    public static byte[] charToByte(char[] buf) {
        byte[] rec = new byte[buf.length];
        int i;
        for (i = 0; i < buf.length; i++)
            rec[i] = (byte) buf[i];
        return rec;
    }

    public static char[] byteToChar(byte[] buf) {
        char[] rec = new char[buf.length];
        int i;
        for (i = 0; i < buf.length; i++)
            rec[i] = (char) buf[i];
        return rec;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    // 当创建一个Servcie对象之后，会首先调用这个函数
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        print("ser建立");
        verify = shangwang.verify;
        remain = shangwang.remain;

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
        // TODO Auto-generated method stub
        return START_STICKY;
        // START_STICKY：如果service进程被kill掉，保留service的状态为开始状态，但不保留递送的intent对象。随后系统会尝试重新创建service，由于服务状态为开始状态，所以创建服务后一定会调用onStartCommand(Intent,int,int)方法。如果在此期间没有任何启动命令被传递到service，那么参数Intent将为null。
        // START_NOT_STICKY：“非粘性的”。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统不会自动重启该服务。
        // START_REDELIVER_INTENT：重传Intent。使用这个返回值时，如果在执行完onStartCommand后，服务被异常kill掉，系统会自动重启该服务，并将Intent的值传入。
        // START_STICKY_COMPATIBILITY：START_STICKY的兼容版本，但不保证服务被kill后一定能重启。
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stubo
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

    protected void stopService() {
        Intent intent = new Intent(this, Ser.class);
        stopService(intent);
    }

    public void print(String p) {
        // System.out.println("OUT:"+p);
        // show(p);
        // SystemClock.sleep(200);
    }

    public char[] getcmd(int cmd) {
        char[] data = new char[remain.length + 7];
        data[0] = 0x7E;
        data[1] = (char) cmd;
        data[2] = 0x14;
        data[3] = 0x00;
        int i;
        for (i = 0; i < remain.length; i++)
            data[i + 4] = (char) remain[i];
        char[] crc = new char[remain.length + 3];
        for (i = 0; i < remain.length + 3; i++)
            crc[i] = data[i + 1];
        int c = getCRC16(charToByte(crc));
        data[crc.length + 1] = (char) (c & 0xFF);
        data[crc.length + 2] = (char) (c >> 8);
        data[crc.length + 3] = 0x7E;
        return data;
    }

    public char[] user(String number, String password) {
        char[] msg = new char[82];
        String ip = getIp();
        try {
            int i;
            for (i = 0; i < number.length(); i++)
                msg[i] = number.charAt(i);
            for (i = 0; i < password.length(); i++)
                msg[i + 23] = password.charAt(i);
            for (i = 0; i < ip.length(); i++)
                msg[i + 23 + 23] = ip.charAt(i);
            for (i = 0; i < verify.length; i++)
                msg[i + 23 + 23 + 20] = verify[i];
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        return msg;
    }

    public char[] user_noip(String number, String password) {
        char[] msg = new char[62];
        try {
            int i;
            for (i = 0; i < number.length(); i++)
                msg[i] = number.charAt(i);
            for (i = 0; i < password.length(); i++)
                msg[i + 23] = password.charAt(i);
            for (i = 0; i < verify.length; i++)
                msg[i + 23 + 23] = verify[i];
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        return msg;
    }

    public String getIp() {
        String ip = "";
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled()) {
                return "";
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        return ip;
    }

    public String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

    public char[] feng(char[] buf, int cmd) {
        char[] rec = new char[1024];
        char[] jiami = jiami(buf);
        char[] crc = new char[jiami.length + 3];
        char[] rec2 = new char[crc.length + 4];
        rec[0] = 0x7E;
        rec[1] = (char) cmd;
        int len = jiami.length;
        rec[2] = (char) (len & 0xFF);
        rec[3] = (char) (len >> 8);

        int i;
        for (i = 0; i < jiami.length; i++)
            rec[i + 4] = jiami[i];

        for (i = 1; i < jiami.length + 4; i++)
            crc[i - 1] = rec[i];

        int c = getCRC16(charToByte(crc));

        rec[crc.length + 1] = (char) (c & 0xFF);
        rec[crc.length + 2] = (char) (c >> 8);
        rec[crc.length + 3] = 0x7E;
        for (i = 0; i < crc.length + 4; i++)
            rec2[i] = rec[i];

        return rec2;
    }

    public char[] jiefeng(char[] buf) {
        char[] rec = null;
        try {
            int len = 0;
            int buf2 = buf[2], buf3 = buf[3];
            if (buf2 > 256)
                buf2 = 0x100 - 0x10000 + buf2;
            if (buf3 > 256)
                buf3 = 0x100 - 0x10000 + buf3;
            len = buf2 + buf3 * 0x100;
            buf = fanzhuan(buf);
            rec = new char[len];
            for (int i = 0; i < len; i++)
                rec[i] = buf[i + 4];
            if (buf[1] == 1) {
                rec = jiemi(rec);
                // print("解密数据:" + charsToHexString(rec) + ",长度:" + rec.length);
                remain = rec;
            } else {
                recString = new String(charToByte(rec), "GBK");
                recString = recString.subSequence(0, recString.length() - 1)
                        .toString();
                rec = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rec;
    }

    public char[] zhuan(char[] buf) {
        char[] rec = new char[1024];
        int i = 0, j = 0;

        for (i = 0; i < buf.length; i++) {
            if ((buf[i] == 0x7D || buf[i] == 0x7E) && i != 0
                    && i != buf.length - 1) {
                rec[j++] = 0x7D;
                rec[j++] = (char) (buf[i] ^ 0x40);

            } else {
                rec[j++] = buf[i];
            }
        }

        char[] rec2 = new char[j];
        for (i = 0; i < j; i++)
            rec2[i] = rec[i];
        return rec2;
    }

    public char[] fanzhuan(char[] buf) {
        char[] rec = new char[1024];
        int i, j = 0;
        for (i = 0; i < buf.length; i++) {
            if (buf[i] == 0x7D) {
                rec[j++] = (char) (buf[++i] ^ 0x40);
            } else
                rec[j++] = buf[i];
        }

        char[] rec2 = new char[j];
        for (i = 0; i < j; i++)
            rec2[i] = rec[i];
        return rec2;
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
            byte[] send = charToByte(buf);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        // print("读到数据:" + charsToHexString(read2) + ",长度:" + read2.length);
        return read2;
    }

    // public static String bytesToHexString(byte[] src) {
    // StringBuilder stringBuilder = new StringBuilder("");
    // if (src == null || src.length <= 0) {
    // return null;
    // }
    // for (int i = 0; i < src.length; i++) {
    // int v = src[i] & 0xFF;
    // String hv = Integer.toHexString(v);
    // if (hv.length() < 2) {
    // stringBuilder.append(0);
    // }
    // stringBuilder.append(hv.toUpperCase());
    // }
    // return stringBuilder.toString();
    // }

    // public static String charsToHexString(char[] src) {
    // StringBuilder stringBuilder = new StringBuilder("");
    // if (src == null || src.length <= 0) {
    // return null;
    // }
    // for (int i = 0; i < src.length; i++) {
    //
    // int v = src[i] & 0xFF;
    // String hv = Integer.toHexString(v);
    // if (hv.length() < 2) {
    // stringBuilder.append(0);
    // }
    // stringBuilder.append(hv.toUpperCase());
    // // stringBuilder.append(",");
    // }
    // return stringBuilder.toString();
    // }

    public int getCRC16(byte[] bytes) {
        int[] table = {0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014,
                0x8011, 0x8033, 0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027,
                0x0022, 0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D, 0x8077,
                0x0072, 0x0050, 0x8055, 0x805F, 0x005A, 0x804B, 0x004E, 0x0044,
                0x8041, 0x80C3, 0x00C6, 0x00CC, 0x80C9, 0x00D8, 0x80DD, 0x80D7,
                0x00D2, 0x00F0, 0x80F5, 0x80FF, 0x00FA, 0x80EB, 0x00EE, 0x00E4,
                0x80E1, 0x00A0, 0x80A5, 0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4,
                0x80B1, 0x8093, 0x0096, 0x009C, 0x8099, 0x0088, 0x808D, 0x8087,
                0x0082, 0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197,
                0x0192, 0x01B0, 0x81B5, 0x81BF, 0x01BA, 0x81AB, 0x01AE, 0x01A4,
                0x81A1, 0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB, 0x01FE, 0x01F4,
                0x81F1, 0x81D3, 0x01D6, 0x01DC, 0x81D9, 0x01C8, 0x81CD, 0x81C7,
                0x01C2, 0x0140, 0x8145, 0x814F, 0x014A, 0x815B, 0x015E, 0x0154,
                0x8151, 0x8173, 0x0176, 0x017C, 0x8179, 0x0168, 0x816D, 0x8167,
                0x0162, 0x8123, 0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137,
                0x0132, 0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E, 0x0104,
                0x8101, 0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D, 0x8317,
                0x0312, 0x0330, 0x8335, 0x833F, 0x033A, 0x832B, 0x032E, 0x0324,
                0x8321, 0x0360, 0x8365, 0x836F, 0x036A, 0x837B, 0x037E, 0x0374,
                0x8371, 0x8353, 0x0356, 0x035C, 0x8359, 0x0348, 0x834D, 0x8347,
                0x0342, 0x03C0, 0x83C5, 0x83CF, 0x03CA, 0x83DB, 0x03DE, 0x03D4,
                0x83D1, 0x83F3, 0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7,
                0x03E2, 0x83A3, 0x03A6, 0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7,
                0x03B2, 0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E, 0x0384,
                0x8381, 0x0280, 0x8285, 0x828F, 0x028A, 0x829B, 0x029E, 0x0294,
                0x8291, 0x82B3, 0x02B6, 0x02BC, 0x82B9, 0x02A8, 0x82AD, 0x82A7,
                0x02A2, 0x82E3, 0x02E6, 0x02EC, 0x82E9, 0x02F8, 0x82FD, 0x82F7,
                0x02F2, 0x02D0, 0x82D5, 0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4,
                0x82C1, 0x8243, 0x0246, 0x024C, 0x8249, 0x0258, 0x825D, 0x8257,
                0x0252, 0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264,
                0x8261, 0x0220, 0x8225, 0x822F, 0x022A, 0x823B, 0x023E, 0x0234,
                0x8231, 0x8213, 0x0216, 0x021C, 0x8219, 0x0208, 0x820D, 0x8207,
                0x0202};
        int i = 0;
        int len = bytes.length;
        int crc = 0;
        while (i < len) {
            int index = (crc >> 8) ^ bytes[i++];
            if (index < 0)
                index += 256;
            crc = ((crc & 0xFF) << 8) ^ table[index];
        }
        return crc;
    }

    @SuppressLint("TrulyRandom")
    public char[] jiami(char[] data) {
        PublicKey key = getPublicKey();
        Cipher cipher;
        byte[] buf = null;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            buf = cipher.doFinal(charToByte(data));
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        return byteToChar(buf);
    }

    public char[] jiemi(char[] data) {
        PublicKey key = getPublicKey();
        Cipher cipher;
        byte[] buf = null;
        // print("解密前:" + charsToHexString(data));
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            buf = cipher.doFinal(charToByte(data));
        } catch (Exception e) {
            e.printStackTrace();
            print("错误:" + e.getMessage());
        }
        return byteToChar(buf);
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

    public boolean find(String text, String w) {
        // 从text里找w，有则返回真
        return text.indexOf(w) != -1;
    }

}