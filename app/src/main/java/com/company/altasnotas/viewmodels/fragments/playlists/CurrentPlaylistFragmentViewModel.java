package com.company.altasnotas.viewmodels.fragments.playlists;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.Playlist;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class CurrentPlaylistFragmentViewModel  extends ViewModel {
    private BottomSheetDialog bottomSheetDialog;
    private Dialog dialog;
    private TextView dialog_playlist_name, dialog_playlist_desc;

    private Playlist playlist;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private MainActivity mainActivity;

    public void init(Playlist playlist, MainActivity mainActivity, DatabaseReference database_ref, FirebaseAuth mAuth, StorageReference storageReference){
        this.playlist=playlist;
        this.mainActivity=mainActivity;
        this.database_ref=database_ref;
        this.mAuth=mAuth;
        this.storageReference =storageReference;
    }

    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

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

    public static Bitmap rotateImage(Bitmap img, int degree) {
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


    public void openPlaylistSettings(MainActivity mainActivity) {
        bottomSheetDialog = new BottomSheetDialog(mainActivity);
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

    public void settingsCopy(Playlist playlist) {
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

    public void deletePlaylist(Playlist playlist) {
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

                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).removeValue().addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    System.out.println("Playlist deleted");

                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(mainActivity, new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).delete().addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    mainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_home_item);

                                                    for (int i = 0; i < mainActivity.getSupportFragmentManager().getBackStackEntryCount(); i++) {
                                                        mainActivity.getSupportFragmentManager().popBackStack();
                                                    }

                                                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
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
                                            Log.d(MainActivity.FIREBASE, "Photo wasn't found");
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

    public void openDialog(String key, Playlist playlist) {


        dialog = new Dialog(mainActivity, R.style.Theme_AltasNotas);
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

    public void validInput(String key, String name, String desc, Playlist playlist) {
        name = name.trim();
        desc = desc.trim();


        if (name.isEmpty() && desc.isEmpty()) {
            Toast.makeText(mainActivity, "Both fields are empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
        } else {

            if (name.isEmpty() || desc.isEmpty()) {
                if (name.isEmpty()) {
                    Toast.makeText(mainActivity, "Name is empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
                }

                if (desc.isEmpty()) {
                    Toast.makeText(mainActivity, "Description is empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(mainActivity, "Playlist exist with same title!", Toast.LENGTH_SHORT).show();
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

    public void copyPlaylist(String old_key, String name, String desc, Playlist p) {

        p.setTitle(name);
        p.setDescription(desc);

        String key = database_ref.push().getKey();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).setValue(p).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("isAlbum").setValue(p.isAlbum()).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("album").removeValue().addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
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
                                                        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("songs").setValue(favoriteFirebaseSongs).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    //Copy photo
                                                                    storageReference = FirebaseStorage.getInstance().getReference();
                                                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + old_key).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
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

                                                                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).putBytes(byteArray).addOnCompleteListener(mainActivity, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                                    if (!task.isSuccessful()) {
                                                                                                        System.out.println("Error while copying photo");
                                                                                                    } else {
                                                                                                        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(mainActivity, new OnSuccessListener<Uri>() {
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

                                                                            Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                                            if (currentFragment instanceof CurrentPlaylistFragment) {

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

                                                if (snapshot.getChildrenCount() == 0) {
                                                    //Copy photo
                                                    storageReference = FirebaseStorage.getInstance().getReference();
                                                    storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + old_key).getDownloadUrl().addOnCompleteListener(mainActivity, new OnCompleteListener<Uri>() {
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

                                                                            storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).putBytes(byteArray).addOnCompleteListener(mainActivity, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                                                    if (!task.isSuccessful()) {
                                                                                        System.out.println("Error while copying photo");
                                                                                    } else {
                                                                                        storageReference.child("images/playlists/" + mAuth.getCurrentUser().getUid() + "/" + key).getDownloadUrl().addOnSuccessListener(mainActivity, new OnSuccessListener<Uri>() {
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

                                                            Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                            if (currentFragment instanceof CurrentPlaylistFragment) {

                                                                mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment(p.getTitle(), "", p, 0)).commit();
                                                            }
                                                        }
                                                    }).addOnFailureListener(mainActivity, new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                                                            if (currentFragment instanceof CurrentPlaylistFragment) {

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
                } else {
                    Toast.makeText(mainActivity, "Error while adding Playlist.", Toast.LENGTH_SHORT).show();

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

    public Playlist getPlaylist(){
        return playlist;
    }
}
