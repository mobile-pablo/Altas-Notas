package com.company.altasnotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.fragments.playlists.CurrentPlaylistFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
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
    DatabaseReference database_ref;
    FirebaseDatabase database;
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



    }


    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment =null;

            switch (item.getItemId()){
                case R.id.nav_home_item:
                 //   selectedFragment = new HomeFragment();

                 //   Temporarly we are changing selectedFragment to PlayerFragment
                    Playlist x = new Playlist("Spokojnie","Kult", 0,"0","");
                    Song s1 = new Song(0, "Kult", x.getTitle(),"Arahja","https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/songs%2Fkult%2Fspokojnie%2FArahja.mp3?alt=media&token=64a83a64-cfc6-4b0b-a230-1905c67ca9db");
                    Song s2 = new Song(0, "Kult",  x.getTitle(),"Axe","https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/songs%2Fkult%2Fspokojnie%2FAxe.mp3?alt=media&token=01aa3a6a-9d3d-4316-a379-94a3ec921f66");
                    Song s3 = new Song(0, "Kult",  x.getTitle(), "Czarne słońca", "https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/songs%2Fkult%2Fspokojnie%2FCzarne%20s%C5%82o%C5%84ca.mp3?alt=media&token=46d4131c-9d0b-4151-82c7-da3b421fab4e");
                    Song s4 = new Song(0,"Kult",  x.getTitle(), "Do Ani", "https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/songs%2Fkult%2Fspokojnie%2FCzarne%20s%C5%82o%C5%84ca.mp3?alt=media&token=46d4131c-9d0b-4151-82c7-da3b421fab4e");

                    ArrayList<Song> songs = new ArrayList<>();
                    songs.add(s1);
                    songs.add(s2);
                    songs.add(s3);
                    songs.add(s4);
                    x.setSongs(songs);

                    selectedFragment = new CurrentPlaylistFragment(x);
                    break;

                case R.id.nav_fav_item:
                    selectedFragment = new FavoritesFragment();
                    break;

                case R.id.nav_playlist_item:
                    selectedFragment = new PlaylistsFragment();
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
                    selectedFragment = new LoginFragment();}
                else{
                    selectedFragment = new ProfileFragment();
                }
                    break;
            }

            if(selectedFragment!=null)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment).commit();
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

}