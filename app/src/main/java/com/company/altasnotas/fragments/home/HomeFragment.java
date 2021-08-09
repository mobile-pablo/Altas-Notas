package com.company.altasnotas.fragments.home;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.HomeFragmentAdapter;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.Playlist;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    HomeFragmentAdapter adapter;
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private final String[] album_array = new String[1];
    private final String[] author_array = new String[1];
    private ArrayList<Playlist> playlists;
    private ArrayList<String> authors;
    private ArrayList<String> albums;
    MainActivity mainActivity;
    private ImageView profile_img, logout_img;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();
        albums = new ArrayList<>();
        authors = new ArrayList<>();
        playlists = new ArrayList<>();

        mainActivity= (MainActivity) getActivity();
        recyclerView = view.findViewById(R.id.home_recycler_view);
        logout_img = view.findViewById(R.id.home_logout_btn);
        profile_img=  view.findViewById(R.id.home_profile_btn);
        adapter = new HomeFragmentAdapter((MainActivity) getActivity(), authors, albums, playlists);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);


        initializePlaylist("Kult", "Spokojnie");
        initializePlaylist("Johnny Cash", "The Baron");
        initializePlaylist("Bad Bunny", "YHLQMDLG");
        initializePlaylist("Analogs", "Pełnoletnia Oi! Młodzież");
        initializePlaylist("Problem", "Problem");
        initializePlaylist("Problem", "Art Brut Dwa");

        logout_img.setOnClickListener(v -> {
            mainActivity.logoutUser();
        });

        Activity parentActivity=(Activity) view.getContext();
        downloadPhoto(profile_img,parentActivity);

        profile_img.setOnClickListener(v -> {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new ProfileFragment()).commit();
        });
        return view;
    }

    private void downloadPhoto(ImageView profile_img, Activity mainActivity) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storageReference.child("images/profiles/" + mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if(mainActivity!=null)
                            Glide.with(mainActivity).load(uri).error(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                        if ((Integer.parseInt(snapshot.child("login_method").getValue().toString())) != 1) {
                            String url = snapshot.child("photoUrl").getValue().toString();
                        if(url!=null) {
                            if (mainActivity != null)
                                Glide.with(mainActivity).load(url).error(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);

                            }else{
                            Glide.with(mainActivity).load(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);
                                 }
                            Log.d("Storage exception: " + exception.getLocalizedMessage() + "\nLoad from Page URL instead", "FirebaseStorage");

                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DatabaseError: " + error.getMessage(), "FirebaseDatabase");
            }
        });
    }

    private void initializePlaylist(String author, String album) {
        Playlist x = new Playlist();
        if (mAuth.getCurrentUser() != null) {

            database_ref.child("music").child("albums").child(author).child(album).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null) {

                        album_array[0] = snapshot.child("title").getValue().toString();
                        author_array[0] = snapshot.child("description").getValue().toString();


                        albums.add(album);
                        authors.add(author);

                        x.setImage_id(snapshot.child("image_id").getValue().toString());
                        x.setYear(snapshot.child("year").getValue().toString());
                        x.setTitle(album_array[0]);
                        x.setDescription(author_array[0]);
                        x.setDir_title(album);
                        x.setDir_desc(author);

                        playlists.add(x);
                        adapter.notifyDataSetChanged();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(MainActivity.FIREBASE, "Error: " + error.getMessage());
                }

            });


        }
    }

}