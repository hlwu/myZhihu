package com.hlwu.myapp.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.hlwu.myapp.presenter.Presenter;
import com.nostra13.universalimageloader.utils.L;

/**
 * Parent for all fragments that use Presenters and Ui design.
 */
public abstract class BaseFragment<P extends Presenter<U>, U extends Ui> extends Fragment {

//    private static final String KEY_FRAGMENT_HIDDEN = "key_fragment_hidden";
    private static final String TAG = "flaggg_BaseFragment";

    private P mPresenter;

    public abstract P createPresenter();

    public abstract U getUi();

    protected BaseFragment() {
        mPresenter = createPresenter();
    }

    /**
     * Presenter will be available after onActivityCreated().
     *
     * @return The presenter associated with this fragment.
     */
    public P getPresenter() {
        return mPresenter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated()");
        super.onActivityCreated(savedInstanceState);
        mPresenter.onUiReady(getUi());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (savedInstanceState != null) {
//            mPresenter.onRestoreInstanceState(savedInstanceState);
//            if (savedInstanceState.getBoolean(KEY_FRAGMENT_HIDDEN)) {
//                getFragmentManager().beginTransaction().hide(this).commit();
//            }
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.onUiDestroy(getUi());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        mPresenter.onSaveInstanceState(outState);
//        outState.putBoolean(KEY_FRAGMENT_HIDDEN, isHidden());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        ((FragmentDisplayManager) activity).onFragmentAttached(this);
    }
}
