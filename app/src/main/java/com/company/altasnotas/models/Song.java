package com.company.altasnotas.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    //Song isnt on their own objects.Its object connected to array of songs for playlist

    private String author;
    private String album;
    private String title;
    private String path;
    private String image_url;


    private Integer order;

    public Song(String author, String album, String title, String path, String image_url, Integer order) {
        this.author = author;
        this.album = album;
        this.title = title;
        this.path = path;
        this.image_url = image_url;
        this.order = order;
    }

    protected Song(Parcel in) {
        author = in.readString();
        album = in.readString();
        title = in.readString();
        path = in.readString();
        image_url = in.readString();
        order = in.readInt();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

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


    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(album);
        dest.writeString(title);
        dest.writeString(path);
        dest.writeString(image_url);
        dest.writeInt(order);
    }

    private Long dateTime;

    public Long getDateTime() {
        return dateTime;
    }

    public void setDateTime(Long datetime) {
        this.dateTime = datetime;
    }

}
