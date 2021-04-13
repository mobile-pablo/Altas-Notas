package com.company.altasnotas.models;

public class User {
   public String name;
    public String surname;
    public String mail;
    public int age;
    public Boolean gender;
    public String photo_id;
    public int playlist_amount;
    public int fav_song_amount;

    public User(String name, String surname, String mail, int age, boolean gender, String photo_id, int playlist_amount, int fav_song_amount) {
        this.name=name;
        this.surname=surname;
        this.mail=mail;
        this.age=age;
        this.gender=gender;
        this.photo_id=photo_id;
        this.playlist_amount=playlist_amount;
        this.fav_song_amount=fav_song_amount;
    }



}
