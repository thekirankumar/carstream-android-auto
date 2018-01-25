package com.thekirankumar.youtubeauto.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.thekirankumar.youtubeauto.BuildConfig;
import com.thekirankumar.youtubeauto.Manifest;
import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.bookmarks.Bookmark;
import com.thekirankumar.youtubeauto.bookmarks.BookmarkUtils;
import com.thekirankumar.youtubeauto.bookmarks.BookmarksClickCallback;
import com.thekirankumar.youtubeauto.bookmarks.BookmarksFragment;
import com.thekirankumar.youtubeauto.service.MyMediaBrowserService;
import com.thekirankumar.youtubeauto.utils.WebviewUtils;
import com.thekirankumar.youtubeauto.webview.VideoEnabledWebChromeClient;
import com.thekirankumar.youtubeauto.webview.VideoEnabledWebView;
import com.thekirankumar.youtubeauto.webview.VideoWebView;

import java.net.URISyntaxException;


public class WebViewPhoneFragment extends CarFragment implements BookmarksClickCallback {
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 10;
    private static final String BOOKMARKS_FRAGMENT_TAG = "bookmarks";
    private final String TAG = "WebViewCarFragment";
    private VideoWebView webView;
    private EditText editText;
    private ProgressBar progressBar;
    private boolean isNightMode;
    private MediaSessionCompat mediaSessionCompat;
    private MediaBrowserCompat mediaBrowser;

