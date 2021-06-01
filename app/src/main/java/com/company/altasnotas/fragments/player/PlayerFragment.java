package com.company.altasnotas.fragments.player;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.company.altasnotas.BackgroundSoundService;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;


public class PlayerFragment extends Fragment {
    private FirebaseStorage storage;

    private ImageButton main_btn, next_btn, back_btn, fav_btn, settings_btn;
  private Uri localSong;
private   Intent myService;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view=  inflater.inflate(R.layout.fragment_player, container, false);
      storage = FirebaseStorage.getInstance();
    main_btn=  view.findViewById(R.id.player_main_btn);
    next_btn=  view.findViewById(R.id.player_next_btn);
    back_btn=  view.findViewById(R.id.player_back_btn);
    fav_btn = view.findViewById(R.id.player_song_fav_btn);
    settings_btn = view.findViewById(R.id.player_song_options_btn);

        storage.getReference().child("songs/ni_bien_ni_mal.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                localSong=uri;
            }
        });
        MainActivity mainActivity = (MainActivity) getActivity();
         myService = new Intent(getActivity(), BackgroundSoundService.class);



        main_btn.setOnClickListener(v -> {

            if( main_btn.getDrawable().getConstantState().equals( main_btn.getContext().getDrawable(R.drawable.ic_play).getConstantState()))
            {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        myService.putExtra("uri",localSong.toString() );
                        mainActivity.startService(myService);
                    }
                }).start();

               main_btn.setImageResource(R.drawable.ic_pause);
            }
            else
            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                      //  mainActivity.stopService(myService);
                        main_btn.setImageResource(R.drawable.ic_play);
                    }
                }).start();

            }


        });

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