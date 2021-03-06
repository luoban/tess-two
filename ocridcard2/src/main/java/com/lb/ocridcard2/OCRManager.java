package com.lb.ocridcard2;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.support.constraint.solver.widgets.Rectangle;

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
//        mTess.setVariable("tessedit_char_whitelist", getWhiteList());
        mInitialized = true;
        System.out.println("mInitialized");
    }

    public String decode(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        mTess.setImage(bitmap);
        mTess.setVariable("tessedit_char_whitelist","");
        String result =  mTess.getUTF8Text();
        mTess.clear();
        return result;
    }


    public String decodeOnlyEng(Bitmap bitmap) {
        mTess.setImage(bitmap);
        mTess.setVariable("tessedit_char_whitelist","0123456789Xx");
        String result =  mTess.getUTF8Text();
        mTess.clear();
        return result;
    }
}
