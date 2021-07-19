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
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class HomeFragment extends Fragment {

RecyclerView recyclerView;
HomeFragmentAdapter adapter;
    DatabaseReference database_ref;
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
     View view = inflater.inflate(R.layout.fragment_home, container, false);
 /* IMPORTANT CODE
                    Playlist x = new Playlist();


                    CountDownLatch conditionLatch = new CountDownLatch(1);
                    mAuth = FirebaseAuth.getInstance();
                    database = FirebaseDatabase.getInstance();
                    database_ref = database.getReference();
                    final String[] album_array = new String[1];
                    final String[] author_array = new String[1];
                    ArrayList<Song> songs = new ArrayList<>();

                        if (mAuth.getCurrentUser() != null) {

                            database_ref.child("music").child("albums").child("Kult").child("Spokojnie").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    album_array[0] = snapshot.child("songs").getRef().getParent().getKey();
                                    author_array[0] = snapshot.child("songs").getRef().getParent().getParent().getKey();
                                    int i=0;
                                     for (DataSnapshot ds: snapshot.child("songs").getChildren()){
                                         i++;

                                        Song local_song = new Song(0, author_array[0], album_array[0], ds.getKey().toString(),Uri.parse(ds.child("path").getValue().toString()));
                                        songs.add(local_song);


                                         if(i==snapshot.child("songs").getChildrenCount()){
                                             x.setSongs(songs);
                                             selectedFragment[0] = new CurrentPlaylistFragment(x);
                                             conditionLatch.countDown();
                                             getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment[0]).commit();

                                         }
                                     }


                                   x.setImage_id(snapshot.child("image_id").getValue().toString());
                                   x.setYear(snapshot.child("year").getValue().toString());
                                   x.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                                   x.setTitle(album_array[0]);
                                   x.setDescription(author_array[0]);
                                   x.setSong_amount(Integer.valueOf(snapshot.child("song_amount").getValue().toString()));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    conditionLatch.countDown();
                                }
                            });


                        }


           */


        Playlist x = new Playlist();


        CountDownLatch conditionLatch = new CountDownLatch(1);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
        final String[] album_array = new String[1];
        final String[] author_array = new String[1];
        ArrayList<Song> songs = new ArrayList<>();
        ArrayList<Playlist> playlists = new ArrayList<>();

        if (mAuth.getCurrentUser() != null) {

            database_ref.child("music").child("albums").child("Kult").child("Spokojnie").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    album_array[0] = snapshot.child("songs").getRef().getParent().getKey();
                    author_array[0] = snapshot.child("songs").getRef().getParent().getParent().getKey();
                    int i=0;
                    for (DataSnapshot ds: snapshot.child("songs").getChildren()){
                        i++;

                        Song local_song = new Song(0, author_array[0], album_array[0], ds.getKey().toString(), Uri.parse(ds.child("path").getValue().toString()));
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
                    playlists.add(x);
                    playlists.add(x);
                    playlists.add(x);
                    playlists.add(x);
                    playlists.add(x);
                    playlists.add(x);
                    playlists.add(x);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    conditionLatch.countDown();
                }

            });


        }

        System.out.println(x.getTitle());
     recyclerView = view.findViewById(R.id.home_recycler_view);
     adapter = new HomeFragmentAdapter((MainActivity) getActivity(),playlists );
     recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
     recyclerView.setAdapter(adapter);
     return  view;
    }
}