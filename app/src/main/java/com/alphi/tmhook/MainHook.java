package com.alphi.tmhook;

/*
    author: alphi
    createDate: 2022/11/5
*/

import static com.alphi.tmhook.ReflectUtil.findClass;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private Method qi_Mtd;
    private Class<?> aahs;
    private ClassLoader classLoader;
    private Class<?> yyr;
    private Class<?> aszk;
    private Class<?> auffa;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals("com.tencent.tim")) {
            if (classLoader == null) {
                classLoader = loadPackageParam.classLoader;
            }
            hookFriendPhotos();

//  ===================================================================================
            hookMsgListForService();
            hookDeviceListFragment();

            hookShowIQQLevel();
            fixMailModule();
            swapTimLeba();
            new HookSwiftMenuQWeb(classLoader);
        }
    }

    private void swapTimLeba() {
        Class<?> leba = findClass(classLoader, "com.tencent.mobileqq.activity.tim.timme.TimLeba");
        if (leba == null) {
            MLog.e("TimLeba", "not found TimLeba");
            return;
        }
        for (Field field : leba.getDeclaredFields()) {
            Class<?> abav = field.getType();
            Class<?> superclass = abav.getSuperclass();
            if (superclass == null)
                continue;
            if (superclass.getSimpleName().contains("GridListView") || BaseAdapter.class.isAssignableFrom(abav)) {
                MLog.i("aumc", "step for found GridListView");
                for (Method method : abav.getMethods()) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0] == List.class) {
                        MLog.i("aumc", "step for found GridListView's dataList");
                        XposedBridge.hookMethod(method, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                List list = (List) param.args[0];
                                Collections.swap(list, 7, 8);
                            }
                        });
                        break;
                    }
                }
                break;
            }
        }
    }

    private void fixMailModule() {
//        XposedHelpers.findAndHookMethod("com.tencent.tim.activity.TimLebaListMgrActivity", classLoader, "doOnCreate", Bundle.class,
//        new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.d("ataw", "load");
//                Object thisObject = param.thisObject;
//                Field[] fields = thisObject.getClass().getDeclaredFields();
//                for (Field field : fields) {
//                    field.setAccessible(true);
//                    Log.d("ataw", field.getName() + ": " + field.get(thisObject));
//                }
//            }
//        });
        // 以上非根源，为垃圾推理，最终在于QQAppInterface.ek()Ljava/util/List => Laulz！
        Class<?> clazz = findClass(classLoader, "com.tencent.mobileqq.activity.aio.item.StructingMsgItemBuilder");
        if (clazz == null) {
            clazz = findClass(classLoader, "com.tencent.tim.mail.MailPluginPreload");
            if (clazz == null) {
                XposedBridge.log("mailHookInit-err!: not found MailPluginPreload and StructingMsgItemBuilder");
                return;
            }
        }
        if (auffa == null) {
            for (Field field : clazz.getDeclaredFields()) {
                Class<?> aClass = field.getType();
                if (aClass.isPrimitive())
                    continue;
                if (aClass.getFields().length == 1) {
                    Method[] methods = aClass.getDeclaredMethods();
                    if (methods.length == 2) {
                        int found = 0;
                        for (Method method : methods) {
                            if (method.getReturnType() == aClass && Modifier.isStatic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isArray() || method.getReturnType() == boolean.class) {
                                found ++;
                            }
                        }
                        if (found == 2) {
                            auffa = aClass;
                            break;
                        }
                    }
                }
            }
        }
        if (auffa == null) {
            XposedBridge.log("mailHookInit-err!: not found auff$a");
            return;
        }
        XposedHelpers.findAndHookMethod(auffa, "isEnable", new XC_MethodReplacement(5) {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return true;
            }
        });
    }

    private void hookShowIQQLevel() {
        String tag = "hILevel";
        String IQQLevelContent = "Lv.%d";
        Class<?> mCardClass = findClass(classLoader, "com.tencent.mobileqq.data.Card");
        if (mCardClass == null) {
            MLog.e(tag, "not found card class");
            return;
        }
//        for (Method method : mCardClass.getDeclaredMethods()) {
//            XposedBridge.hookMethod(method, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    MLog.d(tag, method.toString());        // getPersonalityLabel 一次
//                    Object thisObject = param.thisObject;
//                    Class<?> thisObjectClass = thisObject.getClass();
//                    String uin = (String) thisObjectClass.getField("uin").get(thisObject);
//                    Integer iQQLevel = (Integer) thisObjectClass.getField("iQQLevel").get(thisObject);
//                    Log.d(tag, uin + ": " + iQQLevel);
//                }
//            });
//        }
        HashMap<String, Integer> iQQLevelMap = new HashMap<>();
        XposedHelpers.findAndHookMethod(mCardClass, "setPhotoShowFlag", boolean.class, new XC_MethodHook(60) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object thisObject = param.thisObject;
                Class<?> thisObjectClass = thisObject.getClass();
                String uin = (String) thisObjectClass.getField("uin").get(thisObject);
                Integer iQQLevel = (Integer) thisObjectClass.getField("iQQLevel").get(thisObject);
//                Log.d(tag, uin + ": " + iQQLevel);
                iQQLevelMap.put(uin, iQQLevel);
            }
        });

        if (aszk == null) {
            Class<?> mBaseProfileFmClass = findClass(classLoader, "com.tencent.tim.activity.profile.BaseProfileFragment");
            for (Field field : mBaseProfileFmClass.getDeclaredFields()) {
                Class<?> typeClass = field.getType();
                if (BaseAdapter.class.isAssignableFrom(typeClass)) {
                    aszk = typeClass;
                }
            }
        }
        XposedHelpers.findAndHookMethod(aszk, "setDataList", List.class,
                new XC_MethodHook() {

                    private Object asznObjTemp;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        List list = (List) param.args[0];
                        if (list.contains(asznObjTemp))
                            return;
                        String uin = null;
                        Object obj = null;
                        for (Object o : list) {
                            Class<?> clazz = o.getClass();
                            String name = (String) clazz.getDeclaredField("name").get(o);
                            if (name.endsWith("账号")) {
                                uin = (String) clazz.getDeclaredField("content").get(o);
                                obj = o;
                            }
                        }
                        Constructor<?> constructor = obj.getClass().
                                getDeclaredConstructor(String.class, String.class, boolean.class, int.class, int.class, int.class);
                        constructor.setAccessible(true);
                        Integer qLevel = null;
                        for (int i = 0; i < 10 || (qLevel != null && qLevel == 0); i++) {
                            qLevel = iQQLevelMap.get(uin);
                        }
                        if (qLevel == null) {
                            MLog.w("qLevel", "qLevelField value is null!");
                            return;
                        }
                        asznObjTemp = constructor.newInstance("QQ等级", String.format(Locale.ENGLISH, IQQLevelContent, qLevel), false, 0, 21, 1);
                        list.add(asznObjTemp);
                    }
                });
    }

    private void hookDeviceListFragment() {
        final String TAG = "yyr";
        if (yyr == null) {
            Class<?> clazz = findClass(classLoader, "com.tencent.mobileqq.activity.contacts.device.DeviceFragment");
            if (clazz == null) {
                MLog.e(TAG, "not found DeviceFragment class");
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
                            param.setResult(layout);
//                            Log.d(TAG, "success");
                        } else {
                            MLog.e(TAG, layout.toString() + ": layout is null");
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
                Class<?> clazz1 = findClass(classLoader, claName);
                if (clazz1 == null) {
                    MLog.e(TAG, "not found auxiliary class");
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
                        Object thisObject = param.thisObject;
                        Object getItem = thisObject.getClass().getDeclaredMethod("getItem", int.class).invoke(thisObject, i0);
//                            MLog.d("aahs", getItem.toString());
                        Class<?> clazz = getItem.getClass();
                        // 排除群消息
                        if (!clazz.getSimpleName().equals("RecentItemTroopMsgData")) {
                            LinearLayout layout = (LinearLayout) param.getResult();
                            if (layout != null) {
//                                    MLog.d(TAG, "getItem: " + getItem.toString());
                                ergodicImageView(layout, false);
                                param.setResult(layout);
                            } else {
                                MLog.e(TAG, layout.toString() + ": layout is null");
                            }
                        }
                    }
                });
    }

    private void hookFriendPhotos() {
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
            MLog.e("QQFaceRoundHook", "not found class err");
            return;
        }

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
            MLog.e("QQFaceRoundHook", "没有找到方法!");
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
                            imageView.setBackground(new BitmapDrawable(imageView.getContext().getResources(), cutRound(mImageViewBackground)));
                    }
                    if (drawable != null && (drawable.getClass() != BitmapDrawable.class)) {
                        imageView.setImageBitmap(cutRound(imageView.getDrawable()));
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
