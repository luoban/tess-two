package com.centerm.lib;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import com.centerm.dev.barcode.camera.CameraManager;
import com.centerm.dev.barcode.decode.CaptureActivityHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 8/27/17.1:07 PM
 */

public abstract class AbsDecodeThread {
    private int width;
    private int height;
    private boolean mStoped = false;
    private CaptureActivityHandler.DecodeListener mListener;
    private Handler mHandler;
    private boolean isAvailable = true;
    private CameraManager mManager;
    private String mName;

    public AbsDecodeThread(String name, CaptureActivityHandler.DecodeListener listener) {
        mName = name;
        mListener = listener;
        HandlerThread thread = new HandlerThread(name);
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mManager = CameraManager.get();
    }


    public CameraManager getCameraManager() {
        return mManager;
    }
    public void push(final Bitmap data) {
        if (mStoped) return;
        isAvailable = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BitmapLuminanceSource source = new BitmapLuminanceSource(data, 0, data.getWidth() / 3, data.getWidth(), data.getWidth() / 3);
                //compressTobmp(source.getMatrix(),source.getWidth(),source.getHeight());
                DecodeResult result = decode(source.getMatrix(), source.getWidth(), source.getHeight());
                isAvailable = true;
                if (result == null) {
                    return;
                }
                if (mManager.isNeedCapture()) {
                    // 生成bitmap
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    data.compress(Bitmap.CompressFormat.JPEG, 50, bout);
                    byte[] bitmap = bout.toByteArray();
                    result.setBitmap(bitmap);
                }
                if (mListener != null) {
                    mListener.onDecode(0, result);
                }
            }
        });
    }

    public void push(final byte[] data) {
        if (mStoped) return;
        isAvailable = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                YuvLumianaceUtil.handle(data, getWidth(), getHeight());
                //compressTobmp(data,getWidth(),getHeight());
                DecodeResult result = decode(data, getWidth(), getHeight());
                isAvailable = true;
                if (result == null) {
                    return;
                }
                System.out.println("DECODED BY " + mName);
                if (mManager.isNeedCapture()) {
                    // 生成bitmap
                    PlanarYUVLuminanceSource source = buildLuminanceSource(data, getWidth(), getHeight());
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    source.renderCroppedGreyscaleBitmap().compress(Bitmap.CompressFormat.JPEG, 50, bout);
                    byte[] bitmap = bout.toByteArray();
                    result.setBitmap(bitmap);
                }
                if (mListener != null) {
                    mListener.onDecode(0, result);
                }
            }
        });
    }

    protected abstract DecodeResult decode(byte[] data, int width, int height);

    public boolean isAvailable() {
        return isAvailable;
    }

    public void release() {
        mStoped = true;
        mHandler.getLooper().quit();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getName() {
        return mName;
    }

    public void configChanged() {
        width = mManager.getPreviewSize().x;
        height = mManager.getPreviewSize().y;
    }


    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data   A preview frame.
     * @param width  The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width;// Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        return new PlanarYUVLuminanceSource(rotatedData, width, height, 0,
                0, width, height, false);
    }

    public void compressTobmp(byte[] yuv,int w,int h) {
        FileOutputStream stream = null;
        try {
            YuvImage image = new YuvImage(yuv, ImageFormat.NV21, w, h, null);
            File file = new File("/mnt/sdcard/testqr/");
            if (!file.exists()) {
                file.mkdirs();
            }
            stream = new FileOutputStream(new File(file, "img_" + SystemClock.uptimeMillis() + ".jpg"));
            image.compressToJpeg(new Rect(0, 0, 640, 480), 80, stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void compressTobmp(PlanarYUVLuminanceSource data) {
        try {
            File file = new File("/mnt/sdcard/testqr/");
            if (!file.exists()) {
                file.mkdirs();
            }
            data.renderCroppedGreyscaleBitmap().compress(Bitmap.CompressFormat.JPEG, 100,
                    new FileOutputStream(new File(file, "img_" + SystemClock.uptimeMillis() + ".jpg")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
