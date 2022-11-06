package com.alphi.tmhook;

/*
    author: alphi
    createDate: 2022/11/5
*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private Method qi_Mtd;
    private Class<?> aahs;
    private ClassLoader classLoader;
    private Class<?> yyr;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals("com.tencent.tim")) {
            if (classLoader == null) {
                classLoader = loadPackageParam.classLoader;
            }
            hookFriendPhoto();

//  ===================================================================================
            hookMsgListForService();
            hookDeviceListFragment();
        }
    }

    private void hookDeviceListFragment() {
        final String TAG = "yyr";
        Class<?> clazz = XposedHelpers.findClass("com.tencent.mobileqq.activity.contacts.device.DeviceFragment", classLoader);
        if (yyr == null) {
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> clazz2 = field.getType();
                if (BaseAdapter.class.isAssignableFrom(clazz2)) {
                    yyr = clazz2;
                    break;
                }
            }
        }
        if (yyr == null) {
            XposedBridge.log(TAG + ": " + "Not Found BaseAdapt!");
            Log.e(TAG, "BaseAdapt is null");
            return;
        }
        XposedHelpers.findAndHookMethod(yyr,
                "getView", int.class, View.class, ViewGroup.class,
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d(TAG, param.getResult().toString());
                        FrameLayout layout = (FrameLayout) param.getResult();
                        if (layout != null) {
                            ergodicImageView(layout, true);
                            param.setResult(layout);
                            Log.d(TAG, "success");
                        } else {
                            Log.e(TAG, layout.toString() + ": layout is null");
                        }
                    }
                });
    }

    private void hookMsgListForService() {
        final String TAG = "aahs";
        String[] claNames = {"com.tencent.mobileqq.confess.BaseMsgListFragment", "com.tencent.mobileqq.activity.Conversation",
                "com.tencent.biz.pubaccount.ecshopassit.EcshopUtils$1", "com.tencent.mobileqq.activity.TroopAssistantActivity",
                "com.tencent.mobileqq.apollo.activity.HotChatCenterFragment", "com.tencent.mobileqq.app.hiddenchat.HiddenChatFragment"
                , "BaseMsgBoxActivity"};
        if (aahs == null) {
            F1:
            for (String claName : claNames) {
                Class<?> clazz1 = XposedHelpers.findClass(claName, classLoader);
                for (Field field : clazz1.getFields()) {
                    Class<?> type = field.getType();
                    if (type.getSuperclass() == BaseAdapter.class) {
                        aahs = type;
                        break F1;
                    }
                }
            }
        }
        XposedHelpers.findAndHookMethod(aahs,
                "getView", int.class, View.class, ViewGroup.class,
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int i0 = (int) param.args[0];
                        Object thisObject = param.thisObject;
                        Object getItem = thisObject.getClass().getDeclaredMethod("getItem", int.class).invoke(thisObject, i0);
//                            Log.d("aahs", getItem.toString());
                        Class<?> clazz = getItem.getClass();
                        // 排除群消息
                        if (!clazz.getSimpleName().equals("RecentItemTroopMsgData")) {
                            LinearLayout layout = (LinearLayout) param.getResult();
                            if (layout != null) {
//                                    Log.d(TAG, "getItem: " + getItem.toString());
                                ergodicImageView(layout, false);
                                param.setResult(layout);
                            } else {
                                Log.e(TAG, layout.toString() + ": layout is null");
                            }
                        }
                    }
                });
    }

    private void hookFriendPhoto() {
        //            XposedHelpers.findAndHookMethod("com.tencent.mobileqq.activity.ChatSettingForTroop", classLoader,
//                    "ax", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            Context context = (Context) param.thisObject;
//                            Toast toast = new Toast(context);
//                            LinearLayout layout = new LinearLayout(context);
//                            ImageView imageView = new ImageView(context);
////                            XposedHelpers.findAndHookMethod("aqbr", classLoader, "b", int.class, String.class, new XC_MethodHook() {
////                                @Override
////                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
////                                    Bitmap bitmap = (Bitmap) param.getResult();
////                                    imageView.setImageBitmap(bitmap);
////                                    layout.addView(imageView);
////                                    layout.addView(newTextView(context, String.valueOf((int)param.args[0])));
////                                    layout.addView(newTextView(context, (String) param.args[1]));
////                                    toast.setView(layout);
////                                    toast.show();
////                                }
////                            });
//                            XposedHelpers.findAndHookMethod("aqbt", classLoader, "a",
//                                    int.class, String.class, int.class, byte.class, new XC_MethodHook() {
//                                @Override
//                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                    Bitmap bitmap = (Bitmap) param.getResult();
//                                    bitmap = cutRound(bitmap);
////                                    imageView.setImageBitmap(bitmap);
////                                    layout.addView(imageView);
////                                    layout.addView(newTextView(context, String.valueOf((int)param.args[0])));
////                                    layout.addView(newTextView(context, (String) param.args[1]));
////                                    toast.setView(layout);
////                                    toast.show();
//                                    param.setResult(bitmap);
//                                }
//                            });

//  -------------------------------------------------------------------------------------------

//            String Tag = "ttt33";
//            try {
//                Class<?> aqgg = XposedHelpers.findClass("aqgg", classLoader);
//                aqgg = XposedHelpers.findClass(aqgg.getName(), null);
//                Log.d(Tag, aqgg.toString());
//                XposedHelpers.findAndHookMethod(aqgg, "s", new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                Log.d(Tag, "beforeHookedMethod: hook");
//                                Log.d(Tag, "beforeHookedMethod: " + param.method);
//                                Bitmap bitmap = (Bitmap) param.getResult();
//                                bitmap = cutRound(bitmap);
//                                param.setResult(bitmap);
//                            }
//                        });
//            } catch (Exception e) {
//                Log.e(Tag, "err", e);
//            }
//                        }
//                    });

//  标注：以上方法废弃！

        // hook 缓存
        Class<?> clazz = XposedHelpers.findClass("com.tencent.mobileqq.app.QQAppInterface", classLoader);
        XposedHelpers.findAndHookMethod(clazz,
                "putBitmapToCache", String.class, Bitmap.class, byte.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Bitmap bitmap = (Bitmap) param.args[1];
                        bitmap = cutRound(bitmap);
                        param.args[1] = bitmap;
                    }
                });

        // hook 刚下载的图片
        if (qi_Mtd == null) {
            Class<?>[] classes = {int.class, String.class, byte.class, int.class, boolean.class, byte.class, int.class};
            for (Method method : clazz.getDeclaredMethods()) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (Arrays.equals(parameterTypes, classes)) {
                    qi_Mtd = method;
                    break;
                }
            }
        }

        if (qi_Mtd != null) {
            XposedBridge.hookMethod(qi_Mtd, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Bitmap bitmap = (Bitmap) param.getResult();
                    bitmap = cutRound(bitmap);
                    param.setResult(bitmap);
                }
            });
        } else {
            Log.e("QQFaceRoundHook", "没有找到方法!");
        }
    }

    private TextView newTextView(Context context, String str) {
        TextView textView = new TextView(context);
        textView.setText(str);
        return textView;
    }

    private Bitmap cutRound(Drawable source) {
        if (source == null)
            return null;
        if (source instanceof BitmapDrawable) {
            return ((BitmapDrawable) source).getBitmap();
        }
        int width = source.getIntrinsicWidth();
        Bitmap outBitmap = Bitmap.createBitmap(width, source.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        source.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        source.draw(canvas);
        return cutRound(outBitmap);
    }

    private Bitmap cutRound(Bitmap source) {
        if (source == null)
            return null;
        long millis = System.currentTimeMillis();
        int width = source.getWidth();
        Bitmap outBitmap = Bitmap.createBitmap(width, source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int r = width / 2;
        canvas.drawCircle(r, r, r, paint);

        //设置图片相交情况下的处理方式
        //setXfermode：设置当绘制的图像出现相交情况时候的处理方式的,它包含的常用模式有：
        //PorterDuff.Mode.SRC_IN 取两层图像交集部分,只显示上层图像
        //PorterDuff.Mode.DST_IN 取两层图像交集部分,只显示下层图像
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //在画布上绘制bitmap
        canvas.drawBitmap(source, 0, 0, paint);
//        Log.w("convertRound", "r=" + r + "; " + (System.currentTimeMillis() - millis) + "ms");
        return outBitmap;
    }

    private void ergodicImageView(ViewGroup v, boolean fixImageBG) {
        String TAG = "ergodicImageView";
        for (int i = 0; i < v.getChildCount(); i++) {
            View view = v.getChildAt(i);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
//                                    Log.w(TAG, "e... " + v.toString());           // debug-2‘
                ergodicImageView(viewGroup, fixImageBG);
            } else {
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = imageView.getDrawable();        // debug-2’
                    if (drawable != null && (drawable.getClass() != BitmapDrawable.class)) {
                        if (fixImageBG) {
                            Drawable mImageViewBackground = imageView.getBackground();
                            if (mImageViewBackground != null)
                                imageView.setBackground(new BitmapDrawable(imageView.getContext().getResources(), cutRound(mImageViewBackground)));
                        }
                        imageView.setImageBitmap(cutRound(imageView.getDrawable()));
                    } else {
                        if (drawable == null) {
                            XposedBridge.log(TAG + "-obj!err: " + imageView + "; drawable is null");
                            Log.e(TAG, imageView.toString() + ": drawable is null");
                        } else if (drawable.getIntrinsicWidth() == 0) {
                            XposedBridge.log(TAG + "-obj!err: " + imageView + "; drawable's width=0");
                            Log.e(TAG, imageView.toString() + ": drawable's width=0");
                        }
                    }
                }
                break;
            }
        }
    }

    private Toast newImageToast(Context context, Bitmap bitmap) {
        Toast toast = new Toast(context);
        LinearLayout layout = new LinearLayout(context);
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(bitmap);
        layout.addView(imageView);
        toast.setView(layout);
        return toast;
    }
}
