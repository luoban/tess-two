package com.centerm.lib;

import android.graphics.Bitmap;
import android.os.SystemClock;

import com.google.zxing.LuminanceSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 10/12/17.10:49 AM
 */

public class BitmapLuminanceSource extends LuminanceSource {
    private final byte[] luminances;

    public BitmapLuminanceSource(Bitmap bitmap,int left,int top,int width,int height) {
        super(width, height);
//        compressTobmp(bitmap);
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), left, top, width, height);
//        compressTobmp(Bitmap.createBitmap(pixels, 0, bitmap.getWidth(), bitmap.getWidth(),  bitmap.getHeight(),
//                Bitmap.Config.ARGB_8888));
        luminances = new byte[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            int srcOffset = y * bitmap.getWidth();
            for (int x = 0; x < width; x++) {
                int pixel = pixels[srcOffset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                if (r == g && g == b) {
                    // Image is already greyscale, so pick any channel.
                    luminances[offset + x] = (byte) r;
                } else {
                    // Calculate luminance cheaply, favoring green.
                    luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
                }
            }
        }
    }
    public void compressTobmp(Bitmap data) {
        try {
            File file = new File("/mnt/sdcard/testqr/");
            if (!file.exists()) {
                file.mkdirs();
            }
            data.compress(Bitmap.CompressFormat.JPEG,100,
                    new FileOutputStream(new File(file, "img_" + SystemClock.uptimeMillis() + ".jpg")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public byte[] getRow(int y, byte[] row) {
        if (y < 0 || y >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
        int width = getWidth();
        if (row == null || row.length < width) {
            row = new byte[width];
        }

        System.arraycopy(luminances, y * width, row, 0, width);
        return row;
    }

    // Since this class does not support cropping, the underlying byte array
    // already contains
    // exactly what the caller is asking for, so give it to them without a copy.
    @Override
    public byte[] getMatrix() {
        return luminances;
    }

}
