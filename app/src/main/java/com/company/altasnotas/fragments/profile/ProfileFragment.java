package com.company.altasnotas.fragments.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.company.altasnotas.R;
import com.company.altasnotas.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {
    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private Uri returnUri;
    private StorageReference storageReference;
    private ShapeableImageView profile_img;
    private ImageButton age_edit_btn, phone_edit_btn, address_edit_btn, profile_img_edit_btn, profile_name_edit_btn;
    private ImageButton age_cancel_btn, phone_cancel_btn, address_cancel_btn;
    private ImageButton age_accept_btn, phone_accept_btn, address_accept_btn;
    private String backup_age, backup_phone, backup_address;
    private EditText   age_edit_t, phone_edit_t, address_edit_t;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view = inflater.inflate(R.layout.fragment_profile, container, false);

      mAuth = FirebaseAuth.getInstance();
      database = FirebaseDatabase.getInstance();
      database_ref = database.getReference();

      age_edit_btn = view.findViewById(R.id.profile_age_edit_btn);
      phone_edit_btn = view.findViewById(R.id.profile_phone_edit_btn);
      address_edit_btn = view.findViewById(R.id.profile_address_edit_btn);

      age_cancel_btn = view.findViewById(R.id.profile_age_cancel_btn);
      phone_cancel_btn = view.findViewById(R.id.profile_phone_cancel_btn);
      address_cancel_btn = view.findViewById(R.id.profile_address_cancel_btn);


      age_accept_btn = view.findViewById(R.id.profile_age_accept_btn);
      phone_accept_btn = view.findViewById(R.id.profile_phone_accept_btn);
      address_accept_btn = view.findViewById(R.id.profile_address_accept_btn);

      age_edit_t = view.findViewById(R.id.profile_age_number);
      phone_edit_t = view.findViewById(R.id.profile_phone_number);
      address_edit_t = view.findViewById(R.id.profile_address_number);

      age_edit_btn.setOnClickListener(v -> {
          backup_age = age_edit_t.getText().toString();
          age_edit_t.setEnabled(true);
          age_edit_btn.setVisibility(View.GONE);
          age_accept_btn.setVisibility(View.VISIBLE);
          age_cancel_btn.setVisibility(View.VISIBLE);
      });
      phone_edit_btn.setOnClickListener(v->{
          backup_phone= phone_edit_t.getText().toString();
          phone_edit_t.setEnabled(true);
          phone_edit_btn.setVisibility(View.GONE);
          phone_accept_btn.setVisibility(View.VISIBLE);
          phone_cancel_btn.setVisibility(View.VISIBLE);
      });
      address_edit_btn.setOnClickListener(v->{
          backup_address= address_edit_t.getText().toString();
          address_edit_t.setEnabled(true);
          address_edit_btn.setVisibility(View.GONE);
          address_accept_btn.setVisibility(View.VISIBLE);
          address_cancel_btn.setVisibility(View.VISIBLE);
      });


      //Cancel

        age_cancel_btn.setOnClickListener(v->{
            age_edit_t.setEnabled(false);
            age_edit_t.setText(backup_age);
            age_cancel_btn.setVisibility(View.GONE);
            age_accept_btn.setVisibility(View.GONE);
            age_edit_btn.setVisibility(View.VISIBLE);
        });
        phone_cancel_btn.setOnClickListener(v->{
            phone_edit_t.setEnabled(false);
            phone_edit_t.setText(backup_phone);
            phone_cancel_btn.setVisibility(View.GONE);
            phone_accept_btn.setVisibility(View.GONE);
            phone_edit_btn.setVisibility(View.VISIBLE);
        });
        address_cancel_btn.setOnClickListener(v->{
            address_edit_t.setEnabled(false);
            address_edit_t.setText(backup_address);
            address_cancel_btn.setVisibility(View.GONE);
            address_accept_btn.setVisibility(View.GONE);
            address_edit_btn.setVisibility(View.VISIBLE);
        });


      //Accept


        age_accept_btn.setOnClickListener(v->{
            age_edit_t.setEnabled(false);
            backup_age= age_edit_t.getText().toString();
            age_cancel_btn.setVisibility(View.GONE);
            age_accept_btn.setVisibility(View.GONE);
            age_edit_btn.setVisibility(View.VISIBLE);
        });
        phone_accept_btn.setOnClickListener(v->{
            phone_edit_t.setEnabled(false);
            backup_phone= phone_edit_t.getText().toString();
            phone_cancel_btn.setVisibility(View.GONE);
            phone_accept_btn.setVisibility(View.GONE);
            phone_edit_btn.setVisibility(View.VISIBLE);
        });
        address_accept_btn.setOnClickListener(v->{
            address_edit_t.setEnabled(false);
            backup_address=address_edit_t.getText().toString();
            address_cancel_btn.setVisibility(View.GONE);
            address_accept_btn.setVisibility(View.GONE);
            address_edit_btn.setVisibility(View.VISIBLE);
        });


      profile_img_edit_btn = view.findViewById(R.id.profile_user_img_btn);
      profile_name_edit_btn = view.findViewById(R.id.profile_name_edit_btn);
      profile_img = view.findViewById(R.id.profile_user_img);
      profile_img_edit_btn.setOnClickListener(v->{
          if(ActivityCompat.checkSelfPermission(getActivity(),
                  Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
          {
              requestPermissions(
                      new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                      2000);
          }
          else {
              startGallery();
          }
      });


      return view;
    }

    private void startGallery() {
        Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        cameraIntent.setType("image/*");
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super method removed
        if (resultCode == RESULT_OK) {

            if (requestCode == 1000) {
                 returnUri = data.getData();
                Bitmap bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profile_img.setImageBitmap(bitmapImage);

                //Upload image

                storageReference = FirebaseStorage.getInstance().getReference();
                storageReference.child("images/"+mAuth.getCurrentUser().getUid()).putFile(returnUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            System.out.println("Upload image is successful!");

                        }else{
                            System.out.println("Upload image failed!");

                        }
                    }
                });
                //Upload rest of information
                updateProfile();
            }
        }

    }

    private void updateProfile() {



       User localUser = new User("","",0,"",0,0,0);
        if(mAuth.getCurrentUser()!=null){
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        localUser.name = snapshot.child("name").getValue().toString();
                        localUser.mail = mAuth.getCurrentUser().getEmail();
                        localUser.age = Integer.parseInt(Objects.requireNonNull(snapshot.child("age").getValue()).toString());
                        localUser.address = snapshot.child("address").getValue().toString();
                        localUser.login_method = Integer.parseInt(snapshot.child("login_method").getValue().toString());
                        localUser.playlist_amount = Integer.parseInt(snapshot.child("playlist_amount").getValue().toString());
                        localUser.fav_song_amount = Integer.parseInt(snapshot.child("fav_song_amount").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }


}