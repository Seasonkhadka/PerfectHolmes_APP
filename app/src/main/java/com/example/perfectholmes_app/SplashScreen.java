package com.example.perfectholmes_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        VideoView videoView = findViewById(R.id.videoView);
        TextView textView = findViewById(R.id.textView11);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splashvideo);
        videoView.setVideoURI(videoUri);


        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        // Disable default media controller
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            // Video started
                            mediaController.hide();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(layoutParams);
        videoView.start();

        videoView.setOnCompletionListener(mp -> {
            // Navigate to next activity when video ends
            Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
            startActivity(intent);
            // Optionally, finish this activity so the user cannot go back to it
        });
    }
}