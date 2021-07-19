package com.company.altasnotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import bolts.Task;

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
            //Selected fragment is in array becouse I wanted to use him in onDataChange in Listener.
            final Fragment[] selectedFragment = {null};

            switch (item.getItemId()){
                case R.id.nav_home_item:
                  selectedFragment[0] = new HomeFragment();

          /* IMPORTANT CODE
                    Playlist x = new Playlist();


                    CountDownLatch conditionLatch = new CountDownLatch(1);
                    mAuth = FirebaseAuth.getInstance();
                    database = FirebaseDatabase.getInstance();
                    database_ref = database.getReference();
                    final String[] album_array = new String[1];
                    final String[] author_array = new String[1];
                    ArrayList<Song> songs = new ArrayList<>();

                        if (mAuth.getCurrentUser() != null) {

                            database_ref.child("music").child("albums").child("Kult").child("Spokojnie").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    album_array[0] = snapshot.child("songs").getRef().getParent().getKey();
                                    author_array[0] = snapshot.child("songs").getRef().getParent().getParent().getKey();
                                    int i=0;
                                     for (DataSnapshot ds: snapshot.child("songs").getChildren()){
                                         i++;

                                        Song local_song = new Song(0, author_array[0], album_array[0], ds.getKey().toString(),Uri.parse(ds.child("path").getValue().toString()));
                                        songs.add(local_song);


                                         if(i==snapshot.child("songs").getChildrenCount()){
                                             x.setSongs(songs);
                                             selectedFragment[0] = new CurrentPlaylistFragment(x);
                                             conditionLatch.countDown();
                                             getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment[0]).commit();

                                         }
                                     }


                                   x.setImage_id(snapshot.child("image_id").getValue().toString());
                                   x.setYear(snapshot.child("year").getValue().toString());
                                   x.setAlbum((Boolean) snapshot.child("isAlbum").getValue());
                                   x.setTitle(album_array[0]);
                                   x.setDescription(author_array[0]);
                                   x.setSong_amount(Integer.valueOf(snapshot.child("song_amount").getValue().toString()));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    conditionLatch.countDown();
                                }
                            });


                        }


           */

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



}