package com.hlwu.myapp.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.support.design.widget.TabLayout;
import android.widget.ProgressBar;

import com.hlwu.myapp.R;
import com.hlwu.myapp.ui.about.AboutActivity;
import com.hlwu.myapp.ui.dailynewslist.DailyNewsFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TabLayout.OnTabSelectedListener {

    private static final String TAG = "flaggg_myApp";
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 100;

    private static final int PAGE_COUNT = 1;

    private SearchView mSearchView;
    private DailyNewsFragment mDailyNewsFragment;
    private FocusFragment mSecondFragment;
    private boolean mDoubleClickFlag = false;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ProgressBar mProgressBar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
//
//        final MenuItem myActionMenuItem = menu.findItem(R.id.search);
//        mSearchView = (SearchView) myActionMenuItem.getActionView();
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                if(!mSearchView.isIconified()) {
//                    mSearchView.setIconified(true);
//                }
//                myActionMenuItem.collapseActionView();
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String s) {
//                return false;
//            }
//        });

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mProgressBar = (ProgressBar) findViewById(R.id.first_loading_progressbar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            mToolbar.setOnClickListener(this);

            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.toolbar_menu_about:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                            break;
                    }
                    return true;
                }
            });
        }

        mViewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(this);

        mTabLayout.setVisibility(View.GONE);
        mViewPager.setVisibility(View.INVISIBLE);
    }

    public ProgressBar getmProgressBar() {
        return mProgressBar;
    }

    public ViewPager getmViewPager() {
        return mViewPager;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                case 1:
                    mSecondFragment = new FocusFragment();
                    return mSecondFragment;
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(TAG, "onRequestPermissionsResult write_external_storage granted");
                } else {

                    Log.d(TAG, "onRequestPermissionsResult write_external_storage denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar:
                if (!mDoubleClickFlag) {
                    mDoubleClickFlag = true;
                    mDoubleSelectedHandler.sendEmptyMessageDelayed(0, 500);
                } else {
                    mDailyNewsFragment.getPresenter().getUi().scrollingToStart();
                }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) {
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
