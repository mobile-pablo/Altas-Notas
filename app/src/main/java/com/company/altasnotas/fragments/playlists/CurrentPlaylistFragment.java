package com.company.altasnotas.fragments.playlists;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.FirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.viewmodels.fragments.favorites.FavoritesFragmentViewModel;
import com.company.altasnotas.viewmodels.fragments.playlists.CurrentPlaylistFragmentViewModel;
import com.company.altasnotas.viewmodels.fragments.profile.ProfileFragmentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CurrentPlaylistFragment extends Fragment {

    private final Playlist playlist;
    public static RecyclerView recyclerView;
    private ImageView imageView;
    private AppCompatTextView title, description;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    public static CurrentPlaylistAdapter adapter;
    private final String author;
    private final String album;
    private final Integer isAlbum;

    private Uri returnUri;
    private StorageReference storageReference;

    public static TextView recyclerViewState;
    private FloatingActionButton fab;
    private CurrentPlaylistFragmentViewModel viewModel;

    private ImageView settings_btn;


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_playlist, container, false);
        MainActivity.main_activty_box.setBackgroundColor(Color.WHITE);
        imageView = view.findViewById(R.id.current_playlist_img);
        title = view.findViewById(R.id.current_playlist_title);
        description = view.findViewById(R.id.current_playlist_description);
        fab = view.findViewById(R.id.current_playlist_photo_btn);
        recyclerViewState = view.findViewById(R.id.current_playlist_recycler_state);

        settings_btn = view.findViewById(R.id.current_playlist_settings);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        viewModel =  new ViewModelProvider(requireActivity()).get(CurrentPlaylistFragmentViewModel.class);
        viewModel.init(playlist, (MainActivity) getActivity(), database_ref, mAuth,storageReference);
        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription() + "\n(" + playlist.getYear() + ")");
        recyclerView = view.findViewById(R.id.current_playlist_recycler_view);

        if (!  viewModel.getPlaylist().getImage_id().isEmpty()) {

            Glide.with(container)
                    .load(playlist.getImage_id())
                    .apply(RequestOptions.centerCropTransform())
                    .error(R.drawable.img_not_found).into(imageView);
        } else {
            Glide.with(container).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).into(imageView);
        }

        fab.setOnClickListener(v ->
        {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        2000);
            } else {
                CropImage.activity()
                        .start(getContext(), this);
            }
        });
        if (isAlbum != 0) {
            settings_btn.setVisibility(View.INVISIBLE);
            initializeAlbum(author, album);
        } else {
            settings_btn.setVisibility(View.VISIBLE);

            initializePlaylist(author);
        }


        MainActivity.currentSongTitle.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
             if(adapter!=null){
                 adapter.notifyDataSetChanged();
             }
            }
        });


        settings_btn.setOnClickListener(v -> {
           viewModel.openPlaylistSettings((MainActivity) getActivity());
        });
        return view;

    }


    public CurrentPlaylistFragment(String author, String album, Playlist playlist, Integer isAlbum) {
        this.playlist = playlist;
        this.author = author;
        this.album = album;
        this.isAlbum = isAlbum;

        /* ViewModel isFav
        0 - Playlist
        -1 - Album
        1 - Fav
         */
    }


    private void initializeAlbum(String author, String album) {

        ArrayList<FirebaseSong> firebaseSongs = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        if (mAuth.getCurrentUser() != null) {
            CountDownLatch conditionLatch = new CountDownLatch(1);
            database_ref.child("music").child("albums").child(author).child(album).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null) {
                        int x = (int) snapshot.child("songs").getChildrenCount();
                        if (x != 0) {
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

                                Song local_song = new Song(playlist.getDir_desc(), playlist.getDir_title(), song.getTitle(), song.getPath(), playlist.getImage_id(), song.getOrder());
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
                        } else {
                            recyclerViewState.setText("Empty Album");
                            recyclerView.setVisibility(View.GONE);
                            recyclerViewState.setVisibility(View.VISIBLE);
                        }
                        playlist.setAlbum(true);

                        fab.setVisibility(View.INVISIBLE);

                        if (x != 0) {
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, -1);
                            adapter.notifyDataSetChanged();
                            Drawable songBg = AppCompatResources.getDrawable(requireContext(), R.drawable.custom_song_bg);
                            recyclerView.setBackground(songBg);
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

        if (mAuth.getCurrentUser() != null) {
            CountDownLatch conditionLatch = new CountDownLatch(1);
            database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null) {
                        for (DataSnapshot da : snapshot.getChildren()) {
                            if (da.child("title").getValue().equals(title)) {
                                snapshot = da;
                                int x = (int) snapshot.child("songs").getChildrenCount();
                                if (x != 0) {
                                    for (DataSnapshot ds : snapshot.child("songs").getChildren()) {


                                        FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
                                        favoriteFirebaseSong.setNumberInAlbum(Integer.valueOf(ds.child("numberInAlbum").getValue().toString()));
                                        favoriteFirebaseSong.setAuthor(ds.child("author").getValue().toString());
                                        favoriteFirebaseSong.setAlbum(ds.child("album").getValue().toString());
                                        favoriteFirebaseSong.setDateTime(Calendar.getInstance().getTimeInMillis());
                                        favoriteFirebaseSongs.add(favoriteFirebaseSong);

                                        try {
                                            Thread.sleep(1);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    if (favoriteFirebaseSongs.size() == x) {
                                        initializePlaylistRecyclerView(favoriteFirebaseSongs);

                                        playlist.setSongs(new ArrayList<>());
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
                                } else {
                                    recyclerViewState.setText("Empty Playlist");
                                    recyclerViewState.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }


                                playlist.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                                if (!playlist.isAlbum()) {
                                    fab.setVisibility(View.VISIBLE);
                                }


                                if (x != 0) {
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                    adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, 0);
                                    adapter.notifyDataSetChanged();
                                    recyclerView.setAdapter(adapter);
                                }
                            }
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(MainActivity.FIREBASE,"Firebase DB Error: " + error.getMessage());
                    conditionLatch.countDown();
                }

            });


        }
    }

    private void initializePlaylistRecyclerView(ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs) {
        ArrayList<Song> songs = new ArrayList<>();
        for (int j = 0; j < favoriteFirebaseSongs.size(); j++) {

            FavoriteFirebaseSong song = favoriteFirebaseSongs.get(j);

            database_ref.child("music").child("albums").child(song.getAuthor()).child(song.getAlbum()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot != null) {
                        for (DataSnapshot ds : snapshot.child("songs").getChildren()) {


                            if (Integer.parseInt(ds.child("order").getValue().toString()) == song.getNumberInAlbum()) {
                                Song local_song = new Song(song.getAuthor(), song.getAlbum(), ds.child("title").getValue().toString(), ds.child("path").getValue().toString(), snapshot.child("image_id").getValue().toString(), song.getNumberInAlbum());
                                local_song.setDateTime(song.getDateTime());
                                songs.add(local_song);

                            }

                            if (songs.size() == favoriteFirebaseSongs.size()) {
                                Collections.sort(songs, (f1, f2) -> f1.getDateTime().compareTo(f2.getDateTime()));
                                playlist.setSongs(songs);

                                if (playlist.getSongs() != null) {
                                    adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, 0);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                    recyclerView.setAdapter(adapter);
                                    Drawable songBg = AppCompatResources.getDrawable(requireContext(), R.drawable.custom_song_bg);
                                    recyclerView.setBackground(songBg);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_CANCELED) {
            Log.d("RESULT","RESULT HAVE BEEN CANCELED");
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                returnUri = result.getUriContent();

                try {

                    Bitmap compresedImg = ProfileFragmentViewModel.getBitmapFormUri(requireActivity(), returnUri);
                    Bitmap compressImgRotated =  CurrentPlaylistFragmentViewModel.rotateImageIfRequired(requireContext(), compresedImg, returnUri);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    compressImgRotated =  viewModel.getResizedBitmap(compressImgRotated, 300);
                    compressImgRotated.compress(Bitmap.CompressFormat.PNG, 100, bao);

                    ProgressDialog progress = new ProgressDialog(getContext());
                    progress.setTitle("Loading Photo");
                    progress.setMessage("Please wait...");
                    progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                    progress.show();


                    byte[] byteArray = bao.toByteArray();

                    compresedImg.recycle();

                    //Upload image
                    storageReference = FirebaseStorage.getInstance().getReference();

                    Bitmap finalCompressImgRotated = compressImgRotated;
                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot != null) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    if (ds.child("title").getValue().toString().equals(playlist.getTitle())) {
                                        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    System.out.println("Upload image is successful!");
                                                    if (getActivity() != null) {
                                                        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                        if (currentFragment instanceof CurrentPlaylistFragment) {
                                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).getDownloadUrl().addOnSuccessListener(requireActivity(), new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri u) {
                                                                    progress.dismiss();
                                                                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(ds.getKey()).child("image_id").setValue(u.toString());
                                                                    playlist.setImage_id(u.toString());
                                                                    Glide.with(requireActivity()).load(finalCompressImgRotated).apply(RequestOptions.centerCropTransform()).into(imageView);

                                                                }
                                                            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    progress.dismiss();
                                                                }
                                                            });

                                                        }
                                                    }

                                                } else {
                                                    System.out.println("Upload image failed!");
                                                    progress.dismiss();
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
                    Log.d(MainActivity.FIREBASE,"Error while compressing and uploading photo to Firebase");
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


}