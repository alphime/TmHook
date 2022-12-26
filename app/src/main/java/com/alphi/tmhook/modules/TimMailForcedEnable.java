package com.alphi.tmhook.modules;

/*
    author: alphi
    createDate: 2022/12/26
*/

import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class TimMailForcedEnable {
    private Class<?> auffa;

    private TimMailForcedEnable() {
        super();
    }

    public static void hook(ClassLoader classLoader) {
        new TimMailForcedEnable().fixMailModule(classLoader);
    }

    private void fixMailModule(ClassLoader classLoader) {
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

}