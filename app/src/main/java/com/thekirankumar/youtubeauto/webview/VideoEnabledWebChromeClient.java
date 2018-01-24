package com.thekirankumar.youtubeauto.webview;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;
import android.widget.VideoView;

/**
 * This class serves as a WebChromeClient to be set to a WebView, allowing it to playVideo video.
 * It works with old VideoView-s as well as the new HTML5VideoFullScreen inner classes.
 * With VideoView (typically API level <11), it will always show full-screen.
 * With HTML5VideoFullScreen (typically API level 11+), it will show both in-page and full-screen.
 * <p>
 * IMPORTANT NOTES:
 * - For API level 11+, android:hardwareAccelerated="true" must be set in the application manifest.
 * - The invoking activity must call VideoEnabledWebChromeClient's onBackPressed() inside of its own onBackPressed().
 * - Tested in Android API level 8+. Only tested on http://m.youtube.com.
 *
 * @author Cristian Perez (http://cpr.name)
 */
public class VideoEnabledWebChromeClient extends WebChromeClient implements OnPreparedListener, OnCompletionListener, OnErrorListener {
    private View.OnTouchListener videoTouchListener;
    private View activityNonVideoView;
    private ViewGroup activityVideoView;
    private View loadingView;
    private VideoEnabledWebView webView;
    private boolean isVideoFullscreen; // Indicates if the video is being displayed using a custom view (typically full-screen)
    private FrameLayout videoViewContainer;
    private CustomViewCallback videoViewCallback;
    private ToggledFullscreenCallback toggledFullscreenCallback;
    private boolean videoStretchingEnabled;

    /**
     * Never use this constructor alone.
     * This constructor allows this class to be defined as an inline inner class in which the user can override methods
     */
    public VideoEnabledWebChromeClient() {
    }

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView    A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     */
    public VideoEnabledWebChromeClient(View activityNonVideoView, ViewGroup activityVideoView) {
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = null;
        this.webView = null;
        this.isVideoFullscreen = false;
    }

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView    A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     * @param loadingView          A View to be shown while the video is loading (typically only used in API level <11). Must be already inflated and without a parent view.
     */
    public VideoEnabledWebChromeClient(View activityNonVideoView, ViewGroup activityVideoView, View loadingView) {
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = loadingView;
        this.webView = null;
        this.isVideoFullscreen = false;
    }

    /**
     * Builds a video enabled WebChromeClient.
     *
     * @param activityNonVideoView A View in the activity's layout that contains every other view that should be hidden when the video goes full-screen.
     * @param activityVideoView    A ViewGroup in the activity's layout that will display the video. Typically you would like this to fill the whole layout.
     * @param loadingView          A View to be shown while the video is loading (typically only used in API level <11). Must be already inflated and without a parent view.
     * @param webView              The owner VideoEnabledWebView. Passing it will enable the VideoEnabledWebChromeClient to detect the HTML5 video ended event and exit full-screen.
     *                             Note: The web page must only contain one video tag in order for the HTML5 video ended event to work. This could be improved if needed (see Javascript code).
     */
    public VideoEnabledWebChromeClient(View activityNonVideoView, ViewGroup activityVideoView, View loadingView, VideoEnabledWebView webView) {
        this.activityNonVideoView = activityNonVideoView;
        this.activityVideoView = activityVideoView;
        this.loadingView = loadingView;
        this.webView = webView;
        this.isVideoFullscreen = false;
    }

    public void setVideoStretchingEnabled(boolean videoStretchingEnabled) {
        this.videoStretchingEnabled = videoStretchingEnabled;
        handleVideoStretching();
    }

    public View.OnTouchListener getVideoTouchListener() {
        return videoTouchListener;
    }

    public void setVideoTouchListener(View.OnTouchListener videoTouchListener) {
        this.videoTouchListener = videoTouchListener;
    }

    /**
     * Indicates if the video is being displayed using a custom view (typically full-screen)
     *
     * @return true it the video is being displayed using a custom view (typically full-screen)
     */
    public boolean isVideoFullscreen() {
        return isVideoFullscreen;
    }

