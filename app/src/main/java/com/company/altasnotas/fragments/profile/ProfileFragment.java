package com.company.altasnotas.fragments.profile;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.canhub.cropper.CropImage;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.databinding.FragmentProfileBinding;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.viewmodels.fragments.profile.ProfileFragmentViewModel;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    public static FragmentProfileBinding binding;

    private DatabaseReference database_ref;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private Uri returnUri = null;
    private ProfileFragmentViewModel viewModel;
    private StorageReference storageReference;
    private ProgressDialog progress;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        mainActivity = (MainActivity) getActivity();
        mainActivity.activityMainBinding.mainActivityBox.setBackgroundColor(Color.WHITE);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        database_ref = database.getReference();


        viewModel = new ViewModelProvider(requireActivity()).get(ProfileFragmentViewModel.class);
        viewModel.downloadProfile();

        binding.profileUserAddImgBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        2000);
            } else {
                CropImage.activity()
                        .start(getContext(), this);
            }
        });
        binding.profileNameEditBtn.setOnClickListener(v -> {
            final EditText taskEditText = new EditText(v.getContext());
            AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                    .setTitle("Change username")
                    .setView(taskEditText)
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            binding.profileFullName.setText(String.valueOf(taskEditText.getText()));
                            viewModel.setProfileName(binding.profileFullName.getText().toString());
                            viewModel.updateProfile();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();


            dialog.setView(taskEditText, 50, 0, 50, 0);
            dialog.show();
            taskEditText.setText(binding.profileFullName.getText());
        });

        binding.profileDeleteBox.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Profile?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            viewModel.deleteProfile();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();
        });

        initializeObservers();
        return view;
    }

    private void initializeObservers() {
        viewModel.getShouldDeleteUser().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer){
                    case 0:
                        showProgress("Deleting Account");
                        break;

                    case 1:
                        if(progress!=null)
                        {
                        progress.dismiss();
                        }
                        deleteUser();
                }
            }
        });

        MainActivity.viewModel.getPhotoUrl().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                          Glide.with(mainActivity).load(s).error(R.drawable.img_not_found).into(binding.profileUserImg);
            }
        });

        viewModel.getCreationDate().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.profileDetailsCreationDate.setVisibility(View.VISIBLE);
                binding.profileDetailsCreationDate.setText(s);
                binding.profileDetailsCreationText.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getProfileName().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.profileFullName.setText(s);
            }
        });

        viewModel.getProfileEmail().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.profileEmail.setText(s);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_CANCELED) {
            Log.d("RESULT", "RESULT HAVE BEEN CANCELED");
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                returnUri = result.getUriContent();
            showProgress("Loading Photo");
                try {

                    Bitmap compresedImg = ProfileFragmentViewModel.getBitmapFormUri(getActivity(), returnUri);
                    Bitmap compressImgRotated = rotateImageIfRequired(getContext(), compresedImg, returnUri);
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    compressImgRotated = getResizedBitmap(compressImgRotated, 300);
                    compressImgRotated.compress(Bitmap.CompressFormat.PNG, 100, bao);
                    byte[] byteArray = bao.toByteArray();

                    compresedImg.recycle();

                    //Upload image

                    //         storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://altas-notas.appspot.com");
                    storageReference = FirebaseStorage.getInstance().getReference();
                    storageReference.child("images/profiles/" + mAuth.getCurrentUser().getUid()).putBytes(byteArray).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.d(MainActivity.FIREBASE, "Upload image failed");
                                progress.dismiss();
                            } else {
                                if (getActivity() != null) {
                                    storageReference.child("images/profiles/" + mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnCompleteListener(getActivity(), new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                MainActivity mainActivity = (MainActivity) getActivity();
                                                if (task.getResult() != null && mainActivity != null) {
                                                    progress.dismiss();
                                                    Glide.with(requireActivity()).load(task.getResult()).apply(RequestOptions.centerCropTransform()).error(R.drawable.img_not_found).into(binding.profileUserImg);
                                                    MainActivity.viewModel.setPhotoUrl(task.getResult().toString());
                                                }
                                            } else if (task.getResult() == null) {
                                                progress.dismiss();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(MainActivity.FIREBASE, "Error while compressing and uploading photo to Firebase");
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

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

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void deleteUser() {
        database_ref.child("users").child(viewModel.uid).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
                                if (isLoggedIn == true) {
                                    LoginManager.getInstance().logOut();
                                }


                                if (task.isSuccessful()) {
                                    for (int i = 0; i <    mainActivity.getSupportFragmentManager().getBackStackEntryCount(); i++) {
                                        mainActivity.getSupportFragmentManager().popBackStack();
                                    }

                                    mAuth.signOut();
                                    if(LoginFragment.mGoogleApiClient!=null){
                                        if (LoginFragment.mGoogleApiClient.isConnected()) {
                                            LoginFragment.mGoogleApiClient.clearDefaultAccountAndReconnect();
                                            Auth.GoogleSignInApi.signOut(LoginFragment.mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(@NonNull Status status) {
                                                    if(status.isSuccess()){
                                                        Log.d("Google", "Signed out from Google");
                                                    }else{
                                                        Log.d("Google", "Error while sigining out from Google");
                                                    }
                                                }
                                            });

                                            LoginFragment.mGoogleSignInClient.signOut();
                                            LoginFragment.mGoogleApiClient.disconnect();
                                        }

                                        LoginFragment.mGoogleApiClient.stopAutoManage(mainActivity);

                                    }



                                    mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer,new LoginFragment()).commit();
                                    mainActivity.updateUI(mAuth.getCurrentUser());

                                }
                                else{
                                  mAuth = FirebaseAuth.getInstance();
                                    if(mAuth.getCurrentUser()!=null){
                                        mAuth.getCurrentUser().delete();

                                        for (int i = 0; i <    mainActivity.getSupportFragmentManager().getBackStackEntryCount(); i++) {
                                            mainActivity.getSupportFragmentManager().popBackStack();
                                        }

                                        Fragment frag = mainActivity.getSupportFragmentManager().findFragmentById(R.id.slidingLayoutFrag);
                                        if (frag instanceof PlayerFragment) {
                                            ((PlayerFragment) frag).dismissPlayer();
                                        }


                                        mAuth.signOut();
                                        if(LoginFragment.mGoogleApiClient!=null){
                                            if (LoginFragment.mGoogleApiClient.isConnected()) {
                                                Auth.GoogleSignInApi.signOut(LoginFragment.mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                                    @Override
                                                    public void onResult(@NonNull Status status) {

                                                    }
                                                });
                                            }
                                            LoginFragment.mGoogleApiClient.stopAutoManage(mainActivity);
                                            LoginFragment.mGoogleApiClient.disconnect();
                                        }


                                        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentContainer,new LoginFragment()).commit();
                                        mainActivity.updateUI(mAuth.getCurrentUser());
                                    }
                                }
                            }
                        });

                    }
                });
    }

    private void showProgress(String title){
        progress = new ProgressDialog(mainActivity);
        progress.setTitle(title);
        progress.setMessage("Please wait...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }


}