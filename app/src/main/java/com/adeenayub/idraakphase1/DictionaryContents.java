package com.adeenayub.idraakphase1;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class DictionaryContents extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_contents);

        VideoView videoView = (VideoView) findViewById(R.id.video1);
        String vpath="android.resource://"+getPackageName()+"/"+R.raw.shape;

        videoView.setVideoPath(vpath);

        //Uri videoUri = (Uri) Uri.parse(vpath);
        //videoView.setVideoURI(videoUri);
        //videoView.start();
        Log.i("Video", "Video Playing....");

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

     videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Toast.makeText(DictionaryContents.this, "Playback completed",Toast.LENGTH_SHORT).show();
            videoView.seekTo(1);
        }
    });
    }
}