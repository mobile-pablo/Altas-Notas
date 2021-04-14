package com.company.altasnotas.viewmodels;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        if(checkData(context,email,password)){
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        Toast.makeText(context, "Login succeed!", Toast.LENGTH_SHORT).show();
                        mainActivity.updateUI(mAuth.getCurrentUser());
                       bottomNavigationView.setSelectedItemId(R.id.nav_home_item);
                       int  count= mainActivity.getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            mainActivity.getSupportFragmentManager().popBackStack();
                        }
                     mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container, new HomeFragment()).commit();
                    }else{
                        Toast.makeText(context, "Wrong email or password",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private boolean checkData(Context context,String email, String  password) {
        if((!email.isEmpty()) && (!password.isEmpty()) ){
            if(email.length()>10 && password.length()>4 ){

               Boolean isValidEmail = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();

                if(isValidEmail==false){
                    Toast.makeText(context, "Wrong email", Toast.LENGTH_SHORT).show();
                }
                println("Email is : $isValidEmail" );
                return isValidEmail;


            }
            else {
                Toast.makeText(context, "Required Length : \nEmail 10+\nPassword 4+", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else{
            Toast.makeText(context, "Fill all forms", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
