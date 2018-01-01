package com.thekirankumar.youtubeauto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;



public class WebViewPhoneFragment extends CarFragment {
    private final String TAG = "WebViewCarFragment";
    private VideoEnabledWebView webView;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview_phone, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.phone_menu, menu);
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
            SharedPreferences car = getActivity().getSharedPreferences("car", Context.MODE_MULTI_PROCESS);
            car.edit().putString("url", webView.getUrl()).commit();
            Toast.makeText(getActivity(), "Page bookmarked", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Goto Android Auto App and click 'Receive from phone' to load this page", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView = view.findViewById(R.id.web_view);
        ViewGroup webViewContainer = view.findViewById(R.id.container);
        ViewGroup fullScreenView = view.findViewById(R.id.full_screen_view);
        webView.setWebViewClient(new WebViewClient());
        VideoEnabledWebChromeClient videoEnabledWebChromeClient = new VideoEnabledWebChromeClient(webViewContainer, fullScreenView, new ProgressBar(getActivity()), webView);
        webView.setWebChromeClient(videoEnabledWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://youtube.com");
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

    }

    @Override
    public void onResume() {
        super.onResume();
        webView.requestFocus();
    }


}
