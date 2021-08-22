package com.company.altasnotas.viewmodels.fragments.login_and_register;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterFragmentViewModel extends ViewModel {

    private DatabaseReference database;
    private FirebaseAuth mAuth;
    private MutableLiveData<Integer> _errorState = new MutableLiveData<>(0);
    private String mail, passOne, passTwo;
    private MutableLiveData<Boolean> _isRegistered = new MutableLiveData<>();


    public LiveData<Boolean> getIsRegistered(){
        return  _isRegistered;
    }
    public LiveData<Integer> getErrorState(){
       return _errorState;
    }
    public void register() {
        if (checkData( mail, passOne, passTwo)) {
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance().getReference();

            mAuth.createUserWithEmailAndPassword(mail, passOne).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                            createUser();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    _errorState.setValue(5);
                }
            });
        }

    }

    private void createUser() {

        User user = new User("Username", mail,"",1);
        /**
         * Login methods :
         *  1 - Mail
         *  2 - Google
         *  3 - Facebook
         */
        database.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(task1 -> {
            _isRegistered.setValue(task1.isSuccessful());
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                _isRegistered.setValue(false);
            }
        });


    }

    public FirebaseAuth getMAuth(){
        return mAuth;
    }

    private boolean checkData(String email, String password, String passwordTwo) {
        if ((!email.isEmpty()) && (!password.isEmpty()) && (!passwordTwo.isEmpty())) {

            if (email.length() > 10 && password.length() > 4 && passwordTwo.length() > 4) {
                if (!password.equals(passwordTwo)) {
                    _errorState.setValue(1);

                    return false;
                } else {

                    Boolean isValidEmail = !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();

                    if (!isValidEmail) {
                        _errorState.setValue(2);
                    }

                    return isValidEmail;

                }
            } else {
                _errorState.setValue(3);
                return false;
            }
        } else {
            _errorState.setValue(4);
            return false;
        }


    }


    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPassOne(String passOne) {
        this.passOne = passOne;
    }

    public void setPassTwo(String passTwo) {
        this.passTwo = passTwo;
    }
}
