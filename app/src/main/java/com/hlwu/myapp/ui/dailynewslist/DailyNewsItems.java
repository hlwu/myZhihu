package com.hlwu.myapp.ui.dailynewslist;

import android.graphics.Bitmap;

/**
 * Created by hlwu on 1/8/18.
 */

public class DailyNewsItems {
    private String title;
    private Bitmap pic;
    private int id;
    private String date;
    public DailyNewsItems(String title, Bitmap pic, int id, String date) {
        this.title = title;
        this.pic = pic;
        this.id = id;
        this.date = date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getPic() {
        return pic;
    }

    public int getId() {
        return id;
    }

    public void setPic(Bitmap pic) {
        this.pic = pic;
    }

    @Override
    public String toString() {
        return "DailyNewsItems{" +
                "title='" + title + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
