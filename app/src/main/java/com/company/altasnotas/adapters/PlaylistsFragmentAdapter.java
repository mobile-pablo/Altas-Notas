package com.company.altasnotas.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.company.altasnotas.models.Playlist;
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

public class PlaylistsFragmentAdapter extends RecyclerView.Adapter<PlaylistsFragmentAdapter.MyViewHolder> {
    private ArrayList<Playlist> playlists;
    private MainActivity mainActivity;

    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    private BottomSheetDialog bottomSheetDialog;

    public PlaylistsFragmentAdapter(MainActivity mainActivity ,ArrayList<Playlist> playlists){
        this.mainActivity=mainActivity;
        this.playlists=playlists;


    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        database_ref= FirebaseDatabase.getInstance().getReference();
        mAuth= FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        return new PlaylistsFragmentAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlists_recycler_row, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    holder.setIsRecyclable(false);
        //Load photo
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid().toString()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot!=null) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        if (ds.child("title").getValue().toString().equals(playlists.get(position).getTitle())) {
                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Glide.with(mainActivity).load(task.getResult()).apply(RequestOptions.centerCropTransform()).override(holder.photo.getWidth(),holder.photo.getWidth()).into(holder.photo);
                                    }else{
                                        Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).override(holder.photo.getWidth(),holder.photo.getWidth()).into(holder.photo);
                                        Log.d("Error while loading photo", "Firebase");
                                    }
                                }
                            });

                        }
                    }
                }else{
                    Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).override(holder.photo.getWidth(),holder.photo.getWidth()).into(holder.photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).override(holder.photo.getWidth(),holder.photo.getWidth()).into(holder.photo);
            }
        });

        String  title = playlists.get(position).getTitle();
        holder.title.setText(title);
        holder.desc.setText(playlists.get(position).getDescription());



        holder.settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            openPlaylistSongSettingsDialog(position, holder);
            }
        });

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(title,"",playlists.get(position), 0)).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    private void openPlaylistSongSettingsDialog(Integer position, MyViewHolder holder) {
        bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_playlist_settings);

        LinearLayout  copy = bottomSheetDialog.findViewById(R.id.bottom_settings_copy_box);
        LinearLayout   delete = bottomSheetDialog.findViewById(R.id.bottom_settings_delete_box);
        LinearLayout  dismissDialog = bottomSheetDialog.findViewById(R.id.bottom_settings_dismiss_box);

        copy.setOnClickListener(v->{
            copyPlaylist(playlists.get(position),holder);
        });

        delete.setOnClickListener(v ->{
            deletePlaylist(playlists.get(position),holder);
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void copyPlaylist(Playlist playlist, MyViewHolder holder) {
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(
                            ds.child("title").getValue().toString().trim().equals(playlist.getTitle())
                            &&
                            ds.child("description").getValue().toString().trim().equals(playlist.getDescription())
                    ){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deletePlaylist(Playlist playlist, MyViewHolder holder) {
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(
                            ds.child("title").getValue().toString().trim().equals(playlist.getTitle())
                                    &&
                                    ds.child("description").getValue().toString().trim().equals(playlist.getDescription())
                    ){

                        String key = ds.getKey();

                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).removeValue().addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    System.out.println("Playlist deleted");
                                    playlists.remove(playlist);
                                    notifyDataSetChanged();

                                    Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                    if(currentFragment instanceof PlaylistsFragment){
                                        PlaylistsFragment playlistsFragment = (PlaylistsFragment) currentFragment;
                                        playlists.remove(playlist);
                                        notifyDataSetChanged();
                                        //    activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,new FavoritesFragment()).commit();
                                        if(playlists.size()==0){
                                            playlistsFragment.recyclerView.setVisibility(View.GONE);
                                            playlistsFragment.recyclerViewState.setVisibility(View.VISIBLE);
                                        }

                                    }

                                }else{
                                    System.out.println("Error while  deleting Playlist");
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error while  deleting Playlist");
            }
        });

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title, desc;
        ImageButton  settings_btn;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.playlists_recycler_row_photo);
            title = itemView.findViewById(R.id.playlists_recycler_row_title);
            desc = itemView.findViewById(R.id.playlists_recycler_row_desc);

            settings_btn = itemView.findViewById(R.id.playlists_recycler_row_settings_btn);
            linearLayout = itemView.findViewById(R.id.playlists_recycler_row_box);
        }
    }
}
