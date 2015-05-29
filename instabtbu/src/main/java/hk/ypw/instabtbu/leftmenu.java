package hk.ypw.instabtbu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.umeng.analytics.MobclickAgent;

@SuppressLint("ClickableViewAccessibility")
public class Leftmenu {
    SlidingMenu menu;
    Activity thisActivity;
    int type;

    public Leftmenu(Activity activity, int mytype) {
        type = mytype;
        thisActivity = activity;
        // configure the SlidingMenu
        menu = new SlidingMenu(thisActivity);
        menu.setMode(SlidingMenu.LEFT);
        // 设置触摸屏幕的模式
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        // menu.setShadowWidth(40);
        // menu.setShadowDrawable(R.drawable.shadow_left);

        // 设置滑动菜单视图的宽度
        menu.setBehindWidth(dip2px(thisActivity, 200));
        // 设置渐入渐出效果的值
        menu.setFadeDegree(0.55f);
        /**
         * SLIDING_WINDOW will include the Title/ActionBar in the content
         * section of the SlidingMenu, while SLIDING_CONTENT does not.
         */
        menu.attachToActivity(thisActivity, SlidingMenu.SLIDING_CONTENT);
        // 为侧滑菜单设置布局
        menu.setMenu(R.layout.leftmenu);

        menu.setBehindScrollScale(0);

        menu.setOnClosedListener(new OnClosedListener() {
            @Override
            public void onClosed() {
                // TODO Auto-generated method stub
                System.out.println("关闭菜单");
                try {
                    setalpha();
                } catch (Exception e) {
                }
            }
        });

        menu.setOnOpenedListener(new OnOpenedListener() {
            @Override
            public void onOpened() {
                // TODO Auto-generated method stub
                System.out.println("打开菜单");
                try {
                    setalpha();
                } catch (Exception e) {
                }
                /**
                 * 在菜单被打开完毕之后,我们把登录按钮的透明度调到255,这样避免了按钮变灰的bug
                 */
            }
        });
        menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("滑动菜单被点击");
                menu.toggle();
                /**
                 * 当滑动菜单被点击的时候,一般是用户想将菜单滑回去,不然他也不会点空白处,而是按钮
                 * 此时我们使用menu.toggle()这个函数,将菜单弹回左边
                 */
            }
        });

        ImageView jiaowuImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_jiaowu);
        ImageView shenghuoImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_shenghuo);
        ImageView shangwangImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_shangwang);
        ImageView ditieImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_ditie);
        ImageView xiaoyuankaImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_xiaoyuanka);
        ImageView aboutImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_about);
        ImageView guancangImageView = (ImageView) thisActivity
                .findViewById(R.id.leftmenu_guancang);

        shangwangImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(0);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setClass(thisActivity, shangwang.class);
                    intent.putExtra("menu", 1);
                    thisActivity.startActivity(intent);
                    MobclickAgent.onEvent(thisActivity, "btbu");
                    if (type != 1)
                        thisActivity.finish();
                    else
                        menu.toggle();
                }
                return true;
            }
        });

        jiaowuImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(1);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setClass(thisActivity, jiaowuchaxun.class);
                    thisActivity.startActivity(intent);
                    MobclickAgent.onEvent(thisActivity, "jiaowu");
                    if (type != 2)
                        thisActivity.finish();
                    else
                        menu.toggle();
                }
                return true;
            }
        });

        shenghuoImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(2);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setClass(thisActivity, shenghuofuwu.class);
                    thisActivity.startActivity(intent);
                    MobclickAgent.onEvent(thisActivity, "shenghuo");
                    if (type != 3)
                        thisActivity.finish();
                    else
                        menu.toggle();
                }
                return true;
            }
        });

        guancangImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(6);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MobclickAgent.onEvent(thisActivity, "guancang");
                    if (type != 6) {
                        Intent intent = new Intent();
                        intent.setClass(thisActivity, Guancang.class);
                        thisActivity.startActivity(intent);
                        thisActivity.finish();
                    } else
                        menu.toggle();
                }
                return true;
            }
        });

        xiaoyuankaImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(3);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    MobclickAgent.onEvent(thisActivity, "xiaoyuanka");
                    if (type != 4) {
                        Intent intent = new Intent();
                        intent.setClass(thisActivity, xiaoyuanka.class);
                        thisActivity.startActivity(intent);
                        thisActivity.finish();
                    } else
                        menu.toggle();
                }
                return true;
            }
        });

        ditieImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(4);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setClass(thisActivity, Ditie.class);
                    thisActivity.startActivity(intent);
                    MobclickAgent.onEvent(thisActivity, "ditie");
                }
                return true;
            }
        });

        aboutImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    leftmenu_ui(5);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent intent = new Intent();
                    intent.setClass(thisActivity, About.class);
                    thisActivity.startActivity(intent);
                    MobclickAgent.onEvent(thisActivity, "about");
                }
                return true;
            }
        });

    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private void setalpha() {
        if (type == 1) {
            thisActivity.findViewById(R.id.shangwang_deglu).getBackground()
                    .setAlpha(255);
            thisActivity.findViewById(R.id.shangwang_duankai).getBackground()
                    .setAlpha(255);
        }
    }

    public void leftmenu_ui(int a) {
        ImageView[] myImageViews = {
                (ImageView) thisActivity.findViewById(R.id.leftmenu_shangwang),
                (ImageView) thisActivity.findViewById(R.id.leftmenu_jiaowu),
                (ImageView) thisActivity.findViewById(R.id.leftmenu_shenghuo),
                (ImageView) thisActivity.findViewById(R.id.leftmenu_xiaoyuanka),
                (ImageView) thisActivity.findViewById(R.id.leftmenu_ditie),
                (ImageView) thisActivity.findViewById(R.id.leftmenu_about),
                (ImageView) thisActivity.findViewById(R.id.leftmenu_guancang),};
        int[] tu1 = {R.drawable.left_shangwang1, R.drawable.left_jiaowu1,
                R.drawable.left_shenghuo1, R.drawable.left_xiaoyuanka1,
                R.drawable.left_ditie1, R.drawable.left_about1,
                R.drawable.left_guancang1,};
        int[] tu0 = {R.drawable.left_shangwang0, R.drawable.left_jiaowu0,
                R.drawable.left_shenghuo0, R.drawable.left_xiaoyuanka0,
                R.drawable.left_ditie0, R.drawable.left_about0,
                R.drawable.left_guancang0,};
        int i;
        for (i = 0; i < myImageViews.length; i++) {
            if (i == a)
                myImageViews[i].setBackgroundResource(tu1[i]);
            else
                myImageViews[i].setBackgroundResource(tu0[i]);
        }
    }
}
