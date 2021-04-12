package com.company.altasnotas;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.company.altasnotas.fragments.favorites.FavoritesFragment;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.playlists.PlaylistsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_bottom);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
        bottomNavigationView.setSelectedItemId(R.id.nav_home_item);

    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener(){
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
                    break;


                case R.id.nav_profile_or_login_item:
                    //Sprawdza czy jest zalogowany czy tez nie  wtedy rozne zadania robi etc
                    //Narazie niech bedzie szło do login

                    selectedFragment = new LoginFragment();
                    break;
            }

            if(selectedFragment!=null)
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, selectedFragment).commit();
            return true;
        }
    };



}