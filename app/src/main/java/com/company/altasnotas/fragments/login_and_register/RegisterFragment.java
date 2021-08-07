package com.company.altasnotas.fragments.login_and_register;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.viewmodels.fragments.login_and_register.RegisterFragmentViewModel;


public class RegisterFragment extends Fragment {

    RegisterFragmentViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        model = new ViewModelProvider(requireActivity()).get(RegisterFragmentViewModel.class);

        EditText et_mail = view.findViewById(R.id.register_email_edittext);
        EditText et_pass_one = view.findViewById(R.id.register_password_one_edittext);
        EditText et_pass_two = view.findViewById(R.id.register_password_two_edittext);
        view.findViewById(R.id.register_w_mail_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                model.register((MainActivity) getActivity(), et_mail.getText().toString().toLowerCase().trim(), et_pass_one.getText().toString().toLowerCase().trim(), et_pass_two.getText().toString().toLowerCase().trim());

            }
        });
        return view;
    }
}