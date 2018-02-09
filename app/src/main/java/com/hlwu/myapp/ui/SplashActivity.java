package com.hlwu.myapp.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.hlwu.myapp.R;

/**
 * Created by hlwu on 2/7/18.
 */

public class SplashActivity extends AppCompatActivity {

    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        mImageView = (ImageView) findViewById(R.id.splash_image);
//        mImageView.setBackgroundResource(R.drawable.splash_default);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
}
