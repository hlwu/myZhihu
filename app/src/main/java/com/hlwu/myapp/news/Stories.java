package com.hlwu.myapp.news;

import java.util.Arrays;

/**
 * Created by hlwu on 1/3/18.
 */

public class Stories {
    private String[] images;
    private int type;
    private int id;
    private String ga_prefix;
    private String title;

    public Stories(String[] images, int type, int id, String ga_prefix, String title) {
        this.images = images;
        this.type = type;
        this.id = id;
        this.ga_prefix = ga_prefix;
        this.title = title;
    }

    public boolean isSameWith(Stories stories) {
        return this.id == stories.getId();
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGa_prefix(String ga_prefix) {
        this.ga_prefix = ga_prefix;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getImages() {
        return images;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getGa_prefix() {
        return ga_prefix;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Stories{" +
                "images=" + Arrays.toString(images) +
                ", type=" + type +
                ", id=" + id +
                ", ga_prefix='" + ga_prefix + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
