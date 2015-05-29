package hk.ypw.instabtbu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

/**
 * @author ypw
 */

public class jiaowu_kebiao extends SwipeBackActivity {
    long uiId = 0;
    String kebiaopath = "";
    String filepath = "";
    // 线程池
    String xueqi = "";
    String result = "";
    Pattern p;
    Matcher m;
    List<String> xueqiList = new ArrayList<>();
    List<String> shijian = new ArrayList<>();
    List<String> mingcheng = new ArrayList<>();
    List<String> xiangxi = new ArrayList<>();
    List<String> didian = new ArrayList<>();
    String showString = "";
    Toast toast;
    String gengxinString = "";
    String titlejuti = "";
    String jutiString = "";
    Activity thisActivity = this;
    private ProgressDialog dialog;

    Runnable kebiaoRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                SharedPreferences sp = getSharedPreferences("data", 0);
                xueqi = sp.getString("xueqi", "");
            } catch (Exception ignored) {
            }
            if (xueqi != null) {
                if (xueqi.length() == 0) {
                    xuanxueqi();
                } else
                    getkebiao();
            }
        }
    };
    Runnable xuanRunnable = new Runnable() {
        @Override
        public void run() {
            xuanxueqi();
        }
    };

    Runnable getkebiaoRunnable = new Runnable() {
        @Override
        public void run() {
            getkebiao();
        }
    };

    private ExecutorService executorService = Executors.newCachedThreadPool();
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                super.handleMessage(msg);
                TextView xueqiTextView = (TextView) findViewById(R.id.jiaowu_dangqianxueqi);
                if (msg.what == 1) {
                    xueqiTextView.setText(xueqi);
                    System.out.println("加载listview");
                    ListView listview = (ListView) findViewById(R.id.jiaowu_listView_kebiao);
                    CustomAdapter customAdapter = new CustomAdapter(
                            thisActivity, shijian);
                    listview.setAdapter(customAdapter);
                    System.out.println("加载listview完毕");
                    if (dialog.isShowing())
                        dialog.dismiss();
                } else if (msg.what == 2) {
                    List<String> xueqiList2 = new ArrayList<>();
                    SharedPreferences sp = getSharedPreferences("data", 0);
                    String num = sp.getString("num_jiaowu", null);
                    assert num != null;
                    num = num.substring(0, 2);
                    int i;
                    for (i = 0; i < xueqiList.size(); i++) {
                        String temp = xueqiList.get(i);
                        xueqiList2.add(temp);
                        if (temp.contains(num)
                                & (Integer.valueOf(temp.substring(
                                temp.length() - 1, temp.length())) == 1))
                            break;
                    }
                    String[] xueqiStrings = new String[xueqiList2.size()];
                    xueqiList2.toArray(xueqiStrings);
                    new AlertDialog.Builder(thisActivity)
                            .setTitle("请选择")
                            .setIcon(R.drawable.ser_logo)
                            .setSingleChoiceItems(xueqiStrings, 0,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface a,
                                                            int which) {
                                            xueqi = xueqiList.get(which);
                                            System.out.println("选择成功：" + xueqi);
                                            xueqiList.clear();
                                            shijian.clear();
                                            mingcheng.clear();
                                            xiangxi.clear();
                                            didian.clear();
                                            SharedPreferences.Editor editor = getSharedPreferences(
                                                    "data", 0).edit();
                                            editor.putString("xueqi", xueqi);
                                            editor.apply();
                                            a.dismiss();
                                            dialog = ProgressDialog.show(
                                                    thisActivity, "登录成功",
                                                    "正在获取课表……", true, true);
                                            executorService
                                                    .submit(getkebiaoRunnable);
                                        }
                                    })
                                    // .setNegativeButton("取消", null)
                            .show();
                } else if (msg.what == 3) {
                    if (dialog.isShowing()) {
                        dialog.setMessage(gengxinString);
                    }
                } else if (msg.what == 4) {
                    if (toast == null)
                        toast = Toast.makeText(getApplicationContext(),
                                showString, Toast.LENGTH_SHORT);
                    else
                        toast.setText(showString);
                    toast.show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // if(android.os.Build.VERSION.SDK_INT<16){
        // setTheme(R.style.android2);
        // System.out.println("版本过低");
        // }else System.out.println(android.os.Build.VERSION.SDK_INT);
        setContentView(R.layout.activity_jiaowu_kebiao);

        uiId = Thread.currentThread().getId();// 获取主线程ID
        System.out.println(jiaowuchaxun.wificonnected);
        filepath = getFilesDir().toString() + "/";

        if (jiaowuchaxun.wificonnected) {
            dialog = ProgressDialog.show(this, "登录成功", "正在获取课表……", true, true);
            MobclickAgent.onEvent(this, "kebiao");
            executorService.submit(kebiaoRunnable);
        } else {
            try {
                SharedPreferences sp = getSharedPreferences("data", 0);
                kebiaopath = sp.getString("lixiankebiao", "");
                assert kebiaopath != null;

                BufferedReader bufReader = new BufferedReader(new FileReader(kebiaopath));
                String line;
                result = "";
                while( ( line = bufReader.readLine() ) != null)
                {
                    result += line;
                }
                bufReader.close();

//                FileInputStream fin = new FileInputStream(kebiaopath);
//                int length = fin.available();
//                byte[] buffer = new byte[length];
//                fin.read(buffer);
//                result = EncodingUtils.getString(buffer, "UTF-8");
//                fin.close();

                System.out.println(result);
                xueqi = "离线课表";
            } catch (Exception e) {
                e.printStackTrace();
            }
            getkebiao(true);
        }
    }

    public void savekebiao(String xueqi, String result) {
        try {

            SharedPreferences.Editor editor = getSharedPreferences("data", 0)
                    .edit();
            editor.putString("lixiankebiao", filepath + xueqi + ".html");
            editor.apply();

            System.out.println(getFilesDir().toString());
            result = result.replaceAll("style=\"display: none;\"", "");
            result = result
                    .replaceAll("-1\" ", "-1\" style=\"display: none;\"");
            result = result.replaceAll(Common.zhongjian(result, "<link", ">"), "");
            while (!result.contains("<script"))
                result = result.replaceAll(Common.zhongjian(result, "<script", ">"),
                        "");

            File file = new File(filepath + xueqi + ".html");
            FileWriter writer = new FileWriter(file);
            writer.write(result);
            writer.close();
            kebiaopath = filepath + xueqi + ".html";
        } catch (Exception e) {
            System.out.println(filepath + xueqi + ".html");
            e.printStackTrace();
            show("保存文件失败，请检查权限。");

        }
    }

    public void getkebiao() {
        getkebiao(false);
    }

    public void getkebiao(boolean lixian) {
        if (!lixian) {
            System.out.println("开始获取课表");
            result = POST(
                    "http://jwgl.btbu.edu.cn/tkglAction.do?method=goListKbByXs&sql=&xnxqh="
                            + xueqi, "");
            savekebiao(xueqi, result);
        }

        try {
            p = Pattern.compile(getString(R.string.regexKebiao));
            m = p.matcher(result);
            String temp;
            while (m.find()) {
                shijian.add(m.group(1));
                temp = m.group(2);
                Pattern p2 = Pattern
                        .compile(getString(R.string.regexKebiaoDetail));
                Matcher m2 = p2.matcher(temp);
                if (m2.find()) {
                    mingcheng.add(m2.group(1));
                    // int i;String myString="";
                    // for(i=1;i<6;i++)if(m2.group(i).length()!=0)myString+=m2.group(i)+"\n";

                    // xiangxi.add(myString.substring(0,myString.length()-1));
                    temp = temp.replace("&nbsp;", "");
                    System.out.println(temp);
                    Pattern p3 = Pattern.compile(getString(R.string.regexKebiao3));
                    Matcher m3 = p3.matcher(temp);
                    temp = m3.replaceAll("\n");
                    while (temp.contains("\n\n")) {
                        temp = temp.replace("\n\n", "\n");
                    }
                    temp = temp.substring(0, temp.length() - 1);
                    xiangxi.add(temp);

                    String[] tempStrings = temp.split("\n");

                    if (tempStrings.length == 5) {
                        String myString = "";
                        if (tempStrings[3].contains("单周"))
                            myString += "单周\n";
                        else if (tempStrings[3].contains("双周"))
                            myString += "双周\n";
                        myString += tempStrings[4];
                        didian.add(myString);
                    } else if (tempStrings.length == 6) {
                        if (!tempStrings[4].contains("暂无"))
                            didian.add(tempStrings[4] + "\n" + tempStrings[5]);
                        else
                            didian.add(tempStrings[5]);
                    } else if (tempStrings.length == 10) {
                        String myString = "";
                        if (tempStrings[3].contains("单周"))
                            myString += "单周\n";
                        else if (tempStrings[3].contains("双周"))
                            myString += "双周\n";
                        else
                            myString += tempStrings[3] + "\n";
                        myString += tempStrings[4] + "\n";

                        if (tempStrings[8].contains("单周"))
                            myString += "单周\n";
                        else if (tempStrings[8].contains("双周"))
                            myString += "双周\n";
                        else
                            myString += tempStrings[8] + "\n";
                        myString += tempStrings[9];

                        didian.add(myString);
                    } else if (tempStrings.length == 12) {
                        String myString = "";
                        if (tempStrings[3].contains("单周"))
                            myString += "单周\n";
                        else if (tempStrings[3].contains("双周"))
                            myString += "双周\n";
                        else
                            myString += tempStrings[3];
                        myString += tempStrings[4] + tempStrings[5] + "\n";

                        if (tempStrings[9].contains("单周"))
                            myString += "单周\n";
                        else if (tempStrings[9].contains("双周"))
                            myString += "双周\n";
                        else
                            myString += tempStrings[9];
                        myString += tempStrings[10] + tempStrings[11];
                        didian.add(myString);
                    } else {
                        didian.add(tempStrings[4]);
                    }
                } else {
                    mingcheng.add("");
                    xiangxi.add("");
                    didian.add("");
                }
            }
        } catch (Exception ignored) {
        }
        System.out.println("finished kebiao");
        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);

    }

    public void xueqi(View v) {
        dialog = ProgressDialog.show(this, "选学期", "正在获取学期列表……", true, true);
        executorService.submit(xuanRunnable);
    }

    public void xuanxueqi() {
        System.out.println("开始选学期");
        result = POST("http://jwgl.btbu.edu.cn/tkglAction.do?method=kbxxXs", "");
        p = Pattern.compile(getString(R.string.regexKebiao_Xueqi));
        m = p.matcher(result);
        m.find();
        while (m.find()) {
            xueqiList.add(m.group(1));
        }

        // int i;
        // for(i=0;i<xueqiList.size();i++)
        // {
        // result =
        // POST("http://jwgl.btbu.edu.cn/tkglAction.do?method=goListKbByXs&sql=&xnxqh="+xueqiList.get(i),
        // "");
        // savekebiao(xueqiList.get(i), result);
        // }

        if (dialog.isShowing())
            dialog.dismiss();

        Message message = new Message();
        message.what = 2;
        handler.sendMessage(message);
    }

    public void kebiao(View v) {
        show(v.toString());
    }

    public void jutikebiao() {
        new AlertDialog.Builder(jiaowu_kebiao.this).setTitle(titlejuti)
                .setMessage(jutiString).setPositiveButton("确定", null).show();
    }

    public int quhangshu(String myString) {
        int hang = 0, wz = 0;
        while ((wz = myString.indexOf("\n", wz) + 1) > 0)
            hang++;
        return hang + 1;
    }

    public String POST(String url, String postdata) {
        String result = "";
        try {
            result = Common.commonPOST(url, postdata);
        } catch (Exception e) {
            if (dialog.isShowing())
                dialog.dismiss();
            show("连接教务管理系统失败。\n请确认信号良好再操作。");
            finish();

        }
        return (result);
    }

    public void yuanshikebiao(View v) {
        // Uri uri = Uri.parse("file://"+kebiaopath);
        // Intent it = new Intent(Intent.ACTION_VIEW,uri );
        // // it.setType("text/html");
        // it.setClassName("com.android.browser",
        // "com.android.browser.BrowserActivity");
        // startActivity(it);
        Intent intent = new Intent();
        intent.setClass(thisActivity, jiaowu_yuanshikebiao.class);
        startActivity(intent);
    }

    public void show(String str) {
        show(str, 0);
    }

    public void show(String str, int d) {
        if (d > 0)
            str = str.substring(0, str.length() - 1);
        System.out.println("show:" + str);
        if (Thread.currentThread().getId() != uiId) {
            showString = str;
            Message message = new Message();
            message.what = 4;
            handler.sendMessage(message);
        } else
            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT)
                    .show();
    }

    private class CustomAdapter extends BaseAdapter {
        private List<String> mData;
        private LayoutInflater mInflater;

        public CustomAdapter(Context context, List<String> data) {
            mData = data;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if (mData == null || mData.size() <= 0) {
                return 0;
            }
            return mData.size() / 7;
        }

        @Override
        public Object getItem(int position) {
            if (mData == null || mData.size() <= 0 || position < 0
                    || position >= mData.size()) {
                return null;
            }
            return mData.get(position / 7 + 1);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_kebiao, null);
            }

            int myid[] = {R.id.jiaowu_zhouyi, R.id.jiaowu_zhouer,
                    R.id.jiaowu_zhousan, R.id.jiaowu_zhousi, R.id.jiaowu_zhouwu};
            int i;
            try {
                for (i = 0; i < 5; i++) {
                    TextView tempTextView = (TextView) convertView
                            .findViewById(myid[i]);
                    if (position % 2 == 0)
                        tempTextView.setBackgroundColor(0xFFF2F2F2);
                    tempTextView.setText(mingcheng.get(position * 7 + i) + "\n"
                            + didian.get(position * 7 + i));
                    int j;
                    int hang = quhangshu(mingcheng.get(position * 7 + i) + "\n"
                            + didian.get(position * 7 + i));
                    if (mingcheng.get(position * 7 + i).length() > 5)
                        hang++;
                    if (didian.get(position * 7 + i).length() > 12)
                        hang++;
                    for (j = 0; j < 5 - hang; j++)
                        tempTextView.append("\n");
                    if (xiangxi.get(position * 7 + i).contains("单周")
                            || xiangxi.get(position * 7 + i).contains("双周")) {
                        tempTextView.setBackgroundColor(0X223474AC);
                    }
                    tempTextView.setTag(position * 7 + i);
                    tempTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                jutiString = xiangxi.get(Integer.valueOf(view
                                        .getTag().toString()));
                                titlejuti = mingcheng.get(Integer.valueOf(view
                                        .getTag().toString()));
                                if (jutiString.length() != 0)
                                    jutikebiao();
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }
            } catch (Exception ignored) {
            }
            return convertView;
        }
    }
}