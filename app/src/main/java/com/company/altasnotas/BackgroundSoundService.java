package com.company.altasnotas;

import android.app.Service;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;


public class BackgroundSoundService extends Service {
    String  song_string;
    int x;
    private static final String TAG = "BackgroundSoundService";
    MediaPlayer player;



    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind()" );

        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {


        song_string= intent.getStringExtra("uri");

        if (song_string != null) {
                System.out.println("Song string : " + song_string);
            player = MediaPlayer.create(this, Uri.parse(song_string));
            player.setLooping(false); // Set looping
            player.setVolume(0.5f, 0.5f);
            if (!(String.valueOf(x) == null || String.valueOf(x).isEmpty())) {
                player.seekTo(x);
            }
            player.start();
        }

        return Service.START_STICKY;
    }

    public IBinder onUnBind(Intent arg0) {
        Log.i(TAG, "onUnBind()");
        return null;
    }

    public void onStop() {
        Log.i(TAG, "onStop()");
    }
    public void onPause() {
        Log.i(TAG, "onPause()");
        x=player.getCurrentPosition();
        player.pause();
    }
    @Override
    public void onDestroy() {
        if(player!=null) {
            player.stop();
            player.reset();
            player.release();
             Log.i(TAG, "onCreate() , service stopped...");
        }
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory()");
    }


}