package com.company.altasnotas.fragments.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.ChoosePlaylistAdapter;
import com.company.altasnotas.databinding.FragmentPlayerBinding;
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.services.BackgroundService;
import com.company.altasnotas.viewmodels.fragments.player.PlayerFragmentViewModel;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Random;


public class PlayerFragment extends Fragment {
    public static Boolean isDimissed;

    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            if(!isReOpen){
                mService.clearOldPlayer();
                mService.initializePlayer(intent);
            }
            initializePlayer();
            initializeMiniPlayer();

            Log.d("PlayerFragment", "OnService Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public static boolean mBound = false;
    public static PlayerView playerView;
    public static BackgroundService mService;
    public static ImageButton fav_btn;
    public static FragmentPlayerBinding binding;
    public static ImageButton shuffleBtn, repeatBtn;
    public static Boolean isChangingFragment;
    public static Boolean isFirstOpen;

    private Long seekedTo;
    private Boolean isReOpen;

    private LinearLayout playerUpperBox;
    private ImageButton settings_btn, hide_btn;
    private Playlist playlist;
    private int position;
    private ImageView song_img;
    private Palette palette;
    private ExoListener exoListener;
    private TextView title, author, current_info, current_info_title;
    private ImageButton next_btn, prev_btn;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
    private Intent intent;
    private LinearLayout player_full_box;
    private ConstraintLayout player_small_box;
    private  Integer isFav;
    private Integer state;
    private Boolean ready;
    private Boolean shouldPlay = null;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetDialog choosePlaylistDialog;
    private BottomSheetDialog songInPlaylistDialog;
    private PlayerFragmentViewModel viewModel;
    private MainActivity mainActivity;
    public VideoView videoView;
    private MediaPlayer mediaPlayer;
    public static   int randomPosition;
    public static Integer shuffleClicked=0;

    //Mini Player
    public static ImageButton mini_fav_btn;
    private TextView mini_player_title, mini_player_desc;
    private ImageView mini_player_img;
    private ImageButton mini_next_btn, mini_prev_btn;
    private DefaultTimeBar miniTimeBar;

    public PlayerFragment(Playlist playlist, int position, long seekedTo, Boolean isReOpen, Integer state, Boolean ready, Integer isFav, Boolean isChangingFragment,Boolean isFirstOpen) {
        this.playlist = null;
        this.playlist = playlist;
        this.position = position;
        this.seekedTo = seekedTo;
        this.isReOpen = isReOpen;
        this.state = state;
        this.ready = ready;
        this.isFav = isFav;
        this.isChangingFragment= isChangingFragment;
        this.isFirstOpen=isFirstOpen;

        //We are sending playlist to this player and let it play all of it
       /*
       isFav
        0  - Playlist
       -1  - Album
        1  - Fav

         */
    }

    public void reinitializeFragment(Playlist playlist, int position, long seekedTo, Boolean isReOpen, Integer state, Boolean ready, Integer isFav, Boolean isChangingFragment) {
        this.playlist = null;
        this.playlist = playlist;
        this.position = position;
        this.seekedTo = seekedTo;
        this.isReOpen = isReOpen;
        this.state = state;
        this.ready = ready;
        this.isFav = isFav;
        this.isChangingFragment= isChangingFragment;
        mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        isDimissed=false;
        clearVideoView();

        videoView.setVisibility(View.INVISIBLE);
        startMusicService();

     if(mBound){
         mainActivity.unbindService(mConnection);
     }
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                BackgroundService.LocalBinder binder = (BackgroundService.LocalBinder) iBinder;
                mService = binder.getService();
                mBound = true;
                if(!isReOpen){
                    mService.clearOldPlayer();
                    mService.initializePlayer(intent);
                }
                initializePlayer();
                initializeMiniPlayer();
                updateUI(mService.getPlayerInstance());
                Log.d("PlayerFragment", "OnService Connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBound = false;
            }
        };

        if(!isDimissed)
        {
            mainActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        mainActivity = (MainActivity) getActivity();
        viewModel = new ViewModelProvider(mainActivity).get(PlayerFragmentViewModel.class);
        if(!isDimissed){
            playerView = binding.playerView.findViewById(R.id.playerView);
            player_full_box = binding.playerView.findViewById(R.id.playerFullBox);
            playerUpperBox = playerView.findViewById(R.id.playerSongUpperBox);
            player_small_box = playerView.findViewById(R.id.playerSmallBox);

            title = playerView.findViewById(R.id.playerSongTitle);
            author = playerView.findViewById(R.id.playerSongDescription);
            song_img = playerView.findViewById(R.id.playerSongImg);
            next_btn= playerView.findViewById(R.id.exo_next);
            prev_btn = playerView.findViewById(R.id.exo_prev);
            shuffleBtn = playerView.findViewById(R.id.customShuffle);
            repeatBtn= playerView.findViewById(R.id.customRepeat);

            videoView = binding.playerView.findViewById(R.id.playerVideoView);
            current_info = playerView.findViewById(R.id.playerSongInfoTextView);
            current_info_title = playerView.findViewById(R.id.playerSongInfoPlaylistTextView);

            mainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);

            fav_btn = binding.playerView.findViewById(R.id.playerSongFavBtn);
            settings_btn = binding.playerView.findViewById(R.id.playerSongSettingsBtn);
            hide_btn = binding.playerView.findViewById(R.id.playerSongHideBtn);
            database_ref = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();

            startMusicService();

            //Mini Player
            findMiniPlayer();

            setUPSlideListener();

            setUpInfoBox();

            reinitializeShuffleBtn();
            reinitializeRepeatBtn();

            fav_btn.setOnClickListener(v -> {

                if (fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {

                    viewModel.addToFav(mainActivity, database_ref, mAuth, playlist, position, fav_btn, mini_fav_btn, CurrentPlaylistFragment.adapter);
                } else {
                    viewModel.removeFromFav(mainActivity, database_ref, mAuth, playlist, position, fav_btn, mini_fav_btn, CurrentPlaylistFragment.adapter);
                }
            });

            settings_btn.setOnClickListener(v -> {
                if (isFav == 0) {
                    openSongInPlaylistsSettingsDialog();
                } else {
                    openSettingsDialog();
                }
            });

            hide_btn.setOnClickListener(v->{
                mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            });

            mini_fav_btn.setOnClickListener(v -> {
               if(viewModel==null){
                   viewModel = new ViewModelProvider(mainActivity).get(PlayerFragmentViewModel.class);
               }
                if (mini_fav_btn.getDrawable().getConstantState().equals(mini_fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState()))
                {
                    viewModel.addToFav(mainActivity,
                            database_ref,
                            mAuth,
                            playlist,
                            position,
                            fav_btn,
                            mini_fav_btn,
                            CurrentPlaylistFragment.adapter);
                }
                else
                {
                    viewModel.removeFromFav(mainActivity,
                            database_ref,
                            mAuth,
                            playlist,
                            position,
                            fav_btn,
                            mini_fav_btn,
                            CurrentPlaylistFragment.adapter);
                }
            });

            shuffleBtn.setOnClickListener(v->{
                if (shuffleBtn.getDrawable().getConstantState().equals(shuffleBtn.getContext().getDrawable(R.drawable.ic_shuffle).getConstantState()))
                {
                    if(playlist.getSongs().size()>1){
                        do{
                            randomPosition = new Random().nextInt(playlist.getSongs().size());
                        }
                        while (randomPosition==position);
                    }else{
                        randomPosition=position;
                    }

                    mService.setShuffleEnabled(true);
                      shuffleBtn.setImageResource(R.drawable.ic_shuffle_clicked);
                }
                else
                {
                    mService.setShuffleEnabled(false);
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle);
                    mService.getPlayerInstance().setShuffleModeEnabled(false);
                }
            });

            repeatBtn.setOnClickListener(v -> {
                SimpleExoPlayer player= mService.getPlayerInstance();
                if (repeatBtn.getDrawable().getConstantState().equals(repeatBtn.getContext().getDrawable(R.drawable.ic_repeat).getConstantState()))
                {
                    player.setRepeatMode(Player.REPEAT_MODE_ALL);
                    mService.setRepeat(Player.REPEAT_MODE_ALL);
                    repeatBtn.setImageResource(R.drawable.ic_repeat_clicked);
                }else if(repeatBtn.getDrawable().getConstantState().equals(repeatBtn.getContext().getDrawable(R.drawable.ic_repeat_clicked).getConstantState())){
                    player.setRepeatMode(Player.REPEAT_MODE_ONE);
                    mService.setRepeat(Player.REPEAT_MODE_ONE);
                    repeatBtn.setImageResource(R.drawable.ic_repeat_one);
                }else{
                    player.setRepeatMode(Player.REPEAT_MODE_OFF);
                    mService.setRepeat(Player.REPEAT_MODE_OFF);
                    repeatBtn.setImageResource(R.drawable.ic_repeat);
                }

            });

            next_btn.setOnClickListener(v -> {
                clearVideoView();
                if(mService!=null){
                    SimpleExoPlayer player = mService.getPlayerInstance();
                   if( player.getRepeatMode()==Player.REPEAT_MODE_ONE){
                       player.seekTo(0);
                       updateUI(player);
                   }else{
                       trackChanged(1);
                   }

                }
            });

            prev_btn.setOnClickListener(v -> {
                videoView.setVisibility(View.INVISIBLE);
                SimpleExoPlayer player = mService.getPlayerInstance();
                if( player.getRepeatMode()==Player.REPEAT_MODE_ONE){
                    player.seekTo(0);
                    updateUI(player);
                }else{
                    trackChanged(0);
                }
            });
        }

        return view;
    }

