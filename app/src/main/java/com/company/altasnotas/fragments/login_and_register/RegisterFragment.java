package com.company.altasnotas.fragments.login_and_register;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.databinding.FragmentRegisterBinding;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.viewmodels.fragments.login_and_register.RegisterFragmentViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class RegisterFragment extends Fragment {

    private RegisterFragmentViewModel viewModel;
    private  FragmentRegisterBinding binding;
    private MainActivity mainActivity;
    private String email, passwordOne, passwordTwo;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container,false);
        View view = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        viewModel = new ViewModelProvider(mainActivity).get(RegisterFragmentViewModel.class);

        mainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);

        binding.registerWMailBtn.setOnClickListener(v -> {
            register();
        });

        initializeObservers();
        return view;
    }

    private void register() {
        email= binding.registerEmailEdittext.getText().toString().toLowerCase().trim();
        passwordOne = binding.registerPasswordOneEdittext.getText().toString().toLowerCase().trim();
        passwordTwo  = binding.registerPasswordTwoEdittext.getText().toString().toLowerCase().trim();
        viewModel.setMail(email);
        viewModel.setPassOne(passwordOne);
        viewModel.setPassTwo(passwordTwo);
        viewModel.register();
    }

    private void initializeObservers() {

        viewModel.getIsRegistered().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
            if(aBoolean){

                int count = mainActivity.getSupportFragmentManager().getBackStackEntryCount();

                for (int i = 0; i < count; i++) {
                    mainActivity.getSupportFragmentManager().popBackStack();
                }
                mainActivity.updateUI(viewModel.getMAuth().getCurrentUser());
                BottomNavigationView bottomNavigationView = mainActivity.findViewById(R.id.main_nav_bottom);
                bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
                Toast.makeText(mainActivity, "Register success", Toast.LENGTH_SHORT).show();

                mainActivity.getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left,R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.main_fragment_container, new HomeFragment(true)).commit();

            }
            }
        });

        viewModel.getErrorState().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                String errorMsg;
                switch (integer){
                    case 0: errorMsg=null; break;
                    case 1: errorMsg="Wrong passwords"; break;
                    case 2:  errorMsg="Wrong email"; break;
                    case 3: errorMsg="Required Length : \nEmail 10+\nPassword 4+"; break;
                    case 4: errorMsg="Fill all forms"; break;
                    case 5: errorMsg="Account on this email exists!"; break;
                    default: errorMsg="Unknown error!"; break;
                }

              if(errorMsg!=null){
                  Toast.makeText(mainActivity, errorMsg,Toast.LENGTH_SHORT).show();
              }
            }
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding=null;
    }
}