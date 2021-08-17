package com.company.altasnotas.viewmodels.fragments.player;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModel;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.mini_player.MiniPlayerFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.viewmodels.fragments.favorites.FavoritesFragmentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class PlayerFragmentViewModel extends ViewModel {

    //Background Pallete
    Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (mostPopulous == null || swatch.getPopulation() > mostPopulous.getPopulation()) {
                    mostPopulous = swatch;
                }
            }
        }
        return mostPopulous;
    }

    public void setUpInfoBackgroundColor(Activity activity, LinearLayout ll, Palette palette) {
        Palette.Swatch swatch = getMostPopulousSwatch(palette);
        if (swatch != null) {
            int endColor = ContextCompat.getColor(ll.getContext(), R.color.black);
            int startColor = swatch.getRgb();

            if (startColor == endColor) {
                startColor = Color.DKGRAY;
            }



            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{startColor, endColor});

            if(activity!=null){
                Glide.with(activity)
                        .load(gradientDrawable)
                        .into(new CustomTarget<Drawable>() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                ll.setBackground(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
        }
    }

    public void removeFromFav(FragmentActivity activity, DatabaseReference database_ref, FirebaseAuth mAuth, Playlist playlist, Integer position, ImageButton fav_btn, CurrentPlaylistAdapter adapter) {
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot firebaseFav : snapshot.getChildren()) {
                    if (firebaseFav.child("album").getValue().toString().trim().equals(playlist.getSongs().get(position).getAlbum().trim())
                            &&
                            firebaseFav.child("numberInAlbum").getValue().toString().trim().equals(playlist.getSongs().get(position).getOrder().toString().trim())
                    ) {
                        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(firebaseFav.getKey()).removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    fav_btn.setImageResource(R.drawable.ic_heart_empty);

                                    if(activity!=null){
                                        Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                        if (currentFragment instanceof CurrentPlaylistFragment) {
                                            adapter.notifyDataSetChanged();
                                        }

                                        if (currentFragment instanceof FavoritesFragment) {
                                            FavoritesFragment fav = (FavoritesFragment) currentFragment;
                                            fav.viewModel.initializeFavorites();

                                            if(MainActivity.currentSongAlbum.getValue().equals(fav.viewModel.playlist.getTitle())){
                                                Log.d("FavoritesFragment", "Album value:"+ MainActivity.currentSongAlbum.getValue()+", Title: "+ fav.viewModel.playlist.getTitle());
                                                Fragment mini_frag = activity.getSupportFragmentManager().findFragmentById(R.id.main_mini_player_container);
                                                if (mini_frag instanceof MiniPlayerFragment) {
                                                    if (mini_frag.isVisible()) {
                                                        ((MiniPlayerFragment) mini_frag).dissmiss_mini();
                                                    }
                                                }
                                            }
                                        }


                                        if(currentFragment instanceof PlayerFragment){
                                            fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.white));
                                            MiniPlayerFragment.fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.black));
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

    public void addToFav(FragmentActivity activity, DatabaseReference database_ref, FirebaseAuth mAuth, Playlist playlist, Integer position, ImageButton fav_btn, CurrentPlaylistAdapter adapter) {
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

                        if(activity!=null){
                            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                            if (currentFragment instanceof CurrentPlaylistFragment) {
                                adapter.notifyDataSetChanged();
                            }

                            if (currentFragment instanceof FavoritesFragment) {
                                FavoritesFragment favoritesFragment = (FavoritesFragment) currentFragment;
                                favoritesFragment.viewModel.initializeFavorites();
                            }

                            if(currentFragment instanceof PlayerFragment){
                                fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_light_orange));
                                MiniPlayerFragment.fav_btn.getDrawable().setTint(ContextCompat.getColor(activity, R.color.project_dark_velvet));
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
