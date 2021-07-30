package com.company.altasnotas.fragments.playlists;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.FirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.viewmodels.ProfileFragmentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CurrentPlaylistFragment extends Fragment {

    private final Playlist playlist;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView title, description;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    CurrentPlaylistAdapter adapter;
    private final String author;
    private final String album;
    private final Integer isAlbum;

    private Uri returnUri;
    private StorageReference storageReference;

    private TextView recyclerViewState;
    private FloatingActionButton fab;
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        View view =  inflater.inflate(R.layout.fragment_current_playlist, container, false);

        imageView = view.findViewById(R.id.current_playlist_img);
        title = view.findViewById(R.id.current_playlist_title);
        description = view.findViewById(R.id.current_playlist_description);
        fab = view.findViewById(R.id.current_playlist_photo_btn);
        recyclerViewState = view.findViewById(R.id.current_playlist_recycler_state);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription()+"\n("+playlist.getYear()+")");

        if(!playlist.getImage_id().isEmpty()) {
            Glide.with(container).load(playlist.getImage_id()).into(imageView);
        }else{
              Glide.with(container).load(R.drawable.img_not_found).into(imageView);
        }

        recyclerView =  view.findViewById(R.id.current_playlist_recycler_view);

        fab.setOnClickListener(v ->
        {
            if(ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        2000);
            }
            else {
                CropImage.activity()
                        .start(getContext(), this);
            }
        });
        if(isAlbum!=0)
        {
        initializeAlbum(author,album);
        }
        else
        {
            initializePlaylist(author);


        }

        return view;

    }

    public CurrentPlaylistFragment(String author, String album, Playlist playlist, Integer isAlbum){
        this.playlist=playlist;
        this.author=author;
        this.album=album;
        this.isAlbum=isAlbum;
    }


    private void initializeAlbum(String author, String album) {

        ArrayList<FirebaseSong> firebaseSongs = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        if (mAuth.getCurrentUser() != null) {
            CountDownLatch conditionLatch = new CountDownLatch(1);
            database_ref.child("music").child("albums").child(author).child(album).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot!=null){
                        int x = (int) snapshot.child("songs").getChildrenCount();
                    if(x!=0) {
                        int i = 0;
                        songs.clear();
                        for (DataSnapshot ds : snapshot.child("songs").getChildren()) {
                            i++;

                            FirebaseSong firebaseSong = new FirebaseSong();
                            firebaseSong.setOrder(Integer.valueOf(ds.child("order").getValue().toString()));
                            firebaseSong.setPath(ds.child("path").getValue().toString());
                            firebaseSong.setTitle(ds.child("title").getValue().toString());
                            firebaseSongs.add(firebaseSong);
                        }


                        Collections.sort(firebaseSongs, (f1, f2) -> f1.getOrder().compareTo(f2.getOrder()));


                        for (FirebaseSong song : firebaseSongs) {

                            Song local_song = new Song(playlist.getDescription(), playlist.getTitle(), song.getTitle(), song.getPath(), playlist.getImage_id());
                            songs.add(local_song);
                        }

                        if (i == x) {
                            playlist.setSongs(songs);
                            conditionLatch.countDown();

                        }

                        try {
                            conditionLatch.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.out.println("ConditionLatch error");
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerViewState.setVisibility(View.GONE);
                    }else{
                        recyclerViewState.setText("Empty Album");
                        recyclerView.setVisibility(View.GONE);
                        recyclerViewState.setVisibility(View.VISIBLE);
                    }
                        playlist.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                        if(playlist.isAlbum()){
                            fab.setVisibility(View.INVISIBLE);
                        }
                        if(x!=0){
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                        adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist,false);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    conditionLatch.countDown();
                }

            });


        }
    }


    private void initializePlaylist(String title) {

        ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        if (mAuth.getCurrentUser() != null) {
            CountDownLatch conditionLatch = new CountDownLatch(1);
            database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot!=null) {
                      for(DataSnapshot da : snapshot.getChildren()){
                          if(da.child("title").getValue().equals(title)){
                              snapshot =da;
                              int x = (int) snapshot.child("songs").getChildrenCount();
                              if (x != 0 ){
                                  int i = 0;
                                  songs.clear();
                                  for (DataSnapshot ds : snapshot.child("songs").getChildren()) {
                                      i++;

                                      FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
                                      favoriteFirebaseSong.setNumerInAlbum(Integer.valueOf(ds.child("numberInAlbum").getValue().toString()));
                                      favoriteFirebaseSong.setAuthor(ds.child("author").getValue().toString());
                                      favoriteFirebaseSong.setAlbum(ds.child("album").getValue().toString());

                                      favoriteFirebaseSongs.add(favoriteFirebaseSong);
                                  }

                                  for (FavoriteFirebaseSong song: favoriteFirebaseSongs) {

                                      database_ref.child("music").child("albums").child(song.getAuthor()).child(song.getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                                          @Override
                                          public void onDataChange(@NonNull DataSnapshot snapshot) {

                                              if(snapshot!=null) {
                                                  for (DataSnapshot ds : snapshot.child("songs").getChildren()) {
                                                      if(Integer.parseInt(ds.child("order").getValue().toString()) == song.getNumerInAlbum()){
                                                          Song local_song = new Song( song.getAuthor(), song.getAlbum(),  ds.child("title").getValue().toString(), ds.child("path").getValue().toString(), snapshot.child("image_id").getValue().toString());
                                                          songs.add(local_song);
                                                      }
                                                  }

                                                  playlist.setSongs(songs);




                                                  if(playlist.getSongs()!=null) {
                                                      adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, true);
                                                      recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                                      recyclerView.setAdapter(adapter);
                                                  }

                                              }
                                          }



                                          @Override
                                          public void onCancelled(@NonNull DatabaseError error) {

                                          }
                                      });



                                  }



                                  if (i == x) {
                                      playlist.setSongs(songs);
                                      conditionLatch.countDown();

                                  }

                                  try {
                                      conditionLatch.await();
                                  } catch (InterruptedException e) {
                                      e.printStackTrace();
                                      System.out.println("ConditionLatch error");
                                  }

                                  recyclerView.setVisibility(View.VISIBLE);
                                  recyclerViewState.setVisibility(View.GONE);
                              }else{
                                  recyclerViewState.setText("Empty Playlist");
                                  recyclerViewState.setVisibility(View.VISIBLE);
                                  recyclerView.setVisibility(View.GONE);
                              }

                                //Load photo
                              database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid().toString()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot snapshot) {
                                      if(snapshot!=null) {
                                          for (DataSnapshot ds : snapshot.getChildren()) {
                                              if (ds.child("title").getValue().toString().equals(playlist.getTitle())) {
                                                  storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).getDownloadUrl().addOnCompleteListener(requireActivity(), new OnCompleteListener<Uri>() {
                                                      @Override
                                                      public void onComplete(@NonNull Task<Uri> task) {
                                                          if(task.isSuccessful()){
                                                              Glide.with(requireActivity()).load(task.getResult()).apply(RequestOptions.centerCropTransform()).into(imageView);
                                                          }else{
                                                              Glide.with(requireActivity()).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).into(imageView);
                                                          }
                                                      }
                                                  });

                                              }
                                          }
                                      }
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("Firebase DB Error: "+error.getMessage(),"FirebaseDatabase");
                                  }
                              });


                              playlist.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                              if(!playlist.isAlbum()){
                                  fab.setVisibility(View.VISIBLE);
                              }


                              if(x!=0) {
                                  recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                  adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, false);
                                  adapter.notifyDataSetChanged();
                                  recyclerView.setAdapter(adapter);
                              }
                          }
                      }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("Firebase DB Error: "+error.getMessage(),"FirebaseDatabase");
                    conditionLatch.countDown();
                }

            });


        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode ==RESULT_CANCELED){
            Log.d("RESULT HAVE BEEN CANCELED", "RESULT");
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                returnUri = result.getUriContent();

                try {

                    Bitmap compresedImg =  ProfileFragmentViewModel.getBitmapFormUri(getActivity(), returnUri);
                    Bitmap compressImgRotated = rotateImageIfRequired(getContext(), compresedImg,returnUri);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    compressImgRotated = getResizedBitmap(compressImgRotated,300);
                    compressImgRotated.compress(Bitmap.CompressFormat.PNG, 100, bao);

                    Glide.with(requireActivity()).load(compressImgRotated).apply(RequestOptions.centerCropTransform()).into(imageView);

                    byte[] byteArray = bao.toByteArray();

                    compresedImg.recycle();

                    //Upload image
                    storageReference = FirebaseStorage.getInstance().getReference();

                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid().toString()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot!=null) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.child("title").getValue().toString().equals(playlist.getTitle())) {
                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                System.out.println("Upload image is successful!");
                                            } else {
                                                System.out.println("Upload image failed!");

                                            }
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



                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Error while compressing and uploading photo to Firebase", "FirebaseStorage");
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}