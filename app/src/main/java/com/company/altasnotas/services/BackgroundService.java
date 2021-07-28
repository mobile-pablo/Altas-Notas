package com.company.altasnotas.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;

import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import android.support.v4.media.session.MediaSessionCompat;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

public class BackgroundService extends Service implements ExoPlayer.EventListener {

    private SimpleExoPlayer player;
    private Playlist playlist;
    private Context context;
    private PlayerNotificationManager playerNotificationManager;
    private final IBinder mBinder = new LocalBinder();
    private final String CHANNEL_ID="6";
    private final String NOTIFICATION_ID="8";
    private Integer position;
    private Long seekedTo;

    private String externalPath;
    private String externalPlaylistTitle;
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
        externalPath = intent.getStringExtra("path");
        externalPlaylistTitle = intent.getStringExtra("playlistTitle");
        seekedTo = intent.getLongExtra("ms",0);
        System.out.println("seekedTo: "+seekedTo);
        if(playlist!=null){
           if(!(playlist.getSongs().get(position).getPath().equals(externalPath) && playlist.getTitle().equals(externalPlaylistTitle))){

               playlist =  intent.getParcelableExtra("playlist");
               position = intent.getIntExtra("pos",0);
               ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
               playlist.setSongs(songs);

               releasePlayer();
               startPlayer();
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
                               intent.putExtra("frag","PlayerFragment");
                               intent.putExtra("playlist", playlist);
                               intent.putExtra("pos", position);
                               intent.putParcelableArrayListExtra("songs", songs);
                               intent.putExtra("ms",player.getContentPosition());
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
             /*
                    try {
                        System.out.println("Parse: "+playlist.getSongs().get(position).getImage_url());
                        Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        return bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
              */
                               return null;

                           }
                       })
                       .setNotificationListener(
                               new PlayerNotificationManager.NotificationListener() {
                                   @Override
                                   public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                                       if (ongoing) {
                                           // Make sure the service will not get destroyed while playing media.
                                           startForeground(notificationId, notification);
                                       } else {
                                           // Make notification cancellable.
                                           stopForeground(false);
                                       }
                                   }

                                   @Override
                                   public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                                       stopForeground(true);
                                       stopSelf();
                                   }
                               })
                       .build();


               MediaSessionCompat mediaSession = new MediaSessionCompat(context, context.getString(R.string.app_name));
               mediaSession.setActive(true);
               playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());

               playerNotificationManager.setSmallIcon(R.drawable.altas_notes);
               playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
               playerNotificationManager.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
               playerNotificationManager.setPlayer(player);
           }
        }else{
            playlist =  intent.getParcelableExtra("playlist");
            position = intent.getIntExtra("pos",0);
            ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
            playlist.setSongs(songs);

         //   startPlayer();
            testingPlayer();
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
                    intent.putExtra("frag","PlayerFragment");
                    intent.putExtra("playlist", playlist);
                    intent.putExtra("pos", position);
                    intent.putParcelableArrayListExtra("songs", songs);
                    intent.putExtra("ms",player.getContentPosition());
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
             /*
                    try {
                        System.out.println("Parse: "+playlist.getSongs().get(position).getImage_url());
                        Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        return bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
              */
                    return null;

                }
            })
                  .setNotificationListener(
                          new PlayerNotificationManager.NotificationListener() {
              @Override
              public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                  if (ongoing) {
                      // Make sure the service will not get destroyed while playing media.
                      startForeground(notificationId, notification);
                  } else {
                      // Make notification cancellable.
                      stopForeground(false);
                  }
              }

              @Override
              public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                      stopForeground(true);
                      stopSelf();
              }
          })
                  .build();

            MediaSessionCompat mediaSession = new MediaSessionCompat(context, context.getString(R.string.app_name));
            mediaSession.setActive(true);
            playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());

            playerNotificationManager.setSmallIcon(R.drawable.altas_notes);
            playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            playerNotificationManager.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
            playerNotificationManager.setPlayer(player);
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
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

        if(seekedTo!=0){
            player.seekTo(seekedTo);
        }

       player.prepare(concatenatingMediaSource,false,false);




        return player;
    }

    public SimpleExoPlayer testingPlayer(){
        player = new SimpleExoPlayer.Builder(this).build();

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse("https://media1.vocaroo.com/mp3/1nS8YXb35PBj"));
// Set the media item to be played.
        player.setMediaItem(mediaItem);
// Prepare the player.
        player.prepare();

        return player;
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



    //Notificion for older version of player
    private void createNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        Intent ii = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(playlist.getSongs().get(position).getAuthor()); //detail mode is the "expanded" notification
        bigText.setBigContentTitle(playlist.getSongs().get(position).getTitle());
        bigText.setSummaryText(playlist.getSongs().get(position).getTitle()); //small text under notification

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.altas_notes); //notification icon
        mBuilder.setContentTitle(playlist.getSongs().get(position).getTitle()); //main title
        mBuilder.setContentText(playlist.getSongs().get(position).getTitle()); //main text when you "haven't expanded" the notification yet
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel("notify_001",
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(channel);
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }

    }

    public SimpleExoPlayer getPlayerInstance() {
        if (player == null) {
            return  startPlayer();
        }else{
        return player;
        }
    }

    public void setPosition(Integer integer){
        position= integer;
    }



    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }


}