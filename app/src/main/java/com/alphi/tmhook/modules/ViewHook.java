package com.alphi.tmhook.modules;

/*
    author: alphi
    createDate: 2023/3/11
*/

import static com.alphi.tmhook.utils.EqualsUtil.isClassEndName;
import static com.alphi.tmhook.utils.EqualsUtil.isEqualsInts;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class ViewHook {
    private final static String TAG = ViewHook.class.getSimpleName();

    private ViewHook() {
        super();
    }

    public static void hook(ClassLoader classLoader) {
        hook3(classLoader);
    }


    /**
     * 视图Hook，强行适配Android夜间模式兼容
     */
    public static void hook1(ClassLoader classLoader) {
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


    public static void hook2(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            private final Set<Unhook> unhookList = new HashSet<>();

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Log.d(TAG, "afterHookedMethod: onCreate");
                Activity context = (Activity) param.thisObject;
                if ((context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                    if (isClassEndName(context.getClass(), "AIOGalleryActivity"))
                        return;
                    View decorView = context.getWindow().getDecorView();
                    reverseColor(decorView);
                    unhookList.addAll(
                            XposedBridge.hookAllConstructors(ImageView.class, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) {
                                    ImageView obj = (ImageView) param.thisObject;
                                    if (obj instanceof ImageButton)
                                        return;
                                    if (obj.getId() == (int) 0x7f0a0d8c) {
                                        obj.setVisibility(View.GONE);
                                        return;
                                    } else if (isEqualsInts(obj.getId(), 0x7f0a1e7b, 0x7f0a1ff0, 0x7f0a24c6, 0x7f0a24c7, 0x7f0a1f83))
                                        return;
                                    reverseColor(obj);
                                }
                            })
                    );
                    unhookList.add(
                            XposedHelpers.findAndHookMethod(ImageView.class, "onMeasure", int.class, int.class, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) {
                                    ImageView obj = (ImageView) param.thisObject;
                                    if (isEqualsInts(obj.getId(), 0x7f0a2cb6))
                                        reverseColor(obj);
                                }
                            })
                    );
                    unhookList.addAll(
                            XposedBridge.hookAllConstructors(RelativeLayout.class, new XC_MethodHook() {
                                @SuppressLint("ResourceType")
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    RelativeLayout obj = (RelativeLayout) param.thisObject;
                                    if (obj.getId() == 0x7f0a1bc1)
                                        reverseColor(obj);
                                }
                            })
                    );

                    for (Method method : Fragment.class.getMethods()) {
                        unhookList.add(
                                XposedBridge.hookMethod(method, new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) {
                                        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(context.getWindow(), decorView);
                                        controller.setAppearanceLightStatusBars(false);
                                    }
                                }));
                    }
                    XposedHelpers.findAndHookMethod(Activity.class, "onDestroy", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            for (Unhook unhook : unhookList) {
                                unhook.unhook();
                            }
                        }
                    });
                }
            }
        });


        // TabImageView
//        Class<?> tabDragAnimationViewClazz = XposedHelpers.findClass("com.tencent.mobileqq.widget.TabDragAnimationView", classLoader);


        XposedBridge.hookAllConstructors(PopupWindow.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "afterHookedMethod: PopupWindow");
            }
        });
        XposedBridge.hookAllConstructors(PopupMenu.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "afterHookedMethod: PopupMenu");
            }
        });
    }

    public static void hook3(ClassLoader classLoader) {
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundDrawable", Drawable.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Drawable arg = (Drawable) param.args[0];
                if (arg == null)
                    return;
                reverseDrawableColor(arg);
            }
        });
        XposedHelpers.findAndHookMethod(View.class, "setBackgroundColor", int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int arg = (int) param.args[0];
                param.args[0] = reverseDrawableColor(arg);
            }
        });
        Class<?> aClass = XposedHelpers.findClass("com.tencent.widget.SingleLineTextView", classLoader);
        XposedHelpers.findAndHookMethod(aClass, "setText", CharSequence.class, new XC_MethodHook() {

            private static Field o; // ColorStateList

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object obj = param.thisObject;
                if (o == null) {
                    for (Field field : aClass.getDeclaredFields()) {
                        if (field.getType() == ColorStateList.class) {
                            o = field;
                            o.setAccessible(true);
                            break;
                        }
                    }
                }
                ColorStateList colorStateList = (ColorStateList) o.get(obj);
                Method getColors = colorStateList.getClass().getMethod("getColors");
                getColors.setAccessible(true);
                int[] colors = (int[]) getColors.invoke(colorStateList);
                int color = colors[0];
                if (color == Color.BLACK) {
                    aClass.getMethod("setTextColor", int.class).invoke(obj, Color.WHITE);
                }
            }
        });
        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
            private static Field o;

            @SuppressLint("SoonBlockedPrivateApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object obj = param.thisObject;
                if (o == null) {
                    o = TextView.class.getDeclaredField("mTextColor");
                    o.setAccessible(true);
                }
                ColorStateList colorStateList = (ColorStateList) o.get(obj);
                Method getColors = colorStateList.getClass().getMethod("getColors");
                getColors.setAccessible(true);
                int[] colors = (int[]) getColors.invoke(colorStateList);
                int color = colors[0];
                if (color == Color.BLACK) {
                    TextView.class.getMethod("setTextColor", int.class).invoke(obj, Color.WHITE);
                }
            }
        });

        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
            private static Field o;

            @SuppressLint("SoonBlockedPrivateApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TextView obj = (TextView) param.thisObject;
                if (o == null) {
                    o = TextView.class.getDeclaredField("mTextColor");
                    o.setAccessible(true);
                }
                ColorStateList colorStateList = (ColorStateList) o.get(obj);
                Method getColors = colorStateList.getClass().getMethod("getColors");
                getColors.setAccessible(true);
                int[] colors = (int[]) getColors.invoke(colorStateList);
                int color = colors[0];
                if (color == Color.BLACK) {
                    obj.setTextColor(Color.WHITE);
                }
            }
        });

//        ReflectUtil.loggingCalledMethod(TAG, aClass);
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

    private static void reverseColor(View view) {
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix(new float[]{
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f});
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    private static void reverseDrawableColor(Drawable drawable) {
        ColorMatrix cm = new ColorMatrix(new float[]{
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f});
        drawable.setColorFilter(new ColorMatrixColorFilter(cm));
    }

    private static int reverseDrawableColor(int colorInt) {
        int a = (colorInt >> 24 & 0xff);
        int r = 255 - ((colorInt >> 16) & 0xff);
        int g = 255 - ((colorInt >> 8) & 0xff);
        int b = 255 - (colorInt & 0xff);
        return Color.argb(a, r, g, b);
    }

    private static int[] resolveRGB(int colorInt) {
        int a = (colorInt >> 24 & 0xff);
        int r = 255 - ((colorInt >> 16) & 0xff);
        int g = 255 - ((colorInt >> 8) & 0xff);
        int b = 255 - (colorInt & 0xff);
        return new int[]{a, r, g, b};
    }
}
