package com.company.altasnotas.fragments.playlists;

import android.Manifest;
import android.app.Dialog;
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
import com.company.altasnotas.viewmodels.ProfileFragmentViewModel;
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
    public RecyclerView recyclerView;
    private ImageView imageView;
    private AppCompatTextView title, description;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    CurrentPlaylistAdapter adapter;
    private final String author;
    private final String album;
    private final Integer isAlbum;

    private Uri returnUri;
    private StorageReference storageReference;

    public TextView recyclerViewState;
    private FloatingActionButton fab;

    private ImageView settings_btn;
    private BottomSheetDialog bottomSheetDialog;

    private Dialog dialog;
    private TextView dialog_playlist_name, dialog_playlist_desc;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_playlist, container, false);

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

        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription() + "\n(" + playlist.getYear() + ")");


        recyclerView = view.findViewById(R.id.current_playlist_recycler_view);

        if (!playlist.getImage_id().isEmpty()) {
            Glide.with(container)
                    .load(playlist.getImage_id())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
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


        settings_btn.setOnClickListener(v -> {
            openPlaylistSettings();
        });
        return view;

    }


    public CurrentPlaylistFragment(String author, String album, Playlist playlist, Integer isAlbum) {
        this.playlist = playlist;
        this.author = author;
        this.album = album;
        this.isAlbum = isAlbum;
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
                    Log.d("Firebase DB Error: " + error.getMessage(), "FirebaseDatabase");
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
            Log.d("RESULT HAVE BEEN CANCELED", "RESULT");
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                returnUri = result.getUriContent();

                try {

                    Bitmap compresedImg = ProfileFragmentViewModel.getBitmapFormUri(requireActivity(), returnUri);
                    Bitmap compressImgRotated = rotateImageIfRequired(requireContext(), compresedImg, returnUri);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    compressImgRotated = getResizedBitmap(compressImgRotated, 300);
                    compressImgRotated.compress(Bitmap.CompressFormat.PNG, 100, bao);

                    Glide.with(requireActivity()).load(compressImgRotated).apply(RequestOptions.centerCropTransform()).into(imageView);

                    byte[] byteArray = bao.toByteArray();

                    compresedImg.recycle();

                    //Upload image
                    storageReference = FirebaseStorage.getInstance().getReference();

                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid().toString()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(ds.getKey()).child("image_id").setValue(u.toString());
                                                                    playlist.setImage_id(u.toString());
                                                                }
                                                            });

                                                        }
                                                    }

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

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void openPlaylistSettings() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_playlist_settings);

        LinearLayout copy = bottomSheetDialog.findViewById(R.id.bottom_settings_copy_box);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.bottom_settings_delete_box);
        LinearLayout dismissDialog = bottomSheetDialog.findViewById(R.id.bottom_settings_dismiss_box);

        copy.setOnClickListener(v -> {
            settingsCopy(playlist);
            bottomSheetDialog.dismiss();
        });

        delete.setOnClickListener(v -> {
            deletePlaylist(playlist);
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void settingsCopy(Playlist playlist) {
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (
                            ds.child("title").getValue().toString().trim().equals(playlist.getTitle())
                                    &&
                                    ds.child("description").getValue().toString().trim().equals(playlist.getDescription())
                    ) {
                        openDialog(ds.getKey(), playlist);
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
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (
                            ds.child("title").getValue().toString().trim().equals(playlist.getTitle())
                                    &&
                                    ds.child("description").getValue().toString().trim().equals(playlist.getDescription())
                    ) {

                        String key = ds.getKey();

                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).removeValue().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    System.out.println("Playlist deleted");

                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(getActivity(), new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).delete().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    MainActivity mainActivity = (MainActivity) getActivity();
                                                    mainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_home_item);

                                                    for (int i = 0; i < mainActivity.getSupportFragmentManager().getBackStackEntryCount(); i++) {
                                                        mainActivity.getSupportFragmentManager().popBackStack();
                                                    }

                                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
                                                    if (task.isSuccessful()) {
                                                        System.out.println("Photo deleted with playlist");
                                                    } else {
                                                        Toast.makeText(getActivity(), "Error while deleting Photo", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d("Firebase", "Photo wasn't found");
                                        }
                                    });

                                } else {
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

    private void openDialog(String key, Playlist playlist) {


        dialog = new Dialog(getActivity(), R.style.Theme_AltasNotas);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.add_playlists_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        ImageButton cancel, accept;

        dialog_playlist_name = dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_name);
        dialog_playlist_desc = dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_desc);

        dialog_playlist_name.setText(playlist.getTitle());
        dialog_playlist_desc.setText(playlist.getDescription());

        cancel = dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_cancel_btn);
        accept = dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_accept_btn);


        cancel.setOnClickListener(v -> dialog.dismiss());


        accept.setOnClickListener(v -> validInput(key, dialog_playlist_name.getText().toString(), dialog_playlist_desc.getText().toString(), playlist));

        dialog.show();

    }

    private void validInput(String key, String name, String desc, Playlist playlist) {
        name = name.trim();
        desc = desc.trim();


        if (name.isEmpty() && desc.isEmpty()) {
            Toast.makeText(getActivity(), "Both fields are empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
        } else {

            if (name.isEmpty() || desc.isEmpty()) {
                if (name.isEmpty()) {
                    Toast.makeText(getActivity(), "Name is empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
                }

                if (desc.isEmpty()) {
                    Toast.makeText(getActivity(), "Description is empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1).toLowerCase();
                String finalName = name;
                String finalDesc = desc;
                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null) {
                            int x = (int) snapshot.getChildrenCount();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                if (ds.child("title").getValue().toString().trim().equals(finalName)) {
                                    x--;
                                    Toast.makeText(getActivity(), "Playlist exist with same title!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (x == snapshot.getChildrenCount()) {
                                dialog.dismiss();
                                copyPlaylist(key, finalName, finalDesc, playlist);
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

    private void copyPlaylist(String old_key, String name, String desc, Playlist p) {

        p.setTitle(name);
        p.setDescription(desc);

        String key = database_ref.push().getKey();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).setValue(p).addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("isAlbum").setValue(p.isAlbum()).addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("album").removeValue().addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //Copy songs
                                        int[] x = {0};
                                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(old_key).child("songs").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                ArrayList<FavoriteFirebaseSong> favoriteFirebaseSongs = new ArrayList<>();
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                    x[0]++;
                                                    FavoriteFirebaseSong favoriteFirebaseSong = new FavoriteFirebaseSong();
                                                    favoriteFirebaseSong.setAuthor(dataSnapshot.child("author").getValue().toString());
                                                    favoriteFirebaseSong.setAlbum(dataSnapshot.child("album").getValue().toString());
                                                    favoriteFirebaseSong.setNumberInAlbum(Integer.valueOf(dataSnapshot.child("numberInAlbum").getValue().toString()));
                                                    favoriteFirebaseSongs.add(favoriteFirebaseSong);
                                                    if (x[0] == snapshot.getChildrenCount()) {
                                                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("songs").setValue(favoriteFirebaseSongs).addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    //Copy photo
                                                                    storageReference = FirebaseStorage.getInstance().getReference();
                                                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + old_key).getDownloadUrl().addOnCompleteListener(requireActivity(), new OnCompleteListener<Uri>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                                            if (task.isComplete() && task.isSuccessful()) {
                                                                                System.out.println("URI HERE FOUND");

                                                                                Uri uri = task.getResult();


                                                                                Thread thread = new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        try {
                                                                                            Bitmap bitmap = loadBitmap(uri.toString());
                                                                                            ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                                                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
                                                                                            byte[] byteArray = bao.toByteArray();

                                                                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).putBytes(byteArray).addOnCompleteListener(requireActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                                    if (!task.isSuccessful()) {
                                                                                                        System.out.println("Error while copying photo");
                                                                                                    } else {
                                                                                                        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(requireActivity(), new OnSuccessListener<Uri>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Uri u) {
                                                                                                                p.setImage_id(u.toString());
                                                                                                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("image_id").setValue(u.toString());
                                                                                                            }
                                                                                                        });
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

                                                                            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                                            if (currentFragment instanceof CurrentPlaylistFragment) {

                                                                                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(p.getTitle(), "", p, 0)).commit();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }

                                                        }).addOnFailureListener(requireActivity(), new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                System.out.println("Error while setting songs");


                                                            }
                                                        });
                                                    }
                                                }

                                                if (snapshot.getChildrenCount() == 0) {
                                                    //Copy photo
                                                    storageReference = FirebaseStorage.getInstance().getReference();
                                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + old_key).getDownloadUrl().addOnCompleteListener(requireActivity(), new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isComplete() && task.isSuccessful()) {
                                                                System.out.println("URI HERE FOUND");

                                                                Uri uri = task.getResult();


                                                                Thread thread = new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            Bitmap bitmap = loadBitmap(uri.toString());
                                                                            ByteArrayOutputStream bao = new ByteArrayOutputStream();
                                                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bao);
                                                                            byte[] byteArray = bao.toByteArray();

                                                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).putBytes(byteArray).addOnCompleteListener(requireActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                    if (!task.isSuccessful()) {
                                                                                        System.out.println("Error while copying photo");
                                                                                    } else {
                                                                                        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(requireActivity(), new OnSuccessListener<Uri>() {
                                                                                            @Override
                                                                                            public void onSuccess(Uri u) {
                                                                                                p.setImage_id(u.toString());
                                                                                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("image_id").setValue(u.toString());
                                                                                            }
                                                                                        });
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

                                                            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                            if (currentFragment instanceof CurrentPlaylistFragment) {

                                                                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(p.getTitle(), "", p, 0)).commit();
                                                            }
                                                        }
                                                    }).addOnFailureListener(requireActivity(), new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                            if (currentFragment instanceof CurrentPlaylistFragment) {

                                                                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(p.getTitle(), "", p, 0)).commit();
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
                } else {
                    Toast.makeText(requireActivity(), "Error while adding Playlist.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public Bitmap loadBitmap(String url) {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is, 8192);
            bm = BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }
}