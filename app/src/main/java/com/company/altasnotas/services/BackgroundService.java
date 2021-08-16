package com.company.altasnotas.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class BackgroundService extends Service implements ExoPlayer.EventListener {

    private SimpleExoPlayer player;
    public Playlist playlist;
    private Context context;
    public PlayerNotificationManager playerNotificationManager;
    private final IBinder mBinder = new LocalBinder();
    private final String CHANNEL_ID = "5423";
    private final String NOTIFICATION_ID = "2421";
    public Integer position;
    public Long seekedTo;
    public Integer isFav;
    private String externalPath, externalPlaylistTitle, externalDescription;
    private   MediaSessionCompat mediaSession;
    private  MediaSessionConnector mediaSessionConnector;
    private final AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void destroyNotif(){

        this.stopForeground(true);
        stopSelf();
        playerNotificationManager.setPlayer(new SimpleExoPlayer.Builder(this).setHandleAudioBecomingNoisy(true).build());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        externalPath = intent.getStringExtra("path");
        externalPlaylistTitle = intent.getStringExtra("playlistTitle");
        externalDescription = intent.getStringExtra("desc");
        seekedTo = intent.getLongExtra("ms", 0);
        isFav = intent.getIntExtra("isFav",0);

        if (playlist != null) {
            if (!(playlist.getSongs().get(position).getPath().equals(externalPath) && playlist.getTitle().equals(externalPlaylistTitle) && playlist.getDescription().equals(externalDescription))) {

                playlist = intent.getParcelableExtra("playlist");
                position = intent.getIntExtra("pos", 0);
                ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
                playlist.setSongs(songs);

                releasePlayer();
           //    startPlayer();
                 testingPlayer();

                playerNotificationManager = PlayerNotificationManager
                        .createWithNotificationChannel(context,CHANNEL_ID,R.string.app_name, Integer.parseInt(NOTIFICATION_ID), new PlayerNotificationManager.MediaDescriptionAdapter() {
                            @Override
                            public CharSequence getCurrentContentTitle(Player player) {
                                return playlist.getSongs().get(position).getTitle();
                            }

                            @Nullable
                            @Override
                            public PendingIntent createCurrentContentIntent(Player player) {
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("frag", "PlayerFragment");
                                intent.putExtra("playlist", playlist);
                                intent.putExtra("pos", position);
                                intent.putParcelableArrayListExtra("songs", songs);
                                intent.putExtra("ms", player.getContentPosition());
                                intent.putExtra("isFav", isFav);
                                intent.putExtra("state", player.getPlaybackState());
                                intent.putExtra("ready", player.getPlayWhenReady());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            }

                            @Nullable
                            @Override
                            public CharSequence getCurrentContentText(Player player) {
                                return playlist.getSongs().get(position).getAuthor();
                            }

                            @Nullable
                            @Override
                            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {


                                Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                                Glide.with(getApplicationContext())
                                        .load(uri).into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        callback.onBitmap(drawableToBitmap(resource));
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
                                return null;

                            }
                        });


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
                                intent.putExtra("frag", "PlayerFragment");
                                intent.putExtra("playlist", playlist);
                                intent.putExtra("pos", position);
                                intent.putParcelableArrayListExtra("songs", songs);
                                intent.putExtra("ms", player.getContentPosition());
                                intent.putExtra("isFav", isFav);
                                intent.putExtra("state", player.getPlaybackState());
                                intent.putExtra("ready", player.getPlayWhenReady());
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                                return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            }

                            @Nullable
                            @Override
                            public CharSequence getCurrentContentText(Player player) {
                                return playlist.getSongs().get(position).getAuthor();
                            }

                            @Nullable
                            @Override
                            public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {


                                Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                                Glide.with(getApplicationContext())
                                        .load(uri).into(new CustomTarget<Drawable>() {
                                    @Override
                                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                        callback.onBitmap(drawableToBitmap(resource));
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
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
        } else {
            playlist = intent.getParcelableExtra("playlist");
            position = intent.getIntExtra("pos", 0);
            ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
            playlist.setSongs(songs);
       //    startPlayer();
             testingPlayer();


            playerNotificationManager = PlayerNotificationManager
                    .createWithNotificationChannel(context,CHANNEL_ID,R.string.app_name, Integer.parseInt(NOTIFICATION_ID), new PlayerNotificationManager.MediaDescriptionAdapter() {
                        @Override
                        public CharSequence getCurrentContentTitle(Player player) {
                            return playlist.getSongs().get(position).getTitle();
                        }

                        @Nullable
                        @Override
                        public PendingIntent createCurrentContentIntent(Player player) {
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("frag", "PlayerFragment");
                            intent.putExtra("playlist", playlist);
                            intent.putExtra("pos", position);
                            intent.putParcelableArrayListExtra("songs", songs);
                            intent.putExtra("ms", player.getContentPosition());
                            intent.putExtra("isFav", isFav);
                            intent.putExtra("state", player.getPlaybackState());
                            intent.putExtra("ready", player.getPlayWhenReady());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        }

                        @Nullable
                        @Override
                        public CharSequence getCurrentContentText(Player player) {
                            return playlist.getSongs().get(position).getAuthor();
                        }

                        @Nullable
                        @Override
                        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {

                            Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                            Glide.with(getApplicationContext())
                                    .load(uri).into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    callback.onBitmap(drawableToBitmap(resource));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                            return null;

                        }


                    });
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
                            intent.putExtra("frag", "PlayerFragment");
                            intent.putExtra("playlist", playlist);
                            intent.putExtra("pos", position);
                            intent.putParcelableArrayListExtra("songs", songs);
                            intent.putExtra("ms", player.getContentPosition());
                            intent.putExtra("isFav", isFav);
                            intent.putExtra("state", player.getPlaybackState());
                            intent.putExtra("ready", player.getPlayWhenReady());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        }

                        @Nullable
                        @Override
                        public CharSequence getCurrentContentText(Player player) {
                            return playlist.getSongs().get(position).getAuthor();
                        }

                        @Nullable
                        @Override
                        public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {

                            Uri uri = Uri.parse(playlist.getSongs().get(position).getImage_url());
                            Glide.with(getApplicationContext())
                                    .load(uri).into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    callback.onBitmap(drawableToBitmap(resource));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
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
    private void createNotificationChannel(String channelId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationUtil.IMPORTANCE_DEFAULT;
            NotificationUtil.createNotificationChannel(context,channelId,R.string.app_name, R.string.app_name, importance);
        }
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
        if (mediaSession != null) {
            mediaSession.setActive(false);
        }

        MainActivity.currentSongAlbum.setValue("");
        MainActivity.currentSongAuthor.setValue("");
        MainActivity.currentSongTitle.setValue("");


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Integer.parseInt(NOTIFICATION_ID));
        stopForeground(true);
        mNotificationManager.cancelAll();
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
        MainActivity.currentSongAlbum.setValue("");
        MainActivity.currentSongAuthor.setValue("");
        MainActivity.currentSongTitle.setValue("");

        stopForeground(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Integer.parseInt(NOTIFICATION_ID));
    }



    public void releasePlayer() {
        if (player != null) {
            playerNotificationManager.setPlayer(null);
            player.release();
            player = null;
        }


        if (mediaSession != null) {
            mediaSession.release();
        }
    }

    public SimpleExoPlayer startPlayer() {

        player = new SimpleExoPlayer.Builder(this).setHandleAudioBecomingNoisy(true).build();

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

        if (seekedTo != 0) {
            player.seekTo(seekedTo);
        }

        player.setAudioAttributes(audioAttributes, true);

        player.prepare(concatenatingMediaSource, false, false);
      mediaSession = new MediaSessionCompat(context, "sample");
      mediaSessionConnector = new MediaSessionConnector(mediaSession);
      mediaSessionConnector.setMediaButtonEventHandler(new MediaSessionConnector.MediaButtonEventHandler() {
          @Override
          public boolean onMediaButtonEvent(Player player, ControlDispatcher controlDispatcher, Intent mediaButtonEvent) {
             KeyEvent event = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Integer x=  event.getKeyCode();

              if (event.getAction() == KeyEvent.ACTION_UP)
              {
               switch (x){
                   case    KeyEvent.KEYCODE_MEDIA_NEXT:
                       player.next();
                       break;
                   case    KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                       player.previous();
                       break;
               }
           }
              return false;
          }
      });
      mediaSessionConnector.setPlayer(player);

        if (mediaSession != null) {
            mediaSession.setActive(true);
        }

        return player;
    }

    public SimpleExoPlayer testingPlayer() {


        Uri uri = Uri.parse("https://media1.vocaroo.com/mp3/1nS8YXb35PBj");



        player = new SimpleExoPlayer.Builder(this).setHandleAudioBecomingNoisy(true).build();

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        ArrayList<MediaSource> mediaSources = new ArrayList<>();
        for (Song song : playlist.getSongs()) {
            MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            mediaSources.add(audioSource);
        }

        player.setAudioAttributes(audioAttributes, true);

        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSources(mediaSources);

        player.seekTo(position, C.INDEX_UNSET);

        if (seekedTo != 0) {
            player.seekTo(seekedTo);
        }

        player.prepare(concatenatingMediaSource, false, false);

        mediaSession = new MediaSessionCompat(context, "sample");
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player);

        if (mediaSession != null) {
            mediaSession.setActive(true);
        }

        return player;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    //Notificion for older version of player
    @RequiresApi(api = Build.VERSION_CODES.O)
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
            return testingPlayer();
       //     return startPlayer();
        } else {
            return player;
        }
    }

    public void setPosition(Integer integer) {
        position = integer;
    }


    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }





}