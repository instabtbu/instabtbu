package hk.ypw.instabtbu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
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
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class shangwang_liulianggoumai extends SwipeBackActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_liulianggoumai);
		myui();

		SharedPreferences sp = getSharedPreferences("data", 0);
		String yueString = sp.getString("yue", "");

		TextView yueTextView = (TextView) findViewById(R.id.liuliang_yue);
		yueTextView.setText(yueString);
	}

	@SuppressWarnings("unused")
	public void myui() {
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		int width = mDisplayMetrics.widthPixels;
		int height = mDisplayMetrics.heightPixels;
		float density = mDisplayMetrics.density;
		double w = width / 700.0;

		mypoint head = setView(R.id.liulianggoumai_head, 0, 0, width,
				(int) (width * 408.0 / 720.0));

		mypoint yue = setView(R.id.liuliang_yue, (int) (width * 500 / 720),
				(int) (width * 308 / 720), -2, -2);

		mypoint liuliang_8 = setView(R.id.liulianggoumai_button_8,
				(int) (width * 0.08), (int) (head.height + width * 0.04), width
						- (int) (width * 0.08) * 2,
				(width - (int) (width * 0.08) * 2) * 269 / 589);

		mypoint liuliang_20 = setView(R.id.liulianggoumai_button_20,
				(int) (width * 0.08),
				(int) (liuliang_8.y + liuliang_8.height + width * 0.04), width
						- (int) (width * 0.08) * 2,
				(width - (int) (width * 0.08) * 2) * 269 / 589);
	}

	public mypoint setView(int id, int x, int y, int wid, int hei) {
		View myView = findViewById(id);
		LayoutParams myParams = new LayoutParams(wid, hei);
		myParams.setMargins(x, y, 0, 0);
		myView.setLayoutParams(myParams);
		return new mypoint(x, y, wid, hei);
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

	String psw = "";
	private ProgressDialog dialog2;
	private ExecutorService executorService = Executors.newCachedThreadPool();

	Runnable rbao8g = new Runnable() {
		@Override
		public void run() {
			SharedPreferences sp = getSharedPreferences("data", 0);
			String num = sp.getString("num", null);

			String result = "";
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
			String num = sp.getString("num", null);
			String result = "";
			result = POST("https://self.btbu.edu.cn/cgi-bin/nacgi.cgi",
					"textfield=" + num + "&textfield2=" + psw
							+ "&jsidx=2&radio=2&Submit=%CC%E1%BD%BB&nacgicmd=2");
			System.out.println(result);
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

			// if(result.indexOf("成功申请包月(当月流量用完停止上网)方式")!=-1)show("成功申请包月(当月流量用完停止上网)方式");
			// else
			/**
			 * <td width="90%" height="388" class="STYLE11" align="center">
			 * 您没有输入上网密码<br>
			 */

		}
	};

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

	Activity thisActivity = this;

	public String POST(String url, String postdata) {
		String result = "";
		if (shangwang.isWifiConnected(this)) {
			try {
				System.out.println(url);
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
				HttpClient httpclient = SSLSocketFactoryEx.getNewHttpClient();
				httpclient.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
				// 请求超时
				httpclient.getParams().setParameter(
						CoreConnectionPNames.SO_TIMEOUT, 30000);
				// 读取超时
				HttpResponse hResponse;
				hResponse = httpclient.execute(hPost);
				System.out.println(shangwang.delaytime);

				if (hResponse.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(hResponse.getEntity());
					result = new String(result.getBytes("ISO_8859_1"), "gbk");
					// 转码
				}
			} catch (Exception e) {
				e.printStackTrace();
				show("连接BTBU失败。\n请确认信号良好再操作。");
			}
		} else {
			result = "wifi未连接!";
		}
		return (result);
	}

	String showString = "";

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
			} catch (Exception e) {
			}
		}
	};

	public class mypoint {
		int x;
		int y;
		int width;
		int height;

		public mypoint(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}
}
