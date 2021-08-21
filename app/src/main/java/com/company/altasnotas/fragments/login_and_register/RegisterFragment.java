package com.company.altasnotas.fragments.login_and_register;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.databinding.FragmentRegisterBinding;
import com.company.altasnotas.viewmodels.fragments.login_and_register.RegisterFragmentViewModel;


public class RegisterFragment extends Fragment {

    RegisterFragmentViewModel model;
    private static FragmentRegisterBinding binding;
    private MainActivity mainActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container,false);
        View view = binding.getRoot();
        mainActivity = (MainActivity) getActivity();
        model = new ViewModelProvider(requireActivity()).get(RegisterFragmentViewModel.class);
        mainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);

        binding.registerWMailBtn.setOnClickListener(v -> {
            model.register((MainActivity) getActivity(), binding.registerEmailEdittext.getText().toString().toLowerCase().trim(), binding.registerPasswordOneEdittext.getText().toString().toLowerCase().trim(), binding.registerPasswordTwoEdittext.getText().toString().toLowerCase().trim());

        });
        return view;
    }
}