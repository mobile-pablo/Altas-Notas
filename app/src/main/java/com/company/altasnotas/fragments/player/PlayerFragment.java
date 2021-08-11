package com.company.altasnotas.fragments.player;

import android.app.Activity;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;
import androidx.palette.graphics.Target;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.ChoosePlaylistAdapter;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.services.BackgroundService;
import com.company.altasnotas.viewmodels.fragments.favorites.FavoritesFragmentViewModel;
import com.company.altasnotas.viewmodels.fragments.player.PlayerFragmentViewModel;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class PlayerFragment extends Fragment {
    private ImageButton fav_btn;
    private Button settings_btn;
    private Playlist playlist;
    int position;
    private ImageView song_img;
    private ExoListener exoListener;
    private TextView title, author;

    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;

    public static PlayerView playerView;
    public static BackgroundService mService;
    private boolean mBound = false;
    private Intent intent;
    private Long seekedTo;
    private Boolean isReOpen;
    private Palette palette;
    ConstraintLayout player_full_box;
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


    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetDialog choosePlaylistDialog;

    private PlayerFragmentViewModel viewModel;

    public PlayerFragment(Playlist playlist, int position, long seekedTo, Boolean isReOpen) {
        this.playlist = null;
        this.playlist = playlist;
        this.position = position;
        this.seekedTo = seekedTo;
        this.isReOpen=isReOpen;
        //We are sending playlist to this player and let it play all of it
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        title = view.findViewById(R.id.player_song_title);
        author = view.findViewById(R.id.player_song_author);
        song_img = view.findViewById(R.id.player_song_img);
        playerView = view.findViewById(R.id.player_view);
        playerView.setBackgroundColor(Color.TRANSPARENT);
        player_full_box = view.findViewById(R.id.player_full_box);
        viewModel = new ViewModelProvider(requireActivity()).get(PlayerFragmentViewModel.class);

        fav_btn = view.findViewById(R.id.player_song_fav_btn);
        settings_btn = view.findViewById(R.id.player_song_options_btn);


        setUI();

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




        database_ref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //Loading fav btn state
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


        fav_btn.setOnClickListener(v -> {

            if (fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {

                viewModel.addToFav(database_ref,mAuth,playlist,position,fav_btn);
            } else {
                viewModel.removeFromFav( getActivity(), database_ref,mAuth,playlist,position,fav_btn);
            }
        });

        settings_btn.setOnClickListener(v -> {
            openSettingsDialog();
        });


        return view;
    }

    private void openSettingsDialog() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_song_settings_layout);

        LinearLayout showAlbum = bottomSheetDialog.findViewById(R.id.bottom_settings_album_box);
        LinearLayout addToPlaylist = bottomSheetDialog.findViewById(R.id.bottom_settings_playlists_box);
        LinearLayout share = bottomSheetDialog.findViewById(R.id.bottom_settings_share_box);
        LinearLayout dismissDialog = bottomSheetDialog.findViewById(R.id.bottom_settings_dismiss_box);

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
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(playlist.getSongs().get(position).getAuthor(), playlist.getSongs().get(position).getAlbum(), x, 1)).addToBackStack("null").commit();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.d(MainActivity.FIREBASE,"Error: " + error.getMessage());
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
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void addToPlaylist() {
        choosePlaylistDialog = new BottomSheetDialog(getContext());
        choosePlaylistDialog.setContentView(R.layout.choose_playlist_dialog);

        RecyclerView chooseRecyclerView = choosePlaylistDialog.findViewById(R.id.choose_playlist_recycler_view);
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

                if (x == snapshot.getChildrenCount()) {


                    ChoosePlaylistAdapter choosePlaylistAdapter = new ChoosePlaylistAdapter((MainActivity) requireActivity(), choosePlaylistDialog, playlist.getSongs().get(position), playlists_titles, playlists_keys);
                    chooseRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                    choosePlaylistAdapter.notifyDataSetChanged();
                    chooseRecyclerView.setAdapter(choosePlaylistAdapter);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        choosePlaylistDialog.show();
    }

    private void share() {

        // The application exists
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, "Altas Notas");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My favorite Song is \"" + playlist.getSongs().get(position).getTitle() + "\" from \"" + playlist.getSongs().get(position).getAuthor() + "\".\nListen this on \"Altas Notas\".\nExternal Link: [ " + playlist.getSongs().get(position).getPath() + " ]");
        startActivity(Intent.createChooser(shareIntent, "Share using"));
        getContext().startActivity(shareIntent);

    }


    private void initializePlayer() {
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
            if(isReOpen)
            {
                //By this When Notification is Open and ExoPlayer is Paused. It remains that way.
                if( player.getPlayWhenReady() && player.getPlaybackState() == Player.STATE_READY ){
                        player.setPlayWhenReady(true);
                }else {
                    player.setPlayWhenReady(false);
                }
            }
            else
            {
            player.setPlayWhenReady(true);
            }
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


    private void setUI() {
        if(getActivity()!=null) {
            Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
            if (currentFragment instanceof PlayerFragment) {
                Glide.with(getActivity()).load(playlist.getSongs().get(position).getImage_url()).error(R.drawable.img_not_found).into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if(resource!=null) {
                            song_img.setImageDrawable(resource);
                            Bitmap b = drawableToBitmap(resource);
                            palette = Palette.from(b).generate();
                          viewModel.setUpInfoBackgroundColor(getActivity(), player_full_box, palette,settings_btn);
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
    }
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public class ExoListener implements Player.Listener {
        SimpleExoPlayer player;

        public ExoListener(SimpleExoPlayer player) {
            this.player = player;
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            MainActivity.currentSongTitle = playlist.getSongs().get(position).getTitle();
            MainActivity.currentSongAlbum=playlist.getTitle();
            MainActivity.currentSongAuthor=playlist.getDescription();
            mService.setPosition(player.getCurrentWindowIndex());
            position = player.getCurrentWindowIndex();
            CurrentPlaylistFragment.adapter.notifyDataSetChanged();

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
            CurrentPlaylistFragment.adapter.notifyDataSetChanged();
            setUI();
            fav_btn.setImageResource(R.drawable.ic_heart_empty);

            //Loading fav btn state
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

            mService.setPosition(position);
        }
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        initializePlayer();
    }
}

