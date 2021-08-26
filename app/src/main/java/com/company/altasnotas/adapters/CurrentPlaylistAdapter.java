package com.company.altasnotas.adapters;


import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.exoplayer2.Player;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class CurrentPlaylistAdapter extends RecyclerView.Adapter<CurrentPlaylistAdapter.MyViewHolder> {
    public static  Playlist playlist;
    private final MainActivity activity;
    private final ArrayList<Song> songs;
    private final Integer isFavFragment;
    private final DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public CurrentPlaylistAdapter(MainActivity activity, Playlist playlist, Integer isFavFragment) {
        CurrentPlaylistAdapter.playlist = playlist;
        songs = playlist.getSongs();
        this.activity = activity;
        this.isFavFragment = isFavFragment;

        if (activity != null) {

            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
            if (currentFragment instanceof FavoritesFragment) {
                FavoritesFragment favoritesFragment = (FavoritesFragment) currentFragment;
                if (playlist.getSongs().size() == 0) {
                    FavoritesFragment.binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                    FavoritesFragment.binding.currentPlaylistRecyclerState.setText("Empty Favorites");
                    FavoritesFragment.binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                } else {
                    FavoritesFragment.binding.currentPlaylistRecyclerView.setVisibility(View.VISIBLE);
                    FavoritesFragment.binding.currentPlaylistRecyclerState.setVisibility(View.GONE);
                }

            }
        }
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView currentTitle, currentAuthor;
        ImageButton currentFav_btn, currentSettings_btn;
        ImageView photo;
        LinearLayout currentBox;
        DatabaseReference databaseReference;
        FirebaseAuth mAuth;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            currentTitle = itemView.findViewById(R.id.currentPlaylistRowTitle);
            currentAuthor = itemView.findViewById(R.id.currentPlaylistRowAuthor);
            currentFav_btn = itemView.findViewById(R.id.currentPlaylistRowFavBtn);
            currentSettings_btn = itemView.findViewById(R.id.currentPlaylistRowSettingsBtn);

            photo = itemView.findViewById(R.id.currentPlaylistRowImage);
            currentBox = itemView.findViewById(R.id.currentPlaylistRowBox);

            databaseReference = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.current_playlist_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        holder.currentTitle.setText(songs.get(position).getTitle());
        holder.currentAuthor.setText(songs.get(position).getAuthor());

         if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(songs.get(position).getTitle()) &&
            MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getTitle()) &&
            MainActivity.viewModel.getCurrentSongAuthor().getValue().equals( playlist.getDescription()))
        {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                holder.currentTitle.setTextColor(activity.getColor(R.color.project_light_orange));
                holder.currentFav_btn.getDrawable().setTint(activity.getColor(R.color.project_light_orange));
                }
                else
                {
                    holder.currentTitle.setTextColor(ContextCompat.getColor( activity,R.color.project_light_orange));
                    holder.currentFav_btn.getDrawable().setTint(ContextCompat.getColor(activity,R.color.project_light_orange));
                }
           }
           else
           {
            holder.currentTitle.setTextColor(Color.BLACK);
            holder.currentFav_btn.getDrawable().setTint(Color.BLACK);
           }

        holder.currentBox.setOnClickListener(v ->
          {

                if (! ((MainActivity.viewModel.getCurrentSongTitle().getValue().equals(playlist.getSongs().get(position).getTitle())) &&
                       MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getTitle()) &&
                       MainActivity.viewModel.getCurrentSongAuthor().getValue().equals(playlist.getDescription())))
                {
                   MainActivity.viewModel.setCurrentSongTitle(playlist.getSongs().get(position).getTitle());
                    MainActivity.viewModel.setCurrentSongAlbum(playlist.getTitle());
                  MainActivity.viewModel.setCurrentSongAuthor(playlist.getDescription());
                  holder.currentFav_btn.getDrawable().setTint(ContextCompat.getColor(activity,R.color.project_light_orange));

                  PlayerFragment.isDimissed=false;


                    if(PlayerFragment.mService!=null){
                        PlayerFragment.mService.setRepeat(Player.REPEAT_MODE_OFF);
                        PlayerFragment.mService.setShuffleEnabled(false);
                        PlayerFragment.mService.getPlayerInstance().setShuffleModeEnabled(false);
                    }
                    PlayerFragment playerFragment = new PlayerFragment(playlist, position, 0, false, null, null, isFavFragment);
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.slidingLayoutFrag, playerFragment).commit();
                    activity.activityMainBinding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
        }
        );
        //Loading fav btn
        holder.databaseReference.child("fav_music")
                .child(holder.mAuth.getCurrentUser().getUid())
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null) {
                            Song mySong = songs.get(position);

                            for (DataSnapshot ds : snapshot.getChildren()) {

                                if (ds.child("album").getValue().equals(mySong.getAlbum())
                                                &&
                                                ds.child("author").getValue().equals(mySong.getAuthor())){
                                    //Same album and Author now we check song title
                                    holder.databaseReference
                                            .child("music")
                                            .child("albums")
                                            .child(mySong.getAuthor())
                                            .child(mySong.getAlbum())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snap) {
                                                    for (DataSnapshot s : snap.child("songs").getChildren()) {

                                                        if (s.child("order").getValue().toString().trim().equals(ds.child("numberInAlbum").getValue().toString().trim()) &&
                                                            s.child("title").getValue().equals(mySong.getTitle())){
                                                            //We found a song in Album and We need to set icon
                                                            holder.currentFav_btn.setImageResource(R.drawable.ic_heart_full);


                                                            if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(songs.get(position).getTitle()) &&
                                                                    MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getTitle()) &&
                                                                    MainActivity.viewModel.getCurrentSongAuthor().getValue().equals( playlist.getDescription())) {

                                                                holder.currentFav_btn.getDrawable().setTint(ContextCompat.getColor(activity,R.color.project_light_orange));
                                                            }
                                                            else
                                                            {

                                                                if (holder.currentFav_btn.getDrawable().getConstantState().equals(holder.currentFav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
                                                                    holder.currentFav_btn.getDrawable().setTint(Color.BLACK);
                                                                }
                                                                else
                                                                {
                                                                    holder.currentFav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_dark_velvet));
                                                                }
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

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (holder.currentFav_btn.getDrawable().getConstantState().equals(holder.currentFav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
            holder.currentFav_btn.getDrawable().setTint(Color.BLACK);
        }


        holder.currentFav_btn.setOnClickListener(v -> {
            if (holder.currentFav_btn.getDrawable().getConstantState().equals(holder.currentFav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
                addToFav(position, holder.currentFav_btn);
            } else {
                removeFromFav(position, holder.currentFav_btn);
            }
        });

        holder.currentSettings_btn.setOnClickListener(v -> {
            if (isFavFragment != 0) {
                openSongSettingDialog(position, holder);
            } else {
                openPlaylistSongSettingsDialog(position, holder);
            }
        });

        holder.databaseReference.child("music").child("albums").child(songs.get(position).getAuthor()).child(songs.get(position).getAlbum()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                holder.currentAuthor.setText(snap.child("description").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (playlist.isAlbum()) {
            holder.photo.setVisibility(View.GONE);
        } else {
            holder.photo.setVisibility(View.VISIBLE);
            Glide.with(activity.getApplicationContext()).load(songs.get(position).getImage_url()).error(R.drawable.img_not_found).into(holder.photo);
        }


    }

    private void removeFromFav(Integer position, ImageButton fav_btn) {

        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot firebaseFav : snapshot.getChildren()) {

                    if
                    (
                            playlist.getSongs().get(position).getOrder().toString().trim().equals(firebaseFav.child("numberInAlbum").getValue().toString().trim())
                                    &&
                                    firebaseFav.child("album").getValue().toString().trim().equals(playlist.getSongs().get(position).getAlbum().trim())
                    ) {
                        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(firebaseFav.getKey()).removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                    fav_btn.getDrawable().setTint(Color.BLACK);
                                    Fragment miniFrag = activity.getSupportFragmentManager().findFragmentById(R.id.slidingLayoutFrag);
                                    Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                                    if (currentFragment instanceof FavoritesFragment)
                                    {

                                        FavoritesFragment favoritesFragment = (FavoritesFragment) currentFragment;
                                        if(miniFrag instanceof  PlayerFragment)
                                        {
                                            if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(playlist.getSongs().get(position).getTitle()) &&
                                                    MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getTitle()) &&
                                                    MainActivity.viewModel.getCurrentSongAuthor().getValue().equals( playlist.getDescription())
                                            ){

                                                ((PlayerFragment) miniFrag).dismissPlayer();

                                             }
                                        else
                                        {
                                            if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(playlist.getSongs().get(position).getTitle()) &&
                                                    MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getSongs().get(position).getVisualAlbum()) &&
                                                    MainActivity.viewModel.getCurrentSongAuthor().getValue().equals( playlist.getSongs().get(position).getVisualAuthor()))
                                            {
                                                    PlayerFragment.mini_fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                                    PlayerFragment.mini_fav_btn.getDrawable().setTint(Color.BLACK);

                                                    PlayerFragment.fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                                    PlayerFragment.fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_light_orange));
                                             }

                                            }
                                        }

                                        favoritesFragment.viewModel.initializeFavorites();
                                        //    activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,new FavoritesFragment()).commit();
                                        if(playlist!=null)
                                        {
                                            if (playlist.getSongs().size() == 0) {
                                                FavoritesFragment.binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                                                FavoritesFragment.binding.currentPlaylistRecyclerState.setText("Empty Favorites");
                                                FavoritesFragment.binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        else
                                        {
                                            FavoritesFragment.binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                                            FavoritesFragment.binding.currentPlaylistRecyclerState.setText("Empty Favorites");
                                            FavoritesFragment.binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                                        }

                                    }
                                    else
                                    {
                                        if(miniFrag instanceof  PlayerFragment)
                                        {
                                            if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(playlist.getSongs().get(position).getTitle()) &&
                                                    MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getTitle()) &&
                                                    MainActivity.viewModel.getCurrentSongAuthor().getValue().equals( playlist.getDescription()))
                                            {

                                              PlayerFragment.mini_fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                              PlayerFragment.mini_fav_btn.getDrawable().setTint(Color.BLACK);


                                                PlayerFragment.fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                                PlayerFragment.fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_light_orange));
                                            }else {
                                                if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(playlist.getSongs().get(position).getTitle()) &&
                                                   MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getSongs().get(position).getAlbum()) &&
                                                 MainActivity.viewModel.getCurrentSongAuthor().getValue().equals(playlist.getSongs().get(position).getAuthor())){
                                                    PlayerFragment.mini_fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                                    PlayerFragment.mini_fav_btn.getDrawable().setTint(Color.BLACK);


                                                    PlayerFragment.fav_btn.setImageResource(R.drawable.ic_heart_empty);
                                                    PlayerFragment.fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_light_orange));
                                                }
                                            }
                                        }

                                      notifyDataSetChanged();
                                        //    activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,new FavoritesFragment()).commit();
                                        if(currentFragment instanceof CurrentPlaylistFragment){
                                         if(playlist!=null)
                                        {
                                            if (playlist.getSongs().size() == 0)
                                            {
                                                CurrentPlaylistFragment.binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                                                CurrentPlaylistFragment.binding.currentPlaylistRecyclerState.setText("Empty Playlist");
                                                CurrentPlaylistFragment.binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                                            }
                                        }
                                        else
                                        {
                                            CurrentPlaylistFragment.binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                                            CurrentPlaylistFragment.binding.currentPlaylistRecyclerState.setText("Empty Playlist");
                                            CurrentPlaylistFragment.binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                                        }
                                        }
                                    }


                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addToFav(Integer position, ImageButton fav_btn) {
        String key = database_ref.push().getKey();

        database_ref
                .child("music")
                .child("albums")
                .child(playlist.getSongs().get(position).getAuthor())
                .child(playlist.getSongs().get(position).getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.child("songs").getChildren()) {
                    if (dataSnapshot.child("title").getValue().equals(playlist.getSongs().get(position).getTitle())) {
                        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("numberInAlbum").setValue(dataSnapshot.child("order").getValue().toString());
                        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("album").setValue(playlist.getSongs().get(position).getAlbum());
                        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("author").setValue(playlist.getSongs().get(position).getAuthor());
                        fav_btn.setImageResource(R.drawable.ic_heart_full);
                             Fragment frag = activity.getSupportFragmentManager().findFragmentById(R.id.slidingLayoutFrag);
                            if (frag instanceof PlayerFragment) {
                                ((PlayerFragment) frag).setUI(((PlayerFragment) frag).getPosition());
                            }
                        if(MainActivity.viewModel.getCurrentSongTitle().getValue().equals(playlist.getSongs().get(position).getTitle()) &&
                                MainActivity.viewModel.getCurrentSongAlbum().getValue().equals(playlist.getTitle()) &&
                                MainActivity.viewModel.getCurrentSongAuthor().getValue().equals( playlist.getDescription())
                        )
                                        {
                            fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_light_orange));
                               }
                                        else
                                        {
                            fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_dark_velvet));
                        }


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return playlist.getSongs().size();
    }

    private void openPlaylistSongSettingsDialog(Integer position, MyViewHolder holder) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());

        bottomSheetDialog.setContentView(R.layout.bottom_playlist_song_settings_layout);

      bottomSheetDialog.getBehavior().setPeekHeight(MainActivity.dialogHeight);





        LinearLayout showAlbum = bottomSheetDialog.findViewById(R.id.bottomSettingsAlbumBox);
        LinearLayout share = bottomSheetDialog.findViewById(R.id.bottomSettingsShareBox);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.bottomSettingsDeleteBox);
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
                            if(!(x.getTitle().equals(playlist.getTitle()) && x.getDescription().equals(playlist.getDescription())))
                                        {
                                activity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(playlist.getSongs().get(position).getAuthor(), playlist.getSongs().get(position).getAlbum(), x, 1)).addToBackStack("null").commit();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.d(MainActivity.FIREBASE, "Error: " + error.getMessage());
                    }

                });


            }
        });

        share.setOnClickListener(v -> {
            share(position);
            bottomSheetDialog.dismiss();
        });

        delete.setOnClickListener(v -> {
            deleteSongFromPlaylist(position, holder);
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void deleteSongFromPlaylist(Integer position, MyViewHolder holder) {
        database_ref.child("music").child("playlists").child(mAuth.getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (
                            playlist.getTitle().trim().equals(ds.child("title").getValue().toString().trim())
                                    &&
                                    playlist.getDescription().equals(ds.child("description").getValue().toString().trim())
                    ) {


                        String playlist_key = ds.getKey();
                        for (DataSnapshot da : ds.child("songs").getChildren()) {
                            if (playlist.getSongs().get(position).getOrder().toString().trim().equals(da.child("numberInAlbum").getValue().toString().trim())
                                    &&
                                    playlist.getSongs().get(position).getAuthor().trim().equals(da.child("author").getValue().toString().trim())
                                    &&
                                    playlist.getSongs().get(position).getAlbum().trim().equals(da.child("album").getValue().toString().trim())
                            ) {
                                String song_key = da.getKey();
                                database_ref.child("music").child("playlists").child(mAuth.getUid()).child(playlist_key).child("songs").child(song_key).removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                                            if (currentFragment instanceof CurrentPlaylistFragment) {
                                                CurrentPlaylistFragment currentPlaylistFragment = (CurrentPlaylistFragment) currentFragment;
                                                playlist.getSongs().remove(playlist.getSongs().get(position));
                                                notifyDataSetChanged();
                                                if (playlist.getSongs().size() == 0) {
                                                    CurrentPlaylistFragment.binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                                                    CurrentPlaylistFragment.binding.currentPlaylistRecyclerState.setText("Empty Playlist");
                                                    CurrentPlaylistFragment.binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        } else {
                                            Log.d(MainActivity.FIREBASE,"Couldn't delete song.");
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openSongSettingDialog(Integer position, MyViewHolder holder) {

    BottomSheetDialog    bottomSheetDialog = new BottomSheetDialog(activity);
        bottomSheetDialog.setContentView(R.layout.bottom_song_settings_layout);
        bottomSheetDialog.getBehavior().setPeekHeight(MainActivity.dialogHeight);
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
                            if(!(x.getTitle().equals(playlist.getTitle()) && x.getDescription().equals(playlist.getDescription()))) {
                                activity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(playlist.getSongs().get(position).getAuthor(), playlist.getSongs().get(position).getAlbum(), x, 1)).addToBackStack("null").commit();
                            }
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
            addToPlaylist(position, holder);
            bottomSheetDialog.dismiss();
        });
        share.setOnClickListener(v -> {
            share(position);
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void addToPlaylist(Integer position, MyViewHolder holder) {
        BottomSheetDialog  choosePlaylistDialog = new BottomSheetDialog(holder.itemView.getContext());
        choosePlaylistDialog.setContentView(R.layout.choose_playlist_dialog);
        choosePlaylistDialog.getBehavior().setPeekHeight(MainActivity.dialogHeight);
        RecyclerView chooseRecyclerView = choosePlaylistDialog.findViewById(R.id.choosePlaylistRecyclerView);
        TextView chooseState = choosePlaylistDialog.findViewById(R.id.choosePlaylistRecyclerViewState);
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

                if (snapshot.getChildrenCount()!=0) {
                    if (x == snapshot.getChildrenCount()) {

                        ChoosePlaylistAdapter choosePlaylistAdapter = new ChoosePlaylistAdapter(activity, choosePlaylistDialog, playlist.getSongs().get(position), playlists_titles, playlists_keys);
                        chooseRecyclerView.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                        choosePlaylistAdapter.notifyDataSetChanged();
                        chooseRecyclerView.setAdapter(choosePlaylistAdapter);
                    }
                    chooseState.setVisibility(View.GONE);
                    chooseRecyclerView.setVisibility(View.VISIBLE);
                       }
                                        else
                                        {
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

    private void share(Integer position) {

        // The application exists
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_TITLE, "Altas Notas");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My favorite Song is \"" + playlist.getSongs().get(position).getTitle() + "\" from \"" + playlist.getSongs().get(position).getAuthor() + "\".\nListen this on \"Altas Notas\".\nExternal Link: [ " + playlist.getSongs().get(position).getPath() + " ]");
        activity.startActivity(Intent.createChooser(shareIntent, "Share using"));
        activity.startActivity(shareIntent);

    }
}
