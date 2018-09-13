package com.lb.ocridcard2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Stack;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 18-9-12.下午5:48
 */
public class BitmapHandler {
    public static int RGB_BLACK = 0xFF000000;
    public static int RGB_WHITE = 0XFFFFFFFF;

    public static Bitmap convertImageToGray(Bitmap srcImage) {
        Bitmap bmp = Bitmap.createBitmap(srcImage.getWidth(),srcImage.getHeight(), Bitmap.Config.ARGB_8888);
        int lenx = srcImage.getWidth();
        int leny = srcImage.getHeight();

        for (int i = 0; i < lenx; i++) {
            for (int j = 0; j < leny; j++) {
                int pxl = srcImage.getPixel(i, j);
                int r = (pxl >> 16) & 0xff;
                int g = (pxl >> 8) & 0xff;
                int b = (pxl & 0xff);
                int gray = (int) (((float) r) * 0.299f + ((float) g) * 0.587f + ((float) b) * 0.114f);
                if (gray >= 0x80) {
                    bmp.setPixel(i, j, RGB_WHITE);
                } else {
                    bmp.setPixel(i, j, RGB_BLACK);
                }
            }
        }
        return bmp;
    }

    public static  Bitmap compressPixel(String filePath){
        Bitmap bmp = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        //setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = 2;
        //inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[16 * 1024];
        try {
            //load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
            if (bmp == null) {
                BitmapFactory.decodeFile(filePath, options);
            }
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }finally {
            return bmp;
        }
    }

    public static void save(Bitmap bmp, String file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static final int MOVE_LEFT = 0;
    private static final int MOVE_TOP = 1;
    private static final int MOVE_RIGHT = 2;
    private static final int MOVE_BOTTOM = 3;

}
