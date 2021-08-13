package com.company.altasnotas;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.adapters.CurrentPlaylistAdapter;
import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.mini_player.MiniPlayerFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.services.BackgroundService;
import com.company.altasnotas.viewmodels.fragments.login_and_register.LoginFragmentViewModel;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    public MutableLiveData<String> photoUrl;
    public BottomNavigationView bottomNavigationView;
    public static String currentSongTitle="", currentSongAlbum ="",currentSongAuthor="";
    public static Integer dialogHeight;
    public static final String FIREBASE = "Firebase";
    public  static View  mini_player;

    private String frag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);



        DisplayMetrics displayMetrics = new DisplayMetrics();
       getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        dialogHeight= (int) (height* 0.4);

        bottomNavigationView = findViewById(R.id.main_nav_bottom);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        photoUrl = new MutableLiveData<>("");
        mAuth = FirebaseAuth.getInstance();

        updateUI(mAuth.getCurrentUser());
        downloadPhoto();



        if (savedInstanceState == null) {
            if (mAuth.getCurrentUser() != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment(false)).commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LoginFragment()).commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_login_item);
            }
        }



        mini_player = findViewById(R.id.main_mini_player_container);
        mini_player.setVisibility(View.GONE);


    }


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //Selected fragment is in array becouse I wanted to use him in onDataChange in Listener.
            final Fragment[] selectedFragment = {null};

            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
            switch (item.getItemId()) {
                case R.id.nav_home_item:
                    selectedFragment[0] = new HomeFragment(false);
                    break;

                case R.id.nav_fav_item:
                    selectedFragment[0] = new FavoritesFragment();
                    break;

                case R.id.nav_playlist_item:
                    selectedFragment[0] = new PlaylistsFragment();
                    break;


                case R.id.nav_login_item:
                        selectedFragment[0] = new LoginFragment();
                    break;
            }

            if (selectedFragment[0] != null)
                //.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment[0]).commit();
            return true;
        }
    };

    public void logoutUser() {
        //Logout

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_mini_player_container);
        if (currentFragment instanceof MiniPlayerFragment) {
            MiniPlayerFragment miniPlayerFragment= (MiniPlayerFragment) currentFragment;
            miniPlayerFragment.dissmiss_mini();
        }


        if(CurrentPlaylistFragment.adapter!=null){
            CurrentPlaylistFragment.adapter.notifyDataSetChanged();
        }

        photoUrl.setValue("");


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn == true) {
            LoginManager.getInstance().logOut();
        }

        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            updateUI(null);
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        Fragment anotherFragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
        if (anotherFragment instanceof LoginFragment) {
            LoginFragment loginFragment = (LoginFragment) anotherFragment;

            if(loginFragment.mGoogleApiClient!=null){
                if (loginFragment.mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(loginFragment.mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()){
                                Log.d("Google", "Signed out from Google");
                            }else{
                                Log.d("Google", "Error while sigining out from Google");
                            }
                        }
                    });

                    loginFragment.mGoogleApiClient.disconnect();
                }

                loginFragment.mGoogleApiClient.stopAutoManage(MainActivity.this);

            }
        }



        if (isLoggedIn == true || mAuth.getCurrentUser() == null) {

            for (int i = 0; i <   getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_left,R.anim.fade_out, R.anim.fade_in, R.anim.slide_out_left).replace(R.id.main_fragment_container, new LoginFragment()).commit();
        }
    }


    public void updateUI(FirebaseUser user) {

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_bottom);
        Menu menu = bottomNavigationView.getMenu();

        if (user != null) {
            menu.findItem(R.id.nav_fav_item).setVisible(true);
            menu.findItem(R.id.nav_home_item).setVisible(true);
            menu.findItem(R.id.nav_playlist_item).setVisible(true);
            menu.findItem(R.id.nav_login_item).setVisible(false);
        } else {
            menu.findItem(R.id.nav_fav_item).setVisible(false);
            menu.findItem(R.id.nav_home_item).setVisible(false);
            menu.findItem(R.id.nav_playlist_item).setVisible(false);
            menu.findItem(R.id.nav_login_item).setVisible(true);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        frag = intent.getStringExtra("frag");
        if (frag != null) {
            if (frag.equals("PlayerFragment")) {

                for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                    getSupportFragmentManager().popBackStack();
                }

                Playlist playlist = intent.getParcelableExtra("playlist");
                Integer position = intent.getIntExtra("pos", 0);
                long seekedTo = intent.getLongExtra("ms", 0);
                ArrayList<Song> local_songs = intent.getParcelableArrayListExtra("songs");
                playlist.setSongs(local_songs);

                Fragment fragment = getSupportFragmentManager().findFragmentByTag("Player");
                if(fragment != null)
                {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                PlayerFragment playerFragment = new PlayerFragment(playlist, position, seekedTo,true);
                getSupportFragmentManager().beginTransaction().addToBackStack("null").replace(R.id.main_fragment_container, playerFragment, "Player").commit();
            }
        }else{
            System.out.println("Frag is null");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (  LoginFragment.mGoogleApiClient != null &&   LoginFragment.mGoogleApiClient.isConnected()) {
            LoginFragment.mGoogleApiClient.stopAutoManage(this);
            LoginFragment.mGoogleApiClient.disconnect();
        }
    }

    public void downloadPhoto() {

        DatabaseReference database_ref = FirebaseDatabase.getInstance().getReference();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        //  Image download
        if(mAuth.getCurrentUser()!=null) {
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    storageReference.child("images/profiles/" + mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            photoUrl.setValue(uri.toString());
                               // Glide.with(mainActivity).load(uri).error(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            if ((Integer.parseInt(snapshot.child("login_method").getValue().toString())) != 1) {
                                String url = snapshot.child("photoUrl").getValue().toString();
                                if (url != null) {

                                    photoUrl.setValue(url);
                                     //   Glide.with(mainActivity).load(url).error(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);

                                } else {
                                    photoUrl.setValue("");
                                   // Glide.with(mainActivity).load(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);
                                }
                                Log.d(FIREBASE,"Storage exception: " + exception.getLocalizedMessage() + "\nLoad from Page URL instead");

                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(FIREBASE,"DatabaseError: " + error.getMessage());
                }
            });
        }
    }
}