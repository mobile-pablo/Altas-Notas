package com.company.altasnotas.models;

public class User {
   public String name;
    public String mail;
    public int age;
    public String address;
    public int login_method;
    public int playlist_amount;
    public int fav_song_amount;



    public User(String name, String mail, int age,String address,int login_method, int playlist_amount, int fav_song_amount) {
        this.name=name;
        this.mail=mail;
        this.age=age;
        this.address=address;
        this.login_method = login_method;
        this.playlist_amount=playlist_amount;
        this.fav_song_amount=fav_song_amount;

    }



}
