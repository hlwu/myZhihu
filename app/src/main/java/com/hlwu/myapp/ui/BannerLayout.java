package com.hlwu.myapp.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlwu.myapp.R;

/**
 * Created by hlwu on 1/18/18.
 */

public class BannerLayout extends FrameLayout {

    private ImageView mImageView;
    private TextView mTextView;

    public BannerLayout(@NonNull Context context, ImageView mImageView, TextView mTextView) {
        this(context, null);
    }

    public BannerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.d("flaggg", "onInterceptouchEvent super: " + super.onInterceptTouchEvent(ev));
//        super.onInterceptTouchEvent(ev);
//        return false;
//    }

    public BannerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.banner_layout, this);
        this.mImageView = (ImageView) view.findViewById(R.id.banner_image);
        this.mTextView = (TextView) view.findViewById(R.id.banner_text);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public ImageView getmImageView() {
        return mImageView;
    }

    public TextView getmTextView() {
        return mTextView;
    }

    public void setmImageView(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    public void setmTextView(TextView mTextView) {
        this.mTextView = mTextView;
    }
}
