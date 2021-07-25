package com.company.altasnotas.adapters;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.services.BackgroundService;
import com.google.android.exoplayer2.util.Util;

import java.io.Serializable;
import java.util.ArrayList;

public class CurrentPlaylistAdapter extends RecyclerView.Adapter<CurrentPlaylistAdapter.MyViewHolder> {
private final Playlist playlist;
private MainActivity activity;
private ArrayList<Song> songs;
private Boolean isFavFragment;

    public CurrentPlaylistAdapter(MainActivity activity, Playlist playlist, Boolean isFavFragment){
        this.playlist=playlist;
        songs =playlist.getSongs();
        this.activity =activity;
        this.isFavFragment=isFavFragment;
    }



    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView currentTitle,currentAuthor;
        ImageButton currentFav_btn, currentSettings_btn;
        ImageView photo;
        ConstraintLayout currentBox;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
           currentTitle = itemView.findViewById(R.id.current_playlist_row_title);
           currentAuthor = itemView.findViewById(R.id.current_playlist_row_author);
           currentFav_btn = itemView.findViewById(R.id.current_playlist_row_fav);
           currentSettings_btn = itemView.findViewById(R.id.current_playlist_row_settings);

           photo = itemView.findViewById(R.id.current_playlist_row_img);
           currentBox = itemView.findViewById(R.id.current_playlist_row_box);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.current_playlist_row, parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.currentTitle.setText(songs.get(position).getTitle());
        holder.currentAuthor.setText(songs.get(position).getAuthor());

        holder.currentBox.setOnClickListener(v -> {
            PlayerFragment playerFragment = new PlayerFragment(playlist, position);
             activity.getSupportFragmentManager().beginTransaction().addToBackStack("null").replace(R.id.main_fragment_container, playerFragment).commit();

        });

        holder.currentFav_btn.setOnClickListener(v -> Toast.makeText(holder.itemView.getContext(), "Fav btn of item is clicked!",Toast.LENGTH_SHORT).show());

        holder.currentSettings_btn.setOnClickListener(v -> Toast.makeText(holder.itemView.getContext(), "Settings btn of item is clicked!",Toast.LENGTH_SHORT).show());

        if(playlist.isAlbum()==true){
            holder.photo.setVisibility(View.INVISIBLE);
        }else{
            Glide.with(activity.getApplicationContext()).load(songs.get(position).getImage_url()).into(holder.photo);
        }






    }

    @Override
    public int getItemCount() {
        return playlist.getSongs().size();
    }


}
