package com.alphi.tmhook.utils;

/*
    author: alphi
    createDate: 2022/11/27
*/

import android.util.Log;

import org.springframework.lang.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;

public class ReflectUtil {
    public static Class<?> findClass(ClassLoader classLoader, @NonNull String... classNames) {
        List<String> errList = new ArrayList<>();
        for (String className: classNames) {
            className = className.trim();
            if (className.charAt(0) == 'L') className = className.substring(1);
            String str = className.replaceAll("/", ".").replaceAll(";", "");
            Class<?> classIfExists = XposedHelpers.findClassIfExists(str, classLoader);
            if (classIfExists == null) {
                for (int i=1; i <= 6; i++) {
                    Class<?> aClassTemp = XposedHelpers.findClassIfExists(str + "$" + i, classLoader);
                    if (aClassTemp != null) {
                        Constructor<?>[] constructors = aClassTemp.getConstructors();
                        if (constructors.length > 0) classIfExists = constructors[0].getParameterTypes()[0];
                        if (classIfExists != null) break;
                    }
                }
                errList.add(str.substring(str.lastIndexOf(".")+1));
            }
            return classIfExists;
        }
        Log.e("loadClass", "不存在类: " + Arrays.toString(errList.toArray()));
        return null;
    }

    /**
     * 打印变量信息
     * @param tag 标签
     * @param obj obj对象
     * @param containClazz 过滤obj对象包含的子对象的变量类型
     * @throws IllegalAccessException 如果获取子对象出现错误则抛出
     */
    public static void loggingField(String tag, Object obj, Class<?>... containClazz) throws IllegalAccessException {
        if (obj == null) {
            Log.d(tag, "obj is null");
            return;
        }
        Log.d(tag, "-----" + "logging-obj-- '" + obj + "' -------");
        Field[] fields = obj.getClass().getDeclaredFields();
        int cLength = containClazz.length;
        for (Field field : fields) {
            boolean boo = true;
            Class<?> type = field.getType();
            if (cLength > 0) {
                boo = false;
                for (Class<?> clazz : containClazz) {
                    if (clazz == type) {
                        boo = true;
                        break;
                    }
                }
            }
            if (boo) {
                field.setAccessible(true);
                Log.d(tag, field.getName() + ": " + field.get(obj));
            }
        }
    }
}
