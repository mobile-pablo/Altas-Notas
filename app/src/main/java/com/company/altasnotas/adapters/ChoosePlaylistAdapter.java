package com.company.altasnotas.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
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

public class ChoosePlaylistAdapter extends RecyclerView.Adapter<ChoosePlaylistAdapter.MyViewHolder> {
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<String> titles,keys;
    private MainActivity mainActivity;
    public ChoosePlaylistAdapter(MainActivity mainActivity,  ArrayList<String> titles, ArrayList<String> keys) {
        this.mainActivity =mainActivity;
        this.titles = titles;
        this.keys = keys;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChoosePlaylistAdapter.MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_playlist_row, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.title.setText(titles.get(position));

        //Load photo
        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + keys.get(position)).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Glide.with(mainActivity).load(task.getResult()).apply(RequestOptions.centerCropTransform()).override(holder.image.getWidth(),holder.image.getWidth()).into(holder.image);
                }else{
                    Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).override(holder.image.getWidth(),holder.image.getWidth()).into(holder.image);
                    Log.d("Error while loading photo", "Firebase");
                }
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



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.choose_playlist_row_image);
            title = itemView.findViewById(R.id.choose_playlist_row_title);
        }
    }
}
