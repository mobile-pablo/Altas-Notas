package com.company.altasnotas.viewmodels;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragmentViewModel extends ViewModel {
    /**
     *
     * Two functions below are copied from : https://programming.vip/docs/android-uri-to-bitmap-image-and-compress.html
     * It helps me compress the photo to 5 times worse quality than the original (5MB -> 1MB)
     */
    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //Image resolution is based on 480x800
        float hh = 800f;//The height is set as 800f here
        float ww = 480f;//Set the width here to 480f
        //Zoom ratio. Because it is a fixed scale, only one data of height or width is used for calculation
        int be = 1;//be=1 means no scaling
        if (originalWidth > originalHeight && originalWidth > ww) {//If the width is large, scale according to the fixed size of the width
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//If the height is high, scale according to the fixed size of the width
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //Proportional compression
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//Set scaling
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//Mass compression again
    }
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//Quality compression method, here 100 means no compression, store the compressed data in the BIOS
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //Cycle to determine if the compressed image is greater than 100kb, greater than continue compression
            baos.reset();//Reset the BIOS to clear it
            //First parameter: picture format, second parameter: picture quality, 100 is the highest, 0 is the worst, third parameter: save the compressed data stream
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//Here, the compression options are used to store the compressed data in the BIOS
            if(options>=30){
            options -= 10;//10 less each time
                }else{
                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//Store the compressed data in ByteArrayInputStream
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//Generate image from ByteArrayInputStream data
                return bitmap;
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//Store the compressed data in ByteArrayInputStream
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//Generate image from ByteArrayInputStream data
        return bitmap;
    }


    public void downloadProfile(MainActivity mainActivity, FirebaseAuth mAuth, DatabaseReference database_ref, FirebaseStorage storage, TextView profile_name, TextView profile_email, EditText age_edit_t, EditText phone_edit_t, EditText address_edit_t, ShapeableImageView profile_img) {

        User localUser = new User("Username","","","","","",0,0,0);
        if(mAuth.getCurrentUser()!=null){
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        localUser.name = snapshot.child("name").getValue().toString();
                        localUser.mail = mAuth.getCurrentUser().getEmail();
                        localUser.age = snapshot.child("age").getValue().toString();
                        localUser.phone = snapshot.child("phone").getValue().toString();
                        localUser.photoUrl = snapshot.child("photoUrl").getValue().toString();
                        localUser.address = snapshot.child("address").getValue().toString();

                        profile_email.setText(localUser.mail);
                        profile_name.setText( localUser.name);
                        age_edit_t.setText(localUser.age);
                        phone_edit_t.setText(localUser.phone);
                        address_edit_t.setText(localUser.address);

                        //  Image download
                      StorageReference storageReference = storage.getReference();

                        database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                storageReference.child("images/profiles/" + mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        if(mainActivity!=null)

                                            Glide.with(mainActivity.getApplicationContext()).load(uri).into(profile_img);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        if((Integer.parseInt(snapshot.child("login_method").getValue().toString()))!=1) {
                                            String url = snapshot.child("photoUrl").getValue().toString();

                                            if(mainActivity!=null)
                                                Glide.with(mainActivity.getApplicationContext()).load(url).into(profile_img);

                                            Log.d("Storage exception: " + exception.getLocalizedMessage() + "\nLoad from Page URL instead", "FirebaseStorage");

                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("DatabaseError: "+error.getMessage(),"FirebaseDatabase");
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("FirebaseDatabase error: "+error.getMessage(), "FirebaseDatabase");
                }
            });

        }
    }

    public void updateProfile(FirebaseAuth mAuth, DatabaseReference database_ref, TextView profile_name, EditText age_edit_t, EditText phone_edit_t, EditText address_edit_t) {



        User localUser = new User("Username","","","","","",0,0,0);
        if(mAuth.getCurrentUser()!=null){
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        localUser.name = snapshot.child("name").getValue().toString();
                        localUser.mail = mAuth.getCurrentUser().getEmail();
                        localUser.age = snapshot.child("age").getValue().toString();
                        localUser.phone = snapshot.child("phone").getValue().toString();
                        localUser.address = snapshot.child("address").getValue().toString();
                        localUser.photoUrl = snapshot.child("photoUrl").getValue().toString();
                        localUser.login_method = Integer.parseInt(snapshot.child("login_method").getValue().toString());
                        localUser.playlist_amount = Integer.parseInt(snapshot.child("playlist_amount").getValue().toString());
                        localUser.fav_song_amount = Integer.parseInt(snapshot.child("fav_song_amount").getValue().toString());

                        //After we download data from db, We update its according to Inputed Data


                        localUser.name = profile_name.getText().toString();
                        localUser.age = age_edit_t.getText().toString();
                        localUser.phone = phone_edit_t.getText().toString();
                        localUser.address = address_edit_t.getText().toString();

                        database_ref.child("users").child(mAuth.getCurrentUser().getUid()).setValue(localUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}
