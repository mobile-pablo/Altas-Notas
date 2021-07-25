package com.company.altasnotas.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media2.exoplayer.external.source.ExtractorMediaSource;

import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.fragments.player.PlayerFragment;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;


public class BackgroundService extends Service implements ExoPlayer.EventListener {

    private SimpleExoPlayer player;
    private Playlist playlist;
    private Context context;
    private PlayerNotificationManager playerNotificationManager;
    private String streamUrl = "https://firebasestorage.googleapis.com/v0/b/altas-notas.appspot.com/o/songs%2Fbad%20bunny%2Fyhlqmdlg%2FHABLAMOS%20MA%C3%91ANA.mp3?alt=media&token=07a9d499-22b5-4586-9b57-7219b2812335";
    private final IBinder mBinder = new LocalBinder();
    private String CHANNEL_ID="6", NOTIFICATION_ID="8";
    private Integer position;

    @Override
    public void onCreate() {
        super.onCreate();
        context =this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playlist =  intent.getParcelableExtra("playlist");
       position = intent.getIntExtra("pos",0);
       ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
       playlist.setSongs(songs);
        if (player == null) {
            startPlayer();
      /*      playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this,CHANNEL_ID,R.string.exo_download_notification_channel_name,Integer.parseInt(NOTIFICATION_ID),   new PlayerNotificationManager.MediaDescriptionAdapter() {
                @Override
                public CharSequence getCurrentContentTitle(Player player) {
                    //  return player.getCurrentMediaItem().mediaMetadata.title;
                    return playlist.getSongs().get(position).getTitle();
                }

                @Nullable
                @Override
                public PendingIntent createCurrentContentIntent(Player player) {
                    Intent intent = new Intent(context, MainActivity.class);
                    return PendingIntent.getActivity(context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                }

                @Nullable
                @Override
                public CharSequence getCurrentContentText(Player player) {
                    return playlist.getSongs().get(position).getAuthor();
                }

                @Nullable
                @Override
                public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(playlist.getSongs().get(position).getImage_url()));
                        return bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            } );

       */

          playerNotificationManager = new PlayerNotificationManager.Builder(context,
                  Integer.parseInt(NOTIFICATION_ID),
                  CHANNEL_ID,
                  new PlayerNotificationManager.MediaDescriptionAdapter() {
                @Override
                public CharSequence getCurrentContentTitle(Player player) {
                    return playlist.getSongs().get(position).getTitle();
                }

                @Nullable
                @Override
                public PendingIntent createCurrentContentIntent(Player player) {
                    Intent intent = new Intent(context, MainActivity.class);
                    return PendingIntent.getActivity(context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                }

                @Nullable
                @Override
                public CharSequence getCurrentContentText(Player player) {
                    return playlist.getSongs().get(position).getAuthor();
                }

                @Nullable
                @Override
                public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                    try {
                        System.out.println("Parse: "+playlist.getSongs().get(position).getImage_url());
                        Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        return bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }

                }
            })
                  .setNotificationListener(
                          new PlayerNotificationManager.NotificationListener() {
              @Override
              public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                      startForeground(notificationId,notification);
              }

              @Override
              public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                  if (dismissedByUser) {
                      stopForeground(true);
                      stopSelf();
                  }
              }
          })
                  .build();
            playerNotificationManager.setPlayer(player);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
       releasePlayer();
        super.onDestroy();
    }

    private void releasePlayer() {
        if (player != null) {
            playerNotificationManager.setPlayer(null);
            player.release();
            player = null;
        }
    }

    public SimpleExoPlayer startPlayer(){

        player = new SimpleExoPlayer.Builder(this).build();

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        ArrayList<MediaSource> mediaSources = new ArrayList<>();
        for (Song song : playlist.getSongs()) {
            Uri uri = Uri.parse(song.getPath());
            MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            mediaSources.add(audioSource);
        }




        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSources(mediaSources);

        player.seekTo(position, C.INDEX_UNSET);

       player.prepare(concatenatingMediaSource,false,false);
        return player;
    }

    private void startForeground() {
        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        channelId =    createNotificationChannel("my_service", "My Background Service");
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId="";
        }

        Notification.Builder notificationBuilder = new Notification.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.altas_notes)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();


        startForeground(101, notification);
    }


    //Basic Notification
    //Not ExoPlayer Notification!
    Notification createNotif(){
        String notificationChannelID="ENDLESS SERVICE CHANNELL";
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
          channel = new NotificationChannel(notificationChannelID, "Endless Service notifications Chanell", NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("Endless Service Channel");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
       PendingIntent pendingIntent =  PendingIntent.getActivity(context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder = new Notification.Builder(this,notificationChannelID);
        }else{
            builder = new Notification.Builder(this);
        }

        return builder.setContentTitle("Endless Service")
                .setContentText("This is your favorite endless service working")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.altas_notes)
                .setTicker("Ticker text")
                .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
                .build();
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel( String channelId,  String channelName){
   NotificationChannel  chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
   chan.setLightColor(Color.BLUE);
     chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service =  ( NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    public SimpleExoPlayer getPlayerInstance() {
        if (player == null) {
            return  startPlayer();
        }else{
        return player;
        }
    }


    public String getSongPath(){
        return playlist.getSongs().get(position).getPath();
    }

    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}