package com.company.altasnotas.fragments.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.services.BackgroundService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;


public class PlayerFragment extends Fragment {
    private FirebaseStorage storage;
    private ImageButton fav_btn;
    private Button settings_btn;
    private Playlist playlist;
    int position;
    private ImageView song_img;
    private ExoListener exoListener;
    private TextView title, author;


    private PlayerView playerView;
    private BackgroundService mService;
    private boolean mBound = false;
    private Intent intent;
    private final ServiceConnection mConnection = new ServiceConnection() {
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

    public PlayerFragment(Playlist playlist, int position) {
        this.playlist=null;
        this.playlist = playlist;
        this.position = position;
        //We are sending playlist to this player and let it play all of it
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        storage = FirebaseStorage.getInstance();
        title = view.findViewById(R.id.player_song_title);
        author = view.findViewById(R.id.player_song_author);
        song_img = view.findViewById(R.id.player_song_img);
        playerView = view.findViewById(R.id.player_view);
        playerView.setBackgroundColor(Color.TRANSPARENT);
        setUI();



        intent = new Intent(getActivity(), BackgroundService.class);
        System.out.println("Playlist name: "+playlist.getTitle());
        intent.putExtra("playlist", playlist);
        intent.putExtra("pos", position);
        intent.putExtra("path", playlist.getSongs().get(position).getPath());
        intent.putExtra("playlistTitle", playlist.getTitle());
        intent.putParcelableArrayListExtra("songs", playlist.getSongs());
            Util.startForegroundService(getActivity(), intent);


        fav_btn = view.findViewById(R.id.player_song_fav_btn);
        settings_btn = view.findViewById(R.id.player_song_options_btn);

        fav_btn.setOnClickListener(v -> {

            if (fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
                fav_btn.setImageResource(R.drawable.ic_heart_full);
            } else {
                fav_btn.setImageResource(R.drawable.ic_heart_empty);
            }
        });
        return view;
    }


    private void initializePlayer() {
        if (mBound) {
            SimpleExoPlayer player = mService.getPlayerInstance();
            exoListener = new ExoListener(player);
            player.addListener(exoListener);
            playerView.setShutterBackgroundColor(Color.TRANSPARENT);
            playerView.setKeepContentOnPlayerReset(true);
            playerView.setPlayer(player);
            playerView.setUseController(true);
            playerView.showController();
            playerView.setControllerShowTimeoutMs(0);
            playerView.setCameraDistance(0);
            playerView.setControllerAutoShow(true);
            player.setPlayWhenReady(true);
            playerView.setControllerHideOnTouch(false);

        }
    }

    @Override
    public void onStart() {
        super.onStart();
      //  intent = new Intent(getActivity(), BackgroundService.class);
      //  getActivity().stopService(intent);
        requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void setUI() {

        title.setText(playlist.getSongs().get(position).getTitle());
        author.setText(playlist.getSongs().get(position).getAuthor());
        Glide.with(requireContext()).load(playlist.getSongs().get(position).getImage_url()).into(song_img);


    }


    public class ExoListener implements Player.Listener {
        SimpleExoPlayer player;

        public ExoListener(SimpleExoPlayer player) {
            this.player = player;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            Log.d("playbackState = " + playbackState + " playWhenReady = " + playWhenReady, "Exo");
            switch (playbackState) {
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
            switch (error.type) {
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

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            position = player.getCurrentWindowIndex();
            System.out.println("Gramy piosenke: " + playlist.getSongs().get(player.getCurrentWindowIndex()).getTitle());
            title.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getTitle());
            author.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getAuthor());
            Glide.with(getContext()).load(playlist.getSongs().get(player.getCurrentWindowIndex()).getImage_url()).into(song_img);
            mService.setPosition(position);
        }
    }


}

