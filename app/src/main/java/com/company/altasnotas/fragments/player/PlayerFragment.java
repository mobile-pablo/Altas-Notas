package com.company.altasnotas.fragments.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.models.PlaylistSong;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


public class PlayerFragment extends Fragment {
    private FirebaseStorage storage;
    private ArrayList<JcAudio> jcAudios = new ArrayList<>();
    private JcPlayerView jcplayerView;
    private ImageButton fav_btn, settings_btn;
    private ArrayList<PlaylistSong> playlist;


    public PlayerFragment(ArrayList<PlaylistSong> playlist){
        this.playlist = playlist;
        //We are sending playlist to this player and let it play all of it
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
      View view=  inflater.inflate(R.layout.fragment_player, container, false);
      storage = FirebaseStorage.getInstance();
      jcplayerView = (JcPlayerView) view.findViewById(R.id.jcplayer);


    fav_btn = view.findViewById(R.id.player_song_fav_btn);
    settings_btn = view.findViewById(R.id.player_song_options_btn);

        storage.getReference().child("songs/ni_bien_ni_mal.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

            }
        });


        for (PlaylistSong song: playlist){
            jcAudios.add(JcAudio.createFromURL(song.getTitle(),song.getPath()));
        }


    if(jcAudios!=null && jcAudios.size()>0)
    {
    jcplayerView.initPlaylist(jcAudios, null);
    jcplayerView.createNotification(R.drawable.altas_notes);
    }
    fav_btn.setOnClickListener(v->{

        if( fav_btn.getDrawable().getConstantState().equals( fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState()))
        {
            fav_btn.setImageResource(R.drawable.ic_heart_full);
        }else{
            fav_btn.setImageResource(R.drawable.ic_heart_empty);
        }
    });
      return view;
    }


}