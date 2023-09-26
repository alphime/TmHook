package com.alphi.tmhook.modules;

import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * IDEA 2022/1/4
 */

public class HookWebSecurity {
    private final String TAG = this.getClass().getSimpleName();

    public static void hook(ClassLoader classLoader) {
        new HookWebSecurity(classLoader);
    }

    private HookWebSecurity(ClassLoader classLoader) {
        Class<?> aClass = findClass(classLoader, "com.tencent.mobileqq.webprocess.WebAccelerateHelper$CommonJsPluginFactory");
        if (aClass == null) {
            Class<?> aClassTemp = findClass(classLoader, "com.tencent.mobileqq.webprocess.WebAccelerateHelper");
            if (aClassTemp == null) {
                XposedBridge.log("HookWebSecurity: 加载失败，未找到类 WebAccelerateHelper");
                return;
            }
            Method[] methods = aClassTemp.getDeclaredMethods();
            for (Method m : methods) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                for (Class<?> ct : parameterTypes) {
                    if (ct.getSimpleName().contains("CommonJsPluginFactory")) {
                        aClass = ct;
                        break;
                    }
                }
            }
        }

        for (Method m : aClass.getDeclaredMethods()) {
            if (m.getReturnType() == List.class) {
                Log.d(TAG, "HookWebSecurity: a1");
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        ArrayList array = (ArrayList) param.getResult();
                        ArrayList arrayTemp = (ArrayList) array.clone();
                        for (Object obj : arrayTemp) {
                            Class<?> mPlugin = obj.getClass();
                            try {
                                String mPluginNameSpace = (String) mPlugin.getField("mPluginNameSpace").get(obj);
                                if (mPluginNameSpace.equals("forceHttps") || mPluginNameSpace.contains("UrlSaveVerify")
                                        || mPluginNameSpace.equals("Webso") || mPluginNameSpace.contains("Report")) {
                                    array.remove(obj);
                                    Log.d(TAG, "WebSec: rm-" + mPlugin.getName());
                                    continue;
                                }
                                Method[] methods = mPlugin.getDeclaredMethods();
                                for (Method m : methods) {
                                    Class<?>[] parameterTypes = m.getParameterTypes();
                                    if (parameterTypes.length > 0) {
                                        Class<?> parameterType = parameterTypes[0];
                                        if (parameterType.getSimpleName().equals("MessageRecord")) {
                                            array.remove(obj);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }
                        param.setResult(array);
                    }
                });
                break;
            }
        }

        Class<?> AbsWebViewClass = findClass(classLoader, "com.tencent.mobileqq.webview.AbsWebView");
        Method method_bindAllJavaScript;
        try {
            method_bindAllJavaScript = AbsWebViewClass.getDeclaredMethod("bindAllJavaScript");
        } catch (NoSuchMethodException e) {
            try {
                method_bindAllJavaScript = AbsWebViewClass.getDeclaredMethod("bindBaseJavaScript");
            } catch (NoSuchMethodException ex) {
                e.addSuppressed(ex);
                XposedBridge.log(e);
                return;
            }
        }

        XposedBridge.hookMethod(method_bindAllJavaScript, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "afterHookedMethod: a2");
                Field mPluginListField = AbsWebViewClass.getField("mPluginList");
                ArrayList mPluginList = (ArrayList) mPluginListField.get(AbsWebViewClass);
                ArrayList arrayTemp = (ArrayList) mPluginList.clone();
                for (Object o : arrayTemp) {
                    Class<?> mPlugin = o.getClass();
                    String mPluginNameSpace = (String) mPlugin.getField("mPluginNameSpace").get(o);
                    if (mPluginNameSpace.contains("UrlSaveVerify") || mPluginNameSpace.equals("Webso")
                            || mPluginNameSpace.contains("Report")) {
                        mPluginList.remove(o);
                        Log.d(TAG, "WebSec: rm-" + mPlugin.getName());
                        continue;
                    }
                    Method[] methods = mPlugin.getMethods();
                    for (Method m : methods) {
                        Class<?>[] parameterTypes = m.getParameterTypes();
                        if (parameterTypes.length > 0) {
                            Class<?> parameterType = parameterTypes[0];
                            if (parameterType.getSimpleName().equals("MessageRecord")) {
                                mPluginList.remove(o);
                                Log.d(TAG, "HookWebSecurity: hooked!");
                            }
                        }
                    }
                }
                mPluginListField.set(AbsWebViewClass, mPluginList);
            }
        });

        Log.d(TAG, "HookWebSecurity: success!");
    }
}
