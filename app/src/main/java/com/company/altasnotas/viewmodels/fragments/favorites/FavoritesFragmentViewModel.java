package com.company.altasnotas.viewmodels.fragments.favorites;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class FavoritesFragmentViewModel extends ViewModel {

    public  RecyclerView recyclerView;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private Playlist playlist;
    public static CurrentPlaylistAdapter adapter;
    private CountDownLatch conditionLatch;
    private ImageView imageView;
    private TextView title, description;
    private ImageView settings;
    public TextView fav_state;

    private MainActivity mainActivity;

    public void init(RecyclerView recyclerView, ImageView imageView, TextView title, TextView description, ImageView settings, TextView fav_state, MainActivity mainActivity) {
        this.recyclerView = recyclerView;
        this.playlist = playlist;
        this.adapter = adapter;
        this.imageView = imageView;
        this.title = title;
        this.description = description;
        this.settings = settings;
        this.fav_state = fav_state;
        this.mainActivity = mainActivity;


        conditionLatch = new CountDownLatch(1);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
    }







    public void addSongToFavFirebase(String author, String album, Integer i) {
        String key = database_ref.push().getKey();
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("album").setValue(album);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("author").setValue(author);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("numberInAlbum").setValue(i);
    }


    public void initializeFavorites() {
        playlist = new Playlist();
        ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs = new ArrayList<>();
        playlist.setImage_id("");
        playlist.setAlbum(false);
        playlist.setTitle("Favorites");
        playlist.setDescription("Store here Your favorites Songs!");
        playlist.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription() + "\n(" + playlist.getYear() + ")");

        Glide.with(mainActivity).load(R.drawable.fav_songs).into(imageView);


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


                            recyclerView.setVisibility(View.VISIBLE);
                            fav_state.setVisibility(View.GONE);

                            if (favoriteFirebaseSongs.size() == x) {
                                initializeFavoritesRecyclerView(favoriteFirebaseSongs);
                            }
                        } else {
                            fav_state.setText("Empty Favorites");
                            recyclerView.setVisibility(View.GONE);
                            fav_state.setVisibility(View.VISIBLE);
                        }


                    } else {
                        Log.d(MainActivity.FIREBASE,"This Song doesnt exist in Album");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    conditionLatch.countDown();
                }


            });
        }
    }

    public void initializeFavoritesRecyclerView(ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs) {
        ArrayList<Song> songs = new ArrayList<>();
        for (int i = 0; i < favoriteFirebaseSongs.size(); i++) {
            FavoriteFirebaseSong song = favoriteFirebaseSongs.get(i);
            database_ref.child("music").child("albums").child(song.getAuthor()).child(song.getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot != null) {
                        for (DataSnapshot ds : snapshot.child("songs").getChildren()) {
                            if (Integer.parseInt(ds.child("order").getValue().toString()) == song.getNumberInAlbum()) {

                                Song local_song = new Song(snapshot.child("dir_desc").getValue().toString(), snapshot.child("dir_title").getValue().toString(), ds.child("title").getValue().toString(), ds.child("path").getValue().toString(), snapshot.child("image_id").getValue().toString(), song.getNumberInAlbum());
                                local_song.setDateTime(song.getDateTime());
                                songs.add(local_song);
                            }
                        }

                        if (songs.size() == favoriteFirebaseSongs.size()) {
                            Collections.sort(songs, (f1, f2) -> f1.getDateTime().compareTo(f2.getDateTime()));


                            playlist.setSongs(songs);

                            if (playlist.getSongs() != null) {
                                adapter = new CurrentPlaylistAdapter( mainActivity, playlist, 1);
                                recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                if (mainActivity != null) {
                                    Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                    if (currentFragment instanceof FavoritesFragment) {
                                        Drawable songBg = AppCompatResources.getDrawable(mainActivity, R.drawable.custom_song_bg);
                                        recyclerView.setBackground(songBg);
                                    }
                                }

                            }
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
