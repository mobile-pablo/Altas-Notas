package com.company.altasnotas.fragments.playlists;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.models.Playlist;

public class CurrentPlaylistFragment extends Fragment {

    private final Playlist playlist;
    private RecyclerView recyclerView;
    private ImageView imageView;
    private TextView title, description;
    private CurrentPlaylistAdapter adapter;
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        View view =  inflater.inflate(R.layout.fragment_current_playlist, container, false);

        imageView = view.findViewById(R.id.current_playlist_img);
        title = view.findViewById(R.id.current_playlist_title);
        description = view.findViewById(R.id.current_playlist_description);


        title.setText(playlist.getTitle());
        description.setText(playlist.getDescription());


       recyclerView =  view.findViewById(R.id.current_playlist_recycler_view);
       adapter = new CurrentPlaylistAdapter((MainActivity) getActivity(), playlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        return view;

    }

    public CurrentPlaylistFragment(Playlist playlist){
        this.playlist=playlist;
    }

}