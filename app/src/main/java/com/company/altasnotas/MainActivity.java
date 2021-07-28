package com.company.altasnotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.company.altasnotas.services.BackgroundService;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    public  String photoUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_bottom);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        mAuth = FirebaseAuth.getInstance();

        updateUI(mAuth.getCurrentUser());

        String frag = getIntent().getStringExtra("frag");


        if(savedInstanceState==null) {
            if (mAuth.getCurrentUser() != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LoginFragment()).commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_profile_or_login_item);
                bottomNavigationView.getMenu().findItem(R.id.nav_profile_or_login_item).setTitle("Login");
            }
        }


        if(frag!=null) {
            if (frag.equals("PlayerFragment")) {
                System.out.println(frag);



                Playlist playlist = getIntent().getParcelableExtra("playlist");
               Integer position = getIntent().getIntExtra("pos", 0);
               long seekedTo = getIntent().getLongExtra("ms", 0);
                ArrayList<Song> local_songs = getIntent().getParcelableArrayListExtra("songs");
                playlist.setSongs(local_songs);
                PlayerFragment playerFragment = new PlayerFragment(playlist, position, seekedTo);
                System.out.println(local_songs.size());
                getSupportFragmentManager().beginTransaction().addToBackStack("null").replace(R.id.main_fragment_container, playerFragment).commit();
            }
        }
    }


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //Selected fragment is in array becouse I wanted to use him in onDataChange in Listener.
            final Fragment[] selectedFragment = {null};

            switch (item.getItemId()){
                case R.id.nav_home_item:
                     selectedFragment[0] = new HomeFragment();
                     break;

                case R.id.nav_fav_item:
                    selectedFragment[0] = new FavoritesFragment();
                    break;

                case R.id.nav_playlist_item:
                    selectedFragment[0] = new PlaylistsFragment();
                    break;

                case R.id.nav_logout_item:
                    //Logout
                    photoUrl=null;
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                    if(isLoggedIn==true){

                        LoginManager.getInstance().logOut();
                    }

                    if(mAuth.getCurrentUser()!=null) {
                        mAuth.signOut();
                        updateUI(null);
                        FacebookSdk.sdkInitialize(getApplicationContext());
                    }

                    if(isLoggedIn==true || mAuth.getCurrentUser()==null){

                        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LoginFragment()).commit();
                    }
                    break;


                case R.id.nav_profile_or_login_item:
                if(mAuth.getCurrentUser()==null){
                    selectedFragment[0] = new LoginFragment();}
                else{
                    selectedFragment[0] = new ProfileFragment();
                }
                    break;
            }

            if(selectedFragment[0] !=null)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment[0]).commit();
            return true;
        }
    };


    public void updateUI(FirebaseUser user) {

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_bottom);
        Menu menu = bottomNavigationView.getMenu();

        if(user!=null){

            menu.findItem(R.id.nav_logout_item).setVisible(true);
            menu.findItem(R.id.nav_fav_item).setVisible(true);
            menu.findItem(R.id.nav_home_item).setVisible(true);
            menu.findItem(R.id.nav_playlist_item).setVisible(true);
            menu.findItem(R.id.nav_profile_or_login_item).setTitle("Profile");
        }else{
            menu.findItem(R.id.nav_logout_item).setVisible(false);
            menu.findItem(R.id.nav_fav_item).setVisible(false);
            menu.findItem(R.id.nav_home_item).setVisible(false);
            menu.findItem(R.id.nav_playlist_item).setVisible(false);
            menu.findItem(R.id.nav_profile_or_login_item).setTitle("Login");

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
    protected void onDestroy() {
     Intent intent = new Intent(this, BackgroundService.class);
        stopService(intent);
        super.onDestroy();
    }
}