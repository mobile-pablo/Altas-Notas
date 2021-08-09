package com.company.altasnotas.viewmodels.fragments.playlists;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

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
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class PlaylistsFragmentViewModel extends ViewModel {
    private Dialog dialog;
    private Context context;
    private MainActivity mainActivity;
    private EditText dialog_playlist_name, dialog_playlist_desc;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;



    public void init(MainActivity mainActivity, DatabaseReference database_ref, FirebaseAuth mAuth){
        this.dialog=dialog;
        this.mainActivity=mainActivity;
        this.context = mainActivity.getApplicationContext();
        this.dialog_playlist_name=dialog_playlist_name;
        this.dialog_playlist_desc=dialog_playlist_desc;
        this.database_ref = database_ref;
        this.mAuth = mAuth;

    }
    public void openDialog(MainActivity mainActivity) {


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

        cancel = dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_cancel_btn);
        accept = dialog.getWindow().getDecorView().findViewById(R.id.add_playlist_dialog_accept_btn);


        cancel.setOnClickListener(v -> dialog.dismiss());


        accept.setOnClickListener(v -> validInput(dialog_playlist_name.getText().toString(), dialog_playlist_desc.getText().toString()));

        dialog.show();

    }

    public void validInput(String name, String desc) {
        name = name.trim();
        desc = desc.trim();


        if (name.isEmpty() && desc.isEmpty()) {
            Toast.makeText(context, "Both fields are empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
        } else {
            if (name.isEmpty() || desc.isEmpty()) {
                if (name.isEmpty()) {
                    Toast.makeText(context, "Name is empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
                }

                if (desc.isEmpty()) {
                    Toast.makeText(context, "Description is empty.\nPlease fill data.", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(context, "Playlist exist with same title!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            if (x == snapshot.getChildrenCount()) {
                                dialog.dismiss();
                                createPlaylist(finalName, finalDesc);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(MainActivity.FIREBASE,"Firebase DB error");
                    }
                });

            }
        }
    }

    public void createPlaylist(String name, String desc) {

        Playlist playlist = new Playlist();

        playlist.setTitle(name);
        playlist.setAlbum(false);
        playlist.setDescription(desc);
        playlist.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        playlist.setImage_id("");
        playlist.setSongs(null);
        String key = database_ref.push().getKey();
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).setValue(playlist).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(context, "Error while adding Playlist.", Toast.LENGTH_SHORT).show();
                } else {
                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("isAlbum").setValue(playlist.isAlbum()).addOnCompleteListener(mainActivity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("album").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mainActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_up,R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_up).replace(R.id.main_fragment_container, new CurrentPlaylistFragment(name, "", playlist, 0)).commit();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });
    }
}
