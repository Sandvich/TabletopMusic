package uk.org.puppykit.dreamfall;

// Android Imports
import android.media.MediaDescription;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//Java Imports
import java.util.ArrayList;
import java.util.List;

public class SongService extends MediaBrowserService {
    private static final String MY_MEDIA_ROOT_ID = "dreamfall";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "dreamfall_empty";

    // Playlists go here
    @Override
    public void onCreate() {
        System.out.print("Running OnCreate method.");
        super.onCreate();

        // Initialise media session
        MediaSession session = new MediaSession(getApplicationContext(), "Dreamfall Media App");
        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        MediaPlayer player = new MediaPlayer();

        // Set neutral state
        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY |
                            PlaybackState.ACTION_PAUSE|
                            PlaybackState.ACTION_STOP)
                .setState(PlaybackState.STATE_STOPPED, 0, 0)
                .build();
        session.setPlaybackState(state);
        session.setCallback(new CustomCallbacks(this, session, getApplicationContext()));

        // Let things communicate
        setSessionToken(session.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (clientPackageName.equals("uk.org.puppykit.dreamfall") && clientUid == 0) {
            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
        }
        else {
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentMediaId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
        if (parentMediaId.equals(MY_EMPTY_MEDIA_ROOT_ID)) {
            result.sendResult(null);
        } else {
            List<MediaBrowser.MediaItem> items = Library.getMediaItems();
            result.sendResult(items);
        }
    }
}
