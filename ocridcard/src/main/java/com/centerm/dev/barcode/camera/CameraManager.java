package com.centerm.dev.barcode.camera;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;

import com.centerm.dev.barcode.camera.base.AspectRatio;
import com.centerm.dev.barcode.camera.base.Constants;
import com.centerm.dev.barcode.decode.CaptureActivityHandler;
import com.centerm.dev.barcode.decode.LuminanceDetector;
import com.centerm.lib.DecodeResult;

import java.util.Set;

public class CameraManager implements CaptureActivityHandler.DecodeListener {
    public static final int ERROR_TIMEOUT = 3;
    private static final int ERROR_CANCEL = -2001;


    public static final int MODE_FASTER = 2;
    public static final int MODE_HIGHER_ACCURATE = 0;

    private boolean persist = true;
    private boolean needCapture = true;
    private int requestedCameraId = 0;
    private int exposureMode;
    private String focusMode = "auto";
    private boolean focusForever = true;
    private Point mPreviewSize = new Point(640,480);
    private int flashMode;
    private int focusInterval = 800;
    private int timeout = Integer.MAX_VALUE;

    private CameraView mCameraView;
    private CaptureActivityHandler mCaputreHandler;
    private CaptureActivityHandler.DecodeListener mDecodeProxy;
    private LuminanceDetector mLuminanceDetector;
    private String charset = "UTF-8";

    private static CameraManager cameraManager;
    private PixelFormat pf;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getFlashMode() {
        return flashMode;
    }

    public static CameraManager get() {
        if (cameraManager == null) {
            cameraManager = new CameraManager();
        }

        return cameraManager;
    }

    private CameraManager() {
    }

    public void bindCameraView(CameraView cameraView) {
        mCameraView = cameraView;
        mCameraView.setFlash(getFlashMode());
        mCameraView.setAdjustViewBounds(true);
        mCameraView.setAspectRatio(AspectRatio.parse("16:9"));
        mCameraView.addCallback(mPreviewCallback);
        mCameraView.setAutoFocus(true);
        mCaputreHandler = new CaptureActivityHandler(this);
    }

    public void bindDecodeCallback(CaptureActivityHandler.DecodeListener listener) {
        mDecodeProxy = listener;
    }

    public void bindLuminanceDetector(int average, LuminanceDetector.OnLuminanceChangedListener luminanceChangedListener) {
        mLuminanceDetector = new LuminanceDetector(average);
        mLuminanceDetector.setListener(luminanceChangedListener);
    }

    public Point getPreviewSize() {
        return findBestPreviewSizeValue();
    }

    private Point findBestPreviewSizeValue() {
        return mPreviewSize;
    }


    public void setPersist(boolean persist) {
        this.persist = persist;
    }


    public boolean isPersist() {
        return persist;
    }


    public boolean isNeedCapture() {
        return needCapture;
    }

    public void setNeedCapture(boolean needCapture) {
        this.needCapture = needCapture;
    }

    public void setCameraId(int cameraId) {
        this.requestedCameraId = cameraId;
    }

    public int getCameraId() {
        return requestedCameraId;
    }

    public void switchCamera() {
        if (requestedCameraId == 0) {
            requestedCameraId = 1;
        } else {
            requestedCameraId = 0;
        }
    }

    public int getExposureMode() {
        return exposureMode;
    }


    public String getFocusMode() {
        return focusMode;
    }

    public boolean isFocusForever() {
        return focusForever;
    }


    public void setFlashMode(int flashMode) {
        if (flashMode != 0xFF) {
            this.flashMode = flashMode;
        }
    }

    public int getFocusInterval() {
        return focusInterval;
    }

    public void setFocusInterval(int focusInterval) {
        this.focusInterval = focusInterval;
    }

    public void switchFlashLight() {
        int flashMode = getFlashMode();
        if (flashMode == Constants.FLASH_TORCH) {
            flashMode = Constants.FLASH_OFF;
        } else {
            flashMode = Constants.FLASH_TORCH;
        }
        this.flashMode = flashMode;
        mCameraView.setFlash(flashMode);
    }


    public void setFocusMode(String focusMode) {
        this.focusMode = focusMode;
    }

    public void setExposureMode(int exposureMode) {
        this.exposureMode = exposureMode;
    }


    public void setFocusForever(boolean focusForever) {
        this.focusForever = focusForever;
    }

    public void setPreviewSize(Point mPreviewSize) {
        this.mPreviewSize = mPreviewSize;
    }


    public void start() {
        if (mCameraView != null) {
            stop();
        }
        if (flashMode != 0xFF) {
            mCameraView.setFlash(flashMode);
        }
        mCameraView.setFacing(getCameraId());
        mCameraView.start();
        mCaputreHandler.startPreview();
        if (mLuminanceDetector != null) {
            mLuminanceDetector.reset();
        }
    }

    public synchronized void stop() {
        if (mCameraView != null) {
            mCameraView.stop();
            mCaputreHandler.stopPreview();
        }
    }

    public synchronized void release() {
        if (getFlashMode() != 0) {
            switchFlashLight();
        }
        mCameraView = null;
        mCaputreHandler.release();
        if (mLuminanceDetector != null)
            mLuminanceDetector.release();
        mCaputreHandler = null;
    }

    private CameraView.Callback mPreviewCallback = new CameraView.Callback() {

        @Override
        public void onPreviewCallback(CameraView cameraView, byte[] bmp) {
            super.onPreviewCallback(cameraView, bmp);
            mCaputreHandler.onPreviewFrame(bmp);
            if (mLuminanceDetector != null) {
                mLuminanceDetector.handle(bmp, getPreviewSize().x, getPreviewSize().y);
            }
        }

        @Override
        public void onPreviewCallback(CameraView cameraView, Bitmap bmp) {
            super.onPreviewCallback(cameraView, bmp);
            mCaputreHandler.onPreviewFrame(bmp);
            if (mLuminanceDetector != null) {
                mLuminanceDetector.handle(bmp);
            }
        }

        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
        }

    };

    @Override
    public void onDecode(int ret, DecodeResult result) {
        if (mDecodeProxy != null) {
            mDecodeProxy.onDecode(ret, result);
        }
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    public void onCanceled() {
        onDecode(CameraManager.ERROR_CANCEL, null);
    }

    public void callback(String content) {
        System.out.println("解码：" + content);
    }

    public int getScanMode() {
        return MODE_HIGHER_ACCURATE;
    }

    public void updatePixelParams(PixelFormat pf) {
        this.pf = pf;
    }

    public PixelFormat getPf() {
        return pf;
    }
}
