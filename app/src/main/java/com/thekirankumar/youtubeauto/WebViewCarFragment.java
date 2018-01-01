package com.thekirankumar.youtubeauto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.CarNotConnectedException;
import android.support.car.hardware.CarSensorEvent;
import android.support.car.hardware.CarSensorManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchController;
import com.google.android.apps.auto.sdk.SearchItem;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WebViewCarFragment extends CarFragment {
    public static final String YOUTUBE_HOME_URL_BASE = "https://youtube.com";
    public static final String YOUTUBE_SEARCH_URL_BASE = "https://www.youtube.com/results?search_query=";
    public static final String YOUTUBE_AUTOSUGGEST_URL_BASE = "http://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=";
    public static final int AUTOSUGGEST_DEBOUNCE_DELAY_MILLIS = 500;
    private final String TAG = "WebViewCarFragment";
    private HandlerThread handlerThread;
    private Runnable searchRunnable;
    private TextView carSpeedView;
    private final CarSensorManager.OnSensorChangedListener mSensorsListener = new CarSensorManager.OnSensorChangedListener() {
        @Override
        public void onSensorChanged(CarSensorManager sensorManager, CarSensorEvent ev) {
            try {
                if (ev.sensorType == CarSensorManager.SENSOR_TYPE_PARKING_BRAKE) {
                    CarSensorEvent.ParkingBrakeData parkingBrakeData = ev.getParkingBrakeData();
                    if (parkingBrakeData != null) {
                        // Do something with this
                        boolean isParkingEngaged = parkingBrakeData.isEngaged;
                    }
                } else if (ev.sensorType == CarSensorManager.SENSOR_TYPE_CAR_SPEED) {
                    CarSensorEvent.CarSpeedData carSpeedData = ev.getCarSpeedData();
                    if (carSpeedData != null) {
                        float carSpeed = carSpeedData.carSpeed;
                        if (carSpeedView != null) {
                            carSpeedView.setText(Math.round(carSpeed));
                        }

                    }
                }
            } catch (Error e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private boolean isSearchShown = false;
    private CarSensorManager sensorManager;
    private final CarConnectionCallback mCarConnectionCallback = new CarConnectionCallback() {
        @Override
        public void onConnected(Car car) {
            try {
                CarSensorManager sensorManager = (CarSensorManager) car.getCarManager(Car.SENSOR_SERVICE);
                WebViewCarFragment.this.sensorManager = sensorManager;
                refreshParkingBrakeSensor(sensorManager);
                refreshSpeedSensor(sensorManager);
            } catch (Exception e) {
                Log.w(TAG, "Error setting up car connection", e);
            }
        }

        @Override
        public void onDisconnected(Car car) {
            Log.d(TAG, "Disconnected from car");
        }
    };
    private VideoEnabledWebView webView;

    public WebViewCarFragment() {
        // Required empty public constructor
    }

    private void refreshParkingBrakeSensor(CarSensorManager sensorManager) throws CarNotConnectedException {
        sensorManager.addListener(mSensorsListener, CarSensorManager.SENSOR_TYPE_PARKING_BRAKE,
                CarSensorManager.SENSOR_RATE_NORMAL);
        CarSensorEvent ds = sensorManager.getLatestSensorEvent(CarSensorManager.SENSOR_TYPE_PARKING_BRAKE);
        if (ds != null) {
            mSensorsListener.onSensorChanged(sensorManager, ds);
        }
    }

    private void refreshSpeedSensor(CarSensorManager sensorManager) throws CarNotConnectedException {
        sensorManager.addListener(mSensorsListener, CarSensorManager.SENSOR_TYPE_CAR_SPEED,
                CarSensorManager.SENSOR_RATE_NORMAL);
        CarSensorEvent ds = sensorManager.getLatestSensorEvent(CarSensorManager.SENSOR_TYPE_CAR_SPEED);
        if (ds != null) {
            mSensorsListener.onSensorChanged(sensorManager, ds);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Car mCar = Car.createCar(context, mCarConnectionCallback);
        mCar.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webview_car, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //FirebaseAnalytics.getInstance(getContext()).logEvent("CarFragment_created", null);

        final MainCarActivity mainCarActivity = (MainCarActivity) getContext();
        final CarUiController carUiController = mainCarActivity.getCarUiController();
        final SearchController searchController = carUiController.getSearchController();
        handlerThread = new HandlerThread("autosuggest");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        webView = view.findViewById(R.id.web_view);
        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });
        Button refreshButton = view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        Button receiveButton = view.findViewById(R.id.receive_button);
        Button fullScreenButton = view.findViewById(R.id.fullscreen_button);
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.isVideoFullscreen()) {
                    webView.exitFullScreen();
                } else {
                    webView.requestFullScreen();
                }
            }
        });
        Button searchButton = view.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSearchShown) {
                    isSearchShown = true;
                    carUiController.getSearchController().showSearchBox();
                } else {
                    isSearchShown = false;
                    carUiController.getSearchController().hideSearchBox();
                    carUiController.getStatusBarController().hideAppHeader();
                }
            }
        });


        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences car = getContext().getSharedPreferences("car", Context.MODE_MULTI_PROCESS);
                String url = car.getString("url", null);
                if (url != null) {
                    webView.loadUrl(url);
                }
            }
        });

        View webViewContainer = view.findViewById(R.id.container);
        ViewGroup fullScreenVideoView = view.findViewById(R.id.full_screen_view);
        webView.setWebViewClient(new WebViewClient());
        VideoEnabledWebChromeClient videoEnabledWebChromeClient = new VideoEnabledWebChromeClient(webViewContainer, fullScreenVideoView, new ProgressBar(getContext()), webView);
        webView.setWebChromeClient(videoEnabledWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
        webView.loadUrl(YOUTUBE_HOME_URL_BASE);
        webView.requestFocus();
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (webView.canGoBack() && webView.hasFocus()) {
                                webView.goBack();
                                return true;
                            }
                            break;
                    }
                }
                return false;
            }
        });


        searchController.setSearchCallback(new SearchCallback() {
            @Override
            public void onSearchItemSelected(SearchItem searchItem) {
                String encoded = null;
                try {
                    encoded = URLEncoder.encode(String.valueOf(searchItem.getTitle()), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                webView.loadUrl(YOUTUBE_SEARCH_URL_BASE + encoded);

            }

            @Override
            public boolean onSearchSubmitted(String s) {
                String encoded = null;
                try {
                    encoded = URLEncoder.encode(s, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                webView.loadUrl(YOUTUBE_SEARCH_URL_BASE + encoded);

                return true;
            }

            @Override
            public void onSearchTextChanged(final String s) {
                handler.removeCallbacks(searchRunnable);
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        String encoded = null;
                        try {
                            encoded = URLEncoder.encode(s, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        OkHttpClient client = new OkHttpClient();

                        Request request = new Request.Builder()
                                .url(YOUTUBE_AUTOSUGGEST_URL_BASE + encoded)
                                .build();
                        final ArrayList<SearchItem> results = new ArrayList<>();
                        try {
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                JSONArray jsonArray = new JSONArray(response.body().string());
                                Object o = jsonArray.get(1);
                                if (o != null && o instanceof JSONArray) {
                                    for (int i = 0; i < ((JSONArray) o).length(); i++) {
                                        String autoSuggest = (String) ((JSONArray) o).get(i);
                                        SearchItem.Builder builder = new SearchItem.Builder();
                                        builder.setTitle(autoSuggest);
                                        results.add(builder.build());
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                searchController.setSearchItems(results);
                            }
                        });
                    }
                };
                handler.postDelayed(searchRunnable, AUTOSUGGEST_DEBOUNCE_DELAY_MILLIS);
            }
        });
        carUiController.getStatusBarController().hideAppHeader();
        videoEnabledWebChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                if (fullscreen) {
                    searchController.hideSearchBox();
                    carUiController.getStatusBarController().hideAppHeader();
                    carUiController.getStatusBarController().hideConnectivityLevel();
                }
            }
        });


    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (webView != null) {
            webView.requestFocus();
            webView.playVideoIfPaused();
        }
    }

    @Override
    public void onPause() {
        if (webView != null) {
            webView.pauseVideo();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        handlerThread.quit();
        super.onDestroy();
    }

}
