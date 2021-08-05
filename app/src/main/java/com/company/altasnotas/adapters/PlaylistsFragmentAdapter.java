package com.company.altasnotas.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.viewmodels.ProfileFragmentViewModel;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

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

       if(mainActivity!=null){
           Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
           if(currentFragment instanceof PlaylistsFragment){
               PlaylistsFragment playlistsFragment = (PlaylistsFragment) currentFragment;

               if (playlists.size() == 0) {
                   playlistsFragment.recyclerView.setVisibility(View.GONE);
                   playlistsFragment.recyclerViewState.setVisibility(View.VISIBLE);
               }else{
                   playlistsFragment.recyclerView.setVisibility(View.VISIBLE);
                   playlistsFragment.recyclerViewState.setVisibility(View.GONE);
               }
           }
       }
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
                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).getDownloadUrl().addOnSuccessListener(mainActivity, new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(mainActivity).load(uri).apply(RequestOptions.centerCropTransform()).override(holder.photo.getWidth(),holder.photo.getWidth()).into(holder.photo);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    System.out.println("Error: "+exception.getMessage());
                                    Log.d("Firebase", "Photo wasn't found");
                                    Glide.with(mainActivity.getApplicationContext()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).override(holder.photo.getWidth(),holder.photo.getWidth()).into(holder.photo);

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
               mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(title,"",playlists.get(position), 0)).addToBackStack(null).commit();
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
            settingsCopy(playlists.get(position),holder);
            bottomSheetDialog.dismiss();
        });

        delete.setOnClickListener(v ->{
            deletePlaylist(playlists.get(position));
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void settingsCopy(Playlist playlist, MyViewHolder holder) {
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(
                            ds.child("title").getValue().toString().trim().equals(playlist.getTitle())
                            &&
                            ds.child("description").getValue().toString().trim().equals(playlist.getDescription())
                    ){
                        openDialog(ds.getKey(),holder,playlist);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deletePlaylist(Playlist playlist) {
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
                                      storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(mainActivity, new OnSuccessListener<Uri>() {
                                          @Override
                                          public void onSuccess(Uri uri) {
                                              storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).delete().addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      if (task.isSuccessful()) {
                                                          System.out.println("Photo deleted with playlist");
                                                      } else {
                                                          Toast.makeText(mainActivity, "Error while deleting Photo", Toast.LENGTH_SHORT).show();
                                                      }
                                                  }
                                              });
                                          }
                                      }).addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception exception) {
                                              Log.d("Firebase", "Photo wasn't found");
                                          }
                                      });;



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
    private void openDialog(String key, MyViewHolder holder, Playlist playlist) {



        holder.dialog = new Dialog(mainActivity, R.style.Theme_AltasNotas);
        holder.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        holder.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        holder.dialog.setCanceledOnTouchOutside(true);
        holder.dialog.setContentView(R.layout.add_playlists_dialog);
        holder.dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        holder.dialog.getWindow().setGravity(Gravity.CENTER);

        ImageButton cancel, accept;

        holder.dialog_playlist_name = holder.dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_name);
        holder.dialog_playlist_desc = holder.dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_desc);

        cancel = holder.dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_cancel_btn);
        accept = holder.dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_accept_btn);



        cancel.setOnClickListener(v -> holder.dialog.dismiss());


        accept.setOnClickListener(v -> validInput(key,holder.dialog_playlist_name.getText().toString(), holder.dialog_playlist_desc.getText().toString(), holder, playlist));

        holder.dialog.show();

    }

    private void validInput(String key,String name ,String desc,MyViewHolder holder, Playlist playlist) {
        name = name.trim();
        desc = desc.trim();


        if(name.isEmpty() && desc.isEmpty()){
            Toast.makeText(mainActivity, "Both fields are empty.\nPlease fill data.",Toast.LENGTH_SHORT).show();
        }else{

            if(name.isEmpty() || desc.isEmpty()){
                if(name.isEmpty()){
                    Toast.makeText(mainActivity, "Name is empty.\nPlease fill data.",Toast.LENGTH_SHORT).show();
                }

                if(desc.isEmpty()){
                    Toast.makeText(mainActivity, "Description is empty.\nPlease fill data.",Toast.LENGTH_SHORT).show();
                }
            }else{
                name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
                desc = desc.substring(0,1).toUpperCase() + desc.substring(1).toLowerCase();
                String finalName = name;
                String finalDesc = desc;
                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot!=null){
                            int x= (int) snapshot.getChildrenCount();
                            for(DataSnapshot ds: snapshot.getChildren()){
                                if(ds.child("title").getValue().toString().trim().equals(finalName)){
                                    x--;
                                    Toast.makeText(mainActivity, "Playlist exist with same title!",Toast.LENGTH_SHORT).show();
                                }
                            }

                            if(x ==snapshot.getChildrenCount()){
                                holder.dialog.dismiss();
                                copyPlaylist(key,finalName, finalDesc, playlist);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Firebase DB error", "FirebaseDatabase");
                    }
                });

            }
        }
    }

    private void copyPlaylist(String old_key, String name ,String desc, Playlist p) {

        p.setTitle(name);
        p.setDescription(desc);

        String key = database_ref.push().getKey();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).setValue(p).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("isAlbum").setValue(p.isAlbum()).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("album").removeValue().addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Copy songs
                                int[] x = {0};
                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(old_key).child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs= new ArrayList<>();
                                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                            x[0]++;
                                            FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
                                            favoriteFirebaseSong.setAuthor(dataSnapshot.child("author").getValue().toString());
                                            favoriteFirebaseSong.setAlbum(dataSnapshot.child("album").getValue().toString());
                                            favoriteFirebaseSong.setNumberInAlbum(Integer.valueOf(dataSnapshot.child("numberInAlbum").getValue().toString()));
                                            favoriteFirebaseSongs.add(favoriteFirebaseSong);
                                            if(x[0]==snapshot.getChildrenCount()){
                                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("songs").setValue(favoriteFirebaseSongs).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            //Copy photo
                                                            storageReference = FirebaseStorage.getInstance().getReference();
                                                            storageReference.child("images/playlists/"+mAuth.getCurrentUser().getUid()+"/"+old_key).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                    if(task.isComplete() && task.isSuccessful()){
                                                                        System.out.println("URI HERE FOUND");

                                                                        Uri uri = task.getResult();


                                                                        Thread thread = new Thread(new Runnable(){
                                                                            @Override
                                                                            public void run() {
                                                                                try {
                                                                                    Bitmap bitmap = loadBitmap(uri.toString());
                                                                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                                                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
                                                                                    byte[] byteArray = bao.toByteArray();

                                                                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).putBytes(byteArray).addOnCompleteListener(mainActivity, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                            if (!task.isSuccessful()){
                                                                                                System.out.println("Error while copying photo");
                                                                                            }
                                                                                        }
                                                                                    });



                                                                                } catch (Exception e) {
                                                                                    Log.e("Thread", e.getMessage());
                                                                                }
                                                                            }
                                                                        });
                                                                        thread.start();




                                                                    }

                                                                    Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                                    if(currentFragment instanceof PlaylistsFragment) {

                                                                        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(p.getTitle(), "", p, 0)).commit();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }

                                                }).addOnFailureListener(mainActivity, new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        System.out.println("Error while setting songs");


                                                    }
                                                });
                                            }
                                        }

                                        if(snapshot.getChildrenCount()==0){
                                            //Copy photo
                                            storageReference = FirebaseStorage.getInstance().getReference();
                                            storageReference.child("images/playlists/"+mAuth.getCurrentUser().getUid()+"/"+old_key).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if(task.isComplete() && task.isSuccessful()){
                                                        System.out.println("URI HERE FOUND");

                                                        Uri uri = task.getResult();


                                                        Thread thread = new Thread(new Runnable(){
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    Bitmap bitmap = loadBitmap(uri.toString());
                                                                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
                                                                    byte[] byteArray = bao.toByteArray();

                                                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).putBytes(byteArray).addOnCompleteListener(mainActivity, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                            if (!task.isSuccessful()){
                                                                                System.out.println("Error while copying photo");
                                                                            }
                                                                        }
                                                                    });



                                                                } catch (Exception e) {
                                                                    Log.e("Thread", e.getMessage());
                                                                }
                                                            }
                                                        });
                                                        thread.start();




                                                    }

                                                    Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                    if(currentFragment instanceof PlaylistsFragment) {

                                                        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(p.getTitle(), "", p, 0)).commit();
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
                        });
                            }
                        }
                    });
                }else{
                    Toast.makeText(mainActivity, "Error while adding Playlist.",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public Bitmap loadBitmap(String url)
    {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is, 8192);
            bm = BitmapFactory.decodeStream(bis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }



    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView photo;
        TextView title, desc;
        ImageButton  settings_btn;
        LinearLayout linearLayout;

        TextView dialog_playlist_name, dialog_playlist_desc;
        Dialog dialog;
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
