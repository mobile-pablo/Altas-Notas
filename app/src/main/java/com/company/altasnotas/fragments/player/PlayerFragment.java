package com.company.altasnotas.fragments.player;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.altasnotas.MainActivity;
import com.company.altasnotas.R;
import com.company.altasnotas.models.Playlist;
import com.company.altasnotas.models.Song;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.sql.DataSource;


public class PlayerFragment extends Fragment {
    private FirebaseStorage storage;
    private ImageButton fav_btn;
    private Button settings_btn;
    private final Playlist playlist;
    int position;
    private ImageView song_img;

    private TextView title, author;


    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;

    public PlayerFragment(Playlist playlist, int position){
        this.playlist = playlist;
        this.position=position;
        //We are sending playlist to this player and let it play all of it
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
      View view=  inflater.inflate(R.layout.fragment_player, container, false);
      storage = FirebaseStorage.getInstance();
      title = view.findViewById(R.id.player_song_title);
      author = view.findViewById(R.id.player_song_author);
      song_img =view.findViewById(R.id.player_song_img);



        title.setText(playlist.getSongs().get(position).getTitle());
        author.setText(playlist.getSongs().get(position).getAuthor());
        Glide.with(getContext()).load(playlist.getSongs().get(position).getImage_url()).into(song_img);



        initPlayer(view);





    fav_btn = view.findViewById(R.id.player_song_fav_btn);
    settings_btn = view.findViewById(R.id.player_song_options_btn);

      /**
 *         if(jcplayerView.getCurrentAudio()!=null){
 *             //Here I check if I get back to song I been before.
 *             // If I been I want to seekTo current timeStamp.
 *             if (jcplayerView.getCurrentAudio().hashCode() == jcAudios.get(position).hashCode())
 *             {
 *                 System.out.println("Same song!");
 *
 *             } else{
 *
 *             }
 *         }else{
 *             jcplayerView.playAudio(jcAudios.get(position));
 *         }
 */


    fav_btn.setOnClickListener(v->{

        if( fav_btn.getDrawable().getConstantState().equals(fav_btn.getContext().getDrawable(R.drawable.ic_heart_empty).getConstantState()))
        {
            fav_btn.setImageResource(R.drawable.ic_heart_full);
        }else{
            fav_btn.setImageResource(R.drawable.ic_heart_empty);
        }
    });
      return view;
    }

    private void initPlayer(View view) {

        playerView= view.findViewById(R.id.player_view);
        playerView.setControllerShowTimeoutMs(0);
        playerView.setCameraDistance(0);
        simpleExoPlayer = new SimpleExoPlayer.Builder(getContext()).build();
        playerView.setPlayer(simpleExoPlayer);
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "app"));
            ArrayList<MediaSource> mediaSources = new ArrayList<>();
            for (Song song : playlist.getSongs()) {
                Uri uri = Uri.parse(song.getPath());
                MediaSource audioSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
                mediaSources.add(audioSource);
            }




            ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
            concatenatingMediaSource.addMediaSources(mediaSources);

            simpleExoPlayer.seekTo(position, C.INDEX_UNSET);

            simpleExoPlayer.prepare(concatenatingMediaSource,false,false);
            simpleExoPlayer.setPlayWhenReady(false);

            simpleExoPlayer.addListener(new ExoListener());

    }



    class ExoListener implements Player.Listener{
        Player player = playerView.getPlayer();

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            // Video playback status
            title.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getTitle());
            author.setText(playlist.getSongs().get(player.getCurrentWindowIndex()).getAuthor());
            Glide.with(getContext()).load(playlist.getSongs().get(player.getCurrentWindowIndex()).getImage_url()).into(song_img);
            System.out.println("Gramy piosenke: "+playlist.getSongs().get((int) player.getCurrentWindowIndex()).getTitle());

            Log.d("playbackState = " + playbackState + " playWhenReady = " + playWhenReady,"Exo");
            switch (playbackState){
                case Player.STATE_IDLE:
                    // free
                    break;
                case Player.STATE_BUFFERING:
                    // Buffer
                    break;
                case Player.STATE_READY:
                    // Get ready
                    break;
                case Player.STATE_ENDED:
                    // End
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // Report errors
            switch (error.type){
                case ExoPlaybackException.TYPE_SOURCE:
                    // Error loading resources
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    // Errors in rendering
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    // unexpected error
                    break;
            }
        }

    }


}

