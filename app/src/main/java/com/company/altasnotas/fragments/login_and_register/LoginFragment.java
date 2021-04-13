package com.company.altasnotas.fragments.login_and_register;

import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.viewmodels.LoginFragmentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import pl.droidsonroids.gif.GifImageButton;


public class LoginFragment extends Fragment {


    LoginFragmentViewModel model;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view = inflater.inflate(R.layout.fragment_login, container, false);

      model = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(LoginFragmentViewModel.class);
       EditText email_editext = view.findViewById(R.id.login_email_edittext);
        EditText password_editext = view.findViewById(R.id.login_password_edittext);
      view.findViewById(R.id.login_w_mail_btn).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            model.login((MainActivity)getActivity(),email_editext.getText().toString().toLowerCase().trim(),password_editext.getText().toString().toLowerCase().trim());
          }
      });

      view.findViewById(R.id.jump_to_register_btn).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.main_fragment_container, new RegisterFragment()).commit();
          }
      });
        return view;
    }
}