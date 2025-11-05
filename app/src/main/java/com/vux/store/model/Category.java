package com.vux.store.model;

public class Category {
    private int id;
    private String name;
    private String slug;
    private String icon_url;
    private String image_url;
    private int sort_order;

    // getters & setters
    public int getId(){ return id; }
    public void setId(int id){ this.id = id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getIconUrl(){ return icon_url; }
    public void setIconUrl(String u){ this.icon_url = u; }
    // (others as needed)
}
