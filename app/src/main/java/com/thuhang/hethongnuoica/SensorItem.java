package com.thuhang.hethongnuoica;

public class SensorItem {
    private String title;
    private String value;
    private int animationRes;

    public SensorItem(String title, String value, int animationRes) {
        this.title = title;
        this.value = value;
        this.animationRes = animationRes;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public int getAnimationRes() {
        return animationRes;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAnimationRes(int animationRes) {
        this.animationRes = animationRes;
    }
}
