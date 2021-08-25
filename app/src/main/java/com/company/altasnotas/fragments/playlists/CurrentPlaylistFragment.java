package com.company.altasnotas.fragments.playlists;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.databinding.FragmentCurrentPlaylistBinding;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.FirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.viewmodels.fragments.playlists.CurrentPlaylistFragmentViewModel;
import com.company.altasnotas.viewmodels.fragments.profile.ProfileFragmentViewModel;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CurrentPlaylistFragment extends Fragment {

    private final Playlist playlist;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    public static CurrentPlaylistAdapter adapter;
    private final String author;
    private final String album;
    private final Integer isAlbum;
    private MainActivity mainActivity;
    private Uri returnUri;
    private StorageReference storageReference;
    private BottomSheetDialog bottomSheetDialog;
    private Dialog dialog;
    private TextView dialog_playlist_name, dialog_playlist_desc;

    private CurrentPlaylistFragmentViewModel viewModel;
    public static FragmentCurrentPlaylistBinding binding;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       binding= FragmentCurrentPlaylistBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        mainActivity = (MainActivity) getActivity();
        mainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        viewModel =  new ViewModelProvider(requireActivity()).get(CurrentPlaylistFragmentViewModel.class);
        viewModel.setPlaylist(playlist);
        binding.currentPlaylistTitle.setText(playlist.getTitle());
        binding.currentPlaylistDescription.setText(playlist.getDescription() + "\n(" + playlist.getYear() + ")");

        if (!  viewModel.getPlaylist().getImage_id().isEmpty()) {

            Glide.with(container)
                    .load(playlist.getImage_id())
                    .apply(RequestOptions.centerCropTransform())
                    .error(R.drawable.img_not_found).into( binding.currentPlaylistImg);
        } else {
            Glide.with(container).load(R.drawable.img_not_found).apply(RequestOptions.centerCropTransform()).into(binding.currentPlaylistImg);
        }

        binding.currentPlaylistPhotoBtn.setOnClickListener(v ->
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
            binding.currentPlaylistSettingsBtn.setVisibility(View.INVISIBLE);
            initializeAlbum(author, album);
        } else {
            binding.currentPlaylistSettingsBtn.setVisibility(View.VISIBLE);

            initializePlaylist(author);
        }


       MainActivity.viewModel.getCurrentSongTitle().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
             if(adapter!=null){
                 adapter.notifyDataSetChanged();
             }
            }
        });


        binding.currentPlaylistSettingsBtn.setOnClickListener(v -> {
          openPlaylistSettings();
        });

        viewModel.setMainActivity(mainActivity);
        initializeObservers();
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
                                Object gif =ds.child("gif_url").getValue();
                                String gif_str = "";
                                if(gif!=null){
                                    gif_str = gif.toString();
                                }
                                firebaseSong.setGifUrl(gif_str);
                                firebaseSongs.add(firebaseSong);
                            }


                            Collections.sort(firebaseSongs, (f1, f2) -> f1.getOrder().compareTo(f2.getOrder()));


                            for (FirebaseSong song : firebaseSongs) {

                                Song local_song = new Song(playlist.getDir_desc(), playlist.getDir_title(), song.getTitle(), song.getPath(), playlist.getImage_id(), song.getOrder(),song.getGifUrl());
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
                            binding.currentPlaylistRecyclerView.setVisibility(View.VISIBLE);
                            binding.currentPlaylistRecyclerState.setVisibility(View.GONE);
                        } else {
                            binding.currentPlaylistRecyclerState.setText("Empty Album");
                            binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                            binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                        }
                        playlist.setAlbum(true);

                        binding.currentPlaylistPhotoBtn.setVisibility(View.INVISIBLE);

                        if (x != 0) {
                            binding.currentPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                            adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, -1);
                            adapter.notifyDataSetChanged();
                            Drawable songBg = AppCompatResources.getDrawable(requireContext(), R.drawable.custom_song_bg);
                            binding.currentPlaylistRecyclerView.setBackground(songBg);
                            binding.currentPlaylistRecyclerView.setAdapter(adapter);
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

                                    binding.currentPlaylistRecyclerView.setVisibility(View.VISIBLE);
                                    binding.currentPlaylistRecyclerState.setVisibility(View.GONE);
                                } else {
                                    binding.currentPlaylistRecyclerState.setText("Empty Playlist");
                                    binding.currentPlaylistRecyclerState.setVisibility(View.VISIBLE);
                                    binding.currentPlaylistRecyclerView.setVisibility(View.GONE);
                                }


                                playlist.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                                if (!playlist.isAlbum()) {
                                    binding.currentPlaylistPhotoBtn.setVisibility(View.VISIBLE);
                                }


                                if (x != 0) {
                                    binding.currentPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                    adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, 0);
                                    adapter.notifyDataSetChanged();
                                    binding.currentPlaylistRecyclerView.setAdapter(adapter);
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
                                Object gif = ds.child("gif_url").getValue();
                                String gif_str="";
                                if(gif!=null){
                                    gif_str=gif.toString();
                                }
                                Song local_song = new Song(song.getAuthor(), song.getAlbum(), ds.child("title").getValue().toString(), ds.child("path").getValue().toString(), snapshot.child("image_id").getValue().toString(), song.getNumberInAlbum(),gif_str);
                                local_song.setDateTime(song.getDateTime());

                                songs.add(local_song);

                            }

                            if (songs.size() == favoriteFirebaseSongs.size()) {
                                Collections.sort(songs, (f1, f2) -> f1.getDateTime().compareTo(f2.getDateTime()));
                                playlist.setSongs(songs);

                                if (playlist.getSongs() != null) {
                                    adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist, 0);
                                    binding.currentPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                                    binding.currentPlaylistRecyclerView.setAdapter(adapter);
                                    Drawable songBg = AppCompatResources.getDrawable(requireContext(), R.drawable.custom_song_bg);
                                    binding.currentPlaylistRecyclerView.setBackground(songBg);
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
                    Bitmap compressImgRotated =  ProfileFragment.rotateImageIfRequired(requireContext(), compresedImg, returnUri);
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
                                                        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
                                                        if (currentFragment instanceof CurrentPlaylistFragment) {
                                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + ds.getKey()).getDownloadUrl().addOnSuccessListener(requireActivity(), new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri u) {
                                                                    progress.dismiss();
                                                                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(ds.getKey()).child("image_id").setValue(u.toString());
                                                                    playlist.setImage_id(u.toString());
                                                                    Glide.with(requireActivity()).load(finalCompressImgRotated).apply(RequestOptions.centerCropTransform()).into(binding.currentPlaylistImg);

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


    private void openPlaylistSettings() {
        bottomSheetDialog = new BottomSheetDialog(mainActivity);
        bottomSheetDialog.setContentView(R.layout.bottom_playlist_settings);
        bottomSheetDialog.getBehavior().setPeekHeight(MainActivity.dialogHeight);
        LinearLayout copy = bottomSheetDialog.findViewById(R.id.bottomSettingsCopyBox);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.bottomSettingsDeleteBox);
        LinearLayout dismissDialog = bottomSheetDialog.findViewById(R.id.bottomSettingsDismissBox);

        copy.setOnClickListener(v -> {
            viewModel.settingsCopy(playlist);
            bottomSheetDialog.dismiss();
        });

        delete.setOnClickListener(v -> {
            viewModel.deletePlaylist(playlist);
            bottomSheetDialog.dismiss();
        });
        dismissDialog.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }
    private void openDialog(String key, Playlist playlist) {

        dialog = new Dialog(mainActivity, R.style.Theme_AltasNotas);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.add_playlists_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        ImageButton cancel, accept;

        dialog_playlist_name = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogName);
        dialog_playlist_desc = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogDesc);

        dialog_playlist_name.setText(playlist.getTitle());
        dialog_playlist_desc.setText(playlist.getDescription());

        cancel = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogCancelBtn);
        accept = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogAcceptBtn);


        cancel.setOnClickListener(v -> dialog.dismiss());

        accept.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.setOldKey(key);
            viewModel.setP(playlist);
            viewModel.validInput(dialog_playlist_name.getText().toString(), dialog_playlist_desc.getText().toString());
        });

        dialog.show();

    }
    private void initializeObservers(){
        viewModel.getValidErrorState().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                String errorMsg=null;
                switch (integer){
                    case 1: errorMsg="Both fields are empty.\nPlease fill data."; break;
                    case 2:errorMsg="Name is empty.\nPlease fill data."; break;
                    case 3: errorMsg="Description is empty.\nPlease fill data."; break;
                    case 4: errorMsg="Playlist exist with same title!"; break;
                    default: errorMsg="Unknown error!"; break;
                }

                if(errorMsg!=null){
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getDeleteState().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                String errorMsg=null;
                switch (integer){
                    case 0: errorMsg="Photo deleted with playlist"; break;
                    case 1: errorMsg="Error while deleting Photo"; break;
                    case 2:errorMsg="Photo wasn't found"; break;
                    case 3:
                    case 4: errorMsg="Error while  deleting Playlist"; break;
                    case 5: errorMsg="Photo to copy isnt found!"; break;
                    default: errorMsg="Unknown error!"; break;
                }

                if(errorMsg!=null){
                   Log.d("DeletePlaylist", errorMsg);
                }

                switch (integer){
                    case 0:
                    case 2:
                    case 3:
                    case 4:
                    default:
                        if(errorMsg!=null){
                        Log.d("DeletePlaylist", errorMsg);
                    }
                    break;

                    case 1:
                if(errorMsg!=null) {
                    Toast.makeText(mainActivity, errorMsg, Toast.LENGTH_SHORT).show();
                }
                    break;
                }

               if(integer<5){
                   openHomeFragment();
               }
            }
        });

        viewModel.getShouldOpenCopy().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    openDialog(viewModel.getKey(), viewModel.getPlaylist());
                    viewModel.setShouldOpenCopy(false);
                }
            }
        });

        viewModel.getShouldOpenCurrentFragment().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    openCurrentFragment();
                }
            }
        });

    }

    private void openCurrentFragment() {
        Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.mainFragmentContainer);
        if (currentFragment instanceof CurrentPlaylistFragment) {

            mainActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_up,R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_up).replace(R.id.mainFragmentContainer, new CurrentPlaylistFragment(viewModel.getP().getTitle(), "", viewModel.getP(), 0)).commit();
        }
    }

    private void openHomeFragment() {
        mainActivity.activityMainBinding.mainNavBottom.setSelectedItemId(R.id.nav_home_item);

        for (int i = 0; i < mainActivity.getSupportFragmentManager().getBackStackEntryCount(); i++) {
            mainActivity.getSupportFragmentManager().popBackStack();
        }
        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer, new HomeFragment(false)).commit();
    }


}