package com.centerm.dev.barcode.decode;

import android.graphics.Bitmap;

import com.centerm.dev.barcode.camera.CameraManager;
import com.centerm.lib.AbsDecodeThread;
import com.centerm.lib.DecodeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DecodeManager implements CaptureActivityHandler.DecodeListener {
    private CaptureActivityHandler mHandler;
    private List<AbsDecodeThread> mDeocdeThread;

    DecodeManager(CaptureActivityHandler handler) {
        mHandler = handler;
        mDeocdeThread = new ArrayList<>();
        initDecoders();
    }

    private void initDecoders() {
        mDeocdeThread.add(new OCRDecoder(this));
    }

    public synchronized void push(byte[] yuv) {
        for (AbsDecodeThread thread : mDeocdeThread) {
            if (thread.isAvailable()) {
                thread.push(yuv);
                break;
            }
        }
    }

    public synchronized void push(Bitmap yuv) {
        for (AbsDecodeThread thread : mDeocdeThread) {
            if (thread.isAvailable()) {
                thread.push(yuv);
                break;
            }
        }
    }

    public void release() {
        for (AbsDecodeThread thread : mDeocdeThread) {
            thread.release();
        }
        mDeocdeThread.clear();
    }

    @Override
    public void onDecode(int ret, DecodeResult result) {
        mHandler.onDecoded(result);
    }


    public void configChanged() {
        for (AbsDecodeThread thread : mDeocdeThread) {
            thread.configChanged();
        }
    }
}
