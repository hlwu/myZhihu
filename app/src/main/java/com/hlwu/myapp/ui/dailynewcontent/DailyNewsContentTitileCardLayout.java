package com.hlwu.myapp.ui.dailynewcontent;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlwu.myapp.R;

/**
 * Created by hlwu on 1/23/18.
 */

public class DailyNewsContentTitileCardLayout extends FrameLayout {

    private ImageView mImageView;
    private TextView mTitleText, mImageSource;

    public DailyNewsContentTitileCardLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.daily_news_content_title_card_layout, this);
        this.mImageView = (ImageView) view.findViewById(R.id.title_image);
        this.mTitleText = (TextView) view.findViewById(R.id.title_text);
        this.mImageSource = (TextView) view.findViewById(R.id.title_image_source);
    }

    public void setmImageView(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    public void setmTitleText(TextView mTitleText) {
        this.mTitleText = mTitleText;
    }

    public void setmImageSource(TextView mImageSource) {
        this.mImageSource = mImageSource;
    }

    public ImageView getmImageView() {
        return mImageView;
    }

    public TextView getmTitleText() {
        return mTitleText;
    }

    public TextView getmImageSource() {
        return mImageSource;
    }
}
