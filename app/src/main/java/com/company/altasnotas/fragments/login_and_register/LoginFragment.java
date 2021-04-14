package com.company.altasnotas.fragments.login_and_register;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.viewmodels.LoginFragmentViewModel;


import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.BuildConfig;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;




public class LoginFragment extends Fragment {

    CallbackManager callbackManager;
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

        // Initialize Facebook Login button
        FacebookSdk.sdkInitialize(getContext());

        callbackManager = CallbackManager.Factory.create();


        loginButton = view.findViewById(R.id.fb_new_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.setFragment(this);


        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                Log.d("Dziala ", "Token: "+ token.getUserId());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
              Log.d("Facebook","Login Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook","Login error: "+exception.toString());
            }
        });






        ImageButton fb_btn = view.findViewById(R.id.login_fb_btn);
        fb_btn.setOnClickListener(v -> loginButton.performClick());





        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user !=null){
            MainActivity mainActivity = (MainActivity) getActivity();
           mainActivity.updateUI(user);
        }
    }





    private void handleFacebookAccessToken(AccessToken token) {
        MainActivity mainActivity  = (MainActivity) getActivity();
        mAuth = FirebaseAuth.getInstance();

        String TAG="Facebook";
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mainActivity.updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(mainActivity.getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            mainActivity.updateUI(null);
                        }
                    }
                });

        mAuth.signInWithCredential(credential).addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    mainActivity.updateUI(user);
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment());
                    BottomNavigationView bottomNavigationView =  mainActivity.findViewById(R.id.main_nav_bottom);
                    bottomNavigationView.setSelectedItemId(R.id.nav_home_item);

                } else {

                    mainActivity.updateUI(null);
                }
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

    }
}