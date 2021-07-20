package com.company.altasnotas.fragments.home;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.adapters.HomeFragmentAdapter;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.FirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;

public class HomeFragment extends Fragment {

RecyclerView recyclerView;
HomeFragmentAdapter adapter;
    DatabaseReference database_ref;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    String[] album_array = new String[1];
    String[] author_array = new String[1];
    ArrayList<Playlist> playlists = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
     View view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();

     initializePlaylist("Kult", "Spokojnie");
     initializePlaylist("Johnny Cash", "The Baron");
     initializePlaylist("Bad Bunny", "YHLQMDLG");
     initializePlaylist("Analogs", "Pełnoletnia Oi! Młodzież");
     
     recyclerView = view.findViewById(R.id.home_recycler_view);
     adapter = new HomeFragmentAdapter((MainActivity) getActivity(),playlists );
     recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
     recyclerView.setAdapter(adapter);
     return  view;
    }

    private void initializePlaylist(String author, String album) {
        ArrayList<FirebaseSong> firebaseSongs = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        CountDownLatch conditionLatch = new CountDownLatch(1);
        Playlist x = new Playlist();
        if (mAuth.getCurrentUser() != null) {

            database_ref.child("music").child("albums").child(author).child(album).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot!=null){

                   album_array[0] = snapshot.child("title").getValue().toString();
                    author_array[0] = snapshot.child("description").getValue().toString();

                    int i=0;
                    songs.clear();
                    for (DataSnapshot ds: snapshot.child("songs").getChildren()){
                        i++;

                        FirebaseSong firebaseSong = new FirebaseSong();
                        firebaseSong.setOrder(Integer.valueOf(ds.child("order").getValue().toString()));
                        firebaseSong.setPath(ds.child("path").getValue().toString());
                        firebaseSong.setTitle(ds.child("title").getValue().toString());
                        firebaseSongs.add(firebaseSong);
                       }


                    Collections.sort(firebaseSongs, (f1, f2) -> f1.getOrder().compareTo(f2.getOrder()));


                    for (FirebaseSong song: firebaseSongs) {

                        Song local_song = new Song(0, author_array[0], album_array[0], song.getTitle(), song.getPath());
                        songs.add(local_song);

                        if(i==snapshot.child("songs").getChildrenCount()){
                            x.setSongs(songs);
                            conditionLatch.countDown();
                        }
                    }

                    x.setImage_id(snapshot.child("image_id").getValue().toString());
                    x.setYear(snapshot.child("year").getValue().toString());
                    x.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                    x.setTitle(album_array[0]);
                    x.setDescription(author_array[0]);
                    x.setSong_amount(Integer.valueOf(snapshot.child("song_amount").getValue().toString()));
                    playlists.add(x);
                    adapter.notifyDataSetChanged();

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