    /**
     * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
     *
     * @param callback A VideoEnabledWebChromeClient.ToggledFullscreenCallback callback
     */
    public void setOnToggledFullscreen(ToggledFullscreenCallback callback) {
        this.toggledFullscreenCallback = callback;
    }


    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        Log.d(getClass().getName(), "onShowCustomView() called with: view = [" + view + "], callback = [" + callback + "]");
        if (view instanceof FrameLayout) {

            // A video wants to be shown
            FrameLayout frameLayout = (FrameLayout) view;
            View focusedChild = frameLayout.getFocusedChild();
            if (focusedChild != null) {
                focusedChild.requestFocus();
                focusedChild.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                                webView.goBack();
                                return true;
                            }
                        }
                        return false;
                    }
                });
            }

            // Save video related variables
            this.isVideoFullscreen = true;
            this.videoViewContainer = frameLayout;
            this.videoViewCallback = callback;

            // Hide the non-video view, add the video view, and show it
            activityNonVideoView.setVisibility(View.INVISIBLE);
            activityVideoView.addView(videoViewContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            activityVideoView.setVisibility(View.VISIBLE);
            activityVideoView.setBackgroundColor(Color.BLACK);
            if (focusedChild != null) {
                focusedChild.setBackgroundColor(Color.BLACK);
                if (videoTouchListener != null) {
                    focusedChild.setOnTouchListener(videoTouchListener);
                }
            }


            if (focusedChild instanceof VideoView) {
                // VideoView (typically API level <11)
                VideoView videoView = (VideoView) focusedChild;
                // Handle all the required events
                videoView.setOnPreparedListener(this);
                videoView.setOnCompletionListener(this);
                videoView.setOnErrorListener(this);
            } else // Usually android.webkit.HTML5VideoFullScreen$VideoSurfaceView, sometimes android.webkit.HTML5VideoFullScreen$VideoTextureView
            {
                // HTML5VideoFullScreen (typically API level 11+)
                // Handle HTML5 video ended event
                if (webView != null && webView.getSettings().getJavaScriptEnabled()) {
                    // Run javascript code that detects the video end and notifies the interface
                    handleVideoStretching();
                }
            }

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                toggledFullscreenCallback.toggledFullscreen(true);
            }
        }
    }

    private void handleVideoStretching() {
        String js = "javascript:";
        js += "_ytrp_html5_video = document.getElementsByTagName('video')[0];";
        js += "if (_ytrp_html5_video !== undefined) {" +
                "var originalWidth = _ytrp_html5_video.style.width; var originalHeight = _ytrp_html5_video.style.height; var originalTop = _ytrp_html5_video.style.top;" +
                //" _ytrp_html5_video.style.height=window.innerHeight+'px';" +
                //"_ytrp_html5_video.style.width=window.innerWidth+'px';" +
                //"_ytrp_html5_video.style.top=0;" +
                "";
        if (videoStretchingEnabled && isVideoFullscreen()) {
            js += "_ytrp_html5_video.style.objectFit='fill';" +
                    " _ytrp_html5_video.style.height=window.innerHeight+'px';" +
                    "_ytrp_html5_video.style.width=window.innerWidth+'px';" +
                    "_ytrp_html5_video.style.top=0;";
        } else {
            //js += "_ytrp_html5_video.style.objectFit='none';";

        }
        js += "}";
        webView.loadUrl(js);
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) // Only available in API level 14+
    {
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        // This method must be manually (internally) called on video end in the case of VideoView (typically API level <11)
        // This method must be manually (internally) called on video end in the case of HTML5VideoFullScreen (typically API level 11+) because it's not always called automatically
        // This method must be manually (internally) called on back key press (from this class' onBackPressed() method)

        if (isVideoFullscreen) {
            if (webView != null && webView.getSettings().getJavaScriptEnabled()) {
                // Run javascript code that detects the video end and notifies the interface
                String js = "javascript:";
                js += "_ytrp_html5_video = document.getElementsByTagName('video')[0];";
//                js += "if (_ytrp_html5_video !== undefined) {" +
//                        "_ytrp_html5_video.style.height=originalHeight;" +
//                        "_ytrp_html5_video.style.width=originalWidth;" +
//                        "_ytrp_html5_video.style.top=originalTop;";
//
//                js += "}";
                webView.loadUrl(js);
            }
            // Hide the video view, remove it, and show the non-video view
            activityVideoView.setVisibility(View.INVISIBLE);
            activityVideoView.removeView(videoViewContainer);
            activityNonVideoView.setVisibility(View.VISIBLE);

            // Call back
            if (videoViewCallback != null) videoViewCallback.onCustomViewHidden();

            // Reset video related variables
            isVideoFullscreen = false;
            videoViewContainer = null;
            videoViewCallback = null;

            // Notify full-screen change
            if (toggledFullscreenCallback != null) {
                toggledFullscreenCallback.toggledFullscreen(false);
            }
        }
    }

    @Override
    public View getVideoLoadingProgressView() // Video will start loading, only called in the case of VideoView (typically API level <11)
    {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
            return loadingView;
        } else {
            return super.getVideoLoadingProgressView();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) // Video will start playing, only called in the case of VideoView (typically API level <11)
    {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) // Video finished playing, only called in the case of VideoView (typically API level <11)
    {
        onHideCustomView();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) // Error while playing video, only called in the case of VideoView (typically API level <11)
    {
        return false; // By returning false, onCompletion() will be called
    }

    /**
     * Notifies the class that the back key has been pressed by the user.
     * This must be called from the Activity's onBackPressed(), and if it returns false, the activity itself should handle it. Otherwise don't do anything.
     *
     * @return Returns true if the event was handled, and false if it is not (video view is not visible)
     */
    public boolean onBackPressed() {
        if (isVideoFullscreen) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            request.grant(request.getResources());
        }
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        super.onGeolocationPermissionsShowPrompt(origin, callback);
        callback.invoke(origin, true, true);
    }


    public interface ToggledFullscreenCallback {
        public void toggledFullscreen(boolean fullscreen);
    }

}