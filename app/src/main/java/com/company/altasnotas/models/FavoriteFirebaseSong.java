package com.company.altasnotas.models;

public class FavoriteFirebaseSong {
    private Integer numberInAlbum;
    private String author;
    private String album;

    public FavoriteFirebaseSong(){

    }

    public FavoriteFirebaseSong(Integer numberInAlbum, String author,String album) {
        this.numberInAlbum = numberInAlbum;
        this.author = author;
        this.album=album;
    }

    public Integer getNumberInAlbum() {
        return numberInAlbum;
    }

    public void setNumberInAlbum(Integer numberInAlbum) {
        this.numberInAlbum = numberInAlbum;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() { return album; }

    public void setAlbum(String album) { this.album = album; }
}
