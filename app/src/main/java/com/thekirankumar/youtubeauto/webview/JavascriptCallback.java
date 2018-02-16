package com.thekirankumar.youtubeauto.webview;

import android.os.Handler;
import android.webkit.JavascriptInterface;


/**
 * Created by kiran.kumar on 08/01/18.
 */

public class JavascriptCallback {

    private final Handler handler;
    private JSCallbacks callbacks;

    public JavascriptCallback(JSCallbacks callbacks) {
        this.handler = new Handler();
        this.callbacks = callbacks;
    }

    @JavascriptInterface
    public void onVideoEvent(final String event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callbacks.onJSVideoEvent(event);
            }
        });
    }

    @JavascriptInterface
    public void onVideoCurrentTimeResult(final int currentTime) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callbacks.onVideoCurrentTimeResult(currentTime);
            }
        });
    }

    @JavascriptInterface
    public void onVideoDiscovered() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callbacks.onVideoElementDiscovered();
            }
        });
    }

    @JavascriptInterface
    public void showKeyboard(final String oldText) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callbacks.onShowKeyboardFromJS(oldText);
            }
        });
    }

    @JavascriptInterface
    public void hideKeyboard() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callbacks.onHideKeyboardFromJS();
            }
        });
    }

    public interface JSCallbacks {
        void onJSVideoEvent(String event);

        void onVideoElementDiscovered();

        void onVideoCurrentTimeResult(int currentTime);

        void onShowKeyboardFromJS(String oldText);

        void onHideKeyboardFromJS();
    }

}
