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
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private  DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();
    private    FirebaseAuth mAuth = FirebaseAuth.getInstance();
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
                        System.out.println("Database fav song: "+ds.child("author").getValue().toString()+", Mysong: "+mySong.getAuthor()+"\n");

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

                                           //    holder.currentAuthor.setText(snap.child("description").getValue().toString());
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


        holder.currentFav_btn.setOnClickListener(v -> {
            if (holder.currentFav_btn.getDrawable().getConstantState().equals(holder.currentFav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
                addToFav(position,holder.currentFav_btn);
            } else {
                removeFromFav(position,holder.currentFav_btn);
            }
        });

        holder.currentSettings_btn.setOnClickListener(v -> Toast.makeText(holder.itemView.getContext(), "Settings btn of item is clicked!",Toast.LENGTH_SHORT).show());

        holder.databaseReference.child("music").child("albums").child(songs.get(position).getAuthor()).child(songs.get(position).getAlbum()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                holder.currentAuthor.setText(snap.child("description").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(playlist.isAlbum()){
            holder.photo.setVisibility(View.INVISIBLE);
        }else{
            Glide.with(activity.getApplicationContext()).load(songs.get(position).getImage_url()).into(holder.photo);
        }






    }

    private void removeFromFav(Integer position, ImageButton fav_btn) {

        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot firebaseFav: snapshot.getChildren()){

                    if( firebaseFav.child("album").getValue().toString().trim().equals(playlist.getSongs().get(position).getAlbum().trim())){
                        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(firebaseFav.getKey()).removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    fav_btn.setImageResource(R.drawable.ic_heart_empty);

                                    playlist.getSongs().remove(playlist.getSongs().get(position));
                                   activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new FavoritesFragment()).commit();
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
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("numberInAlbum").setValue(position+1);
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("album").setValue(playlist.getDir_title());
        database_ref.child("fav_music").child(mAuth.getCurrentUser().getUid()).child(key).child("author").setValue(playlist.getDir_desc());
        fav_btn.setImageResource(R.drawable.ic_heart_full);
    }

    @Override
    public int getItemCount() {
        return playlist.getSongs().size();
    }


}
