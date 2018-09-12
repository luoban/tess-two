package com.centerm.lib;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 10/26/17.10:07 AM
 */

public class YuvLumianaceUtil {
    private static final int COMTRAST = 1;
    private static final int BRIGHTNESS = 255;

    public static void handle(byte[] data, int width, int height) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                data[i * width + j] = handle(data[i * width + j]);
            }
        }
    }

    public static byte handle(int y) {
        return (byte) (((y - 16) * COMTRAST) + BRIGHTNESS + 16);
    }

}
