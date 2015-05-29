package hk.ypw.instabtbu;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
import java.util.ArrayList;
import java.util.List;

public class Common {
    public String zhongjian(String text, String textl, String textr) {
        // ==================================================================
        // 函数名：zhongjian
        // 作者：ypw
        // 功能：取中间文本,这是对于不用考虑起始位置的情况的zhongjian函数重写
        // 输入参数：text,textl(左边的text),textr(右边的text)
        // 返回值：String
        // ==================================================================
        return zhongjian(text, textl, textr, 0);
    }

    public String zhongjian(String text, String textl, String textr, int start) {
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
        return text.substring(left + textl.length(), right);
    }

    static HttpClient myClient = new DefaultHttpClient();

    public String commonPOST(String url, String postdata) throws IOException {
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
        myClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        // 请求超时
        myClient.getParams().setParameter(
                CoreConnectionPNames.SO_TIMEOUT, 30000);
        // 读取超时
        HttpResponse hResponse;
        hResponse = myClient.execute(hPost);
        if (hResponse.getStatusLine().getStatusCode() == 200) {
            result = EntityUtils.toString(hResponse.getEntity());
            // result = new String(result.getBytes("ISO_8859_1"),"gbk");
            // 转码
        }
        return result;
    }

    public Bitmap GET(String url) {
        Bitmap bitmap = null;
        HttpGet httpRequest = new HttpGet(url);
        try {
            HttpResponse httpResponse = myClient.execute(httpRequest);
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
}
