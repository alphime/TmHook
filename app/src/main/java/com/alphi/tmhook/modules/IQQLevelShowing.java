package com.alphi.tmhook.modules;

/*
    author: alphi
    createDate: 2022/12/26
*/

import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import android.widget.BaseAdapter;

import com.alphi.tmhook.utils.MLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class IQQLevelShowing {
    private Class<?> aszk;

    private IQQLevelShowing() {
        super();
    }


    /**
     * hook好友名片，添加QQ等级显示
     */
    public static void hook(ClassLoader classLoader) {
        new IQQLevelShowing().hookShowIQQLevel(classLoader);
    }

    private void hookShowIQQLevel(ClassLoader classLoader) {
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
                        List<Object> list = (List<Object>) param.args[0];
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

}
