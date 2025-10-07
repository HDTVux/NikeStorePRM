package com.example.nikestore.model;

public class Banner {
    public int id;
    public String title;
    public String subtitle;
    public String image_url;
    public String deeplink;

    public Banner() {
    }

    public Banner(int id, String title, String subtitle, String image_url, String deeplink) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.image_url = image_url;
        this.deeplink = deeplink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    @Override
    public String toString() {
        return "Banner{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", image_url='" + image_url + '\'' +
                ", deeplink='" + deeplink + '\'' +
                '}';
    }
}
