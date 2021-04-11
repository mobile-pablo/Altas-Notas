package com.company.altasnotas.models;

public class playlist {
    private String title;
    private String description;
    private Integer song_amount;
    private String image_id;


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


}
