package com.company.altasnotas.fragments.profile;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.viewmodels.ProfileFragmentViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {
    private DatabaseReference database_ref;
    private final FirebaseStorage storage = FirebaseStorage.getInstance("gs://altas-notas.appspot.com");
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private Uri returnUri=null;
    private ProfileFragmentViewModel model;
    private StorageReference storageReference;
    private ShapeableImageView profile_img;
    private TextView profile_name, profile_email;
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
          age_edit_t = view.findViewById(R.id.profile_age_number);
          phone_edit_t = view.findViewById(R.id.profile_phone_number);
          address_edit_t = view.findViewById(R.id.profile_address_number);

          profile_name = view.findViewById(R.id.profile_full_name);
          profile_email = view.findViewById(R.id.profile_email);
          profile_img = view.findViewById(R.id.profile_user_img);

          model =  new ViewModelProvider(requireActivity()).get(ProfileFragmentViewModel.class);
          model.downloadProfile((MainActivity) getActivity(), mAuth,  database_ref,  storage, profile_name, profile_email,  age_edit_t,phone_edit_t, address_edit_t, profile_img);

          age_edit_btn = view.findViewById(R.id.profile_age_edit_btn);
          phone_edit_btn = view.findViewById(R.id.profile_phone_edit_btn);
          address_edit_btn = view.findViewById(R.id.profile_address_edit_btn);

          age_cancel_btn = view.findViewById(R.id.profile_age_cancel_btn);
          phone_cancel_btn = view.findViewById(R.id.profile_phone_cancel_btn);
          address_cancel_btn = view.findViewById(R.id.profile_address_cancel_btn);


          age_accept_btn = view.findViewById(R.id.profile_age_accept_btn);
          phone_accept_btn = view.findViewById(R.id.profile_phone_accept_btn);
          address_accept_btn = view.findViewById(R.id.profile_address_accept_btn);


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
                model.updateProfile(mAuth,database_ref ,profile_name, age_edit_t, phone_edit_t, address_edit_t);
            });
          phone_accept_btn.setOnClickListener(v->{
                phone_edit_t.setEnabled(false);
                backup_phone= phone_edit_t.getText().toString();
                phone_cancel_btn.setVisibility(View.GONE);
                phone_accept_btn.setVisibility(View.GONE);
                phone_edit_btn.setVisibility(View.VISIBLE);
                model.updateProfile(mAuth,database_ref ,profile_name, age_edit_t, phone_edit_t, address_edit_t);
            });
          address_accept_btn.setOnClickListener(v->{
                address_edit_t.setEnabled(false);
                backup_address=address_edit_t.getText().toString();
                address_cancel_btn.setVisibility(View.GONE);
                address_accept_btn.setVisibility(View.GONE);
                address_edit_btn.setVisibility(View.VISIBLE);
                model.updateProfile(mAuth,database_ref ,profile_name, age_edit_t, phone_edit_t, address_edit_t);
            });

          profile_img_edit_btn = view.findViewById(R.id.profile_user_img_btn);
          profile_name_edit_btn = view.findViewById(R.id.profile_name_edit_btn);

          profile_img_edit_btn.setOnClickListener(v->{
              if(ActivityCompat.checkSelfPermission(getActivity(),
                      Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
              {
                  requestPermissions(
                          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                          2000);
              }
              else {
                  CropImage.activity()
                          .start(getContext(), this);
              }
          });
          profile_name_edit_btn.setOnClickListener(v->{
              final EditText taskEditText = new EditText(v.getContext());
              AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                      .setTitle("Change username")
                      .setView(taskEditText)
                      .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              profile_name.setText(String.valueOf(taskEditText.getText()));
                              model.updateProfile(mAuth,database_ref ,profile_name, age_edit_t, phone_edit_t, address_edit_t);
                          }
                      })
                      .setNegativeButton("Cancel", null)
                      .create();


              dialog.setView(taskEditText,50,0,50,0);
                dialog.show();
              taskEditText.setText(profile_name.getText());
          });


      return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode ==RESULT_CANCELED){
           Log.d("RESULT","RESULT HAVE BEEN CANCELED");
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
               returnUri = result.getUriContent();

                try {

                    Bitmap compresedImg =  ProfileFragmentViewModel.getBitmapFormUri(getActivity(), returnUri);
                    Bitmap compressImgRotated = rotateImageIfRequired(getContext(), compresedImg,returnUri);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    compressImgRotated = getResizedBitmap(compressImgRotated,300);
                    compressImgRotated.compress(Bitmap.CompressFormat.PNG, 100, bao);

                    Glide.with(requireActivity()).load(compressImgRotated).apply(RequestOptions.centerCropTransform()).error(R.drawable.img_not_found).into(profile_img);

                    byte[] byteArray = bao.toByteArray();

                    compresedImg.recycle();

                    //Upload image

                    storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child("images/profiles/"+mAuth.getCurrentUser().getUid()).putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(!task.isSuccessful()){
                                Log.d(MainActivity.FIREBASE, "Upload image failed");
                            }
                        }
                    });



                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(MainActivity.FIREBASE,"Error while compressing and uploading photo to Firebase");
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

            InputStream input = context.getContentResolver().openInputStream(selectedImage);
            ExifInterface ei;
            if (Build.VERSION.SDK_INT > 23)
                ei = new ExifInterface(input);
            else
                ei = new ExifInterface(selectedImage.getPath());

            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(img, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(img, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(img, 270);
                default:
                    return img;
            }
        }
    private static Bitmap rotateImage(Bitmap img, int degree) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }



}