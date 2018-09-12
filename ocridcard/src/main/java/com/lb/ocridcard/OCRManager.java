package com.lb.ocridcard;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 18-9-12.上午10:09
 */
public class OCRManager {
    private static final String TRAIN_DATA = "";
    private TessBaseAPI mTess;
    private boolean mInitialized;
    public static OCRManager mManager;

    private OCRManager() {
        mTess = new TessBaseAPI();
    }

    public static OCRManager get() {
        if (mManager == null) {
            mManager = new OCRManager();
        }
        return mManager;
    }

    public void init() {
        if (mInitialized) {
            return;
        }
        //存放tessdata的文件路径 就是chi_sim.traineddata文件的位置chi_sim.traineddata
        String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
        //选择语言 chi_sim 简体中文  eng 英文
        String language = "chi_sim";
        File dir = new File(datapath + "tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        mTess.init(datapath, language);
        mInitialized = true;
    }

    public String decode(Bitmap bitmap) {
        mTess.setImage(bitmap);
        return mTess.getUTF8Text();
    }

    public String decode(byte[] data, int width, int height,int bpp, int bpl) {
        mTess.setImage(data, width, height, bpp, bpl);
        return mTess.getUTF8Text();
    }
}
