package com.lb.ocridcard2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.IOException;
import java.util.Stack;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 18-9-13.上午9:34
 */
public class IDCardRecognition {

    private String TMP_PIC = "/mnt/sdcard/ocr/2.jpg";

    private String mBmpPath;
    private Bitmap bitmap;
    private int mWidth;
    private int mHeight;
    private IDCardInfo mInfo;
    public IDCardRecognition(String bmpPath) throws IOException {
        mBmpPath = bmpPath;
        Bitmap bmp = BitmapHandler.compressPixel(mBmpPath);
        if (bmp == null) {
            throw new IOException("no pic found");
        }
        bitmap = BitmapHandler.convertImageToGray(bmp);
        mInfo = new IDCardInfo();
        mInfo.setBitmap(bitmap);
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
    }

    public IDCardInfo recognize() {
        //身份证号
        Bitmap bmp = findIDNumRegion();
        String id = OCRManager.get().decodeOnlyEng(bmp);
        mInfo.setId(id);
        bmp = Bitmap.createBitmap(bitmap, 30, 40, mWidth - mWidth / 3 -40,mHeight -  bmp.getHeight() -40);
        String textResult = OCRManager.get().decode(bmp);
        String[] texts = textResult.split("\n");
        for (int i = 0 ;i< texts.length ;i ++) {
            String t = texts[i];
            System.out.println("解析：" + t);
            if (t.contains("姓") || t.contains("名")) {
                int index = -1;
                if (t.contains("名")) {
                    index = t.indexOf("名") + 1;
                } else if (t.contains("姓")) {
                    index = t.indexOf("姓") + 3;
                }
                if (index != -1) {
                    mInfo.setName(t.substring(index));
                }
            } else if (t.contains("性") || t.contains("别")) {
                int index = -1;
                if (t.contains("别")) {
                    index = t.indexOf("别") + 1;
                } else if (t.contains("性")) {
                    index = t.indexOf("性") + 3;
                }
                if (index != -1) {
                    mInfo.setSex(t.substring(index, index + 2));
                }
            } else if (t.contains("出") || t.contains("生") || t.contains("日")) {
                int index = -1;
                int endIndex = -1;
                if (t.contains("生")) {
                    index = t.indexOf("生") + 1;
                    endIndex = t.indexOf("日");
                } else if (t.contains("出")) {
                    index = t.indexOf("出") + 3;
                    endIndex = t.indexOf("日");
                }
                if (index != -1) {
                    String birth = "";
                    if (endIndex != -1) {
                        birth = t.substring(index, endIndex);
                    } else {
                        endIndex = t.indexOf(" ", index);
                        if (endIndex != -1) {
                            birth = t.substring(index, endIndex);
                        } else {
                            birth = t.substring(index);
                        }
                    }
                    mInfo.setBirth(birth);
                }
            } else if (t.contains("址")) {
                String addr = "";
                int index = t.indexOf("址") + 1;
                int end = t.indexOf(" ", index+5);
                if (end == -1) {
                    addr = t.substring(index);
                } else {
                    addr = t.substring(index, end);
                }
                addr += texts[i + 1];
                mInfo.setAddress(addr);
            }
            mInfo.setMz("汉");
        }

        return mInfo;
    }

    private Bitmap findIDNumRegion() {
        int fromY = 0;
        int toY = 0;

        boolean findFirstLine = false;
        //从下往上数
        for (int i = mHeight - 1; i > 0; i--) {
            int blackCount = 0;
            for (int j = 0; j < mWidth; j++) {
                int pix = bitmap.getPixel(j, i);
                //计算一行像素中黑色像素点个数，以确定这行像素是否是一行字里面的
                if (pix == BitmapHandler.RGB_BLACK) {
                    blackCount++;
                }
            }
            if (blackCount > 30) {
                if (!findFirstLine) {
                    findFirstLine = true;
                    toY = i;
                }
            } else {
                if (findFirstLine) {
                    fromY = i;
                    break;
                }
            }
        }
        System.out.println("from " + fromY + " to " + toY);
        return Bitmap.createBitmap(bitmap, 0, fromY - 5, mWidth, toY - fromY + 5);
    }


}
