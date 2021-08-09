package com.company.altasnotas.viewmodels.fragments.profile;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.home.HomeFragment;
import com.company.altasnotas.fragments.login_and_register.LoginFragment;
import com.company.altasnotas.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragmentViewModel extends ViewModel {
    /**
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
            if (options >= 30) {
                options -= 10;//10 less each time
            } else {
                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//Store the compressed data in ByteArrayInputStream
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//Generate image from ByteArrayInputStream data
                return bitmap;
            }
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//Store the compressed data in ByteArrayInputStream
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//Generate image from ByteArrayInputStream data
        return bitmap;
    }

    public void deleteProfile(MainActivity activity ,FirebaseAuth mAuth, DatabaseReference database_ref){
        String uid = mAuth.getCurrentUser().getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        ArrayList<String> keys = new ArrayList<>();
        final Integer[] x = {0};
        /*
        We need to delete
        - Playlists data   + img
        - Profile data +img
        - Fav music
         */

        storageReference.child("images").child("profiles").child(mAuth.getCurrentUser().getUid()).delete().addOnFailureListener(activity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(MainActivity.FIREBASE, "Image not found");
            }
        });


        database_ref.child("music").child("playlists").child(uid).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){
                    keys.add(ds.getKey());
                }

                if(keys.size() == snapshot.getChildrenCount()){
                    for(String key: keys){
                        storageReference.child("images/playlists/"+uid+"/"+ key).delete().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                x[0]++;
                                System.out.println("Playlist img deleted, X: "+x[0]+", "+keys.size());

                                if(x[0]==keys.size()){
                                    database_ref.child("music").child("playlists").child(uid).removeValue();
                                }
                            }
                        });



                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database_ref.child("fav_music").child(uid).removeValue();

        database_ref.child("users").child(uid).removeValue().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mAuth.getCurrentUser().delete().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        for (int i = 0; i <    activity.getSupportFragmentManager().getBackStackEntryCount(); i++) {
                            mAuth.signOut();
                            activity.getSupportFragmentManager().popBackStack();
                        }
                        activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_container,new LoginFragment()).commit();

                    }
                });

              }
        });
    }

    public void downloadProfile(MainActivity mainActivity, FirebaseAuth mAuth, DatabaseReference database_ref, FirebaseStorage storage, TextView profile_name, TextView profile_email, CircleImageView profile_img, TextView creationText, TextView creationDate) {

        User localUser = new User("Username", "", "", "", "", "", 0, 0, 0);
        if (mAuth.getCurrentUser() != null) {
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        localUser.name = snapshot.child("name").getValue().toString();
                        localUser.mail = mAuth.getCurrentUser().getEmail();
                        localUser.age = snapshot.child("age").getValue().toString();
                        localUser.phone = snapshot.child("phone").getValue().toString();
                        localUser.photoUrl = snapshot.child("photoUrl").getValue().toString();
                        localUser.address = snapshot.child("address").getValue().toString();

                        profile_email.setText(localUser.mail);
                        profile_name.setText(localUser.name);

                        creationText.setVisibility(View.VISIBLE);
                        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy ");
                        Long creationDateLong =mAuth.getCurrentUser().getMetadata().getCreationTimestamp();
                        Date date = new Date( creationDateLong);
                        creationDate.setVisibility(View.VISIBLE);
                        creationDate.setText(formatter.format(date));

                        //  Image download
                        StorageReference storageReference = storage.getReference();
                        database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                storageReference.child("images/profiles/" + mAuth.getCurrentUser().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        System.out.println("IMG FOUND");
                                        if(mainActivity!=null)
                                            Glide.with(mainActivity).load(uri).error(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {

                                        if ((Integer.parseInt(snapshot.child("login_method").getValue().toString())) != 1) {
                                            String url = snapshot.child("photoUrl").getValue().toString();
                                            if(url!=null) {
                                                if (mainActivity != null)
                                                    Glide.with(mainActivity).load(url).error(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);

                                            }else{
                                                Glide.with(mainActivity).load(R.drawable.img_not_found).apply(RequestOptions.circleCropTransform()).into(profile_img);
                                            }
                                            Log.d("Storage exception: " + exception.getLocalizedMessage() + "\nLoad from Page URL instead", "FirebaseStorage");

                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("DatabaseError: " + error.getMessage(), "FirebaseDatabase");
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("FirebaseDatabase error: " + error.getMessage(), "FirebaseDatabase");
                }
            });

        }
    }

    public void updateProfile(FirebaseAuth mAuth, DatabaseReference database_ref, TextView profile_name) {


        User localUser = new User("Username", "", "", "", "", "", 0, 0, 0);
        if (mAuth.getCurrentUser() != null) {
            database_ref.child("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
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
