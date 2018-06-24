package uk.org.puppykit.dreamfall;

// Functionality
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.service.media.MediaBrowserService;

// UI
import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MainPage extends Activity {
    // Code stuff
    private static final String TAG = MainPage.class.getSimpleName();
    SongService mediaBrowserService;
    private BrowserHelper browserHelper;
    private View.OnClickListener onPlayButtonClick;
    private Boolean isPlaying;

    // UI Elements
    private TextView playing_info;
    private ProgressBar playing_progress;
    private ImageView art_playing;
    private ImageButton play_pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        playing_info = findViewById(R.id.playing_info);
        playing_progress = findViewById(R.id.playing_progress);
        art_playing = findViewById(R.id.art_playing);
        play_pause = findViewById(R.id.play_pause_button);

        browserHelper = new BrowserHelper(this, SongService.class);

        // Attach onClick events
        Button playButton = findViewById(R.id.play_button);
        makeListeners();
        playButton.setOnClickListener(onPlayButtonClick);
    }

    @Override
    public void onStart() {
        super.onStart();
        browserHelper.onStart();
        playing_progress.setIndeterminate(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        browserHelper.onStop();
        playing_progress.setIndeterminate(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    // Button functions
    private void makeListeners() {
        onPlayButtonClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaBrowserService.BrowserRoot root = mediaBrowserService.onGetRoot("uk.org.puppykit.dreamfall", 0, null);
                Log.d(TAG, "Root ID: " + root.getRootId());
                Log.d(TAG,"Extras: " + root.getExtras());
            }
        };
    }

    // Callbacks
    private class BrowserListener extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            isPlaying = playbackState != null && playbackState.getState() == PlaybackState.STATE_PLAYING;
            play_pause.setPressed(isPlaying);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            if (metadata != null) {
                String to_display = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) + "\n" +
                                    metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) + " - " +
                                    metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                playing_info.setText(to_display);
                art_playing.setImageBitmap(Library.getAlbumArt(metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)));
            }
        }
    }
}
