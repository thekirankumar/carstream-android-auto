package com.thekirankumar.youtubeauto;

import android.webkit.JavascriptInterface;

/**
 * Created by kiran.kumar on 08/01/18.
 */

public class JavascriptCallback {
    private OnURLChangeListener onURLChangeListener;

    public JavascriptCallback(OnURLChangeListener onURLChangeListener) {
        this.onURLChangeListener = onURLChangeListener;
    }

    @JavascriptInterface
    public void onUrlChange(String url) {
        onURLChangeListener.onURLChange(url);
    }

    public static interface OnURLChangeListener {
        void onURLChange(String url);
    }
}
