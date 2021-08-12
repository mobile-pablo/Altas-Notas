package com.company.altasnotas.fragments.favorites;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.fragments.mini_player.MiniPlayerFragment;
import com.company.altasnotas.models.FavoriteFirebaseSong;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.viewmodels.fragments.favorites.FavoritesFragmentViewModel;
import com.company.altasnotas.viewmodels.fragments.login_and_register.LoginFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class FavoritesFragment extends Fragment {
    public RecyclerView recyclerView;
    private ImageView imageView;
    private TextView title, description;
    private ImageView settings;
    public TextView fav_state;
    private FavoritesFragmentViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_playlist, container, false);




        imageView = view.findViewById(R.id.current_playlist_img);
        title = view.findViewById(R.id.current_playlist_title);
        description = view.findViewById(R.id.current_playlist_description);
        settings = view.findViewById(R.id.current_playlist_settings);
        settings.setVisibility(View.INVISIBLE);
        fav_state = view.findViewById(R.id.current_playlist_recycler_state);
        recyclerView = view.findViewById(R.id.current_playlist_recycler_view);

        viewModel  = new ViewModelProvider(requireActivity()).get(FavoritesFragmentViewModel.class);

        viewModel.init(recyclerView,imageView,title,description,settings,fav_state, (MainActivity) getActivity());

        viewModel.initializeFavorites();

        if(MiniPlayerFragment.playerView!=null){
            if(MiniPlayerFragment.playerView.getPlayer()!=null){
                MainActivity.mini_player.setVisibility(View.VISIBLE);
            }else{
                MainActivity.mini_player.setVisibility(View.GONE);
            }
        }

        return view;
    }


}