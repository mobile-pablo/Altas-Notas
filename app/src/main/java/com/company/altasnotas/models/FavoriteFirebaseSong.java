package com.company.altasnotas.models;

public class FavoriteFirebaseSong {
    private Integer numerInAlbum;
    private String author;
    private String album;

    public FavoriteFirebaseSong(){

    }

    public FavoriteFirebaseSong(Integer numerInAlbum, String author,String album) {
        this.numerInAlbum = numerInAlbum;
        this.author = author;
        this.album=album;
    }

    public Integer getNumerInAlbum() {
        return numerInAlbum;
    }

    public void setNumerInAlbum(Integer numerInAlbum) {
        this.numerInAlbum = numerInAlbum;
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
