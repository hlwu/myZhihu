package com.hlwu.myapp.news;

/**
 * Created by hlwu on 1/18/18.
 */

public class StoriesContent {
    private String body;
    private String image_source;
    private String title;
    private String image;
    private String share_url;
    private String[] js;
    private String ga_prefix;
    private String[] images;
    private int type;
    private int id;
    private String[] css;

    public StoriesContent(String body, String image_source, String title,
                          String image, String share_url, String[] js, String ga_prefix,
                          String[] images, int type, int id, String[] css) {
        this.body = body;
        this.image_source = image_source;
        this.title = title;
        this.image = image;
        this.share_url = share_url;
        this.js = js;
        this.ga_prefix = ga_prefix;
        this.images = images;
        this.type = type;
        this.id = id;
        this.css = css;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setImage_source(String image_source) {
        this.image_source = image_source;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public void setJs(String[] js) {
        this.js = js;
    }

    public void setGa_prefix(String ga_prefix) {
        this.ga_prefix = ga_prefix;
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

    public void setCss(String[] css) {
        this.css = css;
    }

    public String getBody() {
        return body;
    }

    public String getImage_source() {
        return image_source;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getShare_url() {
        return share_url;
    }

    public String[] getJs() {
        return js;
    }

    public String getGa_prefix() {
        return ga_prefix;
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

    public String[] getCss() {
        return css;
    }
}
