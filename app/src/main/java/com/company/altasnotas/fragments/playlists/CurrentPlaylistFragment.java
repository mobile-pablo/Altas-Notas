package com.company.altasnotas.fragments.playlists;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
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
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class CurrentPlaylistFragment extends Fragment {

    private final Playlist playlist;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView title, description;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    CurrentPlaylistAdapter adapter;
    private final String author;
    private final String album;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        View view =  inflater.inflate(R.layout.fragment_current_playlist, container, false);

        imageView = view.findViewById(R.id.current_playlist_img);
        title = view.findViewById(R.id.current_playlist_title);
        description = view.findViewById(R.id.current_playlist_description);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();


        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription()+"\n("+playlist.getYear()+")");

        Glide.with(container).load(playlist.getImage_id()).into(imageView);

        recyclerView =  view.findViewById(R.id.current_playlist_recycler_view);
        initializePlaylist(author,album);
        return view;

    }



    public CurrentPlaylistFragment(String author, String album , Playlist playlist){
        this.playlist=playlist;
        this.author=author;
        this.album=album;
    }


    private void initializePlaylist(String author, String album) {

        ArrayList<FirebaseSong> firebaseSongs = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        if (mAuth.getCurrentUser() != null) {
            CountDownLatch conditionLatch = new CountDownLatch(1);
            database_ref.child("music").child("albums").child(author).child(album).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot!=null){
                        int x = (int) snapshot.child("songs").getChildrenCount();
                        System.out.println("Children count: "+x);
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

                            Song local_song = new Song( playlist.getDescription(), playlist.getTitle(), song.getTitle(), song.getPath(), playlist.getImage_id());
                            songs.add(local_song);
                        }

                        if(i==x){
                            playlist.setSongs(songs);
                            conditionLatch.countDown();
                            System.out.println("Size: "+songs.size());
                        }

                        try {
                            conditionLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("ConditionLatch error");
                        }

                        playlist.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                        playlist.setSong_amount(Integer.valueOf(snapshot.child("song_amount").getValue().toString()));

                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist,false);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
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