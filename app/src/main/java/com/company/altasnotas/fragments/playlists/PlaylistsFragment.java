package com.company.altasnotas.fragments.playlists;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.adapters.PlaylistsFragmentAdapter;
import com.company.altasnotas.models.FirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;

public class PlaylistsFragment extends Fragment {


private RecyclerView recyclerView;
private FloatingActionButton fab;
    private FirebaseDatabase database;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
private Dialog dialog;
private EditText dialog_playlist_name,dialog_playlist_desc;
private ArrayList<Playlist> playlists;
private PlaylistsFragmentAdapter adapter;

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

       initalizeList();



       return view;
    }

    private void initalizeList() {
        playlists = new ArrayList<>();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Playlist playlist = new Playlist();
                    playlist.setTitle(dataSnapshot.child("title").getValue().toString());
                    playlist.setDescription(dataSnapshot.child("description").getValue().toString());
                    playlist.setImage_id(dataSnapshot.child("image_id").getValue().toString());
                    playlist.setYear(dataSnapshot.child("year").getValue().toString());
                    playlist.setAlbum((Boolean) dataSnapshot.child("isAlbum").getValue());

                    playlists.add(playlist);
                }


                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                adapter = new PlaylistsFragmentAdapter((MainActivity) getActivity(), playlists);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                                    Toast.makeText(requireContext(), "Playlist exist with same title!",Toast.LENGTH_SHORT).show();
                                }
                            }

                            if(x ==snapshot.getChildrenCount()){
                                dialog.dismiss();
                                createPlaylist(finalName, finalDesc);
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

    private void createPlaylist(String name ,String desc) {

          Playlist playlist = new Playlist();

        playlist.setTitle(name);
        playlist.setAlbum(false);
        playlist.setDescription(desc);
        playlist.setYear(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        playlist.setImage_id("");
        playlist.setSongs(null);
        String key = database_ref.push().getKey();
        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).setValue(playlist).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                  Toast.makeText(getContext(), "Error while adding Playlist.",Toast.LENGTH_SHORT).show();
                }else{
                    database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("isAlbum").setValue(playlist.isAlbum()).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).child(key).child("album").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            adapter.notifyDataSetChanged();
                                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new CurrentPlaylistFragment( name,"",playlist, 0)).commit();
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