    private void clearVideoView() {
        mediaPlayer=null;
        videoView.setVisibility(View.INVISIBLE);
        videoView.stopPlayback();
        videoView.clearAnimation();
        videoView.suspend();
        videoView.setVideoURI(null);
    }

    private void trackChanged(Integer shouldAdd) {

      if(shouldAdd!=null){
          changePosition(mService.getPlayerInstance(), shouldAdd);
      }

      updateUI(mService.getPlayerInstance());
    }

    private void reinitializeRepeatBtn() {
        if(mService!=null){
            Integer isRepeated = mService.getRepeat();
            if(isRepeated!=null){
                switch (isRepeated){
                    case 2: repeatBtn.setImageResource(R.drawable.ic_repeat_clicked); break;
                    case 1: repeatBtn.setImageResource(R.drawable.ic_repeat_one); break;
                    case 0: repeatBtn.setImageResource(R.drawable.ic_repeat); break;
                    default: Log.d("ExoPlayer", "Wrong State on Repeat Btn!"); break;
                }
            }
        }
    }


    private void startMusicService() {
        intent = new Intent(getActivity(), BackgroundService.class);
        intent.putExtra("playlist", playlist);
        intent.putExtra("pos", position);
        intent.putExtra("path", playlist.getSongs().get(position).getPath());
        intent.putExtra("playlistTitle", playlist.getTitle());
        intent.putExtra("desc", playlist.getDescription());
        intent.putExtra("ms", seekedTo);
        intent.putExtra("isFav", isFav);
        intent.putParcelableArrayListExtra("songs", playlist.getSongs());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(mainActivity, intent);

        } else {
            mainActivity.startService(intent);
        }
    }

    private void reinitializeShuffleBtn() {
        if(mService!=null){
         Boolean isShuffled = mService.getShuffle();
            if(isShuffled!=null){
                if(isShuffled){
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_clicked);
                }else{
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle);
                }
            }
        }
    }

    private void setUpInfoBox() {
        String message =null;
        switch (isFav){
            case -1: message = "Album"; break;
            case 0: message = "Playlist";break;
            case 1: message = "";break;
        }

        current_info.setText("Playing from "+message);
        current_info_title.setText(playlist.getTitle());
    }

    private void setUPSlideListener() {
          mainActivity.activityMainBinding.slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    binding.miniPlayerView.setVisibility(View.GONE);
                }

                if (newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    binding.miniPlayerView.setVisibility(View.VISIBLE);
                }

                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    binding.miniPlayerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void findMiniPlayer() {
        mini_fav_btn = binding.miniPlayerView.findViewById(R.id.mini_player_fav_btn);
        mini_player_title = binding.miniPlayerView.findViewById(R.id.mini_player_title);
        mini_player_desc = binding.miniPlayerView.findViewById(R.id.mini_player_description);
        mini_next_btn = binding.miniPlayerView.findViewById(R.id.exo_next);
        mini_prev_btn =binding.miniPlayerView.findViewById(R.id.exo_prev);
        mini_player_img = binding.miniPlayerView.findViewById(R.id.mini_player_img);
        miniTimeBar = binding.miniPlayerView.findViewById(R.id.exo_progress);
        miniTimeBar.setEnabled(false);
        mini_player_title.setSelected(true);
        mini_player_desc.setSelected(true);

        mini_next_btn.setOnClickListener(v->{
            next_btn.callOnClick();
        });
        mini_prev_btn.setOnClickListener(v -> {
            prev_btn.callOnClick();
        });
    }

    public void dismissPlayer() {
        clearVideoView();
        mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        MainActivity.clearCurrentSong();

        if (playerView != null) {
            mService.destroyNotif();
        }

        MainActivity.clearCurrentSong();
        notifyAdapters();


        shouldPlay=true;
        if (mBound) {
        mainActivity.unbindService(mConnection);
        mBound=false;
        }

        Log.d("PlayerFragment", "Player dismissed!");
        isDimissed=true;
    }

    private void notifyAdapters() {
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (currentFragment instanceof CurrentPlaylistFragment) {
            if (CurrentPlaylistFragment.adapter != null) {
                CurrentPlaylistFragment.adapter.notifyDataSetChanged();
            }
        }
        if (currentFragment instanceof FavoritesFragment) {
            FavoritesFragment favoritesFragment = (FavoritesFragment) currentFragment;
            if (favoritesFragment.viewModel != null) {
                if (favoritesFragment.viewModel.adapter != null) {
                    favoritesFragment.viewModel.adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void initializeMiniPlayer() {
        if (mBound) {
            if (mService.position != null) {
                SimpleExoPlayer player = mService.getPlayerInstance();
                exoListener = new ExoListener(player);
                player.addListener(exoListener);
                binding.miniPlayerView.setKeepContentOnPlayerReset(true);
                binding.miniPlayerView.setPlayer(player);
                binding.miniPlayerView.setUseController(true);
                binding.miniPlayerView.showController();
                binding.miniPlayerView.setControllerShowTimeoutMs(0);
                binding.miniPlayerView.setCameraDistance(0);
                binding.miniPlayerView.setControllerAutoShow(true);
                binding.miniPlayerView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
                binding.miniPlayerView.setShutterBackgroundColor(Color.TRANSPARENT);
                binding.miniPlayerView.setControllerHideOnTouch(false);
            }
        }
    }

    private void openSongInPlaylistsSettingsDialog() {
        songInPlaylistDialog = new BottomSheetDialog(getContext());
        songInPlaylistDialog.setContentView(R.layout.bottom_playlist_song_player_settings_layout);

        LinearLayout showAlbum = songInPlaylistDialog.findViewById(R.id.bottomSettingsAlbumBox);
        LinearLayout showPlaylist = songInPlaylistDialog.findViewById(R.id.bottomSettingsShowPlaylistBox);
        LinearLayout share = songInPlaylistDialog.findViewById(R.id.bottomSettingsShareBox);
        LinearLayout dismissDialog = songInPlaylistDialog.findViewById(R.id.bottomSettingsDismissBox);

        showAlbum.setOnClickListener(v -> {
            //Shows album
            //Download playlist
            Playlist x = new Playlist();
            if (mAuth.getCurrentUser() != null) {

                database_ref.child("music").child("albums").child(playlist.getSongs().get(position).getAuthor()).child(playlist.getSongs().get(position).getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null)
                        {
                            x.setImage_id(snapshot.child("image_id").getValue().toString());
                            x.setYear(snapshot.child("year").getValue().toString());
                            x.setTitle(snapshot.child("title").getValue().toString());
                            x.setDescription(snapshot.child("description").getValue().toString());
                            x.setDir_title(playlist.getSongs().get(position).getAlbum());
                            x.setDir_desc(playlist.getSongs().get(position).getAuthor());
                            songInPlaylistDialog.dismiss();
                            mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(playlist.getSongs().get(position).getAuthor(), playlist.getSongs().get(position).getAlbum(), x, 1)).addToBackStack("null").commit();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.d(MainActivity.FIREBASE, "Error: " + error.getMessage());
                    }

                });


            }
        });

        showPlaylist.setOnClickListener(v -> {
            for (int i = 0; i < getActivity().getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(playlist.getTitle(), "", playlist, 0)).addToBackStack("null").commit();
            mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            songInPlaylistDialog.dismiss();
        });

        share.setOnClickListener(v -> {
            share();
            mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            songInPlaylistDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> songInPlaylistDialog.dismiss());

        songInPlaylistDialog.show();
    }

    private void openSettingsDialog() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_song_settings_layout);

        LinearLayout showAlbum = bottomSheetDialog.findViewById(R.id.bottomSettingsAlbumBox);
        LinearLayout addToPlaylist = bottomSheetDialog.findViewById(R.id.bottomSettingsPlaylistsBox);
        LinearLayout share = bottomSheetDialog.findViewById(R.id.bottomSettingsShareBox);
        LinearLayout dismissDialog = bottomSheetDialog.findViewById(R.id.bottomSettingsDismissBox);

        showAlbum.setOnClickListener(v -> {
            //Shows album
            //Download playlist
            Playlist x = new Playlist();
            if (mAuth.getCurrentUser() != null) {

                database_ref.child("music").child("albums").child(playlist.getSongs().get(position).getAuthor()).child(playlist.getSongs().get(position).getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null) {
                            x.setImage_id(snapshot.child("image_id").getValue().toString());
                            x.setYear(snapshot.child("year").getValue().toString());
                            x.setTitle(snapshot.child("title").getValue().toString());
                            x.setDescription(snapshot.child("description").getValue().toString());
                            x.setDir_title(playlist.getSongs().get(position).getAlbum());
                            x.setDir_desc(playlist.getSongs().get(position).getAuthor());
                            bottomSheetDialog.dismiss();
                            mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(playlist.getSongs().get(position).getAuthor(), playlist.getSongs().get(position).getAlbum(), x, 1)).addToBackStack("null").commit();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.d(MainActivity.FIREBASE, "Error: " + error.getMessage());
                    }

                });


            }
        });

        addToPlaylist.setOnClickListener(v -> {
            //Add to playlist
            addToPlaylist();
            bottomSheetDialog.dismiss();
        });

        share.setOnClickListener(v -> {
            share();
            mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void addToPlaylist() {
        choosePlaylistDialog = new BottomSheetDialog(getContext());
        choosePlaylistDialog.setContentView(R.layout.choose_playlist_dialog);
        TextView chooseState = choosePlaylistDialog.findViewById(R.id.choosePlaylistRecyclerViewState);
        RecyclerView chooseRecyclerView = choosePlaylistDialog.findViewById(R.id.choosePlaylistRecyclerView);
        ArrayList<String> playlists_titles = new ArrayList<>();
        ArrayList<String> playlists_keys = new ArrayList<>();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int x = 0;
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    x++;

                    playlists_titles.add(playlistSnapshot.child("title").getValue().toString());
                    playlists_keys.add(playlistSnapshot.getKey());


                }

                if (snapshot.getChildrenCount() != 0) {
                    if (x == snapshot.getChildrenCount()) {


                        ChoosePlaylistAdapter choosePlaylistAdapter = new ChoosePlaylistAdapter((MainActivity) requireActivity(), choosePlaylistDialog, playlist.getSongs().get(position), playlists_titles, playlists_keys);
                        chooseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        choosePlaylistAdapter.notifyDataSetChanged();
                        chooseRecyclerView.setAdapter(choosePlaylistAdapter);
                    }

                    chooseState.setVisibility(View.GONE);
                    chooseRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    chooseState.setVisibility(View.VISIBLE);
                    chooseRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        choosePlaylistDialog.show();
    }

    private void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, "Altas Notas");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My favorite Song is \"" + playlist.getSongs().get(position).getTitle() + "\" from \"" + playlist.getSongs().get(position).getAuthor() + "\".\nListen this on \"Altas Notas\".\nExternal Link: [ " + playlist.getSongs().get(position).getPath() + " ]");
        startActivity(Intent.createChooser(shareIntent, "Share using"));
        getContext().startActivity(shareIntent);

    }

    public void initializePlayer() {
        if (mBound) {
            if (mService.position != null) {
                if(!isFirstOpen){
                mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                SimpleExoPlayer player = mService.getPlayerInstance();
                if(player==null){
                    player= mService.testingPlayer();
                //    player= mService.startPlayer();
                }
                exoListener = new ExoListener(player);
                player.addListener(exoListener);
                playerView.setKeepContentOnPlayerReset(true);
                playerView.setPlayer(player);
                playerView.setUseController(true);
                playerView.showController();
                playerView.setControllerShowTimeoutMs(0);
                playerView.setCameraDistance(0);
                playerView.setControllerAutoShow(true);

                Log.d("ExoPlayer", "State :"+state);
                Log.d("ExoPlayer", "Ready: "+ready);

                if (state == null || ready == null) {
                    if (isReOpen) {
                        //By this When Notification is Open and ExoPlayer is Paused. It remains that way.
                        player.setPlayWhenReady(player.getPlayWhenReady() && player.getPlaybackState() == Player.STATE_READY);
                    } else {
                        if (shouldPlay != null) {
                            player.setPlayWhenReady(shouldPlay);
                        } else {
                            player.setPlayWhenReady(true);
                        }
                    }


                } else {

                    player.setPlayWhenReady(ready && state == Player.STATE_READY);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!isDimissed)
        {
        mainActivity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void setUI(Integer position) {
        mAuth = FirebaseAuth.getInstance();
        if (mainActivity != null) {
            Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.slidingLayoutFrag);
            if (currentFragment instanceof PlayerFragment) {
                Glide.with(mainActivity).load(playlist.getSongs().get(position).getImage_url()).error(R.drawable.img_not_found).into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                       if(resource!=null){
                           song_img.setImageDrawable(resource);
                           mini_player_img.setImageDrawable(resource);
                           Bitmap b = drawableToBitmap(resource);
                           palette = Palette.from(b).generate();
                           videoView.setVisibility(View.VISIBLE);
                           setUpInfoBackgroundColor();
                           fav_btn.getDrawable().setTint(ContextCompat.getColor(mainActivity, R.color.project_light_orange));
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

                         if(snapshot.exists()){

                                     //We add extra space because This Light font have small spacing between words.
                                     String local_title = playlist.getSongs().get(position).getTitle();
                                     String local_desc =snapshot.child("description").getValue().toString();
                                     title.setTag(" ");
                                     String space = (String) title.getTag();
                                     local_title =local_title.replace(space, (space += " "));
                                     title.setText(local_title);
                                     mini_player_title.setText(local_title);
                                     title.setTag(space);


                                     author.setText(local_desc);
                                     mini_player_desc.setText(local_desc);
                         }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


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
                                                                                    fav_btn.getDrawable().setTint(ContextCompat.getColor(mainActivity, R.color.project_light_orange));
                                                                                    mini_fav_btn.setImageResource(R.drawable.ic_heart_full);
                                                                                    mini_fav_btn.getDrawable().setTint(ContextCompat.getColor(mainActivity, R.color.project_dark_velvet));


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

                if (mini_fav_btn.getDrawable().getConstantState().equals(mini_fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
                    mini_fav_btn.getDrawable().setTint(Color.BLACK);
                } else {
                    mini_fav_btn.getDrawable().setTint(ContextCompat.getColor(mainActivity, R.color.project_dark_velvet));
                }

            }
        } else {
            Log.d("Activity", "Activity is null");
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public void setSongState(boolean b) {
        shouldPlay = b;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(!isDimissed){
            initializePlayer();
            initializeMiniPlayer();
            reinitializeRepeatBtn();
            reinitializeShuffleBtn();
            if(mService!=null){
                if(mService.getPlayerInstance()!=null){
                  updateUI(mService.getPlayerInstance());
                }
            }
        }
    }

    public void setUpInfoBackgroundColor() {
            if(!playlist.getSongs().get(position).getGifUrl().isEmpty()) {
                initializeVideoView();
            }
            else {
                setUpDefaultBackground();
            }

        }

    private void setUpDefaultBackground() {
        if(playlist.getSongs().get(position).getGifUrl().isEmpty()){
            Palette.Swatch swatch =  palette.getDominantSwatch();
            if (swatch != null) {
                int swatchRgb = swatch.getRgb();

                String hex = Integer.toHexString(swatchRgb);
                Log.d("Hex", hex);
                String[] startCTable = hex.split("");
                String alphaHex ="",redHex ="", greenHex="", blueHex="";

                for (int i = 0; i <startCTable.length; i++) {
                    switch (i)
                    {
                        case 0:
                        case 1:
                            alphaHex += startCTable[i];
                            break;

                        case 2:
                        case 3:
                            redHex +=startCTable[i];
                            break;


                        case 4:
                        case 5:
                            greenHex +=startCTable[i];
                            break;


                        case 6:
                        case 7:
                            blueHex +=startCTable[i];
                            break;

                    }
                }
                //We have taken color from Swatch divided to seprate Colors and now we turn them into Integers

                Integer alphaInt, redInt, greenInt, blueInt;

                alphaInt = Integer.parseInt(alphaHex,16);
                redInt = manipulateColor(Integer.parseInt(redHex,16), 1.4f);
                greenInt = manipulateColor(Integer.parseInt(greenHex,16), 1.4f);
                blueInt = manipulateColor(Integer.parseInt(blueHex,16), 1.4f);

                Log.d("ColorLight","R: "+redInt+", "+ ", G: "+greenInt+", B: "+blueInt);

                Integer startColor = Color.argb(alphaInt,redInt, greenInt, blueInt);

                if(alphaInt>20)
                {
                    alphaInt -=0;
                }

                if ((redInt*0.299 + greenInt*0.587 + blueInt*0.114) > 130){

                    playerUpperBox.setBackground(ContextCompat.getDrawable(mainActivity,R.drawable.custom_player_upper_box_bg));
                }else{
                    playerUpperBox.setBackground(null);
                }

                Integer darkerRed =   manipulateColor(redInt,0.2f);
                Integer darkerGreen = manipulateColor(greenInt,0.2f);
                Integer darkerBlue =  manipulateColor(blueInt,0.2f);
                Log.d("ColorDark","R: "+darkerRed+", "+ ", G: "+darkerGreen+", B: "+darkerBlue);
                Integer endColor = Color.argb(alphaInt,darkerRed,darkerGreen,darkerBlue);

                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{startColor, endColor});





                player_small_box.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.INVISIBLE);

                Glide.with(mainActivity)
                        .load(gradientDrawable)
                        .error(R.drawable.custom_player_fragment_bg)
                        .into(new CustomTarget<Drawable>()
                        {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                player_full_box.setBackground(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

            }
    }
}

    public void initializeVideoView() {

        if(!playlist.getSongs().get(position).getGifUrl().isEmpty()) {
            if(mediaPlayer==null){
                System.out.println("Initialize VIDEO!");
                player_small_box.setVisibility(View.INVISIBLE);
                player_full_box.setBackgroundResource(R.drawable.custom_full_box_canvas_bg);
                String url = playlist.getSongs().get(position).getGifUrl();
                Uri uri = Uri.parse(url);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //set this BEFORE start playback
                    videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
                }

                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.d("VideoView", "Error");
                        return false;
                    }
                });

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer=mp;
                        mp.setVolume(0,0);
                        mp.setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                                .build());
                    }
                });

                //This line below helps not make big bandwidth usage
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.start();
                    }
                });


                // Init Video
                videoView.setMediaController(null);
                videoView.setVideoURI(uri);
                Log.d("VideoLayout", "VideoLayout setting source");

            }


         if(!videoView.isPlaying()){
             videoView.start();
             Log.d("VideoLayout", "VideoLayout started");
         }


        }else{
            Log.d("VideoLayout", "VideoLayout source is null");
        }
    }


    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public Integer getPosition() {
        return position;
    }

    public class ExoListener implements Player.Listener {
        SimpleExoPlayer player;

        public ExoListener(SimpleExoPlayer player) {
            this.player = player;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            playerView.setPlayer(player);
            state = player.getPlaybackState();
            ready = player.getPlayWhenReady();
            if (CurrentPlaylistFragment.adapter != null) {
                CurrentPlaylistFragment.adapter.notifyDataSetChanged();
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
            if(isFirstOpen){
                position=player.getCurrentWindowIndex();
                mService.setPosition(position);
                updateUI(player);
                isFirstOpen=false;
            }else{
                if(position!=player.getCurrentWindowIndex()){
                    clearVideoView();
                    position = player.getCurrentWindowIndex();
                    mService.setPosition(position);
                    updateUI(player);

                }
            }
        }
    }

    public void updateUI(SimpleExoPlayer player) {
        if(mainActivity.activityMainBinding.slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN){
            mainActivity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        fav_btn.setImageResource(R.drawable.ic_heart_empty);
        mini_fav_btn.setImageResource(R.drawable.ic_heart_empty);
        setUI(position);
        MainActivity.viewModel.setCurrentSongTitle(playlist.getSongs().get(position).getTitle());
        MainActivity.viewModel.setCurrentSongAlbum(playlist.getTitle());
        MainActivity.viewModel.setCurrentSongAuthor(playlist.getDescription());

        if (CurrentPlaylistFragment.adapter != null) {
            CurrentPlaylistFragment.adapter.notifyDataSetChanged();
        }
    }

    private void changePosition(SimpleExoPlayer player, Integer shouldAdd) {
        Log.d("PlayerFragment", "changePosition called!");
        if(!mService.getShuffle()){
            position = player.getCurrentWindowIndex();

                   if(shouldAdd!=null){
                       if(shouldAdd==1){
                           if(position< playlist.getSongs().size()-1){
                               position++;
                           }
                       }else{
                           if(position>0){
                               position--;
                           }
                       }
                   }

            mService.setPosition(position);
            player.seekTo(position, C.INDEX_UNSET);
        }
        else
        {
            shuffleClicked++;
         //   if( shuffleClicked%4==0){
                position =randomPosition;
                mService.setPosition(position);
                player.seekTo(position, C.INDEX_UNSET);

                if(playlist.getSongs().size()>1){
                    do{
                        randomPosition = new Random().nextInt(playlist.getSongs().size());
                    }
                    while (randomPosition==position);


                }else{
                    randomPosition = position;
                }
        //    }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeNotifOnStop();
        clearVideoView();
    }

    private void removeNotifOnStop() {
        if(isChangingFragment!=null){
            if(!isChangingFragment){
                if(mBound){
                    dismissPlayer();
                }
            }else{
                isChangingFragment=false;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
      if(mediaPlayer!=null){
          videoView.pause();
      }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mediaPlayer!=null){
            if(!videoView.isPlaying()){
                videoView.start();
            }
        }
    }
}

