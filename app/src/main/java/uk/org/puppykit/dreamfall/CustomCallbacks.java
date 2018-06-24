package uk.org.puppykit.dreamfall;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaDescription;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

class CustomCallbacks extends MediaSession.Callback {
    private SongService service;
    private MediaPlayer player;
    private MediaSession session;
    private Context context;
    private PlaybackState.Builder stateBuilder;
    private Notification.Builder notifBuilder;

    CustomCallbacks(SongService service, MediaSession session, Context context) {
        this.service = service;
        player = new MediaPlayer();
        this.session = session;
        this.context = context;
        stateBuilder = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PAUSE|
                        PlaybackState.ACTION_STOP);
        notifBuilder = new Notification.Builder(context , "default");
    }

    @Override
    public void onPlay () {
        service.startService(new Intent("android.media.browse.MediaBrowserService.START"));
        stateBuilder.setState(PlaybackState.STATE_PLAYING, 0, 1);
        session.setPlaybackState(stateBuilder.build());
        session.setActive(true);
        player.start();

        // Set notification metadata
        MediaController controller = session.getController();
        MediaDescription metadata = controller.getMetadata().getDescription();
        notifBuilder.setContentTitle(metadata.getTitle())
                    .setContentText(metadata.getSubtitle())
                    .setSubText(metadata.getDescription())
                    .setLargeIcon(metadata.getIconBitmap())

                    // Switch to this app when the notification is pressed
                    .setContentIntent(controller.getSessionActivity())
                    // Stop service when notification is swiped away
                    .setDeleteIntent(PendingIntent.getActivity(context, 0, stopPlayback, 0))
                    // Make the controls visible on the lock screen
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    // Make use of MediaStyle features
                    .setStyle(new Notification.MediaStyle()
                            .setMediaSession(session.getSessionToken())
                            .setShowActionsInCompactView(0)
                    );
                    // TODO: Add play and stop buttons

        service.startForeground(Notification.DEFAULT_ALL, notifBuilder.build());
    }

    public void onStop() {
        service.stopSelf();
        stateBuilder.setState(PlaybackState.STATE_STOPPED, 0,0);
        session.setPlaybackState(stateBuilder.build());
        session.setActive(false);
        player.stop();
        service.stopForeground(true);
    }

    public void onPause() {
        player.pause();
        stateBuilder.setState(PlaybackState.STATE_PAUSED, player.getCurrentPosition(),0);
        service.stopForeground(false);
    }

    private Intent stopPlayback = new Intent("android.media.browse.MediaBrowserService.PLAYBACK_STOP");
}
