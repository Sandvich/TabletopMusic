package uk.org.puppykit.dreamfall;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;

public class MainPage extends Activity {
    SongService mediaBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        mediaBrowser = new SongService();
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
}
