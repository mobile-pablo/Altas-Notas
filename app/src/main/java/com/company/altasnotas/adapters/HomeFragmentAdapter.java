package com.company.altasnotas.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;

import java.util.ArrayList;

public class HomeFragmentAdapter extends RecyclerView.Adapter<HomeFragmentAdapter.MyViewHolder>  {
    MainActivity mainActivity;
    ArrayList<Playlist> playlists;

    public HomeFragmentAdapter(MainActivity mainActivity, ArrayList<Playlist> playlists){
        this.mainActivity=mainActivity;
        this.playlists=playlists;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeFragmentAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_home_row, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    Glide.with(mainActivity.getApplicationContext()).load(playlists.get(position).getImage_id()).into(holder.home_row_img);
    holder.home_row_year.setText(playlists.get(position).getYear());
    holder.home_row_title.setText(playlists.get(position).getTitle());
    holder.home_row_author.setText(playlists.get(position).getDescription());

    holder.home_row_img.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(playlists.get(position))).commit();

        }
    });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView home_row_img;
        TextView home_row_year,home_row_title, home_row_author;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

          home_row_img = itemView.findViewById(R.id.playlist_home_row_img);
          home_row_year = itemView.findViewById(R.id.playlist_home_row_year);
          home_row_title = itemView.findViewById(R.id.playlist_home_row_title);
          home_row_author = itemView.findViewById(R.id.playlist_home_row_author);
        }
    }
}
