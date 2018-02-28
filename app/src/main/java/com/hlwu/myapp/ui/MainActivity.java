package com.hlwu.myapp.ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.support.design.widget.TabLayout;
import android.widget.DatePicker;
import android.widget.ProgressBar;

import com.hlwu.myapp.R;
import com.hlwu.myapp.ui.about.AboutActivity;
import com.hlwu.myapp.ui.dailynewslist.DailyNewsFragment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
                        case R.id.toolbar_menu_tools:
                            showTools();
                            break;
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

    private void showTools() {
        List<String> tools = Arrays.asList(getResources().getStringArray(R.array.tools_list));
        String items[] = new String[tools.size()];
        for (int i = 0; i < tools.size(); i++) {
            items[i] = tools.get(i);
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        showBirthdayPicker();
                        break;
                    case 1:
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showBirthdayPicker() {
        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                Calendar from = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                from.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                from.set(Calendar.MILLISECOND, 0);
                end.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.MINUTE, 0);
                end.set(Calendar.SECOND, 0);
                end.set(Calendar.MILLISECOND, 0);
                int days = (int) getDays(from, end);
                showLivedDaysMessage(days, year, monthOfYear, dayOfMonth,
                        (monthOfYear == end.get(Calendar.MONTH) && (dayOfMonth == end.get(Calendar.DAY_OF_MONTH))));
            }
        };
        Calendar c = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(MainActivity.this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))/*.setTitle("choice your birthday")*//*.show()*/;
        d.setTitle(R.string.lived_days_title);
        d.show();
    }

    private void showLivedDaysMessage(int days, int year, int monthOfYear,int dayOfMonth, boolean todayIsBirthday) {
        StringBuffer sb = new StringBuffer();
        sb.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        if (todayIsBirthday) {
            builder.setTitle(R.string.lived_days_msg_today_is_birthday);
        }
        if (days < 0) {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_1, sb.toString()));
        } else if (days < 700) {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_2, days));
        } else if (days < 3650) {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_3, days));
        } else if (days < 7300) {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_4, days));
        } else if (days < 18000) {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_5, days));
        } else {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_6, days));
        }
        if (monthOfYear == 1 && dayOfMonth == 29) {
            builder.setMessage(getResources().getString(R.string.lived_days_msg_7, days));
        }
        builder.create().show();
    }

    private double getDays(Calendar c1, Calendar c2) {
        Date date1 = c1.getTime();
        Date date2 = c2.getTime();
        return getDays(date1, date2);
    }

    private double getDays(Date date1, Date date2) {
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        double d = (time2 - time1) / 1000d / 60 / 60 / 24;
        return d;
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
