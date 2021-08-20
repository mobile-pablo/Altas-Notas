package com.company.altasnotas.fragments.favorites;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.databinding.FragmentCurrentPlaylistBinding;
import com.company.altasnotas.viewmodels.fragments.favorites.FavoritesFragmentViewModel;

public class FavoritesFragment extends Fragment {

    public FavoritesFragmentViewModel viewModel;
    public static FragmentCurrentPlaylistBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCurrentPlaylistBinding.inflate(inflater,container,false);
        View view = binding.getRoot();


        MainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);

        binding.currentPlaylistSettings.setVisibility(View.INVISIBLE);

        viewModel  = new ViewModelProvider(requireActivity()).get(FavoritesFragmentViewModel.class);

        viewModel.init(
                binding, (MainActivity) getActivity());

        viewModel.initializeFavorites();

        return view;
    }


}