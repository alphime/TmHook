package com.alphi.tmhook.modules;

/*
    author: alphi
    createDate: 2022/12/26
*/

import static com.alphi.tmhook.utils.CanvasDrawRoundUtil.cutRound;
import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alphi.tmhook.utils.MLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class PhotoFixRound {
    private Method qi_Mtd;
    private ClassLoader classLoader;
    private Class<?> aahs;
    private Class<?> yyr;
    private Method shareActionSheetBuilderB;

    private PhotoFixRound() {
        super();
    }

    /**
     * 裁剪好友头像为圆头像
     */
    public static void hook(ClassLoader classLoader) {
        PhotoFixRound p = new PhotoFixRound();
        p.classLoader = classLoader;
        p.hookMsgList();
        p.hookCacheMapFromNet();
        p.hookDeviceListFragment();
        p.hookShareActionMenu();
    }

    /**
     * hook好友头像缓存
     */
    private void hookCacheMapFromNet() {
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
//                MLog.d(Tag, aqgg.toString());
//                XposedHelpers.findAndHookMethod(aqgg, "s", new XC_MethodHook() {
//                            @Override
//                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                                Log.d(Tag, "beforeHookedMethod: hook");
//                                MLog.d(Tag, "beforeHookedMethod: " + param.method);
//                                Bitmap bitmap = (Bitmap) param.getResult();
//                                bitmap = cutRound(bitmap);
//                                param.setResult(bitmap);
//                            }
//                        });
//            } catch (Exception e) {
//                MLog.e(Tag, "err", e);
//            }
//                        }
//                    });

//  标注：以上方法废弃！

        // hook 缓存
        Class<?> clazz = findClass(classLoader, "com.tencent.mobileqq.app.QQAppInterface");
        if (clazz == null) {
            MLog.e("QQFaceRoundHook", "not found Class<QQAppInterface>");
            return;
        }

        XposedHelpers.findAndHookMethod(clazz,
                "putBitmapToCache", String.class, Bitmap.class, byte.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
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
            MLog.e("QQFaceRoundHook", "没有找到方法!");
        }
    }

    /**
     * 对全局消息列表Hook
     * 主要针对软件自带的图标，例如群助手、关联账号、邮件提醒图标等等。。。
     * 但是热启动不一定生效，冷启动还是有效果的。。。
     */
    private void hookMsgList() {
        final String TAG = "aahs";
        String[] claNames = {"com.tencent.mobileqq.confess.BaseMsgListFragment", "com.tencent.mobileqq.activity.Conversation",
                "com.tencent.biz.pubaccount.ecshopassit.EcshopUtils$1", "com.tencent.mobileqq.activity.TroopAssistantActivity",
                "com.tencent.mobileqq.apollo.activity.HotChatCenterFragment", "com.tencent.mobileqq.app.hiddenchat.HiddenChatFragment"
                , "BaseMsgBoxActivity"};
        if (aahs == null) {
            F1:
            for (String claName : claNames) {
                Class<?> clazz1 = findClass(classLoader, claName);
                if (clazz1 == null) {
                    MLog.e(TAG, "not found Class<Auxiliary>");
                    return;
                }
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
                new XC_MethodHook(80) {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int i0 = (int) param.args[0];
                        BaseAdapter baseAdapter = (BaseAdapter) param.thisObject;
                        Object getItem = baseAdapter.getClass().getDeclaredMethod("getItem", int.class).invoke(baseAdapter, i0);
//                            MLog.d("aahs", getItem.toString());
                        assert getItem != null;
                        Class<?> clazz = getItem.getClass();
                        // 排除群消息
                        if (!clazz.getSimpleName().equals("RecentItemTroopMsgData")) {
                            LinearLayout layout = (LinearLayout) param.getResult();
                            if (layout != null) {
//                                    MLog.d(TAG, "getItem: " + getItem.toString());
                                ergodicImageView(layout, false);
                            } else {
                                MLog.e(TAG, layout.toString() + ": layout is null");
                            }
                        }
                    }
                });
    }

    /**
     * Hook 设备列表，比如我的电脑图标或者我的平板等等。。。
     */
    private void hookDeviceListFragment() {
        final String TAG = "yyr";
        if (yyr == null) {
            Class<?> clazz = findClass(classLoader, "com.tencent.mobileqq.activity.contacts.device.DeviceFragment");
            if (clazz == null) {
                MLog.e(TAG, "not found Class<DeviceFragment>");
                return;
            }
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> clazz2 = field.getType();
                if (BaseAdapter.class.isAssignableFrom(clazz2)) {
                    yyr = clazz2;
                    break;
                }
            }
        }
        XposedHelpers.findAndHookMethod(yyr,
                "getView", int.class, View.class, ViewGroup.class,
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        MLog.d(TAG, param.getResult().toString());
                        FrameLayout layout = (FrameLayout) param.getResult();
                        if (layout != null) {
                            ergodicImageView(layout, true);
//                            Log.d(TAG, "success");
                        } else {
                            MLog.e(TAG, layout.toString() + ": layout is null");
                        }
                    }
                });
    }


    private void hookShareActionMenu() {
        Class<?> aClass = XposedHelpers.findClassIfExists("com.tencent.mobileqq.widget.share.ShareActionSheetV2$a", classLoader);
        if (!BaseAdapter.class.isAssignableFrom(aClass))
            aClass = null;
        if (aClass == null) {
            Class<?> aClass1 = XposedHelpers.findClassIfExists("com.tencent.mobileqq.widget.share.ShareActionSheetV2", classLoader);
            for (Field field : aClass1.getFields()) {
                Class<?> aClass2 = field.getType();
                if (BaseAdapter.class.isAssignableFrom(aClass2)) {
                    aClass = aClass2;
                }
                break;
            }
        }
        if (aClass == null) {
            MLog.e("hookShareActionMenu", "not found Class<ShareActionSheetV2$Adapter>");
            return;
        }
        // 有的不会生效，废弃
//        XposedHelpers.findAndHookMethod(aClass, "getView", int.class, View.class, ViewGroup.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                ViewGroup vg = (ViewGroup) param.getResult();
//                ImageView im = (ImageView) vg.getChildAt(0);
//                Drawable background = im.getBackground();
//                if (background != null) {
//                    background = new BitmapDrawable(cutRound(background));
//                    im.setBackground(background);
//                    BaseAdapter adapter = (BaseAdapter) param.thisObject;
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });

        if (shareActionSheetBuilderB == null) {
            for (Method method : aClass.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0].getName().contains("ShareActionSheetBuilder")) {
                        shareActionSheetBuilderB = method;
                        break;
                    }
                }
            }
        }

        if (shareActionSheetBuilderB != null)
            XposedBridge.hookMethod(shareActionSheetBuilderB, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    BaseAdapter adapter = (BaseAdapter) param.thisObject;
                    Object arg = param.args[0];
                    ImageView vIcon = (ImageView) arg.getClass().getField("vIcon").get(arg);
                    Drawable faceDrawable = vIcon.getBackground();
                    if (faceDrawable != null) {
                        BitmapDrawable drawableFixed = new BitmapDrawable(null, cutRound(faceDrawable));
                        vIcon.setBackground(drawableFixed);
                        // 加强处理，防止setBackground没有即刻刷新导致部分头像方形
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                vIcon.setBackground(drawableFixed);
                            }
                        });
                        adapter.notifyDataSetChanged();
                    } else
                        MLog.w("hookShareActionMenu:F-vIcon", "faceDrawable is null?!");
                }
            });
        else
            MLog.e("hookShareActionMenu", "not found Method<?(ShareActionSheetBuilder$B)>");

