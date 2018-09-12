package com.centerm.dev.barcode.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import com.centerm.dev.barcode.camera.CameraManager;
import com.centerm.lib.DecodeResult;
import com.google.zxing.LuminanceSource;

import java.io.ByteArrayOutputStream;

public class CaptureActivityHandler extends Handler {

    private DecodeListener mListener;


    public interface DecodeListener {
        void onDecode(int ret, DecodeResult result);
    }

    private static final int TIMEOUT = 1;
    private DecodeManager mDecodeHandler;
    private State state;

    public void onPreviewFrame(byte[] yuv) {
        if (yuv == null) return;
        mDecodeHandler.push(yuv);
    }

    public void onPreviewFrame(Bitmap bitmap) {
        if (bitmap == null) return;
        mDecodeHandler.push(bitmap);
    }

    public void onDecoded(DecodeResult result) {
        synchronized (state) {
            if (isDone()) {
                return;
            }
            if (CameraManager.get().isPersist()) {
                CameraManager.get().callback(result.getContent());
                return;
            }
            stopPreview();
            if (mListener != null) {
                mListener.onDecode(0, result);
            }
        }
    }
    private enum State {
        PREVIEW, DONE
    }

    public CaptureActivityHandler(DecodeListener listener) {
        mDecodeHandler = new DecodeManager(this);
        mListener = listener;
    }


    @Override
    public void handleMessage(Message message) {
        if (isDone()) {
            return;
        }
        switch (message.what) {
            case TIMEOUT:
                onTimeout();
                break;
        }
    }

    private void onTimeout() {
        synchronized (state) {
            if (isDone()) {
                return;
            }
            stopPreview();
            if (mListener != null) {
                mListener.onDecode(-3, null);
            }
        }
    }

    private synchronized boolean isDone() {
        return state == State.DONE;
    }

    public void stopPreview() {
        state = State.DONE;
        removeMessages(TIMEOUT);
    }

    public void startPreview() {
        mDecodeHandler.configChanged();
        state = State.PREVIEW;
        sendEmptyMessageDelayed(TIMEOUT, CameraManager.get().getTimeout());
    }

    public void release() {
        mDecodeHandler.release();
    }

}
