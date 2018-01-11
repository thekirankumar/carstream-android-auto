package com.thekirankumar.youtubeauto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.Map;

/**
 * This class serves as a WebView to be used in conjunction with a VideoEnabledWebChromeClient.
 * It makes possible:
 * - To detect the HTML5 video ended event so that the VideoEnabledWebChromeClient can exit full-screen.
 * <p>
 * Important notes:
 * - Javascript is enabled by default and must not be disabled with getSettings().setJavaScriptEnabled(false).
 * - setWebChromeClient() must be called before any loadData(), loadDataWithBaseURL() or loadUrl() method.
 *
 * @author Cristian Perez (http://cpr.name)
 */
public class VideoEnabledWebView extends WebView {
    private VideoEnabledWebChromeClient videoEnabledWebChromeClient;
    private boolean addedJavascriptInterface;

    @SuppressWarnings("unused")
    public VideoEnabledWebView(Context context) {
        super(context);
        addedJavascriptInterface = false;
    }

    @SuppressWarnings("unused")
    public VideoEnabledWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addedJavascriptInterface = false;
    }

    @SuppressWarnings("unused")
    public VideoEnabledWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addedJavascriptInterface = false;
    }

    public void requestFullScreen() {
        loadUrl("javascript: function waitForElementToDisplay(selector, time, fn) {\n" +
                "var el = document.querySelector(selector);" +
                "if(el!=null) {" +
                "            fn(el);" +
                "            return;" +
                "        }" +
                "        else {" +
                "            setTimeout(function() {" +
                "                waitForElementToDisplay(selector, time, fn);" +
                "            }, time);\n" +
                "        }" +
                "    }" +
                "waitForElementToDisplay('video', 1000, function(el) {" +
                "rfs = el.requestFullscreen\n" +
                "        || el.webkitRequestFullScreen\n" +
                "        || el.mozRequestFullScreen\n" +
                "        || el.msRequestFullscreen \n" +
                "    ;\n" +
                "\n" +
                "    rfs.call(el);" +
                "})");
    }

    @Override
    public void onPause() {
        //super.onPause();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     *
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    @SuppressWarnings("unused")
    public boolean isVideoFullscreen() {
        return videoEnabledWebChromeClient != null && videoEnabledWebChromeClient.isVideoFullscreen();
    }

    public void exitFullScreen() {
        videoEnabledWebChromeClient.onHideCustomView();
    }

    public void playVideo() {
        loadUrl("javascript: var el = document.getElementsByTagName('video')[0];" +
                "    el.play();");
    }
    public void playVideoIfPaused() {
        loadUrl("javascript: " +
                "if(window.pausedDueToMinimize) {" +
                "var el = document.getElementsByTagName('video')[0];" +
                "    el.play();" +
                "window.pausedDueToMinimize = false;" +
                "}");
    }
    public void pauseVideo() {
        loadUrl("javascript: var el = document.getElementsByTagName('video')[0];\n" +
                "if(!el.paused) {" +
                "el.pause();" +
                "window.pausedDueToMinimize = true;" +
                "}" +
                "");
    }


    /**
     * Pass only a VideoEnabledWebChromeClient instance.
     */
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void setWebChromeClient(WebChromeClient client) {
        getSettings().setJavaScriptEnabled(true);

        if (client instanceof VideoEnabledWebChromeClient) {
            this.videoEnabledWebChromeClient = (VideoEnabledWebChromeClient) client;
        }

        super.setWebChromeClient(client);
    }

    @Override
    public void loadData(String data, String mimeType, String encoding) {
        addJavascriptInterface();
        super.loadData(data, mimeType, encoding);
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        addJavascriptInterface();
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    public void loadUrl(String url) {
        addJavascriptInterface();
        super.loadUrl(url);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        addJavascriptInterface();
        super.loadUrl(url, additionalHttpHeaders);
    }

    private void addJavascriptInterface() {
        if (!addedJavascriptInterface) {
            // Add javascript interface to be called when the video ends (must be done before page load)
            //noinspection all
            addJavascriptInterface(new JavascriptInterface(), "_VideoEnabledWebView"); // Must match Javascript interface name of VideoEnabledWebChromeClient

            addedJavascriptInterface = true;
        }
    }

    public class JavascriptInterface {
        @android.webkit.JavascriptInterface
        @SuppressWarnings("unused")
        public void notifyVideoEnd() // Must match Javascript interface method of VideoEnabledWebChromeClient
        {
            Log.d("___", "GOT IT");
            // This code is not executed in the UI thread, so we must force that to happen
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (videoEnabledWebChromeClient != null) {
                        videoEnabledWebChromeClient.onHideCustomView();
                    }
                }
            });
        }
    }

}