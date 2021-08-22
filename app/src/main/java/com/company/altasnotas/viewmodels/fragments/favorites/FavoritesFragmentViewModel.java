package com.company.altasnotas.viewmodels.fragments.favorites;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

public class FavoritesFragmentViewModel extends ViewModel {


    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    private Playlist playlist;
    public CurrentPlaylistAdapter adapter;
    private CountDownLatch conditionLatch;
    private ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs;
    private MutableLiveData<Integer> _imageViewDrawable = new MutableLiveData<Integer>();

    private MutableLiveData<String>
            _titleText = new MutableLiveData<>(),
            _descriptionText = new MutableLiveData<>();


    private MutableLiveData<Boolean> _favStateBool =
            new MutableLiveData<>();

    public LiveData<Integer> getImageViewDrawable() {
        return _imageViewDrawable;
    }

    public LiveData<String> getTitleText() {
        return _titleText;
    }

    public LiveData<String> getDescriptionText() {
        return _descriptionText;
    }

    public LiveData<Boolean> getFavStateBool() {
        return _favStateBool;
    }


    public void addSongToFavFirebase(String author, String album, Integer i) {
        String key = database_ref.push().getKey();
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("album").setValue(album);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("author").setValue(author);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("numberInAlbum").setValue(i);
    }


    public void initializeFavorites() {

        conditionLatch = new CountDownLatch(1);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();

        playlist = new Playlist();
        favoriteFirebaseSongs = new ArrayList<>();
        playlist.setImage_id("");
        playlist.setAlbum(false);
        playlist.setTitle("Favorites");
        playlist.setDescription("Store here Your favorites Songs!");
        playlist.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        _titleText.setValue(playlist.getTitle());
        _descriptionText.setValue(playlist.getDescription() + "\n(" + playlist.getYear() + ")");

        _imageViewDrawable.setValue(R.drawable.fav_songs);

        if (mAuth.getCurrentUser() != null) {

            database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null) {

                        int x = (int) snapshot.getChildrenCount();

                        if (x != 0) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
                                favoriteFirebaseSong.setNumberInAlbum(Integer.valueOf(ds.child("numberInAlbum").getValue().toString()));
                                favoriteFirebaseSong.setAuthor(ds.child("author").getValue().toString());
                                favoriteFirebaseSong.setAlbum(ds.child("album").getValue().toString());
                                favoriteFirebaseSong.setDateTime(Calendar.getInstance().getTimeInMillis());
                                favoriteFirebaseSongs.add(favoriteFirebaseSong);

                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }


                            _favStateBool.setValue(favoriteFirebaseSongs.size() != x);
                        } else {

                            _favStateBool.setValue(true);
                        }


                    } else {
                        Log.d(MainActivity.FIREBASE, "This Song doesnt exist in Album");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    conditionLatch.countDown();
                }


            });
        }
    }


    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public ArrayList<FavoriteFirebaseSong> getFavoriteFirebaseSongs() {
        return favoriteFirebaseSongs;
    }

    public void setFavoriteFirebaseSongs(ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs) {
        this.favoriteFirebaseSongs = favoriteFirebaseSongs;
    }




}
