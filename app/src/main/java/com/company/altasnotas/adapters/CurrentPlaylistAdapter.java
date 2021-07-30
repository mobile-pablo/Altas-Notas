package com.company.altasnotas.adapters;


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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CurrentPlaylistAdapter extends RecyclerView.Adapter<CurrentPlaylistAdapter.MyViewHolder> {
private final Playlist playlist;
private final MainActivity activity;
private final ArrayList<Song> songs;
private final Boolean isFavFragment;

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
        DatabaseReference databaseReference;
        FirebaseAuth mAuth;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
           currentTitle = itemView.findViewById(R.id.current_playlist_row_title);
           currentAuthor = itemView.findViewById(R.id.current_playlist_row_author);
           currentFav_btn = itemView.findViewById(R.id.current_playlist_row_fav);
           currentSettings_btn = itemView.findViewById(R.id.current_playlist_row_settings);

           photo = itemView.findViewById(R.id.current_playlist_row_img);
           currentBox = itemView.findViewById(R.id.current_playlist_row_box);

           databaseReference = FirebaseDatabase.getInstance().getReference();
           mAuth=FirebaseAuth.getInstance();
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.current_playlist_row, parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.currentTitle.setText(songs.get(position).getTitle());
        holder.currentAuthor.setText(songs.get(position).getAuthor());

        holder.currentBox.setOnClickListener(v -> {
            PlayerFragment playerFragment = new PlayerFragment(playlist, position, 0);
             activity.getSupportFragmentManager().beginTransaction().addToBackStack("null").replace(R.id.main_fragment_container, playerFragment).commit();

        });

        //Loading fav btn
        holder.databaseReference.child("fav_music")
                .child(holder.mAuth.getCurrentUser().getUid())
                .orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot!=null){
                    Song mySong = songs.get(position);

                    for(DataSnapshot ds: snapshot.getChildren()){

                        if(
                                ds.child("album").getValue().equals(mySong.getAlbum())
                                &&
                                ds.child("author").getValue().equals(mySong.getAuthor())
                        )
                        {
                            //Same album and Author now we check song title
                            holder.databaseReference
                                    .child("music")
                                    .child("albums")
                                    .child(mySong.getAuthor())
                                    .child(mySong.getAlbum())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {

                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snap) {

                                                  for(DataSnapshot s: snap.child("songs").getChildren()){

                                                       if(
                                                               s.child("order").getValue().toString().trim().equals(ds.child("numberInAlbum").getValue().toString().trim())
                                                               &&
                                                               s.child("title").getValue().equals(mySong.getTitle())
                                                       ){
                                                     //We found a song in Album and We need to set icon
                                                    holder.currentFav_btn.setImageResource(R.drawable.ic_heart_full);



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
