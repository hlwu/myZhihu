package com.hlwu.myapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.hlwu.myapp.R;
import com.hlwu.myapp.news.TopStories;
import com.hlwu.myapp.presenter.DailyNewsFragmentPresenter;
import com.hlwu.myapp.utils.NetUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class DailyNewsFragment
        extends BaseFragment<DailyNewsFragmentPresenter, DailyNewsFragmentPresenter.DailyNewsFragmentUI>
        implements DailyNewsFragmentPresenter.DailyNewsFragmentUI {

    private String TAG = "flaggg_DailyNewsFragment";

    private PullToRefreshView mPullToRefreshView;
    private RecyclerView mRecyclerView;
    private DailyNewsAdapter mDailyNewsAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private ViewGroup mRootView;
    private static boolean mShouldLoadBeforeNews;
    private List<DailyNewsItems> mDailyNewsItems = new ArrayList<DailyNewsItems>();
    private List<TopStories> mTopStoriesItems = new ArrayList<TopStories>();
    private static final int REFRESH_DELAY = 10000;
    private static final long BANNER_AUTO_TURNING_TIME = 4000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mLinearLayoutManager = new SmoothLinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mDailyNewsAdapter = new DailyNewsAdapter(getActivity(), mDailyNewsItems, mTopStoriesItems);
        mDailyNewsAdapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int id = mDailyNewsItems.get(position).getId();
                Log.d(TAG, "click card: " + mDailyNewsItems.get(position).getTitle());
                Intent intent = new Intent(getActivity(), DailyNewsContentActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
        mRecyclerView.setAdapter(mDailyNewsAdapter);
        mShouldLoadBeforeNews = false;
        mRecyclerView.addOnScrollListener(new OnRecyclerScrollListener());

        mPullToRefreshView = (PullToRefreshView) mRootView.findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefreshComplete() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, REFRESH_DELAY);
            }

            @Override
            public void onRefreshStart() {
                try {
                    checkWriteExternalStoragePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (NetUtil.isNetworkConnected(getContext())) {
                        getPresenter().loadNews("latest");
                    } else {
                        Snackbar.make(mRootView, R.string.no_network_available, Snackbar.LENGTH_LONG).show();
                        mPullToRefreshView.setRefreshing(false);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "got exception: " + e);
                }
            }
        });

        return mRootView;
    }

    @Override
    public DailyNewsFragmentPresenter createPresenter() {
        return new DailyNewsFragmentPresenter();
    }

    @Override
    public DailyNewsFragmentPresenter.DailyNewsFragmentUI getUi() {
        return this;
    }

    @Override
    public List<DailyNewsItems> getmDailyNewsItems() {
        return mDailyNewsItems;
    }

    public List<TopStories> getmTopStoriesItems() {
        return mTopStoriesItems;
    }

    @Override
    public DailyNewsAdapter getRecyclerAdapter() {
        return mDailyNewsAdapter;
    }

    @Override
    public PullToRefreshView getPullToRefreshView() {
        return mPullToRefreshView;
    }

    @Override
    public void showNoNewLatestNews() {
        if (mRootView != null) {
            Snackbar.make(mRootView, R.string.no_new_latest_news, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showFootProgressLoading(boolean isLoading) {
        mDailyNewsAdapter.showFootProgressLoading(isLoading);
    }

    @Override
    public void showDownloadNothing() {
        if (mRootView != null) {
            Snackbar.make(mRootView, R.string.download_nothing, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void scrollingToStart() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    public class OnRecyclerScrollListener extends RecyclerView.OnScrollListener {

        private int lastVisibleItem = 0;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (mDailyNewsAdapter != null && newState == RecyclerView.SCROLL_STATE_IDLE
                    && lastVisibleItem + 1 == mDailyNewsAdapter.getItemCount()) {
                if (mShouldLoadBeforeNews) {
                    mDailyNewsAdapter.showFootProgressLoading(true);
                    if (NetUtil.isNetworkConnected(getContext())) {
                        if (getPresenter().getmOldestNews() != null) {
                            getPresenter().loadNews(getPresenter().getmOldestNews().getDate());
                        } else {
                            mDailyNewsAdapter.showFootProgressLoading(false);
                        }
                    } else {
                        mDailyNewsAdapter.showFootProgressLoading(false);
                        Snackbar.make(mRootView, R.string.no_network_available, Snackbar.LENGTH_LONG).show();
                    }
                }
                mShouldLoadBeforeNews = !mShouldLoadBeforeNews;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mShouldLoadBeforeNews &&
                    lastVisibleItem > mLinearLayoutManager.findLastVisibleItemPosition()) {    // scroll up
                mShouldLoadBeforeNews = false;
            }
            lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();
        }
    }

    public class DailyNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
        private List<DailyNewsItems> dailyNewsItems;
        private Context mContext;
        private ProgressBar mFootProgressBar;
        private TextView mFootTextView;
        private CBViewHolderCreator mCBViewHolderCreator;
        private List<TopStories> topStoriesItems;
        private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = null;

        public DailyNewsAdapter(Context context, List<DailyNewsItems> dailyNewsItems, List<TopStories> topStoriesItems) {
            this.mContext = context;
            this.dailyNewsItems = dailyNewsItems;
            this.topStoriesItems = topStoriesItems;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_DATE) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.date_view, viewGroup, false);
                return new DateViewHolder(v);
            } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.footer_view, viewGroup, false);
                return new FooterViewHolder(v);
            } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_EMPTY) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.empty_view, viewGroup, false);
                return new EmptyViewHolder(v);
            } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_BANNER) {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.banner_view, viewGroup, false);
                final BannerViewHolder bannerViewHolder = new BannerViewHolder(v);
                this.mCBViewHolderCreator = new CBViewHolderCreator<BannerViewHolder>() {
                    @Override
                    public BannerViewHolder createHolder() {
                        return bannerViewHolder;
                    }
                };
                return bannerViewHolder;
            } else {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, viewGroup, false);
                v.setOnClickListener(this);
                RecyclerView.ViewHolder vh = new CardViewHolder(v);
                ((CardViewHolder) vh).mTextView.setOnClickListener(this);
                return vh;
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            int viewType = getItemViewType(position);
            Log.d(TAG, "onBindViewHolder   position: " + position + "; viewType: " + viewType);
            try {
                if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_DATE) {
                    Time today = new Time();
                    today.setToNow();
                    int year = Integer.valueOf(dailyNewsItems.get(position).getDate().substring(0, 4));
                    int month = Integer.valueOf(dailyNewsItems.get(position).getDate().substring(4, 6));
                    int day = Integer.valueOf(dailyNewsItems.get(position).getDate().substring(6, 8));
                    if (dailyNewsItems.get(position).getDate().equals(today.format("%Y%m%d"))) {
                        ((DateViewHolder) viewHolder).mDateView.setText(mContext.getResources().getString(R.string.news_today));
                    } else {
                        String date;
                        Time time = new Time();
                        time.set(day, (month - 1), year);   //Month [0-11]
                        time.normalize(true);
                        if (Locale.getDefault().getLanguage() == "zh") {
                            date = mContext.getResources().getString(R.string.date_zh,
                                    month, day, getPresenter().weekDayToString(time.weekDay));
                        } else {
                            date = mContext.getResources().getString(R.string.date_es,
                                    getPresenter().weekDayToString(time.weekDay),
                                    getPresenter().monthToString(month), day, year);
                        }
                        ((DateViewHolder) viewHolder).mDateView.setText(date);
                    }
                } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_FOOTER) {
                    mFootProgressBar = ((FooterViewHolder) viewHolder).mProgressBar;
                    mFootTextView = ((FooterViewHolder) viewHolder).mTextView;
                } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_EMPTY) {
                    //do nothing
                } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_STORIES) {
                    ((CardViewHolder) viewHolder).mTextView.setText(dailyNewsItems.get(position).getTitle());
                    ((CardViewHolder) viewHolder).mImageView.setImageBitmap(dailyNewsItems.get(position).getPic());
                    ((CardViewHolder) viewHolder).mTextView.setTag(position);
                } else if (viewType == DailyNewsFragmentPresenter.VIEW_TYPE_BANNER) {
                    List<String> imageUris = new LinkedList<String>();
                    List<String> texts = new LinkedList<String>();
                    for (TopStories topStory : topStoriesItems) {
                        imageUris.add(topStory.getImage());
                        texts.add(topStory.getTitle());
                    }
                    ((BannerViewHolder) viewHolder).mConvenientBanner.setPages(mCBViewHolderCreator, imageUris, texts);
                }
                viewHolder.itemView.setTag(position);
            } catch (Exception e) {
                Log.d(TAG, "got exception: " + e);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (dailyNewsItems.isEmpty()) {
                return DailyNewsFragmentPresenter.VIEW_TYPE_EMPTY;
            }
            if (position < getPresenter().getmNewsMapContentLength()) {
                return getPresenter().getViewTypeAtPosition(position);
            } else if (position == getPresenter().getmNewsMapContentLength()) {
                return DailyNewsFragmentPresenter.VIEW_TYPE_FOOTER;
            } else {
                Log.d(TAG, "getItemViewType some logic is wrong");
                return DailyNewsFragmentPresenter.VIEW_TYPE_EMPTY;
            }
        }

        @Override
        public int getItemCount()
        {
            return dailyNewsItems == null ? 1    //1 means empty view
                    : dailyNewsItems.size() + 1;    //1 means footer view
        }

        @Override
        public void onClick(View v) {
            if (mOnRecyclerViewItemClickListener != null) {
                mOnRecyclerViewItemClickListener.onItemClick(v,(int)v.getTag());
            }
        }

        public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
            this.mOnRecyclerViewItemClickListener = listener;
        }

        public void showFootProgressLoading(boolean showProgress) {
            if (mFootProgressBar != null && mFootTextView != null) {
                if (showProgress) {
                    mFootProgressBar.setVisibility(View.VISIBLE);
                    mFootTextView.setVisibility(View.GONE);
                } else {
                    mFootProgressBar.setVisibility(View.GONE);
                    mFootTextView.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , int position);
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageView mImageView;
        public CardViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.card_title);
            mImageView = (ImageView) v.findViewById(R.id.card_pic);
        }

    }
    public static class DateViewHolder extends RecyclerView.ViewHolder {

        public TextView mDateView;
        public DateViewHolder(View v) {
            super(v);
            mDateView = (TextView) v.findViewById(R.id.date_text);
        }

    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ProgressBar mProgressBar;

        public FooterViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.footer_text_view);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.footer_progress_bar);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public EmptyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.empty_text);
        }
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder  implements Holder<String, String> {
        public ConvenientBanner mConvenientBanner;
        private BannerLayout mBannerLayout;

        public BannerViewHolder(View itemView) {
            super(itemView);
            mConvenientBanner = (ConvenientBanner) itemView.findViewById((R.id.convenient_banner));
            mConvenientBanner.setPageIndicator(new int[]{R.drawable.ic_page_indicator, R.drawable.ic_page_indicator_focused})
                    .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                    .startTurning(BANNER_AUTO_TURNING_TIME)
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            int id = mTopStoriesItems.get(position).getId();
                            Log.d(TAG, "click banner: " + mTopStoriesItems.get(position).getTitle());
                            Intent intent = new Intent(getActivity(), DailyNewsContentActivity.class);
                            intent.putExtra("id", id);
                            startActivity(intent);
                        }
                    });
        }

        public View createView(Context context) {
            mBannerLayout = new BannerLayout(context, new ImageView(context), new TextView(context));
            mBannerLayout.getmImageView().setBackgroundResource(R.drawable.foreground_op);
            mBannerLayout.getmImageView().setScaleType(ImageView.ScaleType.CENTER_CROP);
            return mBannerLayout;
        }

        @Override
        public void updateUIIcon(Context context, int position, String uri) {
            mBannerLayout.getmImageView().setBackgroundResource(R.drawable.foreground_op);
            ImageLoader.getInstance().displayImage(uri, mBannerLayout.getmImageView());
        }

        @Override
        public void updateUIText(Context context, int position, String text) {
            mBannerLayout.getmTextView().setText(text);
        }
    }

    public void checkWriteExternalStoragePermission(String permission) {
        if (ContextCompat.checkSelfPermission(getContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                // Show an explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.write_external_storage_explanation)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MainActivity.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Snackbar.make(mRootView, R.string.no_write_external_storage_permission, Snackbar.LENGTH_LONG).show();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MainActivity.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    /**
     * {@link RecyclerView#smoothScrollToPosition(int position)} will call
     * {@link RecyclerView.LayoutManager#smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position)}.
     * So we override the {@link LinearSmoothScroller#calculateSpeedPerPixel(DisplayMetrics)} to change
     * {@link RecyclerView#smoothScrollToPosition(int position)}'s scroll speed.
     */
    private class SmoothLinearLayoutManager extends LinearLayoutManager {
        public SmoothLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            LinearSmoothScroller linearSmoothScroller =
                    new LinearSmoothScroller(recyclerView.getContext()) {
                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return super.calculateSpeedPerPixel(displayMetrics) / 3;
                        }
                    };
            linearSmoothScroller.setTargetPosition(position);
            startSmoothScroll(linearSmoothScroller);
        }
    }
}
