package hk.ypw.instabtbu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

@SuppressLint({ "ClickableViewAccessibility", "WorldReadableFiles" })
public class shenghuofuwu extends Activity {
	leftmenu leftmenu;
	SlidingMenu menu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shenghuo);
		leftmenu = new leftmenu(thisActivity, 3);
		menu = leftmenu.menu;
		myui();

	}

	@SuppressWarnings("unused")
	public void myui() {
		try {
			DisplayMetrics mDisplayMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
			int width = mDisplayMetrics.widthPixels;
			int height = mDisplayMetrics.heightPixels;
			float density = mDisplayMetrics.density;
			double w = width / 700.0;

			setView(R.id.shenghuo_songshui, (int) (0.07 * width),
					(int) (0.07 * width), (int) (w * 274), (int) (w * 198));
			setView(R.id.shenghuo_weixiu,
					(int) (width - 0.07 * width - w * 274),
					(int) (0.07 * width), (int) (w * 274), (int) (w * 198));

			SharedPreferences sp = getSharedPreferences("data", 0);
			String num = sp.getString("num", "还没有登录");
			TextView left_userTextView = (TextView) thisActivity
					.findViewById(R.id.leftmenu_textview_user);
			TextView left_user2TextView = (TextView) thisActivity
					.findViewById(R.id.leftmenu_textview_dianzheli);
			System.out.println(num);
			left_userTextView.setText(num);
			left_user2TextView.setText("请选择");

		} catch (Exception e) {
			e.printStackTrace();
		}

		ImageView songshui = (ImageView) findViewById(R.id.shenghuo_songshui);
		songshui.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent(Intent.ACTION_DIAL, Uri
						.parse("tel:57112640"));
				try {
					startActivity(intent);
				} catch (Exception e) {
					new AlertDialog.Builder(thisActivity).setTitle("提示")
							.setMessage("您的设备暂时不支持拨打电话。")
							.setPositiveButton("确定", null).show();
				}
				Toast.makeText(getApplicationContext(), "送水电话",
						Toast.LENGTH_SHORT).show();
			}
		});

		ImageView weixiu = (ImageView) findViewById(R.id.shenghuo_weixiu);
		weixiu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri
						.parse("tel:81353578"));
				try {
					startActivity(intent);
				} catch (Exception e) {
					new AlertDialog.Builder(thisActivity).setTitle("提示")
							.setMessage("您的设备暂时不支持拨打电话。")
							.setPositiveButton("确定", null).show();
				}
				Toast.makeText(getApplicationContext(), "维修电话",
						Toast.LENGTH_SHORT).show();
			}
		});

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.shenghuo_linear);
		int i;
		Canting[] cantings = {

				new Canting(R.drawable.shenghuo_hanwei, "韩味小厨", "13167516420"),
				new Canting(R.drawable.shenghuo_tiebanke, "铁板客", "13381318000"),
				new Canting(R.drawable.shenghuo_pasta, "Pasta&Curry",
						"15226677533"),
				new Canting(R.drawable.shenghuo_xinjiapo, "新加坡鸡饭",
						"13051976863"),
				new Canting(R.drawable.shenghuo_meiweiduo, "美味多", "15810165416"),
				new Canting(R.drawable.shenghuo_ailin, "爱琳食屋", "13691489457"),
				new Canting(R.drawable.shenghuo_tuerqi, "土耳其烤肉", "15810831105"),
				new Canting(R.drawable.shenghuo_tuode, "托德鸡排", "13120465388"),
				new Canting(R.drawable.shenghuo_zibu, "滋补烩面", "18610621662"),
				new Canting(R.drawable.shenghuo_malatang, "骨汤麻辣烫",
						"13167508739"),
				new Canting(R.drawable.shenghuo_guoqiao, "过桥米线", "13681304280"),
				new Canting(R.drawable.shenghuo_shousi, "寿司太郎", "13264016186"), };

		for (i = 0; i < cantings.length; i++) {
			final String tempcall = cantings[i].dianhua;
			final String tempname = cantings[i].mingcheng;
			ImageView tempImageView = new ImageView(this);
			tempImageView.setImageResource(cantings[i].id);
			tempImageView.setLayoutParams(new LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
					android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
			tempImageView.setAdjustViewBounds(true);
			tempImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(Intent.ACTION_DIAL, Uri
							.parse("tel:" + tempcall));
					try {
						startActivity(intent);
					} catch (Exception e) {
						new AlertDialog.Builder(thisActivity).setTitle("提示")
								.setMessage("您的设备暂时不支持拨打电话。")
								.setPositiveButton("确定", null).show();
					}
					Toast.makeText(getApplicationContext(), tempname,
							Toast.LENGTH_SHORT).show();
				}
			});
			linearLayout.addView(tempImageView);
		}
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
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

	Activity thisActivity = this;

	public class Canting {
		int id;
		String mingcheng;
		String dianhua;

		public Canting(int id, String mingcheng, String dianhua) {
			this.id = id;
			this.mingcheng = mingcheng;
			this.dianhua = dianhua;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		leftmenu.leftmenu_ui(2);
	}

	public mypoint setView(int id, int x, int y, int wid, int hei) {
		View myView = findViewById(id);
		LayoutParams myParams = new LayoutParams(wid, hei);
		myParams.setMargins(x, y, 0, 0);
		myView.setLayoutParams(myParams);
		return new mypoint(x, y, wid, hei);
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
}
