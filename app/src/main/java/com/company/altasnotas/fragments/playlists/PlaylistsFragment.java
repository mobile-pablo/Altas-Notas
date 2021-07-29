package com.company.altasnotas.fragments.playlists;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class PlaylistsFragment extends Fragment {


private RecyclerView recyclerView;
private FloatingActionButton fab;
    private FirebaseDatabase database;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
private Dialog dialog;
private EditText dialog_playlist_name,dialog_playlist_desc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view=  inflater.inflate(R.layout.fragment_playlists, container, false);

       recyclerView = view.findViewById(R.id.playlists_recycler_view);
       fab = view.findViewById(R.id.playlists_floating_btn);

       mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();

       fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               openDialog();
           }
       });
       return view;
    }

    private void openDialog() {


       dialog = new Dialog(getContext(), R.style.Theme_AltasNotas);
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

    private void validInput(String name ,String desc) {
        name = name.trim();
        desc = desc.trim();
        if(name.isEmpty() && desc.isEmpty()){
            Toast.makeText(getContext(), "Both fields are empty.\nPlease fill data.",Toast.LENGTH_SHORT).show();
        }else{
            if(name.isEmpty() || desc.isEmpty()){
                if(name.isEmpty()){
                    Toast.makeText(getContext(), "Name is empty.\nPlease fill data.",Toast.LENGTH_SHORT).show();
                }

                if(desc.isEmpty()){
                    Toast.makeText(getContext(), "Description is empty.\nPlease fill data.",Toast.LENGTH_SHORT).show();
                }
            }else{
                dialog.dismiss();
                createPlaylist(name,desc);
            }
        }
    }

    private void createPlaylist(String name ,String desc) {

        String defaultPhoto = "https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/images%2Fother%2Fimg_not_found.png?alt=media&token=7846c56f-6437-4064-9867-c11662c42d29";
        Playlist playlist = new Playlist();

        playlist.setTitle(name);
        playlist.setAlbum(false);
        playlist.setDescription(desc);
        playlist.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        playlist.setImage_id(defaultPhoto);
        playlist.setSongs(null);

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(name).setValue(playlist).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                  Toast.makeText(getContext(), "Error while adding Playlist.",Toast.LENGTH_SHORT).show();
                }else{
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment( name,"",playlist, 0)).commit();

                }
            }
        });
          }
}