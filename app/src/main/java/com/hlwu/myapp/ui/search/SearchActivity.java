package com.hlwu.myapp.ui.search;

import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.github.xiaozhucdj.sildbacklibrary.SlideBaseActivity;
import com.hlwu.myapp.R;
import com.hlwu.myapp.ui.dailynewslist.DailyNewsFragment;

/**
 * Created by hlwu on 3/5/18.
 */

public class SearchActivity extends SlideBaseActivity implements View.OnClickListener{
    private static final String TAG = "flaggg_SearchActivity";
    private static final int PAGE_COUNT = 1;

    private DailyNewsFragment mDailyNewsFragment;
    private boolean mDoubleClickFlag = false;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;

    @Override
    public int setContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(View contentView) {
        mToolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) contentView.findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) contentView.findViewById(R.id.pager);
        mProgressBar = (ProgressBar) contentView.findViewById(R.id.first_loading_progressbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            mToolbar.setOnClickListener(this);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SearchActivity.this.finish();
                }
            });
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mViewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.INVISIBLE);
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mDailyNewsFragment = new DailyNewsFragment();
                    return mDailyNewsFragment;
                default:
                    return new DailyNewsFragment();
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.page_title_1);
                case 1:
                    return getResources().getString(R.string.page_title_2);
                default:
                    return getResources().getString(R.string.page_title_1);
            }
        }
    }

    public ProgressBar getmProgressBar() {
        return mProgressBar;
    }

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar:
                if (!mDoubleClickFlag) {
                    mDoubleClickFlag = true;
                    mDoubleSelectedHandler.sendEmptyMessageDelayed(0, 500);
                } else {
                    mDailyNewsFragment.getPresenter().getUi().scrollingToStart();
                }
        }
    }

    Handler mDoubleSelectedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mDoubleClickFlag = false;
        }
    };
}
