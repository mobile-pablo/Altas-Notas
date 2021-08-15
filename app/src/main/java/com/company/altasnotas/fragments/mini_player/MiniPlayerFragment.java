package com.company.altasnotas.fragments.mini_player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;


import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.services.BackgroundService;
import com.company.altasnotas.viewmodels.fragments.player.PlayerFragmentViewModel;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MiniPlayerFragment extends Fragment {
  public   MainActivity mainActivity;
    private  Playlist playlist;
    private ImageButton fav_btn;
    private DatabaseReference database_ref;
    private  FirebaseAuth mAuth;
    int position;
    public  PlayerView playerView;
    public BackgroundService mService;
    private boolean mBound = false;
    public  Intent intent;
    private Long seekedTo;
    private  Boolean isReOpen;
    private PlayerFragment  playerFragment;
    private PlayerFragmentViewModel viewModel;
    private  ImageView song_img;
    private  ExoListener exoListener;
    private  TextView title;
    private TextView author;
    private LinearLayout box;
    private ImageButton dismiss_btn;
    public ServiceConnection mConnection = new ServiceConnection() {
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


    public MiniPlayerFragment(Playlist playlist, int position, long seekedTo, Boolean isReOpen, PlayerFragment playerFragment) {
        this.playlist = null;
        this.playlist = playlist;
        this.position = position;
        this.seekedTo = seekedTo;
        this.isReOpen=isReOpen;
        this.playerFragment=playerFragment;
        //We are sending playlist to this player and let it play all of it
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       View view =  inflater.inflate(R.layout.fragment_mini_player, container, false);
        playerView  =view.findViewById(R.id.mini_player_view);
        mainActivity= (MainActivity) getActivity();
        title = view.findViewById(R.id.mini_player_title);
        title.setSelected(true);
        author = view.findViewById(R.id.mini_player_description);
        author.setSelected(true);
        song_img = view.findViewById(R.id.mini_player_img);
        box = view.findViewById(R.id.mini_player_small_box);
        fav_btn = view.findViewById(R.id.mini_player_fav_btn);
        dismiss_btn = view.findViewById(R.id.mini_player_dismiss_btn);
        database_ref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(PlayerFragmentViewModel.class);
        setUI();

        box.setOnClickListener(v -> {

            if(mBound){
                SimpleExoPlayer player = mService.getPlayerInstance();
                if(!(player.getPlayWhenReady() && player.getPlaybackState() == Player.STATE_READY)){
                    playerFragment.setSongState(false);
                }else{
                    playerFragment.setSongState(true);
                }
            }


            Fragment fragment =  getActivity().getSupportFragmentManager().findFragmentByTag("Player");
            if(fragment != null)
            {
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }

            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left)
                    .replace(R.id.main_fragment_container,playerFragment, "Player")
                    .addToBackStack(null)
                    .commit();
        });


        dismiss_btn.setOnClickListener(v->{
            dissmiss_mini();
        });

        fav_btn.setOnClickListener(v -> {

            if (fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {

                viewModel.addToFav( getActivity(),database_ref,mAuth,playlist,position,fav_btn, CurrentPlaylistFragment.adapter);
            } else {
                viewModel.removeFromFav( getActivity(), database_ref,mAuth,playlist,position,fav_btn, CurrentPlaylistFragment.adapter);
            }
        });


        return view;
    }

    public  void dissmiss_mini() {
        MainActivity.mini_player.setVisibility(View.GONE);

        if(playerView!=null) {
            if (playerView.getPlayer() != null) {
                playerView.getPlayer().pause();
                playerView.getPlayer().seekTo(0);
                playerView.setPlayer(null);
            }
            mService.destroyNotif();
      //      mService.stopForeground(false);
   //         mService.stopSelf();
       //     mService.playerNotificationManager.setPlayer(new SimpleExoPlayer.Builder(getContext()).setHandleAudioBecomingNoisy(true).build());
   //         PlayerFragment.mService.playerNotificationManager.setPlayer(new SimpleExoPlayer.Builder(getContext()).setHandleAudioBecomingNoisy(true).build());
        }

        MainActivity.currentSongTitle="";
        MainActivity.currentSongAlbum="";
        MainActivity.currentSongAuthor="";

        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
        if (currentFragment instanceof CurrentPlaylistFragment) {
            CurrentPlaylistFragment.adapter.notifyDataSetChanged();
        }

     Log.d("MiniPlayerFragment", "Mini Player dismissed!");
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        intent = new Intent(getActivity(), BackgroundService.class);
        intent.putExtra("playlist", playlist);
        intent.putExtra("pos", position);
        intent.putExtra("path", playlist.getSongs().get(position).getPath());
        intent.putExtra("playlistTitle", playlist.getTitle());
        intent.putExtra("desc", playlist.getDescription());
        intent.putExtra("ms", seekedTo);
        intent.putParcelableArrayListExtra("songs", playlist.getSongs());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Util.startForegroundService(getActivity(), intent);
        } else {
            getActivity().startService(intent);
        }

    }

    public  void setUI() {
        if(getActivity()!=null) {
            Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_mini_player_container);
            if (currentFragment instanceof MiniPlayerFragment) {
                Glide.with(getActivity()).load(playlist.getSongs().get(position).getImage_url()).error(R.drawable.img_not_found).into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if(resource!=null) {
                            song_img.setImageDrawable(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

                database_ref = FirebaseDatabase.getInstance().getReference();
                database_ref.child("music").child("albums").child(playlist.getSongs().get(position).getAuthor()).child(playlist.getSongs().get(position).getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        title.setText(playlist.getSongs().get(position).getTitle());
                        author.setText(snapshot.child("description").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }
        }
        if (fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_full).getConstantState())) {
            fav_btn.setImageResource(R.drawable.ic_heart_empty);
        }
        //Loading fav btn state
    if(mAuth.getCurrentUser()!=null){
        database_ref.child("fav_music")
                .child(mAuth.getCurrentUser().getUid())
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot != null) {
                            Song mySong = playlist.getSongs().get(position);

                            for (DataSnapshot ds : snapshot.getChildren()) {

                                if (
                                        ds.child("album").getValue().equals(mySong.getAlbum())
                                                &&
                                                ds.child("author").getValue().equals(mySong.getAuthor())
                                ) {
                                    //Same album and Author now we check song title
                                    database_ref
                                            .child("music")
                                            .child("albums")
                                            .child(mySong.getAuthor())
                                            .child(mySong.getAlbum())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snap) {

                                                    for (DataSnapshot s : snap.child("songs").getChildren()) {

                                                        if (
                                                                s.child("order").getValue().toString().trim().equals(ds.child("numberInAlbum").getValue().toString().trim())
                                                                        &&
                                                                        s.child("title").getValue().equals(mySong.getTitle())
                                                        ) {
                                                            //We found a song in Album and We need to set icon
                                                            fav_btn.setImageResource(R.drawable.ic_heart_full);
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    }
    public  void initializePlayer() {
        if (mBound) {
            SimpleExoPlayer player = mService.getPlayerInstance();
            exoListener = new ExoListener(player);
            player.addListener(exoListener);

            playerView.setKeepContentOnPlayerReset(true);
            playerView.setPlayer(player);
            playerView.setUseController(true);
            playerView.showController();
            playerView.setControllerShowTimeoutMs(0);
            playerView.setCameraDistance(0);
            playerView.setControllerAutoShow(true);
            player.setPlayWhenReady(true);
            playerView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
            playerView.setShutterBackgroundColor(Color.TRANSPARENT);
            playerView.setControllerHideOnTouch(false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUI();
    }



    public  class ExoListener implements Player.Listener {
        SimpleExoPlayer player;

        public ExoListener(SimpleExoPlayer player) {
            this.player = player;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
          if(position!=player.getCurrentWindowIndex()){
              MainActivity.currentSongTitle = playlist.getSongs().get(position).getTitle();
              MainActivity.currentSongAlbum=playlist.getTitle();
              MainActivity.currentSongAuthor=playlist.getDescription();
          }
            mService.setPosition(player.getCurrentWindowIndex());
            position = player.getCurrentWindowIndex();
            playerView.setPlayer(player);
            Log.d("Exo","playbackState = " + playbackState + " playWhenReady = " + playWhenReady );
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
            MainActivity.currentSongTitle = playlist.getSongs().get(position).getTitle();
            MainActivity.currentSongAlbum=playlist.getTitle();
            MainActivity.currentSongAuthor=playlist.getDescription();
            mService.setPosition(position);
            setUI();
          if(getActivity()!=null){
              Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
              if (currentFragment instanceof CurrentPlaylistFragment) {
                  CurrentPlaylistFragment.adapter.notifyDataSetChanged();
              }
          }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        initializePlayer();
        setUI();
    }
}