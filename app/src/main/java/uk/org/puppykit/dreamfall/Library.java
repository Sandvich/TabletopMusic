package uk.org.puppykit.dreamfall;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

final class Library {
    // The basics
    private static final TreeMap<String, MediaMetadata> library = new TreeMap<>();
    private static final HashMap<String, String> musicFileName = new HashMap<>();

    public static List<MediaBrowser.MediaItem> getMediaItems() {
        List<MediaBrowser.MediaItem> result = new ArrayList<>();
        for (MediaMetadata metadata: library.values()) {
            result.add(new MediaBrowser.MediaItem(
                    metadata.getDescription(),
                    MediaBrowser.MediaItem.FLAG_PLAYABLE)
            );
        }
        return result;
    }

    // build our library
    static {
        addSong("Rush", "Rush", "Ingvild Hasund", "Dreamfall: The Longest Journey OST", 196, "raw/rush.mp3", "dreamfall_ost");
        addSong("second", "two", "Artist2", "second Album", 2, "", "magnet_dreamfall_ost");
    }

    // Helper functions to build our library
    private static void addSong(
            String mediaId,
            String title,
            String artist,
            String album,
            long duration,
            String fileName,
            String albumArtName
    ) {
        MediaMetadata.Builder builder = new MediaMetadata.Builder();
        MediaMetadata songData = builder.
                putString(MediaMetadata.METADATA_KEY_MEDIA_ID, mediaId).
                putString(MediaMetadata.METADATA_KEY_TITLE, title).
                putString(MediaMetadata.METADATA_KEY_ARTIST, artist).
                putString(MediaMetadata.METADATA_KEY_ALBUM, album).
                putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, getAlbumArtUri(albumArtName)).
                putLong(MediaMetadata.METADATA_KEY_DURATION, TimeUnit.MILLISECONDS.convert(duration, TimeUnit.SECONDS)).
                putString(MediaMetadata.METADATA_KEY_MEDIA_URI, getFileUri(fileName)).
                build();
        library.put(mediaId, songData);
        musicFileName.put(mediaId, fileName);
    }

    private static String getAlbumArtUri(String albumArtResName) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/mipmap/" + albumArtResName;
    }

    private static String getFileUri(String filename) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/raw/" + filename;
    }
}
