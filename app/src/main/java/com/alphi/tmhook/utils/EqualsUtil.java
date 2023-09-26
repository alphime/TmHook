package com.alphi.tmhook.utils;

/*
    author: alphi
    createDate: 2023/5/19
*/

public class EqualsUtil {
    public static boolean isClassEndName(Class<?> clazz, String... endNames) {
        String simpleName = clazz.getSimpleName();
        for (String endName : endNames) {
            if (simpleName.endsWith(endName))
                return true;
        }
        return false;
    }

    public static boolean isEqualsInts(int main, int... equalInts) {
        for (int equalInt : equalInts) {
            if (main == equalInt)
                return true;
        }
        return false;
    }

    public static boolean isEqualsStr(String main, String ... strs) {
        for (String str : strs) {
            if (main.equals(str))
                return true;
        }
        return false;
    }
}
