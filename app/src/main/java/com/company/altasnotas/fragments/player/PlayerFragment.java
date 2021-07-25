package com.company.altasnotas.fragments.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.os.ParcelFileDescriptor;
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
import com.company.altasnotas.services.BackgroundService;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    private BackgroundService mService;
    private boolean mBound = false;
    private Intent intent;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            initializePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

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
        playerView= view.findViewById(R.id.player_view);
        playerView.setBackgroundColor(Color.TRANSPARENT);
        setUI();

         intent = new Intent(getActivity(), BackgroundService.class);
        intent.putExtra("playlist", playlist);
        intent.putExtra("pos", position);
        intent.putParcelableArrayListExtra("songs", playlist.getSongs());
        Util.startForegroundService(getActivity(), intent);



    fav_btn = view.findViewById(R.id.player_song_fav_btn);
    settings_btn = view.findViewById(R.id.player_song_options_btn);

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


    private void initializePlayer() {
        if (mBound) {
            SimpleExoPlayer player = mService.getPlayerInstance();
            player.addListener(new ExoListener(player));
            playerView.setPlayer(player);
            playerView.setUseController(true);
            playerView.showController();
            playerView.setControllerShowTimeoutMs(0);
            playerView.setCameraDistance(0);
            playerView.setControllerAutoShow(true);
            playerView.setControllerHideOnTouch(false);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void setUI() {

        title.setText(playlist.getSongs().get(position).getTitle());
        author.setText(playlist.getSongs().get(position).getAuthor());
        Glide.with(requireContext()).load(playlist.getSongs().get(position).getImage_url()).into(song_img);



    }

   public class ExoListener implements Player.Listener{
        SimpleExoPlayer player;
        public ExoListener(SimpleExoPlayer player){
            this.player=player;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            // Video playback status
            title.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getTitle());
            author.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getAuthor());
            Glide.with(getContext()).load(playlist.getSongs().get(player.getCurrentWindowIndex()).getImage_url()).into(song_img);
            System.out.println("Gramy piosenke: "+playlist.getSongs().get((int) player.getCurrentWindowIndex()).getTitle());

            Log.d("playbackState = " + playbackState + " playWhenReady = " + playWhenReady,"Exo");
            switch (playbackState){
                case Player.STATE_IDLE:
                    // free
                    break;
                case Player.STATE_BUFFERING:
                    // Buffer
                    break;
                case Player.STATE_READY:
                    // Get ready
                    break;
                case Player.STATE_ENDED:
                    // End
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Report errors
            switch (error.type){
                case ExoPlaybackException.TYPE_SOURCE:
                    // Error loading resources
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    // Errors in rendering
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    // unexpected error
                    break;
            }
        }

    }




}

