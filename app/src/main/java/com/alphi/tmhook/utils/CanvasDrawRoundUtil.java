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
import android.util.Log;

public final class CanvasDrawRoundUtil {
    public static Bitmap cutRound(Drawable source) {
        Bitmap bitmap = drawableConvertBitmap(source);
        if (bitmap == null)
            return null;
        return cutRound(bitmap);
    }

    public static Bitmap cutRound(Drawable source, int r) {
        Bitmap bitmap = drawableConvertBitmap(source);
        return cutRound(bitmap, r);
    }

    public static Bitmap cutRound(Bitmap source) {
        int r = source.getWidth() / 2;
        int v = (int) ((r * 1.4 - r));
        int pixel = source.getPixel(v, v);
        if (pixel == 0) {
            int v2 = 2 * r - v;
            pixel = source.getPixel(v2, v);
            if (pixel == 0) {
                pixel = source.getPixel(v2, v2);
                if (pixel == 0) {
//                    Log.d("CanvasDrawRoundUtil", "cutRound: ignore");
                    return source;
                }
            }
        }
        return cutRound(source, r);
    }

    public static Bitmap cutRound(Bitmap source, int r) {
        if (source == null)
            return null;
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawCircle(width / 2, height / 2, r, paint);

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

    public static Bitmap drawableConvertBitmap(Drawable source) {
        if (source == null) {
            Log.e("cutRound()", "drawable is null!");
            return null;
        }
        Bitmap bitmapSource;
        if (source instanceof BitmapDrawable) {
            bitmapSource = ((BitmapDrawable) source).getBitmap();
        } else {
            int width = source.getIntrinsicWidth();
            if (width <= 0) {
                MLog.e("cutRound()", "c-type: " + source.getClass() + ", iw=" + source.getIntrinsicWidth()
                        + ", ih=" + source.getIntrinsicHeight() + ", mw=" + source.getMinimumWidth()
                        + ", mh=" + source.getMinimumHeight());
                return null;
            }
            Bitmap outBitmap = Bitmap.createBitmap(width, source.getIntrinsicWidth(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(outBitmap);
            source.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            source.draw(canvas);
            bitmapSource = outBitmap;
        }
        return bitmapSource;
    }
}
