package com.company.altasnotas.models;

public class User {
   public String name;
    public String mail;
    public String age;
    public String phone;
    public String address;
    public String photoUrl;
    public int login_method;
    public int playlist_amount;
    public int fav_song_amount;



    public User(String name, String mail,String age,String phone,String address,String photoUrl,int login_method, int playlist_amount, int fav_song_amount) {
        this.name=name;
        this.mail=mail;
        this.age=age;
        this.phone = phone;
        this.address=address;
        this.photoUrl=photoUrl;
        this.login_method = login_method;
        this.playlist_amount=playlist_amount;
        this.fav_song_amount=fav_song_amount;

    }



}
