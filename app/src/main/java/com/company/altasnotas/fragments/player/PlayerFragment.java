package com.company.altasnotas.fragments.player;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;


public class PlayerFragment extends Fragment {
    private FirebaseStorage storage;
    private final ArrayList<JcAudio> jcAudios = new ArrayList<>();
    public JcPlayerView jcplayerView;
    private ImageButton fav_btn;
    private Button settings_btn;
    private final Playlist playlist;
    int position;

    public PlayerFragment(Playlist playlist, int position){
        this.playlist = playlist;
        this.position=position;
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


        for (Song song: playlist.getSongs()){
            jcAudios.add(JcAudio.createFromURL(song.getTitle()+"\n"+song.getAuthor(),song.getPath().toString()));
        }





    if(jcAudios!=null && jcAudios.size()>0)
    {
    Glide.with(container).load(playlist.getSongs().get(position).getImage_url()).into((ImageView) view.findViewById(R.id.player_song_img));
    jcplayerView.initPlaylist(jcAudios, null);
    jcplayerView.createNotification(R.drawable.altas_notes);
    }


        jcplayerView.playAudio(jcAudios.get(position));
/**
 *         if(jcplayerView.getCurrentAudio()!=null){
 *             //Here I check if I get back to song I been before.
 *             // If I been I want to seekTo current timeStamp.
 *             if (jcplayerView.getCurrentAudio().hashCode() == jcAudios.get(position).hashCode())
 *             {
 *                 System.out.println("Same song!");
 *
 *             } else{
 *
 *             }
 *         }else{
 *             jcplayerView.playAudio(jcAudios.get(position));
 *         }
 */


    fav_btn.setOnClickListener(v->{

        if( fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState()))
        {
            fav_btn.setImageResource(R.drawable.ic_heart_full);
        }else{
            fav_btn.setImageResource(R.drawable.ic_heart_empty);
        }
    });
      return view;
    }


}

