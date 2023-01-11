package com.alphi.tmhook.modules;

import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import android.util.Log;

import com.alphi.tmhook.utils.MLog;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * IDEA 2022/1/15
 */

public final class HookSwiftMenuQWeb {
    private final String TAG = "qhmk5";

    /**
     * 移除内置浏览器菜单的”QQ浏览器打开“选项
     */
    public static void hook(ClassLoader classLoader) {
        new HookSwiftMenuQWeb(classLoader);
    }

    private HookSwiftMenuQWeb(ClassLoader classLoader) {
        Class<?> h5PluginClass = findClass(classLoader, "Lcom/tencent/biz/pubaccount/util/PublicAccountH5AbilityPlugin;");
        Class<?> qShareUtilsClass = findClass(classLoader, "Lcom/tencent/biz/pubaccount/readinjoy/viola/modules/QShareUtils");
        try {
            if (h5PluginClass == null) {
                MLog.e(TAG, "HookSwiftMenuQWeb: 加载出错; not found Class<PublicAccountH5AbilityPlugin>");
            } else {
                for (Method m : h5PluginClass.getDeclaredMethods()) {
                    if (m.getReturnType() == boolean.class && Arrays.equals(m.getParameterTypes(), new Class<?>[]{ArrayList.class, String.class}) && Modifier.isStatic(m.getModifiers())) {
                        XC_MethodHook xc_methodHook = new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                try {
                                    if (param.args[1].equals("menuItem:openWithQQBrowser")) {
                                        param.setResult(true);
                                        Log.d(TAG, "remove: success");
                                    }
                                } catch (Exception e) {
                                    MLog.e(TAG, "beforeHookedMethod: ", e);
                                }
                            }
                        };
                        XposedBridge.hookMethod(m, xc_methodHook);
                    }
                }
            }
            if (qShareUtilsClass == null) {
                MLog.e(TAG, "HookSwiftMenuQShareUtils: 加载错误; not found Class<QShareUtils>");
            } else {
                for (Method m : qShareUtilsClass.getDeclaredMethods()) {
                    if (m.getReturnType() == List[].class && Arrays.equals(m.getParameterTypes(), new Class<?>[]{boolean.class, Set.class}) && Modifier.isStatic(m.getModifiers())) {
                        XC_MethodHook xc_methodHook = new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                try {
                                    Set<?> set = (Set<?>) param.args[1];
                                    set.remove("menuItem:openWithQQBrowser");
                                    Log.d(TAG, "beforeHookedMethod: " + Arrays.toString(set.toArray()));
                                } catch (Exception e) {
                                    MLog.e(TAG, "HookSwiftMenuQWeb: ", e);
                                }
                            }
                        };
                        XposedBridge.hookMethod(m, xc_methodHook);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MLog.e(TAG, "HookSwiftMenuQWeb: ", e);
        }
    }
}
