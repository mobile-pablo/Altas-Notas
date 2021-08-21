package com.company.altasnotas.viewmodels.activities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {


    private   MutableLiveData<String>
           _currentSongTitle = new MutableLiveData<>(),
           _currentSongAlbum = new MutableLiveData<>(),
           _currentSongAuthor = new MutableLiveData<>(),
           _photoUrl = new MutableLiveData<>();

   public LiveData<String> getCurrentSongTitle(){
       return _currentSongTitle;
   }

    public LiveData<String> getCurrentSongAlbum(){
        return _currentSongAlbum;
    }

    public LiveData<String> getCurrentSongAuthor(){
        return _currentSongAuthor;
    }


    public LiveData<String> getPhotoUrl(){
        return _photoUrl;
    }


    public void setCurrentSongTitle(String title) {
       _currentSongTitle.setValue(title);
    }

    public void setCurrentSongAlbum(String album) {
       _currentSongAlbum.setValue(album);
    }

    public void setCurrentSongAuthor(String author) {
     _currentSongAuthor.setValue(author);
    }


    public void setPhotoUrl(String url) {
        _photoUrl.setValue(url);
    }

}
