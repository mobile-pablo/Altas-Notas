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
  private Uri localSong;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view=  inflater.inflate(R.layout.fragment_player, container, false);
      storage = FirebaseStorage.getInstance();
        jcplayerView = (JcPlayerView) view.findViewById(R.id.jcplayer);


    fav_btn = view.findViewById(R.id.player_song_fav_btn);
    settings_btn = view.findViewById(R.id.player_song_options_btn);

        storage.getReference().child("songs/ni_bien_ni_mal.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                localSong=uri;
            }
        });


        jcAudios.add(JcAudio.createFromURL("Bad Bunny - NI BIEN NI MAL","https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/songs%2Fni_bien_ni_mal.mp3?alt=media&token=387f5d90-b513-48cd-9d64-6fd595ca1e9a"));
        jcplayerView.initPlaylist(jcAudios, null);
        jcplayerView.createNotification(R.drawable.altas_notes);

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