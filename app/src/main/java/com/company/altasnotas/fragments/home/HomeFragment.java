package com.company.altasnotas.fragments.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.HomeFragmentAdapter;
import com.company.altasnotas.databinding.FragmentHomeBinding;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.Playlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private final String[] album_array = new String[1];
    private final String[] author_array = new String[1];

    private HomeFragmentAdapter adapter;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private ArrayList<Playlist> playlists;
    private ArrayList<String> authors;
    private ArrayList<String> albums;
    private MainActivity mainActivity;
    private final Boolean isOpenByLogin;

    public static FragmentHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initalizeFirebaseConnection();

        setUpArrays();

        mainActivity = (MainActivity) getActivity();

        initializeRecyclerView();
        initializePlaylists();

        binding.homeLogoutBtn.setOnClickListener(v -> {
            MainActivity.clearCurrentSong();
            mainActivity.logoutUser();
        });

        if (isOpenByLogin) {
            mainActivity.downloadPhoto();
        }

        MainActivity.viewModel.getPhotoUrl().observe(mainActivity, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (MainActivity.viewModel.getPhotoUrl() != null) {
                    Glide.with(mainActivity).load(MainActivity.viewModel.getPhotoUrl().getValue()).error(R.drawable.img_not_found).into(binding.homeProfileBtn);
                }
            }
        });


        binding.homeProfileBtn.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_up).replace(R.id.mainFragmentContainer, new ProfileFragment()).addToBackStack(null).commit();
        });

        if (mAuth.getCurrentUser() == null) {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_up, R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_up).replace(R.id.mainFragmentContainer, new LoginFragment()).commit();
        }
        return view;
    }

    private void setUpArrays() {
        albums = new ArrayList<>();
        authors = new ArrayList<>();
        playlists = new ArrayList<>();
    }

    private void initalizeFirebaseConnection() {
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
    }

    private void initializeRecyclerView() {
        adapter = new HomeFragmentAdapter((MainActivity) getActivity(), authors, albums, playlists);
        binding.homeRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.homeRecyclerView.setAdapter(adapter);
        mainActivity.activityMainBinding.mainActivityBox.setBackground(getResources().getDrawable(R.drawable.custom_home_fragment_bg));
    }

    public HomeFragment(Boolean isOpenByLogin) {
        this.isOpenByLogin = isOpenByLogin;
    }


    private void initializePlaylists() {

        if (mAuth.getCurrentUser() != null) {

            database_ref.child("music").child("albums").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot ds_author : snapshot.getChildren()) {
                        for (DataSnapshot ds_album : ds_author.getChildren()) {
                            if (ds_album != null) {
                                Playlist x = new Playlist();
                                String author = ds_author.getKey();
                                String album = ds_album.getKey();
                                album_array[0] = ds_album.child("title").getValue().toString();
                                author_array[0] = ds_album.child("description").getValue().toString();


                                albums.add(album);
                                authors.add(author);

                                x.setImage_id(ds_album.child("image_id").getValue().toString());
                                x.setYear(ds_album.child("year").getValue().toString());
                                x.setTitle(album_array[0]);
                                x.setDescription(author_array[0]);
                                x.setDir_title(album);
                                x.setDir_desc(author);

                                playlists.add(x);
                                adapter.notifyDataSetChanged();

                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(MainActivity.FIREBASE, "Error: " + error.getMessage());
                }

            });


        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mainActivity.activityMainBinding.mainActivityBox.setBackground(getResources().getDrawable(R.drawable.custom_home_fragment_bg));
    }
}