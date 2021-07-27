package com.company.altasnotas.fragments.home;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private final String[] album_array = new String[1];
    private final String[] author_array = new String[1];
    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private ArrayList<String> authors;
    private ArrayList<String> albums;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
     View view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
        albums= new ArrayList<>();
        authors = new ArrayList<>();
     initializePlaylist("Kult", "Spokojnie");
     initializePlaylist("Johnny Cash", "The Baron");
     initializePlaylist("Bad Bunny", "YHLQMDLG");
     initializePlaylist("Analogs", "Pełnoletnia Oi! Młodzież");
     
     recyclerView = view.findViewById(R.id.home_recycler_view);
     adapter = new HomeFragmentAdapter((MainActivity) getActivity(),authors,albums, playlists );
     recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
     recyclerView.setAdapter(adapter);
     return  view;
    }

    private void initializePlaylist(String author, String album) {
        Playlist x = new Playlist();
        if (mAuth.getCurrentUser() != null) {

            database_ref.child("music").child("albums").child(author).child(album).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot!=null){

                       album_array[0] = snapshot.child("title").getValue().toString();
                       author_array[0] = snapshot.child("description").getValue().toString();


                       albums.add(album);
                       authors.add(author);

                       x.setImage_id(snapshot.child("image_id").getValue().toString());
                       x.setYear(snapshot.child("year").getValue().toString());
                       x.setTitle(album_array[0]);
                       x.setDescription(author_array[0]);
                       playlists.add(x);
                       adapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Error: "+error.getMessage(),"FirebaseDatabase");
                }

            });


        }
    }
}