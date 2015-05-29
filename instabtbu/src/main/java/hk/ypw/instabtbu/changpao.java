package hk.ypw.instabtbu;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UmengUpdateAgent;

@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility", "InflateParams",
		"WorldReadableFiles", "SimpleDateFormat" })
public class changpao extends Activity {

	leftmenu leftmenu;
	SlidingMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_changpao);

		try {
			UmengUpdateAgent.setUpdateOnlyWifi(false);
			UmengUpdateAgent.update(this);
			MobclickAgent.updateOnlineConfig(this);
		} catch (Exception e) {
		}

		try {
			leftmenu = new leftmenu(thisActivity, 6);
			menu = leftmenu.menu;
			TextView allTextView = (TextView) findViewById(R.id.changpao_text_all);
			allTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					setday(null);
				}
			});
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

			EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
			EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
			MobclickAgent.onEvent(thisActivity, "changpao");

			SharedPreferences sp = getSharedPreferences("data", 0);
			String num = sp.getString("num_changpao", "");
			String psw = sp.getString("psw_changpao", "");
			String all = sp.getString("changpao_day", "16");
			numEditText.setText(num);
			pswEditText.setText(psw);
			allTextView.setText(all);
			myui();

			if (num.length() != 0)
				chacishu(null);

			TextView left_userTextView = (TextView) findViewById(R.id.leftmenu_textview_user);
			TextView left_user2TextView = (TextView) findViewById(R.id.leftmenu_textview_dianzheli);
			if (numEditText.getText().toString().length() == 0)
				left_userTextView.setText("还没有登录");
			else
				left_userTextView.setText(numEditText.getText().toString());
			left_user2TextView.setText("好好跑步~");

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		TextView leftdayTextView = (TextView) findViewById(R.id.changpao_text_leftday);
		Date date = null;
		try {
			date = df.parse("2014-12-22");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date nowDate = new Date();
		int leftday = (int) ((date.getTime() - nowDate.getTime()) / 1000 / 60 / 60 / 24) + 1;
		leftdayTextView.setText(String.valueOf(leftday));
		System.out.println("剩余天数:" + leftday + "," + date.getTime() + ","
				+ nowDate.getTime());
	}

	static boolean wificonnected = false;

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

	Activity thisActivity = this;

	@Override
	public void onResume() {
		super.onResume();
		leftmenu.leftmenu_ui(5);
	}

	private ExecutorService executorService = Executors.newCachedThreadPool();
	// 线程池
	private ProgressDialog dialog2;

	public void chacishu(View v) {
		EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
		EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
		SharedPreferences.Editor editor = getSharedPreferences("data", 0)
				.edit();
		editor.putString("num_changpao", numEditText.getText().toString());
		editor.putString("psw_changpao", pswEditText.getText().toString());
		editor.commit();

		dialog2 = ProgressDialog.show(thisActivity, "正在登录", "正在登录中……", true,
				true);
		myClient = new DefaultHttpClient();
		executorService.submit(runRunnable);
	}

	Runnable runRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
				EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
				System.out.println("登录中");
				String resultString = POST(
						"http://59.64.80.229:8080/ezdcs/login.do", "username="
								+ numEditText.getText() + "&password="
								+ pswEditText.getText()
								+ "&btnlogin.x=31&btnlogin.y=12");

				if (find(resultString, "html:errors")) {
					gengxin("登录成功，获取长跑次数……");
					// 登录成功
					resultString = POST(
							"http://59.64.80.229:8080/ezdcs/stu/StudentResultsModify.do",
							"");
					System.out.println(resultString);
					Pattern p = Pattern
							.compile("<input.+?id='value0.+?value='(.+?)'/>");
					Matcher matcher = p.matcher(resultString);
					if (matcher.find()) {
						cishu = matcher.group(1);
						Message message = new Message();
						message.what = 3;
						handler.sendMessage(message);
					} else
						show("查询失败");
				} else
					show("登录失败,默认密码为888888");
			} catch (Exception e) {
				if (dialog2.isShowing())
					dialog2.dismiss();
			}
			if (dialog2.isShowing())
				dialog2.dismiss();
		}
	};
	String cishu = "";

	void gengxin(String gx) {
		gengxinString = gx;
		Message message = new Message();
		message.what = 1;
		handler.sendMessage(message);
	}

	Toast toast;
	String gengxinString = "";
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
				} else if (msg.what == 3) {
					TextView cishuTextView = (TextView) findViewById(R.id.changpao_text_cishu);
					cishuTextView.setText(cishu);
					TextView leftTextView = (TextView) findViewById(R.id.changpao_text_left);

					TextView allTextView = (TextView) findViewById(R.id.changpao_text_all);
					int all = Integer.valueOf(allTextView.getText().toString());
					int ci = Integer.valueOf(cishu);
					leftTextView.setText(String.valueOf(all - ci));

					ImageView changpao = (ImageView) findViewById(R.id.changpao_bar);
					ImageView bar2 = (ImageView) findViewById(R.id.changpao_bar2);
					ImageView bar3 = (ImageView) findViewById(R.id.changpao_bar3);
					int d = (changpao.getWidth() - bar3.getWidth()) / all;
					android.view.ViewGroup.LayoutParams params = bar2
							.getLayoutParams();
					params.width = d * ci;
					bar2.setLayoutParams(params);
				}
			} catch (Exception e) {
			}
		}
	};

	public void setday(View v) {
		new AlertDialog.Builder(this)
				.setTitle("请选择你想跑的次数")
				.setIcon(R.drawable.ser_logo)
				.setMessage("阳光长跑16次可以为你的体育成绩加3分~")
				.setNegativeButton("16次",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								TextView allTextView = (TextView) findViewById(R.id.changpao_text_all);
								allTextView.setText("16");
								SharedPreferences.Editor editor = getSharedPreferences(
										"data", 0).edit();
								editor.putString("changpao_day", "16");
								editor.commit();
								chacishu(null);
							}
						})
				.setPositiveButton("10次",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								TextView allTextView = (TextView) findViewById(R.id.changpao_text_all);
								allTextView.setText("10");
								SharedPreferences.Editor editor = getSharedPreferences(
										"data", 0).edit();
								editor.putString("changpao_day", "10");
								editor.commit();
								chacishu(null);
							}
						}).show();
	}

	String showString = "";

	public boolean find(String text, String w) {
		if (text.indexOf(w) == -1)
			return false;
		else
			return true;
	}

	public String zhongjian(String text, String textl, String textr) {
		return zhongjian(text, textl, textr, 0);
	}

	public String zhongjian(String text, String textl, String textr, int start) {
		int left = text.indexOf(textl, start);
		int right = text.indexOf(textr, left + textl.length());
		return text.substring(left + textl.length(), right);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public void leftmenu() {
		// configure the SlidingMenu
		menu = new SlidingMenu(thisActivity);
		menu.setMode(SlidingMenu.LEFT);
		// 设置触摸屏幕的模式
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		// menu.setShadowWidth(1);
		// menu.setShadowDrawable(R.drawable.xian);

		// 设置滑动菜单视图的宽度
		menu.setBehindWidth(dip2px(this, 200));
		// 设置渐入渐出效果的值
		menu.setFadeDegree(0.25f);
		/**
		 * SLIDING_WINDOW will include the Title/ActionBar in the content
		 * section of the SlidingMenu, while SLIDING_CONTENT does not.
		 */
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		// 为侧滑菜单设置布局
		menu.setMenu(R.layout.leftmenu);

		menu.setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				// TODO Auto-generated method stub
				System.out.println("打开菜单");

			}
		});
		menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("滑动菜单被点击");
				menu.toggle();
				System.out.println("关闭菜单");
			}
		});
	}

	@SuppressWarnings("unused")
	public void myui() {

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		int width = mDisplayMetrics.widthPixels;
		int height = mDisplayMetrics.heightPixels;
		float density = mDisplayMetrics.density;
		double w = width / 720.0;

		mypoint numleft = setView(R.id.changpao_num1,
				(int) (width * 0.5 - w * 578 / 2), (int) (width * 1.05),
				(int) (w * 86), (int) (w * 73));

		mypoint pswleft = setView(R.id.changpao_psw1, numleft.x,
				(int) (numleft.y + width * 0.12), numleft.width, numleft.height);

		mypoint numright = setView(R.id.changpao_num2,
				(int) (numleft.x + numleft.width), numleft.y, (int) (width
						- numleft.x * 2 - numleft.width), (int) (w * 73));

		mypoint pswright = setView(R.id.changpao_psw2, numright.x, pswleft.y,
				numright.width, numright.height);

	}

	public mypoint setView(int id, int x, int y, int wid, int hei) {
		View myView = findViewById(id);
		LayoutParams myParams = new LayoutParams(wid, hei);
		myParams.setMargins(x, y, 0, 0);
		myView.setLayoutParams(myParams);
		return new mypoint(x, y, wid, hei);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.btbu, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			if (item.getItemId() == R.id.about) {
				Intent intent = new Intent();
				intent.setClass(thisActivity, About.class);
				startActivity(intent);
				MobclickAgent.onEvent(this, "about");
			} else if (item.getItemId() == R.id.feedback) {
				FeedbackAgent agent = new FeedbackAgent(this);
				agent.startFeedbackActivity();
			} else if (item.getItemId() == R.id.addqun) {
				joinQQGroup("aU9Sag6d1GjA3Z3l1kOeHQ-plxiEk1wc");
			} else if (item.getItemId() == R.id.exit) {
				System.exit(0);
			} else if (item.getItemId() == R.id.fenxiang) {
				try {
					Drawable drawable = getResources().getDrawable(
							R.drawable.fenxiang);
					FileOutputStream Os = this.openFileOutput("share.jpg",
							Context.MODE_WORLD_READABLE);
					Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.JPEG, 100, bos);
					byte[] bitmapdata = bos.toByteArray();
					Os.write(bitmapdata);
					Os.close();

					File F = this.getFileStreamPath("share.jpg");
					Uri U = Uri.fromFile(F);
					String fenxiang = getString(R.string.share);
					Intent sendIntent = new Intent();
					ComponentName cn = new ComponentName("com.tencent.mm",
							"com.tencent.mm.ui.tools.ShareToTimeLineUI");
					sendIntent.setComponent(cn);
					sendIntent.setAction(Intent.ACTION_SEND);
					sendIntent.putExtra(Intent.EXTRA_SUBJECT, "instabtbu");
					sendIntent.putExtra(Intent.EXTRA_STREAM, U);
					sendIntent.putExtra(Intent.EXTRA_TEXT, fenxiang);
					sendIntent.putExtra("Kdescription", fenxiang);
					sendIntent.setType("image/*");
					startActivity(sendIntent);
				} catch (Exception e) {
				}
			}

		} catch (Exception ex) {
			MobclickAgent.reportError(thisActivity, ex);
			show(ex.toString());
		}
		return false;
	}

	/****************
	 * 
	 * 发起添加群流程。群号：instabtbu(99254687) 的 key 为： aU9Sag6d1GjA3Z3l1kOeHQ-plxiEk1wc
	 * 调用 joinQQGroup(aU9Sag6d1GjA3Z3l1kOeHQ-plxiEk1wc) 即可发起手Q客户端申请加群
	 * instabtbu(99254687)
	 * 
	 * @param key
	 *            由官网生成的key
	 * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
	 ******************/
	public boolean joinQQGroup(String key) {
		Intent intent = new Intent();
		intent.setData(Uri
				.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
						+ key));
		// 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
		// //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		try {
			startActivity(intent);
			return true;
		} catch (Exception e) {
			show("未安装QQ或安装的版本不支持");
			// 未安装手Q或安装的版本不支持
			return false;
		}
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

	static HttpClient myClient = new DefaultHttpClient();

	public String POST(String url, String postdata) {
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
		try {
			HttpEntity hen = new UrlEncodedFormEntity(params, "gb2312");
			hPost.setEntity(hen);
			myClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 请求超时
			myClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					30000);
			// 读取超时
			HttpResponse hResponse;
			hResponse = myClient.execute(hPost);
			if (hResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(hResponse.getEntity());
				// result = new String(result.getBytes("ISO_8859_1"),"gbk");
				// 转码
			}

		} catch (Exception e) {
			if (dialog2.isShowing())
				dialog2.dismiss();
			show("连接BTBU失败。\n请确认信号良好再操作。");
		}
		return (result);
	}

}