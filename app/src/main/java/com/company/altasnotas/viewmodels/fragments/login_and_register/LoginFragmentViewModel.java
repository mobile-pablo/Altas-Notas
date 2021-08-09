package com.company.altasnotas.viewmodels.fragments.login_and_register;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.models.User;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.ContentValues.TAG;
import static java.sql.DriverManager.println;

public class LoginFragmentViewModel extends ViewModel {
    DatabaseReference database_ref;
    FirebaseDatabase database;
    FirebaseAuth mAuth;

    public void login(MainActivity mainActivity, String email, String password) {

        Context context = mainActivity.getApplicationContext();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();

        BottomNavigationView bottomNavigationView = mainActivity.findViewById(R.id.main_nav_bottom);

        if (checkData(context, email, password)) {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        Toast.makeText(context, "Login succeed!", Toast.LENGTH_SHORT).show();
                        mainActivity.updateUI(mAuth.getCurrentUser());
                        bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
                        int count = mainActivity.getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            mainActivity.getSupportFragmentManager().popBackStack();
                        }

                        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment(true)).commit();
                    } else {
                        Toast.makeText(context, "Wrong email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private boolean checkData(Context context, String email, String password) {
        if ((!email.isEmpty()) && (!password.isEmpty())) {
            if (email.length() > 10 && password.length() > 4) {

                Boolean isValidEmail = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if (isValidEmail == false) {
                    Toast.makeText(context, "Wrong email", Toast.LENGTH_SHORT).show();
                }
                println("Email is : $isValidEmail");
                return isValidEmail;


            } else {
                Toast.makeText(context, "Required Length : \nEmail 10+\nPassword 4+", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(context, "Fill all forms", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    public void firebaseAuthWithGoogle(MainActivity mainActivity, String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        mAuth.signInWithCredential(credential).addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    addUser(mainActivity, 2, task.getResult().getAdditionalUserInfo().isNewUser(),task.getResult().getUser().getPhotoUrl().toString());
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    mainActivity.updateUI(user);
                    BottomNavigationView bottomNavigationView = mainActivity.findViewById(R.id.main_nav_bottom);
                    //We shouldnt could go back so if i were transfering to another activity
                    // I should add finish(); at the end of code after starting another activity
                    bottomNavigationView.setSelectedItemId(R.id.nav_home_item);

                    int count = mainActivity.getSupportFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < count; i++) {
                        mainActivity.getSupportFragmentManager().popBackStack();
                    }
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment(true)).commit();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(mainActivity.getApplicationContext(), "Google Login failed.\nTry another mail", Toast.LENGTH_SHORT).show();
                    mainActivity.updateUI(null);
                }
            }
        });
    }

    private void addUser(MainActivity mainActivity, int i, boolean newUser, String photo) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        User user;
        if (newUser) {
            mainActivity.photoUrl.setValue(photo);
            user = new User(mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getEmail(),  mainActivity.photoUrl.getValue(), i);
            database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
        } else {
            database.child("users").child(mAuth.getCurrentUser().getUid()).child("login_method").setValue(i);
        }
    }

    public void handleFacebookAccessToken(MainActivity mainActivity, AccessToken token) {
        mAuth = FirebaseAuth.getInstance();

        String TAG = "Facebook";
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential).addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    addUser(mainActivity, 3, task.getResult().getAdditionalUserInfo().isNewUser(),mAuth.getCurrentUser().getPhotoUrl() + "/picture?height=1000&access_token=" + token.getToken());

                    FirebaseUser user = mAuth.getCurrentUser();
                    mainActivity.updateUI(user);
                    int count = mainActivity.getSupportFragmentManager().getBackStackEntryCount();
                    for (int i = 0; i < count; i++) {
                        mainActivity.getSupportFragmentManager().popBackStack();
                    }


                    BottomNavigationView bottomNavigationView = mainActivity.findViewById(R.id.main_nav_bottom);
                    bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment(true)).commit();
                } else {
                    AccessToken accessToken = AccessToken.getCurrentAccessToken();
                    boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                    if (isLoggedIn == true) {

                        LoginManager.getInstance().logOut();
                    }
                    Toast.makeText(mainActivity.getApplicationContext(), "Facebook login failed.\nTry another mail", Toast.LENGTH_SHORT).show();
                    mainActivity.updateUI(null);
                }
            }
        });

    }
}
