package hk.ypw.instabtbu;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

public class Common {
    static String ip;

    static char[] verify = null;
    static char[] remain = null;
    static String recString = "";

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
        for (i = 0; i < buf.length; i++) {
            rec[i] = (char) buf[i];
        }
        return rec;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase());
        }
        return stringBuilder.toString();
    }

    public static String charsToHexString(char[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (char aSrc : src) {

            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv.toUpperCase());
            // stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }


    public static int getCRC16(byte[] bytes) {
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

    public static char[] zhuan(char[] buf) {
        char[] rec = new char[1024];
        int i, j = 0;

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

    public static char[] fanzhuan(char[] buf) {
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

    public static char[] feng(char[] buf, int cmd) {
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

    public static char[] jiefeng(char[] buf) {
        char[] rec = null;
        try {
            int len;
            int buf2 = buf[2], buf3 = buf[3];
            if (buf2 > 256)
                buf2 = 0x100 - 0x10000 + buf2;
            if (buf3 > 256)
                buf3 = 0x100 - 0x10000 + buf3;
            len = buf2 + buf3 * 0x100;
            buf = fanzhuan(buf);
            rec = new char[len];
            System.arraycopy(buf, 4, rec, 0, len);
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

    @SuppressLint("TrulyRandom")
    public static char[] jiami(char[] data) {
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

    public static char[] jiemi(char[] data) {
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

    public static String getIp(Context context) {
        String ip = "";
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
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

    public static char[] getcmd(int cmd) {
        char[] data = new char[remain.length + 7];
        data[0] = 0x7E;
        data[1] = (char) cmd;
        data[2] = 0x14;
        data[3] = 0x00;
        int i;
        for (i = 0; i < remain.length; i++)
            data[i + 4] = remain[i];
        char[] crc = new char[remain.length + 3];
        for (i = 0; i < remain.length + 3; i++) {
            crc[i] = data[i + 1];
        }
        int c = getCRC16(charToByte(crc));
        data[crc.length + 1] = (char) (c & 0xFF);
        data[crc.length + 2] = (char) (c >> 8);
        data[crc.length + 3] = 0x7E;
        return data;
    }

    public static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + (i >> 24 & 0xFF);
    }

    public static  char[] user_noip(String number, String password) {
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

    public static char[] user(String number, String password) {
        char[] msg = new char[82];
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

    public static boolean find(String text, String w) {
        // 从text里找w，有则返回真
        return text.contains(w);
    }

    public static String zhongjian(String text, String textl, String textr) {
        // ==================================================================
        // 函数名：zhongjian
        // 作者：ypw
        // 功能：取中间文本,这是对于不用考虑起始位置的情况的zhongjian函数重写
        // 输入参数：text,textl(左边的text),textr(右边的text)
        // 返回值：String
        // ==================================================================
        return zhongjian(text, textl, textr, 0);
    }

    public static String zhongjian(String text, String textl, String textr, int start) {
        // ==================================================================
        // 函数名：zhongjian
        // 作者：ypw
        // 功能：取中间文本,比如
        // zhongjian("abc123efg","abc","efg",0)返回123
        // 输入参数：text,textl(左边的text),textr(右边的text),start(起始寻找位置)
        // 返回值：String
        // ==================================================================
        int left = text.indexOf(textl, start);
        int right = text.indexOf(textr, left + textl.length());
        String zhongjianString = "";
        try{
            zhongjianString = text.substring(left + textl.length(), right);
        }catch (Exception ignore){}
        return zhongjianString;
    }

    static HttpClient commonClient = new DefaultHttpClient();

    public static void resetClient(){
        Common.commonClient = new DefaultHttpClient();
    }
    public static String commonPOST(String url, String postdata) throws IOException {
        String result = "";
        HttpPost hPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String posts[] = postdata.split("&");
        String posts2[];
        int i;
        for (i = 0; i < posts.length; i++) {
            posts2 = posts[i].split("=");
            if (posts2.length == 2)
                params.add(new BasicNameValuePair(posts2[0], posts2[1]));
            else
                params.add(new BasicNameValuePair(posts2[0], ""));
        }
        HttpEntity hen = new UrlEncodedFormEntity(params, "gb2312");
        hPost.setEntity(hen);
        commonClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        // 请求超时
        commonClient.getParams().setParameter(
                CoreConnectionPNames.SO_TIMEOUT, 30000);
        // 读取超时
        HttpResponse hResponse;
        hResponse = commonClient.execute(hPost);
        if (hResponse.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(hResponse.getEntity());
            // result = new String(result.getBytes("ISO_8859_1"),"gbk");
            // 转码
        }
        return result;
    }

    static HttpClient sslClient = SSLSocketFactoryEx.getNewHttpClient();

    public static String SSLPOST(String url, String postdata) throws IOException {
        String result = "";
        // System.out.println(url);
        HttpPost hPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<>();
        String posts[] = postdata.split("&");
        String posts2[];
        int i;
        for (i = 0; i < posts.length; i++) {
            posts2 = posts[i].split("=");
            if (posts2.length == 2)
                params.add(new BasicNameValuePair(posts2[0], posts2[1]));
            else
                params.add(new BasicNameValuePair(posts2[0], ""));
        }
        HttpEntity hen = new UrlEncodedFormEntity(params, "gb2312");
        hPost.setEntity(hen);
        sslClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        // 请求超时
        sslClient.getParams().setParameter(
                CoreConnectionPNames.SO_TIMEOUT, 30000);
        // 读取超时
        HttpResponse hResponse;
        hResponse = sslClient.execute(hPost);

        // System.out.println("网络延迟:"+shangwang.delaytime);

        if (hResponse.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(hResponse.getEntity());
            // result = new String(result.getBytes("ISO_8859_1"),"gbk");
            // 转码
        }
        return result;
    }

    public static String SSLGET(String url) {
        String result = "";
        System.out.println(url);
        HttpGet hGet = new HttpGet(url);
        try {
            sslClient.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
            // 请求超时
            sslClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                    30000);
            // 读取超时
            HttpResponse hResponse;
            hResponse = sslClient.execute(hGet);
            if (hResponse.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(hResponse.getEntity());
                // result = new String(result.getBytes("ISO_8859_1"), "gbk");
                // 转码
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }

    public static String commonGET(String url) {
        String result = "";
        System.out.println(url);
        HttpGet hGet = new HttpGet(url);
        try {
            commonClient.getParams().setParameter(
                    CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
            // 请求超时
            commonClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                    30000);
            // 读取超时
            HttpResponse hResponse;
            hResponse = commonClient.execute(hGet);
            if (hResponse.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(hResponse.getEntity());
                // result = new String(result.getBytes("ISO_8859_1"), "gbk");
                // 转码
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (result);
    }

    public static Bitmap GET(String url) {
        Bitmap bitmap = null;
        HttpGet httpRequest = new HttpGet(url);
        try {
            HttpResponse httpResponse = commonClient.execute(httpRequest);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 取得相关信息 取得HttpEntiy
                HttpEntity httpEntity = httpResponse.getEntity();
                // 获得一个输入流
                InputStream is = httpEntity.getContent();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (Exception ignored) {
        }

        return bitmap;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();
    }

    public static void print(String p) {
        System.out.println("OUT:"+p);
        // show(p);
        // SystemClock.sleep(200);
    }

    public static boolean dengluVPN(String num, String psw){
        boolean success = false;
        String result = "";
        try {
            result = SSLPOST("https://vpn.btbu.edu.cn/dana-na/auth/url_default/login.cgi", "username=" + num + "&password=" + psw);

        }catch (Exception e){

        }

        return  success;
    }
}
