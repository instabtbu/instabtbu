package hk.ypw.instabtbu;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

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
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
public class shangwang extends Activity {
	/**
	 * @author ypw
	 * @time 2014-09-17
	 */

	long uiId = 0;
	leftmenu leftmenu;
	SlidingMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			if (liuliang.length() != 0)
				liuliangTextView.setText(liuliang + " MB");
			if (shebei.length() != 0)
				shebeiTextView.setText(shebei);
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

			if (find(zidongcha, "z") && isWifiConnected(this))
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
				if (checkser() == false) {
					if (isWifiConnected(this))
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
			editor.commit();
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

			if (banben.indexOf(pi.versionName.toString()) == -1) {
				System.out.println("新版本");
				Intent intent1 = new Intent();
				intent1.setClass(thisActivity, First_jieshao.class);
				startActivity(intent1);
				SharedPreferences.Editor editor = getSharedPreferences("data",
						0).edit();
				editor.putString("banben", banben + "\n" + pi.versionName);
				editor.commit();
			}
		} catch (Exception e) {
			try {
				Intent intent1 = new Intent();
				intent1.setClass(thisActivity, First_jieshao.class);
				startActivity(intent1);
				show("欢迎使用instabtbu，您可以在登录成功后启用自动登录功能。");
				PackageInfo pi = this.getPackageManager().getPackageInfo(
						this.getPackageName(), 0);
				SharedPreferences.Editor editor = getSharedPreferences("data",
						0).edit();
				editor.putString("banben", pi.versionName);
				editor.commit();
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}
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
				editor.commit();
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
				editor.commit();
			}
		});

		try {
			leftmenu = new leftmenu(thisActivity, 1);
			menu = leftmenu.menu;

			TextView left_userTextView = (TextView) thisActivity
					.findViewById(R.id.leftmenu_textview_user);
			TextView left_user2TextView = (TextView) thisActivity
					.findViewById(R.id.leftmenu_textview_dianzheli);
			left_userTextView.setText("还没有登录");
			left_user2TextView.setText("请登录");

		} catch (Exception e) {
		}

		final ImageView dengluImageView = (ImageView) findViewById(R.id.shangwang_deglu);
		dengluImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dengluImageView.getBackground().setAlpha(128);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					dengluImageView.getBackground().setAlpha(255);
					if (checkser() == false)
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
				// TODO Auto-generated method stub
				while (true) {
					try {
						checkser();
					} catch (Exception e) {

					}
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

	Activity thisActivity = this;

	@Override
	public void onResume() {
		super.onResume();
		leftmenu.leftmenu_ui(0);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

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

	public void liulianggoumai(View v) {
		MobclickAgent.onEvent(thisActivity, "liulianggoumai");
		Intent intent = new Intent();
		intent.setClass(thisActivity, shangwang_liulianggoumai.class);
		startActivity(intent);
	}

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

	public boolean checkser() {
		boolean boolser = false;
		try {
			boolser = ser.always;
			Boolean isRunning = false;
			ActivityManager activityManager = (ActivityManager) this
					.getApplicationContext().getSystemService(
							Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> serviceList = activityManager
					.getRunningServices(100);
			for (int i = 0; i < serviceList.size(); i++) {
				if (find(serviceList.get(i).service.getClassName(),
						"hk.ypw.instabtbu.ser")) {
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
			} catch (Exception e) {
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		return boolser;
	}

	String liuliangString = "";
	String shebeiString = "";
	String showString = "";
	Toast toast;
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
				} else if (msg.what == 5) {
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

			} catch (Exception e) {

			}
		}
	};

	int liuliangColor = 0;

	Runnable bianseRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (int i = 0; i < 255; i += 5) {
				liuliangColor = Color.argb(255, 255, 255, i);
				Message message = new Message();
				message.what = 6;
				handler.sendMessage(message);
				SystemClock.sleep(20);
			}

		}
	};

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
		if (shangwang.isWifiConnected(this)) {
			// System.out.println("cmd="+zhongjian(postdata,
			// "netlogincmd=","&"));
			try {
				// System.out.println(url);
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
				delaytime = System.currentTimeMillis() - last;
				// System.out.println("网络延迟:"+shangwang.delaytime);

				if (hResponse.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(hResponse.getEntity());
					// result = new String(result.getBytes("ISO_8859_1"),"gbk");
					// 转码
				}
			} catch (Exception e) {
				e.printStackTrace();
				show("连接BTBU失败。\n请确认信号良好再操作。");
			}
		} else {
			result = "wifi未连接!!";
			delaytime = 30000;
		}
		return (result);
	}

	static long delaytime = 0;

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
		return text.indexOf(w) != -1;
	}

	private ExecutorService executorService = Executors.newFixedThreadPool(4);

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
					char[] buf = { 0x7E, 0x11, 0x00, 0x00, 0x54, 0x01, 0x7E };
					gengxin("服务器连接成功,正在发送请求...");
					send(buf);
					gengxin("发送请求成功,正在等待验证...");
					char[] rec = read();
					rec = fanzhuan(rec);
					if (rec.length == 23) {
						verify = new char[16];
						gengxin("服务器验证成功,正在发送登录请求...");
						for (int i = 0; i < 16; i++) {
							verify[i] = rec[i + 4];
						}
						char[] msg = user(editText_num.getText().toString(),
								editText_psw.getText().toString());
						msg = feng(msg, 0x01);
						msg = zhuan(msg);
						send(msg);
						gengxin("发送登录请求成功,正在等待验证...");
						rec = read();
						gengxin("接收返回成功,正在处理...");
						rec = jiefeng(rec);
						try {
							client.close();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (rec != null) {
							SharedPreferences.Editor editor = getSharedPreferences(
									"data", 0).edit();
							editor.putString("num", editText_num.getText()
									.toString());
							editor.putString("psw", editText_psw.getText()
									.toString());
							editor.commit();
							// print("登录成功:" + charsToHexString(rec));
							show("登录成功");
							ser.always = true;
							startService();
						} else {
							print(recString);
							show(recString);
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

	public void print(String p) {
		// System.out.println("OUT:"+p);
		// show(p);
		// SystemClock.sleep(200);
	}

	Socket client = null;
	OutputStream outputStream;
	InputStream inputStream;

	static char[] verify = null;

	static char[] remain = null;

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

	String recString = "";

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
				// recString =
				// recString.subSequence(0,recString.length()-1).toString();
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

	public int getCRC16(byte[] bytes) {
		int[] table = { 0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014,
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
				0x0202 };
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

	// public static String bytesToHexString(byte[] src){
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

	protected void startService() {
		Intent intent = new Intent(this, ser.class);
		startService(intent);
		checkser();
	}

	protected void stopService() {
		Intent intent = new Intent(this, ser.class);
		stopService(intent);
		checkser();
	}

	// private ProgressDialog dialog2;

	public void denglu(View v) {
		// ==================================================================
		// 函数名：denglu
		// 作者：ypw
		// 功能：将登录任务投入线程池中,再对UI进行更新,
		// 输入参数：View v(如果在layout里用onClick,就必须要写View v)
		// 返回值：void
		// ==================================================================
		if (isWifiConnected(this)) {
			// dialog2 = ProgressDialog.show(this, "正在登录", "正在登录中……",true,true);
			executorService.submit(rDenglu);
			EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
			EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);
			editText_num.setEnabled(false);
			editText_psw.setEnabled(false);
			// denglubutton(0);
		} else {
			show("请先连接wifi喔");
		}

	}

	void gengxin(String gx) {
		// gengxinString+=gx+"\n";
		// Message message = new Message();
		// message.what=5;
		// handler.sendMessage(message);
		// show(gx);
	}

	String gengxinString = "";

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

	Runnable rChaliuliang = new Runnable() {
		@Override
		public void run() {
			EditText editText_num = (EditText) findViewById(R.id.shangwang_edit_num);
			EditText editText_psw = (EditText) findViewById(R.id.shangwang_edit_psw);

			print("开始连接");
			connect();
			char[] buf = { 0x7E, 0x11, 0x00, 0x00, 0x54, 0x01, 0x7E };
			send(buf);
			try {
				char[] rec = read();

				rec = fanzhuan(rec);
				if (rec.length == 23) {
					verify = new char[16];
					for (int i = 0; i < 16; i++) {
						verify[i] = rec[i + 4];
					}
					char[] msg = user_noip(editText_num.getText().toString(),
							editText_psw.getText().toString());
					msg = feng(msg, 0x03);
					msg = zhuan(msg);
					send(msg);
					rec = read();
					rec = jiefeng(rec);
					try {
						client.close();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String result = recString;
					String yueString = "";
					if (!find(result, "流量"))
						show(result, 1);
					else {
						try {
							Pattern p = Pattern.compile("([0-9]+)(兆)");
							Matcher m = p.matcher(result);
							int liuliang = 0;
							while (m.find()) {
								String tempString = m.group(1);
								liuliang += Integer.valueOf(tempString)
										.intValue();
							}
							liuliangString = String.valueOf(liuliang);
							p = Pattern.compile("剩余网费.*?(\\w+.\\w*元)");
							m = p.matcher(result);

							if (m.find()) {
								yueString = m.group(1);
							}
						} catch (Exception e) {
							liuliangString += "err";
							e.printStackTrace();
						}

						try {
							Pattern p = Pattern.compile("在线:(\\d+)");
							Matcher m = p.matcher(result);
							if (m.find()) {
								shebeiString = m.group(1);
							}
						} catch (Exception e) {

						}

						SharedPreferences.Editor editor = getSharedPreferences(
								"data", 0).edit();
						editor.putString("liuliang", liuliangString);
						editor.putString("shebei", shebeiString);
						editor.putString("yue", yueString);
						editor.commit();

						Message message = new Message();
						message.what = 4;
						handler.sendMessage(message);
						executorService.submit(bianseRunnable);
						// show("你还有" + liuliangString + "MB流量。");
					}
				}
			} catch (Exception e) {
				show("连接失败\n服务器已断开连接");
			}
		}
	};

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

	Runnable duankaiRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			DatagramSocket datagramSocket = null;
			try {
				datagramSocket = new DatagramSocket();
				print("建立断开socket");
			} catch (Exception e) {
				e.printStackTrace();
			}
			InetAddress ip = null;
			try {
				ip = InetAddress.getByName("192.168.8.8");
				char[] data = getcmd(1);
				// print("断开发送数据:" + charsToHexString(data));
				DatagramPacket datagramPacket = new DatagramPacket(
						charToByte(data), remain.length + 7, ip, 21099);
				datagramSocket.send(datagramPacket);
				datagramSocket.send(datagramPacket);
				datagramSocket.send(datagramPacket);
				remain = null;
				show("断开成功");
			} catch (NullPointerException e) {
				show("你还没有登录");
			} catch (Exception e) {
				e.printStackTrace();
				print("断开出错:" + e.getMessage());
			}
		}
	};
}