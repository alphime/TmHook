package com.alphi.tmhook.utils;

/*
    author: alphi
    createDate: 2022/11/27
*/

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class CanvasDrawRoundUtil {
    public static Bitmap cutRound(Drawable source) {
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

    public static Bitmap cutRound(Bitmap source) {
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
}