//        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.widget.share.ShareActionSheetV2", classLoader, "L",
//                new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        Object obj = param.thisObject;
//                        Field jq = obj.getClass().getDeclaredField("Jq");
//                        jq.setAccessible(true);
//                        List list = (List) jq.get(obj);
//                        for (Object o : list) {
//                            ReflectUtil.loggingField("kkk", o);
//                        }
//                    }
//                });
    }


    /**
     * 遍历 ViewGroup 并只设置 头个ImageView的图像
     * @param v ImageView的ViewGroup
     * @param fixImageBFG 同时修饰ImageView的背景
     */
    private void ergodicImageView(ViewGroup v, boolean fixImageBFG) {
        String TAG = "ergodicImageView";
        for (int i = 0; i < v.getChildCount(); i++) {
            View view = v.getChildAt(i);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
//                                    MLog.w(TAG, "e... " + v.toString());           // debug-2‘
                ergodicImageView(viewGroup, fixImageBFG);
            } else {
                if (view instanceof ImageView) {
                    ImageView imageView = (ImageView) view;
                    Drawable drawable = imageView.getDrawable();        // debug-2’
                    if (fixImageBFG) {
                        Drawable mImageViewBackground = imageView.getBackground();
                        if (mImageViewBackground != null)
                            imageView.setBackground(new BitmapDrawable(null, cutRound(mImageViewBackground)));
                    }
                    if (drawable != null) {
                        imageView.setImageBitmap(cutRound(imageView.getDrawable()));
                    }
                }
                break;
            }
        }
    }
}
