package com.company.altasnotas.fragments.login_and_register;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.viewmodels.fragments.login_and_register.LoginFragmentViewModel;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import static android.content.ContentValues.TAG;


public class LoginFragment extends Fragment {

    private static final int RC_SIGN_IN = 120;
    public static GoogleSignInClient mGoogleSignInClient;
    public static GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private LoginFragmentViewModel model;
    private LoginButton facebookLoginButton;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        model = new ViewModelProvider(requireActivity()).get(LoginFragmentViewModel.class);
        EditText email_editext = view.findViewById(R.id.login_email_edittext);
        EditText password_editext = view.findViewById(R.id.login_password_edittext);
        MainActivity.main_activty_box.setBackgroundColor(Color.WHITE);
        if(mGoogleApiClient!=null){
            if (mGoogleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()){
                            Log.d("Google", "Signed out from Google");
                        }else{
                            Log.d("Google", "Error while sigining out from Google");
                        }
                    }
                });

                mGoogleApiClient.stopAutoManage(getActivity());
               mGoogleApiClient.disconnect();
            }

        }
        view.findViewById(R.id.login_w_mail_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.login((MainActivity) requireActivity(), email_editext.getText().toString().toLowerCase().trim(), password_editext.getText().toString().toLowerCase().trim());
            }
        });
        view.findViewById(R.id.jump_to_register_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left,R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).addToBackStack(null).replace(R.id.main_fragment_container, new RegisterFragment()).commit();
            }
        });

        // Initialize Facebook Login button
        FacebookSdk.sdkInitialize(getContext());

        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton = view.findViewById(R.id.fb_new_login_button);
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.setFragment(this);


        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken token = loginResult.getAccessToken();
                Log.d("Facebook", "Token: " + token.getUserId());
                model.handleFacebookAccessToken((MainActivity) getActivity(), loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "Login Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook", "Login error: " + exception.toString());
            }
        });


        ImageButton fb_btn = view.findViewById(R.id.login_fb_btn);
        fb_btn.setOnClickListener(v -> facebookLoginButton.performClick());


        //Google Auth
        ImageButton google_btn = view.findViewById(R.id.login_google_btn);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mGoogleApiClient==null){
                    mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                            .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                                }
                            })
                            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                            .build();
                }else{
                    if(mGoogleApiClient.isConnected()){
                        mGoogleApiClient.clearDefaultAccountAndReconnect();
                    }
                    mGoogleApiClient.stopAutoManage(getActivity());
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                            .enableAutoManage(getActivity(), new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                                }
                            })
                            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                            .build();
                }

                signIn();
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.updateUI(user);

    }

    private void signIn() {
        mGoogleSignInClient.signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    model.firebaseAuthWithGoogle((MainActivity) getActivity(), account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                }
            } else {
                Log.w(TAG, task.getException().toString());
            }

        }
    }


}