package com.company.altasnotas.models;

import java.util.ArrayList;

public class Playlist {
    private String title;
    private String description;
    private int song_amount;
    private String image_id;
    private String year;
    private boolean isAlbum;
    private ArrayList <Song> songs;

    public Playlist( int song_amount, String image_id, String year, boolean isAlbum){
        this.song_amount=song_amount;
        this.image_id=image_id;
        this.year=year;
        this.isAlbum = isAlbum;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSong_amount() {
        return song_amount;
    }

    public void setSong_amount(Integer song_amount) {
        this.song_amount = song_amount;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }


    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public boolean isAlbum() {
        return isAlbum;
    }

    public void setAlbum(boolean album) {
        this.isAlbum = album;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
