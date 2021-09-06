package com.company.altasnotas.fragments.playlists;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.PlaylistsFragmentAdapter;
import com.company.altasnotas.databinding.FragmentPlaylistsBinding;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.viewmodels.fragments.playlists.PlaylistsFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaylistsFragment extends Fragment {

    public static FragmentPlaylistsBinding binding;

    private FirebaseDatabase database;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
    private ArrayList<Playlist> playlists;
    private PlaylistsFragmentAdapter adapter;
    private PlaylistsFragmentViewModel viewModel;
    private MainActivity mainActivity;
    private Dialog dialog;
    private String name,desc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentPlaylistsBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        mainActivity = (MainActivity) getActivity();
        mainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();

        initializeList();

        viewModel =  new ViewModelProvider(mainActivity).get(PlaylistsFragmentViewModel.class);
        binding.playlistsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              openDialog();
            }
        });

        initializeObservers();

        return view;
    }

    private void initializeObservers() {
        viewModel.getErrorState().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                String errorMsg=null;
                switch (integer){
                    case 0: break;
                    case 1:
                    case 2:
                    case 3: errorMsg="Error while adding Playlist"; break;
                    default: errorMsg="Unknown Error!"; break;
                }
            }
        });

        viewModel.getShouldChangeFragment().observe(getViewLifecycleOwner(), new Observer<Boolean>(){

            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
               initializeList();
               }
            }
        });
    }

    private void initializeList() {
        playlists = new ArrayList<>();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playlists.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Playlist playlist = new Playlist();
                    playlist.setTitle(dataSnapshot.child("title").getValue().toString());
                    playlist.setDescription(dataSnapshot.child("description").getValue().toString());
                    playlist.setImage_id(dataSnapshot.child("image_id").getValue().toString());
                    playlist.setYear(dataSnapshot.child("year").getValue().toString());
                    playlist.setAlbum(false);
                    playlists.add(playlist);
                }


                binding.playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false));
                adapter = new PlaylistsFragmentAdapter(mainActivity, playlists);
                adapter.notifyDataSetChanged();
                binding.playlistsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void openDialog() {


        dialog = new Dialog(mainActivity, R.style.Theme_AltasNotas);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.add_playlists_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        ImageButton cancel, accept;

        TextView  dialog_playlist_name = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogName);
        TextView  dialog_playlist_desc = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogDesc);

        cancel = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogCancelBtn);
        accept = dialog.getWindow().getDecorView().findViewById(R.id.addPlaylistDialogAcceptBtn);


        cancel.setOnClickListener(v -> dialog.dismiss());



        accept.setOnClickListener(v -> {
            name= dialog_playlist_name.getText().toString();
            desc=dialog_playlist_desc.getText().toString();
            validInput();
        });

        dialog.show();

    }

    public void validInput() {
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
                                viewModel.createPlaylist(finalName, finalDesc);
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
}