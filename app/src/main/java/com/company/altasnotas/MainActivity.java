package com.company.altasnotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.company.altasnotas.fragments.profile.ProfileFragment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    DatabaseReference database_ref;
    FirebaseDatabase database;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Facebook
        FacebookSdk.sdkInitialize(MainActivity.this);


        BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_bottom);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        mAuth = FirebaseAuth.getInstance();

        updateUI(MainActivity.this,mAuth.getCurrentUser());

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
                    selectedFragment = new HomeFragment();
                    break;

                case R.id.nav_fav_item:
                    selectedFragment = new FavoritesFragment();
                    break;

                case R.id.nav_playlist_item:
                    selectedFragment = new PlaylistsFragment();
                    break;

                case R.id.nav_logout_item:
                    //Logout

                    if(mAuth.getCurrentUser()!=null) {
                        mAuth.signOut();
                        updateUI(MainActivity.this,null);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new LoginFragment()).commit();
                    }
                    break;


                case R.id.nav_profile_or_login_item:
                    //Sprawdza czy jest zalogowany czy tez nie  wtedy rozne zadania robi etc
                    //Narazie niech bedzie szło do login
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


    public void updateUI(MainActivity activity, FirebaseUser user) {

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
}