    public WebViewPhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
//        if (true) {
//            actions |= PlaybackStateCompat.ACTION_PAUSE;
//        } else {
//            actions |= PlaybackStateCompat.ACTION_PLAY;
//        }
        return actions;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview_phone, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.phone_menu, menu);
        MenuItem infoItem = menu.findItem(R.id.app_info);
        infoItem.setTitle("Version v" + BuildConfig.VERSION_NAME);
        infoItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            String url = "http://www.thekirankumar.com/blog/2017/12/29/play-youtube-video-android-auto-app/?from_app=true";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if (item.getItemId() == R.id.back) {
            webView.goBack();
        } else if (item.getItemId() == R.id.refresh) {
            webView.reload();
        } else if (item.getItemId() == R.id.send) {
            SharedPreferences car = getActivity().getSharedPreferences("car", Context.MODE_PRIVATE);
            car.edit().putString("url", webView.getUrl()).commit();
            Toast.makeText(getActivity(), "Page bookmarked", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Goto Android Auto App and click 'Receive from phone' to load this page", Toast.LENGTH_LONG).show();
        } else if (item.getItemId() == R.id.bookmark_button) {
            showBookmarksScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = view.findViewById(R.id.progress_bar);
        webView = view.findViewById(R.id.web_view);
        editText = view.findViewById(R.id.edittext_url);
        Intent intent = new Intent(getActivity(), MyMediaBrowserService.class);
        getActivity().startService(intent);

        mediaBrowser = new MediaBrowserCompat(getActivity(), new ComponentName(getActivity(), MyMediaBrowserService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            Log.v("MainActivity", "connected");
                            // Ah, here’s our Token again
                            MediaSessionCompat.Token token =
                                    mediaBrowser.getSessionToken();
                            // This is what gives us access to everything
                            MediaControllerCompat controller =
                                    new MediaControllerCompat(getActivity(), token);
                            // Convenience method to allow you to use
                            // MediaControllerCompat.getMediaController() anywhere
                            MediaControllerCompat.setMediaController(
                                    getActivity(), controller);

                        } catch (RemoteException e) {

                        }
                    }

                    @Override
                    public void onConnectionSuspended() {
                        super.onConnectionSuspended();
                    }

                    @Override
                    public void onConnectionFailed() {
                        super.onConnectionFailed();
                    }
                }, null);
        mediaBrowser.connect();
        ViewGroup webViewContainer = view.findViewById(R.id.container);
        ViewGroup fullScreenView = view.findViewById(R.id.full_screen_view);
        webView.setWebViewClient(new CustomWebViewClient());
        VideoEnabledWebChromeClient videoEnabledWebChromeClient = new VideoEnabledWebChromeClient(webViewContainer, fullScreenView, new ProgressBar(getActivity()), webView);
        webView.setWebChromeClient(videoEnabledWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.getSettings().setAllowContentAccess(true);
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        webView.loadUrl("https://www.youtube.com");
        webView.requestFocus();
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub

                if ((actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_GO) || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // hide virtual keyboard
                    onDone(editText, webView);
                    hideKeyboard(editText);
                    return true;
                }
                return false;

            }
        });

    }

    private void hideKeyboard(EditText editTextURL) {
        InputMethodManager imm = (InputMethodManager) editTextURL.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editTextURL.getWindowToken(), 0);
        }
    }

    private void onDone(EditText editText, VideoEnabledWebView webView) {
        String s = editText.getText().toString();
        boolean validUrl = URLUtil.isValidUrl(s);

        if (!validUrl) {
            s = "http://" + s;
            if (URLUtil.isValidUrl(s)) {
                webView.loadUrl(s);
            }
        } else {
            webView.loadUrl(s);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.requestFocus();
        askForRecordAudioPermission();
        handleExternalSdCard();
    }

    private void handleExternalSdCard() {
        if (!isReadExternalFilesGranted()) {
            requestPermissionForReadExtertalStorage();
        }
    }

    public void requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isReadExternalFilesGranted() {
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void askForRecordAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Need Microphone Permission");
                builder.setMessage("To use voice input feature in your car, you need to grant permissions. With voice input, you can search on Youtube with your voice. \nFor e.g., say 'Coldplay' and it will search for Coldplay on Youtube");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }


        }
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        webView.loadUrl(bookmark.getUrl());
    }

    @Override
    public void onBookmarkAddCurrentPage() {
        BookmarkUtils.addBookmark(webView);
    }

    @Override
    public void onBookmarkDelete(Bookmark bookmark) {
        BookmarkUtils.deleteBookmark(bookmark);
    }

    @Override
    public void onBookmarkFragmentClose() {
        hideBookmarksScreen();
    }

    private void hideBookmarksScreen() {
        if (isAdded()) {
            getView().findViewById(R.id.full_screen_view).setVisibility(View.GONE);
            FragmentManager childFragmentManager = getChildFragmentManager();
            Fragment oldFragment = childFragmentManager.findFragmentByTag(BOOKMARKS_FRAGMENT_TAG);
            if (oldFragment != null) {
                FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
                fragmentTransaction.remove(oldFragment);
                fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
        webView.requestFocus();
    }

    private void showBookmarksScreen() {
        if (isAdded()) {
            getView().findViewById(R.id.full_screen_view).setVisibility(View.VISIBLE);
            FragmentManager childFragmentManager = getChildFragmentManager();
            BookmarksFragment bookmarksFragment = (BookmarksFragment) childFragmentManager.findFragmentByTag(BOOKMARKS_FRAGMENT_TAG);
            if (bookmarksFragment == null) {
                bookmarksFragment = new BookmarksFragment();
            }
            FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.full_screen_view, bookmarksFragment, BOOKMARKS_FRAGMENT_TAG);
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentTransaction.commitAllowingStateLoss();
        }

    }

    private class CustomWebViewClient extends WebViewClient {

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                editText.setText(request.getUrl().toString());
            }

            return super.shouldOverrideUrlLoading(view, request);
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            editText.setText(url);
            if (url.startsWith("intent://")) {
                try {
                    Context context = view.getContext();
                    Intent intent = new Intent().parseUri(url, Intent.URI_INTENT_SCHEME);

                    if (intent != null) {
                        view.stopLoading();

                        PackageManager packageManager = context.getPackageManager();
                        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        if (info != null) {
                            context.startActivity(intent);
                        } else {
                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            view.loadUrl(fallbackUrl);

                        }

                        return true;
                    }
                } catch (URISyntaxException e) {
                    Log.e(TAG, "Can't resolve intent://", e);

                }
            }

            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            editText.setText(url);
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "page finished " + url);
            super.onPageFinished(view, url);
            WebviewUtils.injectNightModeCss(webView, isNightMode);
            WebviewUtils.injectFileListingHack(webView);
        }
    }
}
