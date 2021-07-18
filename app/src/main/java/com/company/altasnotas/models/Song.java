package com.company.altasnotas.models;

import android.net.Uri;

import java.net.URI;

public class Song {
    //Song isnt on their own objects.Its object connected to array of songs for playlist
    private int playlistID;
    private String author;
    private String album;
    private String title;
    private Uri path;

    public Song(int playlistID, String author, String album, String title, Uri path){
        this.playlistID=playlistID;
        this.author=author;
        this.album=album;
        this.title=title;
        this.path=path;
    }


    public Integer getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(Integer playlistID) {
        this.playlistID = playlistID;
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


    public Uri getPath() {
        return path;
    }

    public void setPath(Uri path) {
        this.path = path;
    }
}
