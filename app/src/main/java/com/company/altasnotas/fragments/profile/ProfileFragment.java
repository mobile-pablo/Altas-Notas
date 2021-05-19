package com.company.altasnotas.fragments.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.company.altasnotas.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    private final int RESULT_LOAD_IMAGE=5;
    ShapeableImageView profile_img;
    private ImageButton age_btn, phone_btn, address_btn, profile_img_btn;
    private EditText   age_edit_t, phone_edit_t, address_edit_t;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
      View view = inflater.inflate(R.layout.fragment_profile, container, false);

      age_btn = view.findViewById(R.id.profile_age_btn);
      phone_btn = view.findViewById(R.id.profile_phone_btn);
      address_btn = view.findViewById(R.id.profile_address_btn);

      age_edit_t = view.findViewById(R.id.profile_age_number);
      phone_edit_t = view.findViewById(R.id.profile_phone_number);
      address_edit_t = view.findViewById(R.id.profile_address_number);

      age_btn.setOnClickListener(v -> {
          if(age_edit_t.isEnabled()==false) {
              age_edit_t.setEnabled(true);
          }else{
              age_edit_t.setEnabled(false);
          }
      });


      phone_btn.setOnClickListener(v->{
          if(phone_edit_t.isEnabled()==false) {
              phone_edit_t.setEnabled(true);
          }else{
              phone_edit_t.setEnabled(false);
          }
      });


      address_btn.setOnClickListener(v->{
          if(address_edit_t.isEnabled()==false) {
              address_edit_t.setEnabled(true);
          }else{
              address_edit_t.setEnabled(false);
          }
      });


      profile_img_btn = view.findViewById(R.id.profile_user_img_btn);
      profile_img = view.findViewById(R.id.profile_user_img);
      profile_img_btn.setOnClickListener(v->{
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
                Uri returnUri = data.getData();
                Bitmap bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), returnUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profile_img.setImageBitmap(bitmapImage);
            }
        }

    }




}