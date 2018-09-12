package com.centerm.dev.barcode.decode;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.centerm.lib.BitmapLuminanceSource;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 3/1/18.1:51 PM
 */

public class LuminanceDetector {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Handler mDetector;
    private boolean mBusy;
    private OnLuminanceChangedListener mListener;
    private int mAverage;

    private int mLastLumi;
    //防止图像闪动
    private int mCount ;
    private static final int PIC_COUNT = 10;

    public interface OnLuminanceChangedListener {
        void onLuminanceHigh();

        void onLuminanceLow();
    }

    public LuminanceDetector(int avarage) {
        mAverage = avarage;
        HandlerThread handlerThread = new HandlerThread("luminance_thread");
        handlerThread.start();
        mDetector = new Handler(handlerThread.getLooper());
    }

    public void setListener(OnLuminanceChangedListener l) {
        mListener = l;
    }

    public synchronized void handle(final Bitmap bitmap) {
        if (mBusy) return;
        mBusy = true;
        mDetector.post(new Runnable() {
            @Override
            public void run() {
                int luminance = getBright(bitmap);
                calculator(luminance);
                mBusy = false;
            }
        });
    }

    public synchronized void handle(final byte[] yuv, final int width, final int height) {
        if (mBusy) return;
        mBusy = true;
        mDetector.post(new Runnable() {
            @Override
            public void run() {
                int luminance = getBright(yuv, width, height);
                calculator(luminance);
                mBusy = false;
            }
        });
    }
    private void calculator(int luminance) {
        if (luminance == 0) {
            //something invalid
            return;
        }
        if (luminance <= mAverage) {
            if (mLastLumi <= mAverage){
                mCount ++;
            } else {
                mCount = 1;
            }
            if (mCount > PIC_COUNT) {
                notifyLightNotEnough();
            }
        } else {
            if (mLastLumi > mAverage){
                mCount ++;
            } else {
                mCount = 1;
            }
            if (mCount > PIC_COUNT) {
                notifyLightEnough();
            }
        }
        mLastLumi = luminance;
    }

    private void notifyLightNotEnough() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onLuminanceLow();
            }
        });
    }

    private void notifyLightEnough() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onLuminanceHigh();
            }
        });
    }



    private int getBright(Bitmap bm) {
        BitmapLuminanceSource bright = new BitmapLuminanceSource(bm, 0, 0, bm.getWidth(), bm.getHeight());
        return getBright(bright.getMatrix(), bm.getWidth(), bm.getHeight());
    }

    private int getBright(byte[] data, int width, int height) {
        int count = 0;
        int bright = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bright += (data[i * width + j] & 0xff);
                count++;
            }
        }
        return bright / count;
    }


    public void release() {
        if (mDetector != null) {
            mDetector.getLooper().quit();
            mDetector = null;
        }
    }
    public void reset() {
        mLastLumi = 255;
        mCount = 0;
        mBusy = false;
    }

}
