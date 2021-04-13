package com.company.altasnotas.viewmodels;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragmentViewModel extends ViewModel {

    DatabaseReference database;
    FirebaseAuth mAuth;

    public void  register(MainActivity activity, String mail, String pass_one, String pass_two) {
        //Check it later if it works
        if (checkData(activity.getApplicationContext(), mail, pass_one, pass_two)) {
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance().getReference();

            mAuth.createUserWithEmailAndPassword(mail, pass_one).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        User user = new User("", "", mail, 0, true, " ", 0, 0);

                        database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {

                                int count = activity.getSupportFragmentManager().getBackStackEntryCount();

                                for (int i = 0; i < count; i++) {
                                    activity.getSupportFragmentManager().popBackStack();
                                }
                                activity.updateUI(activity,mAuth.getCurrentUser());
                              BottomNavigationView bottomNavigationView =  activity.findViewById(R.id.main_nav_bottom);
                              bottomNavigationView.setSelectedItemId(R.id.nav_home_item);

                                activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,new HomeFragment()).commit();
                                //Walkthrough will be here
                            }else {
                                System.out.println("Error while adding a user to firebase db");
                            }
                        });


                    }
                }

            });
        }

    }



    private boolean checkData(Context context,String email, String password, String passwordTwo) {
        if((!email.isEmpty()) && (!password.isEmpty()) && (!passwordTwo.isEmpty())){

            if(email.length()>10 && password.length()>4 && passwordTwo.length() > 4){
                if(!password.equals(passwordTwo)){
                    Toast.makeText(context, "Wrong passwords", Toast.LENGTH_SHORT).show();
                    return false;
                }else{

                    Boolean isValidEmail = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();

                    if(!isValidEmail){
                        Toast.makeText(context, "Wrong email", Toast.LENGTH_SHORT).show();
                    }

                    return isValidEmail;

                }
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
