package com.company.altasnotas.fragments.login_and_register;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.viewmodels.LoginFragmentViewModel;


import java.util.Objects;
import java.util.concurrent.Executor;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;




public class LoginFragment extends Fragment {

    CallbackManager mCallbackManager;
    LoginFragmentViewModel model;
    LoginButton loginButton;
    FirebaseAuth mAuth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view = inflater.inflate(R.layout.fragment_login, container, false);
      mAuth = FirebaseAuth.getInstance();
      model = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(LoginFragmentViewModel.class);
      EditText email_editext = view.findViewById(R.id.login_email_edittext);
      EditText password_editext = view.findViewById(R.id.login_password_edittext);
      view.findViewById(R.id.login_w_mail_btn).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            model.login((MainActivity) Objects.requireNonNull(getActivity()),email_editext.getText().toString().toLowerCase().trim(),password_editext.getText().toString().toLowerCase().trim());
          }
      });
      view.findViewById(R.id.jump_to_register_btn).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.main_fragment_container, new RegisterFragment()).commit();
          }
      });


      //Po kolei sprawdzić wszystko jutro , najwyzej post na  stacku napisac
        // Initialize Facebook Login button
        FacebookSdk.sdkInitialize(getContext());
         mCallbackManager = CallbackManager.Factory.create();
        loginButton = view.findViewById(R.id.fb_new_login_button);
        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println(loginResult.toString());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                System.out.println("Canceled");
            }

            @Override
            public void onError(FacebookException error) {
                System.out.println("Error: "+error.toString());
            }
        });

        System.out.println("End of FB callback");

        return view;
    }



    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        MainActivity mainActivity  = (MainActivity) getActivity();
        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            mainActivity.updateUI(mainActivity, user);
                            Toast.makeText(mainActivity.getApplicationContext(), "Auth passed", Toast.LENGTH_SHORT).show();
                        } else {

                            Toast.makeText(mainActivity.getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mainActivity.updateUI(mainActivity, null);
                        }
                    }
                });
    }
}