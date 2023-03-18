package com.alphi.tmhook.modules;

/*
    author: alphi
    createDate: 2023/3/11
*/

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class ViewHook {
    private ViewHook() {
        super();
    }

    /**
     * 视图Hook，强行适配Android夜间模式兼容
     */
    public static void hook(ClassLoader classLoader) {
        // 父容器Hook
        XposedHelpers.findAndHookMethod(ViewGroup.class, "layout", int.class, int.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ViewGroup view = (ViewGroup) param.thisObject;
                if (isDarkMode(view.getContext())) {
                    if (view.getBackground() != null && !(view.getBackground() instanceof ColorDrawable)) {
                        view.setBackgroundColor(Color.BLACK);
                        view.setTag(606080, "");
                    }
                }
            }
        });
        // 好友列表 SingLineTextView
        Class<?> aClass = XposedHelpers.findClass("com.tencent.widget.SingleLineTextView", classLoader);
        XposedHelpers.findAndHookMethod(aClass, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.thisObject;
                if (isDarkMode(view.getContext())) {
                    aClass.getMethod("setTextColor", int.class).invoke(view, Color.WHITE);
                    forceDarkForParent(view, 2);
                }
            }
        });
        // 原生 TextView Hook
        XposedHelpers.findAndHookMethod(TextView.class, "onDraw", Canvas.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                TextView tv = (TextView) param.thisObject;
                Context context = tv.getContext();
                if (isDarkMode(context)) {
                    tv.setTextColor(Color.WHITE);
                    forceDarkForParent(tv, 2);
                    if (context.getResources().getResourceEntryName(tv.getId()).startsWith("chat_item_")) {
                        tv.setBackgroundColor(Color.BLACK);
                    }
                    if (tv instanceof EditText) {
                        EditText et = (EditText) tv;
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setColor(Color.parseColor("#2F4F4F"));
                        gradientDrawable.setCornerRadius(16);
                        et.setBackground(gradientDrawable);
                    }
                }
            }
        });
//        XposedHelpers.findAndHookMethod(ImageView.class, "onDraw", Canvas.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Object obj = param.thisObject;
//                if (obj instanceof ImageButton) {
//                    // 无效
//                    ImageButton ib = (ImageButton) obj;
//                    ib.setImageTintList(ColorStateList.valueOf(Color.WHITE));
//                }
//            }
//        });
    }

    private static void forceDarkForParent(View view, int eachCount) {
        for (int i = 0; i < eachCount; i++) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                view = (View) parent;
                if (exceptView((ViewGroup) view)) {
                    view.setBackgroundColor(Color.BLACK);
                } else
                    return;
            }
        }
    }

    private static boolean exceptView(ViewGroup vg) {
        return exceptOldAccountIconSettingsView(vg) && vg.getTag(606080) == null;
    }

    private static boolean exceptOldAccountIconSettingsView(ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            CharSequence description = vg.getChildAt(i).getContentDescription();
            if (description != null)
                Log.d("kkk2", "exceptOldAccountIconSettingsView: " + i + ", " + description);
            if (description != null && description.equals("帐户及设置")) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDarkMode(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
