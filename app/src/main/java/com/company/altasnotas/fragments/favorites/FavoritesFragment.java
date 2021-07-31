package com.company.altasnotas.fragments.favorites;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
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
import java.util.concurrent.CountDownLatch;

public class FavoritesFragment extends Fragment {
    private RecyclerView recyclerView;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private Playlist playlist;
    private CurrentPlaylistAdapter adapter;
    private    CountDownLatch conditionLatch;
    private ImageView imageView;
    private TextView title, description;
    private ImageButton settings;
    private TextView fav_state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View view =  inflater.inflate(R.layout.fragment_current_playlist, container, false);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();

        imageView = view.findViewById(R.id.current_playlist_img);
        title = view.findViewById(R.id.current_playlist_title);
        description = view.findViewById(R.id.current_playlist_description);
        settings = view.findViewById(R.id.current_playlist_settings);
        settings.setVisibility(View.INVISIBLE);
        fav_state=  view.findViewById(R.id.current_playlist_recycler_state);

            conditionLatch = new CountDownLatch(1);
            initializeFavorites();


//        addSongToFirebase("Bad Bunny", "YHLQMDLG",8);



        recyclerView = view.findViewById(R.id.current_playlist_recycler_view);




       return view;
    }

    private void addSongToFirebase( String author, String album, Integer i) {
        String key = database_ref.push().getKey();
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("album").setValue(album);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("author").setValue(author);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("numberInAlbum").setValue(i);
    }


    private void initializeFavorites() {
        playlist = new Playlist();
        ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        playlist.setImage_id("");
        playlist.setAlbum(false);
        playlist.setTitle("Favorites");
        playlist.setDescription("Store here Your favorites Songs!");
        playlist.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription()+"\n("+playlist.getYear()+")");

        Glide.with(requireActivity()).load(R.drawable.fav_songs).into(imageView);


        if (mAuth.getCurrentUser() != null) {
            songs.clear();
            database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot!=null){

                        int x = (int) snapshot.getChildrenCount();
                        if(x!=0) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
                                favoriteFirebaseSong.setNumerInAlbum(Integer.valueOf(ds.child("numberInAlbum").getValue().toString()));
                                favoriteFirebaseSong.setAuthor(ds.child("author").getValue().toString());
                                favoriteFirebaseSong.setAlbum(ds.child("album").getValue().toString());
                                favoriteFirebaseSongs.add(favoriteFirebaseSong);
                            }

                            recyclerView.setVisibility(View.VISIBLE);
                            fav_state.setVisibility(View.GONE);

                            for (int i =0; i<favoriteFirebaseSongs.size(); i++) {
                                FavoriteFirebaseSong song=favoriteFirebaseSongs.get(i);
                                int finalI = i;
                                database_ref.child("music").child("albums").child(song.getAuthor()).child(song.getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot!=null) {
                                            for (DataSnapshot ds : snapshot.child("songs").getChildren()) {
                                                if(Integer.parseInt(ds.child("order").getValue().toString()) == song.getNumerInAlbum()){

                                                    Song local_song = new Song(snapshot.child("dir_desc").getValue().toString(), snapshot.child("dir_title").getValue().toString(),  ds.child("title").getValue().toString(), ds.child("path").getValue().toString(), snapshot.child("image_id").getValue().toString(), song.getNumerInAlbum());
                                                    songs.add(local_song);
                                                }
                                            }

                                            if(finalI ==favoriteFirebaseSongs.size()-1) {
                                                playlist.setSongs(songs);

                                                if (playlist.getSongs() != null) {
                                                    adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, true);
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                                    recyclerView.setAdapter(adapter);
                                                }
                                            }
                                        }
                                    }



                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }else{
                            fav_state.setText("Empty Favorites");
                            recyclerView.setVisibility(View.GONE);
                            fav_state.setVisibility(View.VISIBLE);
                        }


                    }else{
                        System.out.println("This song doesnt exist in Album!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                   conditionLatch.countDown();
                }


            });
        }
    }
}