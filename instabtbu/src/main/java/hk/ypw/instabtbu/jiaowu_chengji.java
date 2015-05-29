package hk.ypw.instabtbu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility" })
public class jiaowu_chengji extends SwipeBackActivity {
	long uiId = 0;
	private ProgressDialog dialog;
	Random random = new Random();
	public static boolean pangtingsheng = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jiaowu_chengji);

		uiId = Thread.currentThread().getId();// 获取主线程ID
		dialog = ProgressDialog.show(this, "登录成功", "正在获取成绩……", true, true);
		MobclickAgent.onEvent(this, "chengji");
		executorService.submit(chengjiRunnable);

	}

	List<String> kechengmingcheng = new ArrayList<String>();
	List<String> chengji = new ArrayList<String>();
	List<String> xuefen = new ArrayList<String>();
	List<String> urlList = new ArrayList<String>();
	private ExecutorService executorService = Executors.newCachedThreadPool();
	// 线程池

	String jidian = "";
	String result = "";
	Runnable chengjiRunnable = new Runnable() {
		@Override
		public void run() {

			Pattern p;
			Matcher m;
			// TODO Auto-generated method stub
			result = POST(
					"http://jwgl.btbu.edu.cn/xszqcjglAction.do?method=queryxscj",
					"PageNum=1");
			gengxin("获取第一页成绩成功，分析中……");
			// System.out.println(result);
			String jidianString = zhongjian(result, "平均学分绩点<span>", "。</span>");
			jidian = jidianString;
			int i = 0;
			String tempString;
			String kemuString = "";
			String chengjiString = "";
			String showString = "";
			showString += "绩点：" + jidian;
			random = new Random();
			SystemClock.sleep(random.nextInt(120));
			p = Pattern.compile("<tr heigth = 23.+?>.+?</tr>");
			m = p.matcher(result);
			while (m.find()) {
				Pattern p2 = Pattern.compile("<td.+?>(.*?(\\w*)(</a>)?)</td>");
				Matcher m2 = p2.matcher(m.group(0));
				if (pangtingsheng) {
					// 旁听生
					while (m2.find()) {
						tempString = m2.group(1);
						if (i % 10 == 2) {
							kemuString = tempString;
							if (kemuString.indexOf('<') == -1)
								kechengmingcheng.add(tempString);
						} else if (i % 10 == 3) {
							chengji.add(zhongjian(tempString, ")\">", "</a>"));
							chengjiString = zhongjian(tempString, ")\">",
									"</a>");
							urlList.add("http://jwgl.btbu.edu.cn"
									+ zhongjian(tempString, "JsMod('", "\""));
						} else if (i % 10 == 8) {
							xuefen.add(tempString);
							showString += "\n" + kemuString + "\t"
									+ chengjiString;
							gengxin(showString);
							SystemClock.sleep(random.nextInt(100));
						}
						i++;
					}
				} else {
					// 普通学生
					while (m2.find()) {
						tempString = m2.group(1);
						if (i % 13 == 4) {
							kemuString = tempString;
							if (kemuString.indexOf('<') == -1)
								kechengmingcheng.add(tempString);
						} else if (i % 13 == 5) {
							chengji.add(zhongjian(tempString, ")\">", "</a>"));
							chengjiString = zhongjian(tempString, ")\">",
									"</a>");
							urlList.add("http://jwgl.btbu.edu.cn"
									+ zhongjian(tempString, "JsMod('", "\""));
						} else if (i % 13 == 10) {
							xuefen.add(tempString);
							showString += "\n" + kemuString + "\t"
									+ chengjiString;
							gengxin(showString);
							SystemClock.sleep(random.nextInt(100));
						}
						i++;
					}
				}
			}
			try {
				int ye = 0;
				p = Pattern.compile("value=(.+?)value=(\\w+).+?末页");
				m = p.matcher(result);
				m.find();
				String yeString = m.group(2);
				ye = Integer.valueOf(yeString);
				int xh = 2;
				while (xh <= ye) {
					showString += "\n共" + String.valueOf(ye) + "页,正在获取第"
							+ String.valueOf(xh) + "页……";
					gengxin(showString);
					i = 0;
					result = POST(
							"http://jwgl.btbu.edu.cn/xszqcjglAction.do?method=queryxscj",
							"PageNum=" + String.valueOf(xh));
					showString += "\n获取成功，正在分析第" + String.valueOf(xh) + "页……";
					gengxin(showString);
					System.out.println("PageNum=" + String.valueOf(xh));
					SystemClock.sleep(random.nextInt(120));
					p = Pattern.compile("<tr heigth = 23.+?>.+?</tr>");
					m = p.matcher(result);
					while (m.find()) {
						Pattern p2 = Pattern
								.compile("<td.+?>(.*?(\\w*)(</a>)?)</td>");
						Matcher m2 = p2.matcher(m.group(0));
						if (pangtingsheng) {
							// 旁听生
							while (m2.find()) {
								tempString = m2.group(1);
								if (i % 10 == 2) {
									kemuString = tempString;
									if (kemuString.indexOf('<') == -1)
										kechengmingcheng.add(tempString);
								} else if (i % 10 == 3) {
									chengji.add(zhongjian(tempString, ")\">",
											"</a>"));
									chengjiString = zhongjian(tempString,
											")\">", "</a>");
									urlList.add("http://jwgl.btbu.edu.cn"
											+ zhongjian(tempString, "JsMod('",
													"\""));
								} else if (i % 10 == 8) {
									xuefen.add(tempString);
									showString += "\n" + kemuString + "\t"
											+ chengjiString;
									gengxin(showString);
									SystemClock.sleep(random.nextInt(100));
								}
								i++;
							}
						} else {
							// 普通学生
							while (m2.find()) {
								tempString = m2.group(1);
								if (i % 13 == 4) {
									kemuString = tempString;
									if (kemuString.indexOf('<') == -1)
										kechengmingcheng.add(tempString);
								} else if (i % 13 == 5) {
									chengji.add(zhongjian(tempString, ")\">",
											"</a>"));
									chengjiString = zhongjian(tempString,
											")\">", "</a>");
									urlList.add("http://jwgl.btbu.edu.cn"
											+ zhongjian(tempString, "JsMod('",
													"\""));
								} else if (i % 13 == 10) {
									xuefen.add(tempString);
									showString += "\n" + kemuString + "\t"
											+ chengjiString;
									gengxin(showString);
									SystemClock.sleep(random.nextInt(100));
								}
								i++;
							}
						}
					}
					xh++;
					SystemClock.sleep(random.nextInt(100));
				}
			} catch (Exception e) {

			}
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
			System.out.println("finished chengji");
			if (dialog.isShowing())
				dialog.dismiss();
		}
	};

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				super.handleMessage(msg);
				TextView jidianTextView = (TextView) findViewById(R.id.jiaowu_jidian);
				if (msg.what == 1) {
					jidianTextView.setText("平均学分绩点：" + jidian);
					ListView listview = (ListView) findViewById(R.id.jiaowu_listView_chengji);
					listview.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							chengjiInt = arg2;
							executorService.submit(pingshichengji);
						}
					});
					CustomAdapter customAdapter = new CustomAdapter(
							thisActivity, kechengmingcheng);
					listview.setAdapter(customAdapter);
				} else if (msg.what == 2) {
					new AlertDialog.Builder(jiaowu_chengji.this)
							.setTitle(kechengmingcheng.get(chengjiInt))
							.setMessage(chengjiString)
							.setPositiveButton("确定", null).show();
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
			}
		}
	};
	String showString = "";
	Toast toast;

	String chengjiString = "";
	int chengjiInt = 0;
	Runnable pingshichengji = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				System.out.println(chengjiInt);
				String result2 = "";
				result2 = POST(urlList.get(chengjiInt), "");
				System.out.println(urlList.get(chengjiInt));
				Pattern pa;
				Matcher ma;
				pa = Pattern.compile("70\" title=.+?>(.+?)</td>");
				ma = pa.matcher(result2);
				String[] fen = { "", "", "", "", "", "", "", "", "", "", "",
						"", "", "", "" };
				int i = 0;
				while (ma.find()) {
					if (Integer.valueOf(ma.group(1)) > 0) {
						fen[i] = ma.group(1);
						System.out.println(fen[i]);
						i++;
					}
				}
				if (fen[0] != "")
					chengjiString = "平时成绩：" + fen[0] + "，期末成绩：" + fen[1]
							+ "\n总成绩：" + fen[2];
				else
					chengjiString = "该科目只有总成绩！";
				System.out.println(chengjiString);
				Message message = new Message();
				message.what = 2;
				handler.sendMessage(message);
			} catch (Exception e) {
			}
		}
	};

	void gengxin(String gx) {

		gengxinString = gx;
		System.out.println(gx);
		Message message = new Message();
		message.what = 3;
		handler.sendMessage(message);

	}

	String gengxinString = "";

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
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			if (mData == null || mData.size() <= 0 || position < 0
					|| position >= mData.size()) {
				return null;
			}
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_chengji, null);
			}
			TextView name = (TextView) convertView
					.findViewById(R.id.jiaowu_name);
			name.setText(kechengmingcheng.get(position));
			TextView xuefen = (TextView) convertView
					.findViewById(R.id.jiaowu_xuefen);
			xuefen.setText(jiaowu_chengji.this.xuefen.get(position));
			TextView chengji = (TextView) convertView
					.findViewById(R.id.jiaowu_chengji);
			try {
				chengji.setText(jiaowu_chengji.this.chengji.get(position));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}
	}

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