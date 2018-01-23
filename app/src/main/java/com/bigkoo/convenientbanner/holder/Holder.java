package com.bigkoo.convenientbanner.holder;

/**
 * Created by Sai on 15/12/14.
 * @param <T> 任何你指定的对象
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public interface Holder<T, E>{
    View createView(Context context);
    void updateUIIcon(Context context, int position, T data);
    void updateUIText(Context context, int position, E texts);
}