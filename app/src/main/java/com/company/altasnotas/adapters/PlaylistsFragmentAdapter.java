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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.Playlist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

        //Load photo
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid().toString()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot!=null) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        System.out.println(ds.child("title").getValue().toString());
                        if (ds.child("title").getValue().toString().equals(playlists.get(position).getTitle())) {
                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        Glide.with(mainActivity).load(task.getResult()).apply(RequestOptions.centerCropTransform()).into(holder.photo);
                                    }else{
                                        Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).into(holder.photo);
                                        Log.d("Error while loading photo", "Firebase");
                                    }
                                }
                            });

                        }
                    }
                }else{
                    Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).into(holder.photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).into(holder.photo);
            }
        });

        String  title = playlists.get(position).getTitle();
        holder.title.setText(title);
        holder.desc.setText(playlists.get(position).getDescription());

        holder.fav_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title, desc;
        ImageButton fav_btn, settings_btn;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.playlists_recycler_row_photo);
            title = itemView.findViewById(R.id.playlists_recycler_row_title);
            desc = itemView.findViewById(R.id.playlists_recycler_row_desc);
            fav_btn = itemView.findViewById(R.id.playlists_recycler_row_fav_btn);
            settings_btn = itemView.findViewById(R.id.playlists_recycler_row_settings_btn);

            linearLayout = itemView.findViewById(R.id.playlists_recycler_row_box);
        }
    }
}
