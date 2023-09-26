package com.alphi.tmhook;

/*
    author: alphi
    createDate: 2022/11/5
*/

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alphi.tmhook.modules.HookSwiftMenuQWeb;
import com.alphi.tmhook.modules.HookWebSecurity;
import com.alphi.tmhook.modules.IQQLevelShowing;
import com.alphi.tmhook.modules.LebaMovedForQzonePlug;
import com.alphi.tmhook.modules.PhotoFixRound;
import com.alphi.tmhook.modules.TimMailForcedActivate;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private ClassLoader classLoader;
    private final String pkgName = "com.tencent.tim";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(pkgName)) {
            if (classLoader == null) {
                classLoader = loadPackageParam.classLoader;
            }

//  ===================================================================================
            PhotoFixRound.hook(classLoader);
            IQQLevelShowing.hook(classLoader);
            TimMailForcedActivate.hook(classLoader);
            LebaMovedForQzonePlug.hook(classLoader);
            HookSwiftMenuQWeb.hook(classLoader);
            HookWebSecurity.hook(classLoader);
            if (Build.DEVICE.equals("markw") || Build.DEVICE.equalsIgnoreCase("alioth")) {
//                ViewHook.hook(classLoader);
            }
        }
    }

    private TextView newTextView(Context context, String str) {
        TextView textView = new TextView(context);
        textView.setText(str);
        return textView;
    }


    public static Toast newImageToast(Context context, Drawable... drawables) {
        Toast toast = new Toast(context);
        LinearLayout layout = new LinearLayout(context);
        for (Drawable drawable : drawables) {
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(drawable);
            layout.addView(imageView);
        }
        toast.setView(layout);
        return toast;
    }
}
