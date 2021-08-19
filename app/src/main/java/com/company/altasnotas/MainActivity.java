package com.company.altasnotas;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    public MutableLiveData<String> photoUrl;
    public BottomNavigationView bottomNavigationView;
    public static MutableLiveData<String> currentSongTitle = new MutableLiveData<>();
    public static MutableLiveData<String> currentSongAlbum = new MutableLiveData<>();
    public static MutableLiveData<String> currentSongAuthor = new MutableLiveData<>();
    public static Integer dialogHeight;
    public static final String FIREBASE = "Firebase";
    public  static FrameLayout slideup_box;
    public static LinearLayout main_activty_box;
    public static SlidingUpPanelLayout slidingUpPanelLayout;

    private String frag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentSongTitle.setValue("");
        currentSongAuthor.setValue("");
        currentSongAlbum.setValue("");
        slideup_box = findViewById(R.id.sliding_layout_frag);
        slidingUpPanelLayout = findViewById(R.id.sliding_layout);
        main_activty_box = findViewById(R.id.main_activity_box);
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
                bottomNavigationView.setVisibility(View.VISIBLE);
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LoginFragment()).commit();
                bottomNavigationView.setVisibility(View.GONE);
            }
        }

        reInitializePlayerViews();
        frag = getIntent().getStringExtra("frag");
        if (frag != null) {
            if (frag.equals("PlayerFragment")) {

                for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                    getSupportFragmentManager().popBackStack();
                }
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.main_fragment_container, new HomeFragment(true), "Player").commit();
            }
        }else{
            Log.d("MainActivity", "Frag is null");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                        SYSTEM_UI_FLAG_LAYOUT_STABLE
        | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }
    //This function is called when I get back to App where I exit by onBackPressed
    private void reInitializePlayerViews() {
       if(PlayerFragment.mBound){
           if(PlayerFragment.mService!=null) {

               if (PlayerFragment.playerView.getPlayer() != null) {

                   Playlist playlist = PlayerFragment.mService.playlist;
                   Integer position = PlayerFragment.mService.position;
                   Integer isFav = PlayerFragment.mService.isFav;
                   Boolean ready = PlayerFragment.mService.getPlayerInstance().getPlayWhenReady();
                   Integer state = PlayerFragment.mService.getPlayerInstance().getPlaybackState();
                   Long seekedTo = PlayerFragment.mService.getPlayerInstance().getContentPosition();

                   System.out.println(ready+","+ state);
                   currentSongTitle.setValue(playlist.getSongs().get(position).getTitle());
                   currentSongAlbum.setValue(playlist.getTitle());
                   currentSongAuthor.setValue(playlist.getDescription());

                   PlayerFragment playerFragment = new PlayerFragment(playlist, position, seekedTo, true, state, null, isFav);
                   if (PlayerFragment.fav_btn.getDrawable().getConstantState().equals(PlayerFragment.fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState())) {
                       PlayerFragment.fav_btn.getDrawable().setTint(Color.BLACK);
                   }
               }
           }
        }
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
            }

            if (selectedFragment[0] != null){
                //.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment[0]).commit();
            }
            return true;
        }
    };

    public void logoutUser() {
        //Logout


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

        if(LoginFragment.mGoogleApiClient!=null){

            if (LoginFragment.mGoogleApiClient.isConnected()) {
                LoginFragment.mGoogleApiClient.clearDefaultAccountAndReconnect();
                Auth.GoogleSignInApi.signOut(LoginFragment.mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()){
                            Log.d("Google", "Signed out from Google");
                        }else{
                            Log.d("Google", "Error while sigining out from Google");
                        }
                    }
                });

                LoginFragment.mGoogleSignInClient.signOut();
                LoginFragment.mGoogleApiClient.disconnect();
            }


            LoginFragment.mGoogleApiClient.stopAutoManage(MainActivity.this);

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
            bottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.GONE);
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
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.main_fragment_container, new HomeFragment(true), "Player").commit();
            }
        }else{
            System.out.println("Frag is null");
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
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
        mAuth= FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null) {
        if(!mAuth.getCurrentUser().getUid().isEmpty()){
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

    @Override
    public void onBackPressed() {


        if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }else{
            super.onBackPressed();
        }
    }
}