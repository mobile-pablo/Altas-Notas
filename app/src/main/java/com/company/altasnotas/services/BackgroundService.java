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
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
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
import com.company.altasnotas.fragments.player.PlayerFragment;
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
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

import static androidx.media2.exoplayer.external.util.Util.getLooper;

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
    public    MediaSessionCompat mediaSession;
    private  MediaSessionConnector mediaSessionConnector;
    private final AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MOVIE)
            .build();
    private Boolean isShuffled=false;
    private Integer isRepeated=0;
    private final String SERVICE ="BACKGROUND SERVICE";

    @Override
    public synchronized void onCreate() {
        super.onCreate();
        context = this;
    }

    @Nullable
    @Override
    public synchronized IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void initializePlayer(Intent intent) {
        externalPath = intent.getStringExtra("path");
        externalPlaylistTitle = intent.getStringExtra("playlistTitle");
        externalDescription = intent.getStringExtra("desc");
        seekedTo = intent.getLongExtra("ms", 0);
        isFav = intent.getIntExtra("isFav",-1);

        playlist = intent.getParcelableExtra("playlist");
        position = intent.getIntExtra("pos", 0);
        ArrayList<Song> songs = intent.getParcelableArrayListExtra("songs");
        playlist.setSongs(songs);
     //  startPlayer();
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




        mediaSession = new MediaSessionCompat(context, context.getString(R.string.app_name));
        mediaSession.setActive(true);
        playerNotificationManager.setMediaSessionToken(mediaSession.getSessionToken());

        playerNotificationManager.setSmallIcon(R.drawable.ic_altas_notes_notif);
        playerNotificationManager.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        playerNotificationManager.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);
        playerNotificationManager.setRewindIncrementMs(0);
        playerNotificationManager.setFastForwardIncrementMs(0);
        playerNotificationManager.setPlayer(player);
    }

    public synchronized void destroyNotif(){
        Log.d(SERVICE, "Destroy Notif called!");
        if(getMainLooper().getThread().isAlive()){
            onDestroy();
        }
    }
    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(SERVICE, "OnStartCommand Called");
        return START_NOT_STICKY;
    }

    @Override
    public synchronized void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        destroyNotif();
        releasePlayer();
        PlayerFragment.playerView.setPlayer(null);
        PlayerFragment.binding.miniPlayerView.setPlayer(null);
    }

    @Override
    public synchronized void onDestroy() {
        Log.d(SERVICE, "onDestroy Called");
        releasePlayer();
        MainActivity.clearCurrentSong();
        stopForeground(true);
        stopSelf();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Integer.parseInt(NOTIFICATION_ID));
        PlayerFragment.isDimissed=true;
        PlayerFragment.playerView.setPlayer(null);
        PlayerFragment.binding.miniPlayerView.setPlayer(null);
        super.onDestroy();
        stopForeground(true);
        stopSelf();
    }

    public synchronized void releasePlayer() {
        if (player != null) {
            playerNotificationManager.setPlayer(null);
            player.pause();
            player.seekTo(0);
         //   player.release();
           // player = null;
        }


        if (mediaSession != null) {
            mediaSession.release();
        }
    }

    public synchronized SimpleExoPlayer startPlayer() {

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

    public synchronized SimpleExoPlayer testingPlayer() {


        Uri localUri = Uri.parse("https://media1.vocaroo.com/mp3/1nS8YXb35PBj");



        player = new SimpleExoPlayer.Builder(this).setHandleAudioBecomingNoisy(true).build();

        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        ArrayList<MediaSource> mediaSources = new ArrayList<>();
        for (Song song : playlist.getSongs()) {
            MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(localUri);
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

    public synchronized Bitmap drawableToBitmap(Drawable drawable) {
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

    public synchronized SimpleExoPlayer getPlayerInstance() {
        return player;
    }

    public synchronized void setPosition(Integer integer) {
        position = integer;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(SERVICE, "OnUnBind Called");
        return super.onUnbind(intent);
    }

    //This function is used When I change Song by RecyclerView
    public void clearOldPlayer() {
        if(player!=null){
            player.pause();
            player.setPlayWhenReady(true);
            player.release();
            player=null;
        }
    }

    public void setShuffleEnabled(boolean b) {
        isShuffled=b;
    }

    public Boolean getShuffle(){
        return isShuffled;
    }

    public void setRepeat(int repeatModeOff) {
        isRepeated= repeatModeOff;
    }

    public Integer getRepeat(){
        return isRepeated;
    }

    public class LocalBinder extends Binder {
        public synchronized  BackgroundService getService() {
            return BackgroundService.this;
        }
    }





}