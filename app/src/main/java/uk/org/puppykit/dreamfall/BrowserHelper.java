package uk.org.puppykit.dreamfall;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class BrowserHelper {
    private static final String TAG = BrowserHelper.class.getSimpleName();

    private final Context context;
    private final Class<?extends MediaBrowserService> songService;
    private final List<MediaController.Callback> callbackList = new ArrayList<>();

    private MediaBrowser mediaBrowser;
    @Nullable
    private MediaController mediaController;

    private final BrowserConnectionCallback browserConnectionCallback;
    private final MediaControllerCallback mediaControllerCallback;
    private final BrowserSubscriptionCallback browserSubscriptionCallback;

    public BrowserHelper(Context context,
                         Class<?extends MediaBrowserService> serviceClass) {
        this.context = context;
        this.songService = serviceClass;
        mediaBrowser = null;
        mediaController = null;
        browserConnectionCallback = new BrowserConnectionCallback();
        mediaControllerCallback = new MediaControllerCallback();
        browserSubscriptionCallback = new BrowserSubscriptionCallback();
    }

    public void onStart() {
        if (mediaBrowser == null) {
            mediaBrowser = new MediaBrowser(
                    context,
                    new ComponentName(context, songService),
                    browserConnectionCallback,
                    null
            );
            mediaBrowser.connect();
        }
        Log.d(TAG,"onStart: Creating MediaBrowser and connecting.");
    }

    public void onStop() {
        if (mediaController != null) {
            if (mediaBrowser.isConnected()) {
                mediaBrowser.disconnect();
                mediaBrowser = null;
            }
            mediaController.unregisterCallback(mediaControllerCallback);
            mediaController = null;
        }

        resetState();
        Log.d(TAG, "onStop: Releasing MediaController, Disconnecting from MediaBrowser");
    }

    protected void onChildrenLoaded(@NonNull String parentId,
                                    @NonNull List<MediaBrowser.MediaItem> children) {
        for (final MediaBrowser.MediaItem mediaItem : children) {
            // Add queue items
            // mediaController.
        }
        mediaController.getTransportControls().prepare();
    }

    @NonNull
    protected final MediaController getMediaController() {
        if (mediaController == null) {
            throw new IllegalStateException("MediaController is null!");
        }
        return mediaController;
    }

    private void resetState() {
        performOnAllCallbacks(new CallbackCommand() {
            public void perform(@NonNull MediaController.Callback callback) {
                callback.onPlaybackStateChanged(null);
            }
        });
        Log.d(TAG, "resetState: ");
    }

    public MediaController.TransportControls getTransportControls() {
        if (mediaController == null) {
            throw new IllegalStateException("MediaController is null!");
        }
        return mediaController.getTransportControls();
    }

    public void registerCallback(MediaController.Callback callback) {
        if (callback != null) {
            callbackList.add(callback);

            if (mediaController != null) {
                final MediaMetadata metadata = mediaController.getMetadata();
                if (metadata != null) {
                    callback.onMetadataChanged(metadata);
                }

                final PlaybackState playbackState = mediaController.getPlaybackState();
                if (playbackState != null) {
                    callback.onPlaybackStateChanged(playbackState);
                }
            }
        }
    }

    private void performOnAllCallbacks(@NonNull CallbackCommand command) {
        for (MediaController.Callback callback : callbackList) {
            if (callback != null) {
                command.perform(callback);
            }
        }
    }

    private interface CallbackCommand { void perform(@NonNull MediaController.Callback callback); }

    // Callbacks
   private class BrowserConnectionCallback extends MediaBrowser.ConnectionCallback {
        @Override
        public void onConnected() {
            mediaController = new MediaController(context, mediaBrowser.getSessionToken());
            mediaController.registerCallback(mediaControllerCallback);

            mediaControllerCallback.onMetadataChanged(mediaController.getMetadata());
            mediaControllerCallback.onPlaybackStateChanged(mediaController.getPlaybackState());
            Log.d(TAG, "Connected to the playback service.");

            mediaBrowser.subscribe(mediaBrowser.getRoot(), browserSubscriptionCallback);
        }

        @Override
        public void onConnectionSuspended() {
            Log.e(TAG, "Service has crashed!");
        }

        @Override
        public void onConnectionFailed() {
            Log.e(TAG, "The service has refused this connection.");
        }
    }

    public class BrowserSubscriptionCallback extends MediaBrowser.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowser.MediaItem> children) {
            BrowserHelper.this.onChildrenLoaded(parentId, children);
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onMetadataChanged(final MediaMetadata metadata) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaController.Callback callback) {
                    callback.onMetadataChanged(metadata);
                }
            });
        }

        @Override
        public void onPlaybackStateChanged(@Nullable final PlaybackState state) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaController.Callback callback) {
                    callback.onPlaybackStateChanged(state);
                }
            });
        }

        @Override
        public void onSessionDestroyed() {
            resetState();
            onPlaybackStateChanged(null);
        }
    }
}
