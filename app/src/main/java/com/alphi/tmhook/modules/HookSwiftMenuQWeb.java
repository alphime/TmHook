package com.alphi.tmhook.modules;



import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * IDEA 2022/1/15
 */

public class HookSwiftMenuQWeb {
    private static ClassLoader classLoader;
    private final String TAG = this.getClass().getSimpleName();

    public HookSwiftMenuQWeb() {
        commonVersion();
    }

    public static void hook(ClassLoader classLoader) {
        HookSwiftMenuQWeb.classLoader = classLoader;
        new HookSwiftMenuQWeb();
    }

    private boolean commonVersion() {
        Class<?> aClass = findClass(classLoader, "Lcom/tencent/mobileqq/webview/swift/component/SwiftBrowserShareMenuHandler;");
        if (aClass == null)
            return false;
        boolean isHook = false;
        for (Method method : aClass.getDeclaredMethods()) {
            if (method.getReturnType() == void.class && Modifier.isPublic(method.getModifiers())) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length >= 2 && parameterTypes[1] == long.class && !parameterTypes[0].isPrimitive()) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // i&512==0 menuItem:openWithQQBrowser
                            if (((long) param.args[1] & 512) == 0) {
                                param.args[1] = (long) param.args[1] + 512;
                            }
                        }
                    });
                    isHook = true;
                }
            }
        }
        return isHook;
    }

}
