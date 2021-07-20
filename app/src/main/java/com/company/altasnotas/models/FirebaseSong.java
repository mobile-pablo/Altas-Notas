package com.company.altasnotas.models;

public class FirebaseSong{
    private Integer order;
    private String path;
    private String title;

    public FirebaseSong(){

    }

    public FirebaseSong(int order, String path, String title) {
        this.order = order;
        this.path = path;
        this.title = title;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
