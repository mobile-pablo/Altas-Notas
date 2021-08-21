package com.company.altasnotas;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    private final Integer START_DELAY=1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        startAnimation();
        setFullScreen();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, START_DELAY);
    }

    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void startAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.img_zoom_out);
        ImageView logo = findViewById(R.id.introLogo);
        logo.startAnimation(animation);

    }
}