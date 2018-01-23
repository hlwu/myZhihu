package com.hlwu.myapp.ui;

import com.hlwu.myapp.presenter.FocusFragmentPresenter;

/**
 * Created by hlwu on 1/11/18.
 */

public class FocusFragment
        extends BaseFragment<FocusFragmentPresenter, FocusFragmentPresenter.FocusFragmentUI>
        implements FocusFragmentPresenter.FocusFragmentUI{


    @Override
    public FocusFragmentPresenter createPresenter() {
        return new FocusFragmentPresenter();
    }

    @Override
    public FocusFragmentPresenter.FocusFragmentUI getUi() {
        return this;
    }
}
