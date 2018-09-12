package com.centerm.lib;

/**
 * Created by 班 on 2016/10/23.
 */

public class DecodeResult {

    private String content;
    private byte[] bitmap;

    public DecodeResult(String result) {
        this.content = result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setBitmap(byte[] bitmap) {
        this.bitmap = bitmap;
    }
}
