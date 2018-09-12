package com.centerm.dev.barcode.decode;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.centerm.dev.barcode.camera.CameraManager;
import com.centerm.lib.AbsDecodeThread;
import com.centerm.lib.DecodeResult;
import com.lb.ocridcard.OCRManager;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 18-9-12.上午11:31
 */
public class OCRDecoder extends AbsDecodeThread {
    private OCRManager mManager;
    private CameraManager mCameraManager;

    public OCRDecoder(CaptureActivityHandler.DecodeListener listener) {
        super("tess", listener);
        mCameraManager = CameraManager.get();
        mManager = OCRManager.get();
        mManager.init();
    }

    @Override
    protected DecodeResult decode(byte[] data, int width, int height) {
        int bpp = mCameraManager.getPf().bytesPerPixel;
        String result = mManager.decode(data, width, height, bpp, width * bpp);
        if (TextUtils.isEmpty(result)) {
            System.out.println("result is null");
            return null;
        }
        System.out.println("result is >> " + result);
        return new DecodeResult(result);
    }
}
