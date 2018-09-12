package com.centerm.lib;

import android.graphics.Bitmap;

import com.google.zxing.LuminanceSource;

public final class PlanarYUVLuminanceSource extends LuminanceSource {
    private static final int THUMBNAIL_SCALE_FACTOR = 2;
    private final byte[] yuvData;
    private final int dataWidth;
    private final int dataHeight;
    private final int left;
    private final int top;

    public PlanarYUVLuminanceSource(byte[] yuvData, int dataWidth, int dataHeight, int left, int top, int width, int height, boolean reverseHorizontal) {
        super(dataWidth, dataHeight);
        if (left + width <= dataWidth && top + height <= dataHeight) {
            this.yuvData = yuvData;
            this.dataWidth = dataWidth;
            this.dataHeight = dataHeight;
            this.left = left;
            this.top = top;
            if (reverseHorizontal) {
                this.reverseHorizontal(width, height);
            }

        } else {
            throw new IllegalArgumentException("Crop rectangle does not fit within image data.");
        }
    }

    public byte[] getRow(int y, byte[] row) {
        if (y >= 0 && y < this.getHeight()) {
            int width = this.getWidth();
            if (row == null || row.length < width) {
                row = new byte[width];
            }

            int offset = (y + this.top) * this.dataWidth + this.left;
            System.arraycopy(this.yuvData, offset, row, 0, width);
            return row;
        } else {
            throw new IllegalArgumentException("Requested row is outside the image: " + y);
        }
    }

    public byte[] getMatrix() {
        int width = this.getWidth();
        int height = this.getHeight();
        if (width == this.dataWidth && height == this.dataHeight) {
            return this.yuvData;
        } else {
            int area = width * height;
            byte[] matrix = new byte[area];
            int inputOffset = this.top * this.dataWidth + this.left;
            if (width == this.dataWidth) {
                System.arraycopy(this.yuvData, inputOffset, matrix, 0, area);
                return matrix;
            } else {
                byte[] yuv = this.yuvData;

                for (int y = 0; y < height; ++y) {
                    int outputOffset = y * width;
                    System.arraycopy(yuv, inputOffset, matrix, outputOffset, width);
                    inputOffset += this.dataWidth;
                }

                return matrix;
            }
        }
    }

    public boolean isCropSupported() {
        return true;
    }

    public LuminanceSource crop(int left, int top, int width, int height) {
        return new PlanarYUVLuminanceSource(this.yuvData, this.dataWidth, this.dataHeight, this.left + left, this.top + top, width, height, false);
    }

    public int[] renderThumbnail() {
        int width = this.getWidth() / 2;
        int height = this.getHeight() / 2;
        int[] pixels = new int[width * height];
        byte[] yuv = this.yuvData;
        int inputOffset = this.top * this.dataWidth + this.left;

        for (int y = 0; y < height; ++y) {
            int outputOffset = y * width;

            for (int x = 0; x < width; ++x) {
                int grey = yuv[inputOffset + x * 2] & 255;
                pixels[outputOffset + x] = -16777216 | grey * 65793;
            }

            inputOffset += this.dataWidth * 2;
        }

        return pixels;
    }

    public Bitmap renderCroppedGreyscaleBitmap() {
        int width = getWidth();
        int height = getHeight();
        int cropWidth = width - left * 2;
        int cropHeight = height - top * 2;
        int[] pixels = new int[cropWidth * cropHeight];
        byte[] yuv = yuvData;
        for (int y = 0; y < cropHeight; y++) {
            int yoffset = width * (top + y);
            for (int x = 0; x < cropWidth; x++) {
                int xOffset = yoffset + left + x;
                pixels[y * cropWidth + x] = 0xFF000000 | (yuv[xOffset] * 0x00010101);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, cropWidth, 0, 0, cropWidth, cropHeight);
        return bitmap;
    }

    public int getThumbnailWidth() {
        return this.getWidth() / 2;
    }

    public int getThumbnailHeight() {
        return this.getHeight() / 2;
    }

    private void reverseHorizontal(int width, int height) {
        byte[] yuvData = this.yuvData;
        int y = 0;

        for (int rowStart = this.top * this.dataWidth + this.left; y < height; rowStart += this.dataWidth) {
            int middle = rowStart + width / 2;
            int x1 = rowStart;

            for (int x2 = rowStart + width - 1; x1 < middle; --x2) {
                byte temp = yuvData[x1];
                yuvData[x1] = yuvData[x2];
                yuvData[x2] = temp;
                ++x1;
            }

            ++y;
        }

    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getDataWidth() {
        return dataWidth;
    }

    public int getDataHeight() {
        return dataHeight;
    }
}