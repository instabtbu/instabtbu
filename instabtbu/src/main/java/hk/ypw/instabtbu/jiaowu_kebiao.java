package hk.ypw.instabtbu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author ypw
 * 
 */
@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
public class jiaowu_kebiao extends SwipeBackActivity {
	long uiId = 0;
	String kebiaopath = "";
	private ProgressDialog dialog;

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

				FileInputStream fin = new FileInputStream(kebiaopath);
				int length = fin.available();
				byte[] buffer = new byte[length];
				fin.read(buffer);
				result = EncodingUtils.getString(buffer, "UTF-8");
				fin.close();
				System.out.println(result);
				xueqi = "离线课表";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getkebiao(true);
		}
	}

	String filepath = "";

	public void savekebiao(String xueqi, String result) {
		try {

			SharedPreferences.Editor editor = getSharedPreferences("data", 0)
					.edit();
			editor.putString("lixiankebiao", filepath + xueqi + ".html");
			editor.commit();

			System.out.println(getFilesDir().toString());
			result = result.replaceAll("style=\"display: none;\"", "");
			result = result
					.replaceAll("-1\" ", "-1\" style=\"display: none;\"");
			result = result.replaceAll(zhongjian(result, "<link", ">"), "");
			while (result.indexOf("<script") == -1)
				result = result.replaceAll(zhongjian(result, "<script", ">"),
						"");

			File file = new File(filepath + xueqi + ".html");
			FileWriter writer = new FileWriter(file);
			writer.write(result);
			writer.close();
			kebiaopath = filepath + xueqi + ".html";
		} catch (Exception e1) {
			System.out.println(filepath + xueqi + ".html");
			System.out.println(e1);
			show("保存文件失败，请检查权限。");

		}
	}

	private ExecutorService executorService = Executors.newCachedThreadPool();
	// 线程池
	String xueqi = "";
	String result = "";
	Pattern p;
	Matcher m;
	List<String> xueqiList = new ArrayList<String>();
	List<String> shijian = new ArrayList<String>();
	List<String> mingcheng = new ArrayList<String>();
	List<String> xiangxi = new ArrayList<String>();
	List<String> didian = new ArrayList<String>();

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
			p = Pattern.compile("<div id=\"(.+?)-2\".*?>(.+?)</div>");
			m = p.matcher(result);
			String temp;
			while (m.find()) {
				shijian.add(m.group(1));
				temp = m.group(2);
				Pattern p2 = Pattern
						.compile("&nbsp;(.*?)<br>(.+?)<br>(.*?)<br><nobr> *(.*?)<nobr><br>(.*?)<br>(.*?)<br>");
				Matcher m2 = p2.matcher(temp);
				if (m2.find()) {
					mingcheng.add(m2.group(1));
					// int i;String myString="";
					// for(i=1;i<6;i++)if(m2.group(i).length()!=0)myString+=m2.group(i)+"\n";

					// xiangxi.add(myString.substring(0,myString.length()-1));
					temp = temp.replace("&nbsp;", "");
					System.out.println(temp);
					Pattern p3 = Pattern.compile("<.+?> *");
					Matcher m3 = p3.matcher(temp);
					temp = m3.replaceAll("\n");
					while (temp.indexOf("\n\n") != -1) {
						temp = temp.replace("\n\n", "\n");
					}
					temp = temp.substring(0, temp.length() - 1);
					xiangxi.add(temp);

					String[] tempStrings = temp.split("\n");

					if (tempStrings.length == 5) {
						String myString = "";
						if (tempStrings[3].indexOf("单周") != -1)
							myString += "单周\n";
						else if (tempStrings[3].indexOf("双周") != -1)
							myString += "双周\n";
						myString += tempStrings[4];
						didian.add(myString);
					} else if (tempStrings.length == 6) {
						if (tempStrings[4].indexOf("暂无") == -1)
							didian.add(tempStrings[4] + "\n" + tempStrings[5]);
						else
							didian.add(tempStrings[5]);
					} else if (tempStrings.length == 10) {
						String myString = "";
						if (tempStrings[3].indexOf("单周") != -1)
							myString += "单周\n";
						else if (tempStrings[3].indexOf("双周") != -1)
							myString += "双周\n";
						else
							myString += tempStrings[3] + "\n";
						myString += tempStrings[4] + "\n";

						if (tempStrings[8].indexOf("单周") != -1)
							myString += "单周\n";
						else if (tempStrings[8].indexOf("双周") != -1)
							myString += "双周\n";
						else
							myString += tempStrings[8] + "\n";
						myString += tempStrings[9];

						didian.add(myString);
					} else if (tempStrings.length == 12) {
						String myString = "";
						if (tempStrings[3].indexOf("单周") != -1)
							myString += "单周\n";
						else if (tempStrings[3].indexOf("双周") != -1)
							myString += "双周\n";
						else
							myString += tempStrings[3];
						myString += tempStrings[4] + tempStrings[5] + "\n";

						if (tempStrings[9].indexOf("单周") != -1)
							myString += "单周\n";
						else if (tempStrings[9].indexOf("双周") != -1)
							myString += "双周\n";
						else
							myString += tempStrings[9];
						myString += tempStrings[10] + tempStrings[11];
						didian.add(myString);
					}

					else {
						didian.add(tempStrings[4]);
					}
				} else {
					mingcheng.add("");
					xiangxi.add("");
					didian.add("");
				}
			}
		} catch (Exception e) {
		}
		System.out.println("finished kebiao");
		Message message = new Message();
		message.what = 1;
		handler.sendMessage(message);

	}

	Runnable kebiaoRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				SharedPreferences sp = getSharedPreferences("data", 0);
				xueqi = sp.getString("xueqi", "");
			} catch (Exception e) {
			}
			if (xueqi.length() == 0) {
				xuanxueqi();
			} else
				getkebiao();

		}
	};

	public void xueqi(View v) {
		dialog = ProgressDialog.show(this, "选学期", "正在获取学期列表……", true, true);
		executorService.submit(xuanRunnable);
	}

	Runnable xuanRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			xuanxueqi();
		}
	};

	public void xuanxueqi() {
		System.out.println("开始选学期");
		result = POST("http://jwgl.btbu.edu.cn/tkglAction.do?method=kbxxXs", "");
		p = Pattern.compile("<option value=\".*?\".*?>(.*?)</option>");
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
					List<String> xueqiList2 = new ArrayList<String>();
					SharedPreferences sp = getSharedPreferences("data", 0);
					String num = sp.getString("num_jiaowu", null);
					num = num.substring(0, 2);
					int i;
					for (i = 0; i < xueqiList.size(); i++) {
						String temp = xueqiList.get(i).toString();
						xueqiList2.add(temp);
						if (temp.indexOf(num) != -1
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
											editor.commit();
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
	String showString = "";
	Toast toast;

	void gengxin(String gx) {
		gengxinString = gx;
		Message message = new Message();
		message.what = 3;
		handler.sendMessage(message);
	}

	String gengxinString = "";

	Runnable getkebiaoRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			getkebiao();
		}
	};
	String titlejuti = "";
	String jutiString = "";

	public void jutikebiao() {
		new AlertDialog.Builder(jiaowu_kebiao.this).setTitle(titlejuti)
				.setMessage(jutiString).setPositiveButton("确定", null).show();
	}

	String kebiaoString = "";
	int kebiaoInt = 0;
	Activity thisActivity = this;

	@SuppressLint("InflateParams")
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

			int myid[] = { R.id.jiaowu_zhouyi, R.id.jiaowu_zhouer,
					R.id.jiaowu_zhousan, R.id.jiaowu_zhousi, R.id.jiaowu_zhouwu };
			int i;
			try {
				List<TextView> zhouList = new ArrayList<TextView>();
				for (i = 0; i < 5; i++) {
					TextView tempTextView = (TextView) convertView
							.findViewById(myid[i]);
					zhouList.add(tempTextView);
					if (position % 2 == 0)
						tempTextView.setBackgroundColor(0xFFF2F2F2);
					tempTextView.setText(mingcheng.get(position * 7 + i) + "\n"
							+ didian.get(position * 7 + i));
					int j = 0;
					int hang = quhangshu(mingcheng.get(position * 7 + i) + "\n"
							+ didian.get(position * 7 + i));
					if (mingcheng.get(position * 7 + i).length() > 5)
						hang++;
					if (didian.get(position * 7 + i).length() > 12)
						hang++;
					for (j = 0; j < 5 - hang; j++)
						tempTextView.append("\n");
					if (xiangxi.get(position * 7 + i).indexOf("单周") != -1
							|| xiangxi.get(position * 7 + i).indexOf("双周") != -1) {
						tempTextView.setBackgroundColor(0X223474AC);
					}
					tempTextView.setTag(position * 7 + i);
					tempTextView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							try {
								jutiString = xiangxi.get(Integer.valueOf(v
										.getTag().toString()));
								titlejuti = mingcheng.get(Integer.valueOf(v
										.getTag().toString()));
								if (jutiString.length() != 0)
									jutikebiao();
							} catch (Exception e) {
							}
						}
					});
				}
			} catch (Exception e) {
			}
			return convertView;
		}
	};

	public int quhangshu(String myString) {
		int hang = 0, wz = 0;
		while ((wz = myString.indexOf("\n", wz) + 1) > 0)
			hang++;
		return hang + 1;
	}

	@SuppressWarnings("unused")
	public void myui() {

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		int width = mDisplayMetrics.widthPixels;
	}

	public String zhongjian(String text, String textl, String textr) {
		return zhongjian(text, textl, textr, 0);
	}

	public String zhongjian(String text, String textl, String textr, int start) {
		int left = text.indexOf(textl, start);
		int right = text.indexOf(textr, left + textl.length());
		return text.substring(left + textl.length(), right);
	}

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
			jiaowuchaxun.myClient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
			// 请求超时
			jiaowuchaxun.myClient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 30000);
			// 读取超时
			HttpResponse hResponse;
			hResponse = jiaowuchaxun.myClient.execute(hPost);
			if (hResponse.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(hResponse.getEntity());
				// result = new String(result.getBytes("ISO_8859_1"),"gbk");
				// 转码
			}

		} catch (Exception e) {
			if (dialog.isShowing())
				dialog.dismiss();
			show("连接教务管理系统失败。\n请确认信号良好再操作。");
			finish();

		}
		return (result);
	}

	public void show(String str) {
		show(str, 0);
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
}