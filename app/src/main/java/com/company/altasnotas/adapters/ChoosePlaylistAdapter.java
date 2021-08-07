package com.company.altasnotas.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ChoosePlaylistAdapter extends RecyclerView.Adapter<ChoosePlaylistAdapter.MyViewHolder> {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<String> titles, keys;
    private MainActivity mainActivity;
    private BottomSheetDialog choosePlaylistDialog;
    private Song song;

    public ChoosePlaylistAdapter(MainActivity mainActivity, BottomSheetDialog choosePlaylistDialog, Song song, ArrayList<String> titles, ArrayList<String> keys) {
        this.mainActivity = mainActivity;
        this.choosePlaylistDialog = choosePlaylistDialog;
        this.song = song;
        this.titles = titles;
        this.keys = keys;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChoosePlaylistAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_playlist_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.title.setText(titles.get(position));


        //Load photo
        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + keys.get(position)).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    holder.image.setVisibility(View.VISIBLE);
                    Glide.with(mainActivity).load(task.getResult()).apply(RequestOptions.centerCropTransform()).override(holder.image.getWidth(), holder.image.getWidth()).into(holder.image);
                } else {
                    holder.image.setVisibility(View.VISIBLE);
                    Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).override(holder.image.getWidth(), holder.image.getWidth()).into(holder.image);
                    Log.d("Error while loading photo", "Firebase");
                }
            }
        });


        holder.linearLayout.setOnClickListener(v -> {
            addToPlaylist(song, keys.get(position));
            choosePlaylistDialog.dismiss();
        });

    }

    private void addToPlaylist(Song song, String key) {
        String push_key = databaseReference.push().getKey();
        FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
        favoriteFirebaseSong.setNumberInAlbum(song.getOrder());
        favoriteFirebaseSong.setAlbum(song.getAlbum());
        favoriteFirebaseSong.setAuthor(song.getAuthor());

        databaseReference.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int x = (int) snapshot.getChildrenCount();
                for (DataSnapshot ds : snapshot.child("songs").getChildren()) {
                    if (ds.child("numberInAlbum").getValue().toString().trim().equals(favoriteFirebaseSong.getNumberInAlbum().toString().trim())
                            &&
                            ds.child("album").getValue().toString().trim().equals(favoriteFirebaseSong.getAlbum().trim())
                            &&
                            ds.child("author").getValue().toString().trim().equals(favoriteFirebaseSong.getAuthor().trim())
                    ) {
                        x--;
                    }
                }


                if (x != snapshot.getChildrenCount()) {
                    Toast.makeText(mainActivity, "This song already exist in this Playlist", Toast.LENGTH_SHORT).show();
                } else {
                    databaseReference.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("songs").child(push_key).setValue(favoriteFirebaseSong).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.d("Error while adding Song to Playlist", "FirebaseDatabase");
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        LinearLayout linearLayout;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.choose_playlist_row_image);
            title = itemView.findViewById(R.id.choose_playlist_row_title);
            linearLayout = itemView.findViewById(R.id.choose_playlist_row_box);
        }
    }


}
