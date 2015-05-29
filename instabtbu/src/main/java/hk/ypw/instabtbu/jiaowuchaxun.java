package hk.ypw.instabtbu;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;

/**
 * 
 * @author ypw
 * 
 */
@SuppressLint({ "ClickableViewAccessibility", "HandlerLeak",
		"WorldReadableFiles" })
public class jiaowuchaxun extends Activity {
	String filepath = "";
	Random random = new Random();

	leftmenu leftmenu;
	SlidingMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jiaowu);
		try {
			leftmenu = new leftmenu(thisActivity, 2);
			menu = leftmenu.menu;

			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

			EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
			EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
			// MobclickAgent.onEvent(thisActivity,"jiaowu");
			filepath = getFilesDir().toString() + "/";

			SharedPreferences sp = getSharedPreferences("data", 0);
			String num = sp.getString("num_jiaowu", null);
			String psw = sp.getString("psw_jiaowu", null);
			numEditText.setText(num);
			pswEditText.setText(psw);

			TextView left_userTextView = (TextView) findViewById(R.id.leftmenu_textview_user);
			TextView left_user2TextView = (TextView) findViewById(R.id.leftmenu_textview_dianzheli);
			if (numEditText.getText().toString().length() == 0)
				left_userTextView.setText("还没有登录");
			else
				left_userTextView.setText(numEditText.getText().toString());
			left_user2TextView.setText("好好学习~");

		} catch (Exception ex) {
		}

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		Double width = (double) mDisplayMetrics.widthPixels;
		Double height = (double) mDisplayMetrics.heightPixels;

		System.out.println("宽高比:" + height / width);

		// if(height/width!=16.0/9.0){
		// ImageView xiaoliImageView =
		// (ImageView)findViewById(R.id.jiaowu_xiaoli);
		// xiaoliImageView.setVisibility(View.INVISIBLE);
		// ImageView qingImageView =
		// (ImageView)findViewById(R.id.jiaowu_qingkongziliao);
		// qingImageView.setVisibility(View.INVISIBLE);
		// }
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
		leftmenu.leftmenu_ui(1);
	}

	private ExecutorService executorService = Executors.newCachedThreadPool();
	// 线程池
	private ProgressDialog dialog2;

	public void xiaoli(View v) {
		Intent intent = new Intent();
		intent.setClass(thisActivity, jiaowu_xiaoli.class);
		startActivity(intent);
	}

	public void chengji(View v) {
		EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
		EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
		SharedPreferences.Editor editor = getSharedPreferences("data", 0)
				.edit();
		editor.putString("num_jiaowu", numEditText.getText().toString());
		editor.putString("psw_jiaowu", pswEditText.getText().toString());
		editor.commit();

		dialog2 = ProgressDialog.show(jiaowuchaxun.this, "正在登录", "正在登录中……",
				true, true);
		myClient = new DefaultHttpClient();
		executorService.submit(chengjiRunnable);
	}

	public void kebiao(View v) {

		if (isWifiConnected(thisActivity)) {
			EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
			EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
			SharedPreferences.Editor editor = getSharedPreferences("data", 0)
					.edit();
			editor.putString("num_jiaowu", numEditText.getText().toString());
			editor.putString("psw_jiaowu", pswEditText.getText().toString());
			editor.commit();

			dialog2 = ProgressDialog.show(thisActivity, "正在登录", "正在登录中……",
					true, true);
			myClient = new DefaultHttpClient();
			executorService.submit(kebiaoRunnable);
			wificonnected = true;
		} else {
			wificonnected = false;
			Intent intent = new Intent();
			intent.setClass(thisActivity, jiaowu_kebiao.class);
			startActivity(intent);
		}

	}

	Bitmap yzmBitmap;
	Runnable chengjiRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				yzmBitmap = GET("http://jwgl.btbu.edu.cn/verifycode.servlet");
				gengxin("获取验证码完毕");
				// Message message = new Message();
				// message.what=1;
				// handler.sendMessage(message);
				EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
				EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
				yzmBitmap = im2bw(yzmBitmap);
				String yzm = shibie(yzmBitmap);
				gengxin(yzm);
				String numString = numEditText.getText().toString();
				if (numString.indexOf("P") != -1)
					jiaowu_chengji.pangtingsheng = true;
				String resultString = POST("http://jwgl.btbu.edu.cn/Logon.do",
						"method=logon&USERNAME=" + numEditText.getText()
								+ "&PASSWORD=" + pswEditText.getText()
								+ "&RANDOMCODE=" + yzm);
				random = new Random();
				Thread.sleep(random.nextInt(120));
				if (find(resultString,
						"http://jwgl.btbu.edu.cn/framework/main.jsp")) {
					gengxin("登录成功，获取权限……");
					POST("http://jwgl.btbu.edu.cn/Logon.do?method=logonBySSO",
							"");
					// 登录成功
					if (dialog2.isShowing())
						dialog2.dismiss();
					loadchengji();
				} else if (find(resultString, "验证码错误")) {
					gengxin("验证码错误，重新登录……");
					run();
				} else if (find(resultString, "errorinfo")) {
					if (dialog2.isShowing())
						dialog2.dismiss();
					show(zhongjian(resultString, "errorinfo\">", "</span>")
							+ "\n默认密码为学号或身份证后六位。");
				} else {
					if (dialog2.isShowing())
						dialog2.dismiss();
					show("登录失败");
				}
			} catch (Exception e) {
				if (dialog2.isShowing())
					dialog2.dismiss();
			}
			if (dialog2.isShowing())
				dialog2.dismiss();
		}
	};

	Runnable kebiaoRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				yzmBitmap = GET("http://jwgl.btbu.edu.cn/verifycode.servlet");
				gengxin("获取验证码完毕");
				EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
				EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
				yzmBitmap = im2bw(yzmBitmap);
				String yzm = shibie(yzmBitmap);
				gengxin(yzm);
				String resultString = POST("http://jwgl.btbu.edu.cn/Logon.do",
						"method=logon&USERNAME=" + numEditText.getText()
								+ "&PASSWORD=" + pswEditText.getText()
								+ "&RANDOMCODE=" + yzm);
				// System.out.println(resultString);
				if (dialog2.isShowing())
					dialog2.dismiss();
				if (find(resultString,
						"http://jwgl.btbu.edu.cn/framework/main.jsp")) {
					gengxin("登录成功，获取权限……");
					POST("http://jwgl.btbu.edu.cn/Logon.do?method=logonBySSO",
							"");
					// 登录成功
					loadkebiao();
				} else if (find(resultString, "验证码错误")) {
					gengxin("验证码错误，重新登录……");
					run();
				} else if (find(resultString, "errorinfo")) {
					if (dialog2.isShowing())
						dialog2.dismiss();
					show(zhongjian(resultString, "errorinfo\">", "</span>")
							+ "\n默认密码为学号或身份证后六位。");
				} else
					show("登录失败");
			} catch (Exception e) {
				if (dialog2.isShowing())
					dialog2.dismiss();
			}
			if (dialog2.isShowing())
				dialog2.dismiss();
		}
	};

	void gengxin(String gx) {
		gengxinString = gx;
		Message message = new Message();
		message.what = 1;
		handler.sendMessage(message);
	}

	String gengxinString = "";
	Toast toast;

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
				}
			} catch (Exception e) {
			}
		}
	};
	String showString = "";

	public boolean find(String text, String w) {
		if (text.indexOf(w) == -1)
			return false;
		else
			return true;
	}

	protected void loadchengji() {
		Intent intent = new Intent();
		intent.setClass(this, jiaowu_chengji.class);
		startActivity(intent);
	}

	protected void loadkebiao() {
		Intent intent = new Intent();
		intent.setClass(this, jiaowu_kebiao.class);
		startActivity(intent);
	}

	public String zhongjian(String text, String textl, String textr) {
		return zhongjian(text, textl, textr, 0);
	}

	public String zhongjian(String text, String textl, String textr, int start) {
		int left = text.indexOf(textl, start);
		int right = text.indexOf(textr, left + textl.length());
		return text.substring(left + textl.length(), right);
	}

	public String shibie(Bitmap myBitmap) {
		String yzm = "";
		String[] myyzm = { "1", "2", "3", "b", "c", "m", "n", "v", "x", "z" };
		try {
			int qietu, duibi;
			int x, y;
			for (qietu = 0; qietu < 4; qietu++) {
				int errnum[] = { 100, 100, 100, 100, 100, 100, 100, 100, 100,
						100 };
				for (duibi = 0; duibi < 10; duibi++) {
					int errpixel = 0;
					InputStream is = getResources().getAssets().open(
							myyzm[duibi] + ".bmp");
					Bitmap rawBitmap = BitmapFactory.decodeStream(is);
					for (y = 0; y < 12; y++) {
						for (x = 0; x < 9; x++) {
							int col = myBitmap.getPixel(x + 3 + 10 * qietu,
									y + 4);
							int col2 = rawBitmap.getPixel(x, y);
							if ((col2 & 0xFF) > (col & 0xFF))
								errpixel++;
						}
						System.out.print("nn");
					}
					errnum[duibi] = errpixel;
				}
				int wz = 0, min = 100, i;
				for (i = 0; i < 10; i++)
					if (errnum[i] < min) {
						min = errnum[i];
						wz = i;
					}
				yzm += myyzm[wz];
			}
		} catch (Exception e) {
		}
		System.out.println(yzm);
		return yzm;
	}

	// final Handler handler = new Handler(){
	// @Override
	// public void handleMessage(Message msg){
	// try{
	// super.handleMessage(msg);
	// //ImageView asdImageView= (ImageView)findViewById(R.id.jiaowu_kebiao);
	// if(msg.what == 1){
	//
	// }
	// }catch(Exception e)
	// {
	//
	// }
	// }
	// };

	public Bitmap im2bw(Bitmap myBitmap) {
		Bitmap bwBitmap = null;
		int x = myBitmap.getWidth();
		int y = myBitmap.getHeight();

		bwBitmap = myBitmap.copy(Config.ARGB_8888, true);
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				int col = bwBitmap.getPixel(i, j);
				int alpha = col & 0xFF000000;
				int red = (col & 0x00FF0000) >> 16;
				int green = (col & 0x0000FF00) >> 8;
				int blue = (col & 0x000000FF);
				// 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
				int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
				if (gray <= 128) {
					gray = 255;
				} else {
					gray = 0;
				}
				// 新的ARGB
				int newColor = alpha | (gray << 16) | (gray << 8) | gray;
				// 设置新图像的当前像素值
				bwBitmap.setPixel(i, j, newColor);
			}
		}

		return bwBitmap;
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

		menu.setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				System.out.println("关闭菜单");
				findViewById(R.id.changpao_cha).getBackground().setAlpha(255);
				findViewById(R.id.jiaowu_chengji).getBackground().setAlpha(255);
				findViewById(R.id.jiaowu_xiaoli).getBackground().setAlpha(255);
			}
		});

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

	public void qingkongziliao(View v) {
		EditText numEditText = (EditText) findViewById(R.id.changpao_num2);
		EditText pswEditText = (EditText) findViewById(R.id.changpao_psw2);
		numEditText.setText("");
		pswEditText.setText("");
		SharedPreferences.Editor editor = getSharedPreferences("data", 0)
				.edit();
		editor.putString("num_jiaowu", "");
		editor.putString("psw_jiaowu", "");
		editor.commit();
		File file = new File(filepath);
		file.delete();
		try {
			File[] childFiles = file.listFiles();
			int i;
			for (i = 0; i < childFiles.length; i++)
				childFiles[i].delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		show("已经清空保存的信息。");
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
		} catch (Exception e) {
		}

		return bitmap;
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Public_menu menu = new Public_menu();
		menu.thisActivity = thisActivity;
		menu.select(item);
		return false;
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
