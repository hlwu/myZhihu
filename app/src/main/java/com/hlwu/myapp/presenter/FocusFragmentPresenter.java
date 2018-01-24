package com.hlwu.myapp.presenter;

import android.content.Context;

import com.hlwu.myapp.ui.base.Ui;

/**
 * Created by hlwu on 1/11/18.
 */

public class FocusFragmentPresenter extends Presenter<FocusFragmentPresenter.FocusFragmentUI> {

    public interface FocusFragmentUI extends Ui {
        Context getContext();
    }
}
