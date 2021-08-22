package com.company.altasnotas.models;

public class User {
    public String name;
    public String mail;
    public String photoUrl;
    public int login_method;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getLogin_method() {
        return login_method;
    }

    public void setLogin_method(int login_method) {
        this.login_method = login_method;
    }

    public User(String name, String mail, String photoUrl, int login_method) {
        this.name = name;
        this.mail = mail;
        this.photoUrl = photoUrl;
        this.login_method = login_method;
    }

    public User(){}


}
