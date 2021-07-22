package com.company.altasnotas.fragments.player;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.ArrayList;

import javax.sql.DataSource;


public class PlayerFragment extends Fragment {
    private FirebaseStorage storage;
    private ImageButton fav_btn;
    private Button settings_btn;
    private final Playlist playlist;
    int position;
    private ImageView song_img;

    private TextView title, author;


    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;

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
      title = view.findViewById(R.id.player_song_title);
      author = view.findViewById(R.id.player_song_author);
      song_img =view.findViewById(R.id.player_song_img);



        title.setText(playlist.getSongs().get(position).getTitle());
        author.setText(playlist.getSongs().get(position).getAuthor());
        Glide.with(getContext()).load(playlist.getSongs().get(position).getImage_url()).into(song_img);



        initPlayer(view);





    fav_btn = view.findViewById(R.id.player_song_fav_btn);
    settings_btn = view.findViewById(R.id.player_song_options_btn);

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

    private void initPlayer(View view) {

        playerView= view.findViewById(R.id.player_view);
        playerView.setControllerShowTimeoutMs(0);
        playerView.setCameraDistance(0);
        simpleExoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
        playerView.setPlayer(simpleExoPlayer);

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "app"));
            ArrayList<MediaSource> mediaSources = new ArrayList<>();
            for (Song song : playlist.getSongs()) {
                MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(song.getPath()));
                mediaSources.add(audioSource);
            }




            ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
            concatenatingMediaSource.addMediaSources(mediaSources);

            simpleExoPlayer.seekTo(position, C.INDEX_UNSET);

            simpleExoPlayer.prepare(concatenatingMediaSource,false,false);
            simpleExoPlayer.setPlayWhenReady(false);

            simpleExoPlayer.addListener(new ExoListener());

    }



    class ExoListener implements Player.Listener{
        Player player = playerView.getPlayer();
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            title.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getTitle());
            author.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getAuthor());
            Glide.with(getContext()).load(playlist.getSongs().get(player.getCurrentWindowIndex()).getImage_url()).into(song_img);
            if (isPlaying) {
                System.out.println("Gramy piosenke: "+playlist.getSongs().get((int) player.getCurrentWindowIndex()).getTitle());

            } else {
               if(player.getPlayWhenReady()==true){
               //Zmiana piosenki
                   }else{
                   System.out.println(player.getPlaybackState());

               }
            }
        }


        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                IOException cause = error.getSourceException();
                if (cause instanceof HttpDataSource.HttpDataSourceException) {
                    // An HTTP error occurred.
                    HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                    // This is the request for which the error occurred.
                    DataSpec requestDataSpec = httpError.dataSpec;
                    // It's possible to find out more about the error both by casting and by
                    // querying the cause.
                    if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                        // Cast to InvalidResponseCodeException and retrieve the response code,
                        // message and headers.
                    } else {
                        // Try calling httpError.getCause() to retrieve the underlying cause,
                        // although note that it may be null.
                    }
                }
            }
        }

    }


}

