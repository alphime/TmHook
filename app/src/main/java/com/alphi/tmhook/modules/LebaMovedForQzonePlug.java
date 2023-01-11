package com.alphi.tmhook.modules;

/*
    author: alphi
    createDate: 2022/12/26
*/

import static com.alphi.tmhook.utils.ReflectUtil.findClass;

import android.widget.BaseAdapter;

import com.alphi.tmhook.utils.MLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class LebaMovedForQzonePlug {

    private LebaMovedForQzonePlug() {
        super();
    }

    /**
     * 调整“我”的功能模块，当选择全部(9个)显示时，QQ空间与工作区对调
     */
    public static void hook(ClassLoader classLoader) {
        new LebaMovedForQzonePlug().swapTimLeba(classLoader);
    }

    private void swapTimLeba(ClassLoader classLoader) {
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
                            protected void beforeHookedMethod(MethodHookParam param) {
                                List<?> list = (List<?>) param.args[0];
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

}
