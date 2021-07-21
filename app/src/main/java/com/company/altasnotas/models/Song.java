package com.company.altasnotas.models;

public class Song {
    //Song isnt on their own objects.Its object connected to array of songs for playlist

    private String author;
    private String album;
    private String title;
    private String path;
    private String image_url;

    public Song(String author, String album, String title, String path, String image_url){
        this.author=author;
        this.album=album;
        this.title=title;
        this.path=path;
        this.image_url = image_url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
