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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.PlaylistsFragmentAdapter;
import com.company.altasnotas.databinding.FragmentPlaylistsBinding;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.viewmodels.fragments.favorites.FavoritesFragmentViewModel;
import com.company.altasnotas.viewmodels.fragments.playlists.PlaylistsFragmentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class PlaylistsFragment extends Fragment {



    private FirebaseDatabase database;
    private DatabaseReference database_ref;
    private FirebaseAuth mAuth;
    private ArrayList<Playlist> playlists;
    private PlaylistsFragmentAdapter adapter;
    private PlaylistsFragmentViewModel viewModel;
    private MainActivity mainActivity;
    public static FragmentPlaylistsBinding binding;
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

        initalizeList();

        viewModel =  new ViewModelProvider(requireActivity()).get(PlaylistsFragmentViewModel.class);
        viewModel.init((MainActivity) getActivity(), database_ref,mAuth);
        binding.playlistsFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.openDialog((MainActivity) getActivity());
            }
        });

        return view;
    }

    private void initalizeList() {
        playlists = new ArrayList<>();

        database_ref.child("music").child("playlists").child(mAuth.getCurrentUser().getUid()).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Playlist playlist = new Playlist();
                    playlist.setTitle(dataSnapshot.child("title").getValue().toString());
                    playlist.setDescription(dataSnapshot.child("description").getValue().toString());
                    playlist.setImage_id(dataSnapshot.child("image_id").getValue().toString());
                    playlist.setYear(dataSnapshot.child("year").getValue().toString());
                    playlist.setAlbum((Boolean) dataSnapshot.child("isAlbum").getValue());

                    playlists.add(playlist);
                }


                binding.playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                adapter = new PlaylistsFragmentAdapter((MainActivity) getActivity(), playlists);
                adapter.notifyDataSetChanged();
                binding.playlistsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}