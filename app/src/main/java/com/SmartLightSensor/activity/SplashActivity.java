package com.SmartLightSensor.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;


import com.SmartLightSensor.MainActivity;
import com.SmartLightSensor.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    final int MY_PERMISSIONS_REQUEST_LOCATION=1;
    @BindView(R.id.imageView)
    android.widget.TextView centerLogo;
    @BindView(R.id.textView)
    android.widget.TextView TextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        ButterKnife.bind(this);
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        TextView.startAnimation(animation);
        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN}, MY_PERMISSIONS_REQUEST_LOCATION);

        }else{
            animate();
            // Write you code here if permission already given.
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    animate();

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

                }

            }

        }
    }

    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;

    private void animate() {
        centerLogo.setTranslationY(-250);
        centerLogo.setAlpha(0f);

        ViewCompat.animate(centerLogo)
                .translationYBy(250)
                .alphaBy(1)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();

        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewCompat.animate(centerLogo)
                            .translationYBy(120)
                            .alphaBy(1)
                            .setStartDelay(STARTUP_DELAY * 2)
                            .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                            new DecelerateInterpolator(1.2f)).start();
                Intent intent;
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);


//                        TextView.setTranslationY(TextView.getHeight());
//
//                        ViewCompat.animate(TextView)
//                                .translationYBy(-TextView.getHeight())
//                                .alphaBy(1)
//                                .setStartDelay(STARTUP_DELAY + ITEM_DELAY + ITEM_DELAY)
//                                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
//                                new DecelerateInterpolator(1.2f)).start();
                }
        }, SPLASH_TIME_OUT);


    }
}
