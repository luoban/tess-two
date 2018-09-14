package com.lb.ocridcard2;

import android.graphics.Bitmap;

/**
 * @auth luoban <luoban@centerm.com>
 * @date 18-9-13.上午9:39
 */
public class IDCardInfo {

    private String name = "";
    private String sex = "";
    private String mz = "";
    private String birth = "";
    private String address = "";
    private String id = "";
    private Bitmap bitmap;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMz() {
        return mz;
    }

    public void setMz(String mz) {
        this.mz = mz;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public String toString() {
        return "IDCardInfo{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", minority='" + mz + '\'' +
                ", birth='" + birth + '\'' +
                ", address='" + address + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
