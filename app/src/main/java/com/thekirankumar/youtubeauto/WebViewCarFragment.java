package com.thekirankumar.youtubeauto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.hardware.CarSensorEvent;
import android.support.car.hardware.CarSensorManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchController;
import com.google.android.apps.auto.sdk.SearchItem;

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
    public static final String GOOGLE_AUTOSUGGEST_URL_BASE = "http://suggestqueries.google.com/complete/search?client=chrome&q=";
    public static final String GOOGLE_SEARCH_URL_BASE = "https://www.google.com/#q=";
    public static final String PREFS = "car";
    public static final String HOME_URL = "home_url";
    private static final String TAG = "WebViewCarFragment";
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
    private SearchMode searchMode = SearchMode.YOUTUBE;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    public WebViewCarFragment() {
        // Required empty public constructor
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
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });
        ImageButton homeButton = view.findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(YOUTUBE_HOME_URL_BASE);
            }
        });
        ImageButton refreshButton = view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        ImageButton voiceButton = view.findViewById(R.id.voice_button);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speechRecognizer != null) {
                    speechRecognizer.stopListening();
                    speechRecognizer = null;
                } else {
                    webView.pauseVideo();
                    final RecognitionProgressView recognitionProgressView = (RecognitionProgressView) view.findViewById(R.id.speech_view);
                    MyRecognitionListener listener = new MyRecognitionListener(new MyRecognitionListener.OnCompleteListener() {
                        @Override
                        public void onVoiceRecognitionComplete(String text) {
                            String encoded = null;
                            try {
                                encoded = URLEncoder.encode(text, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            webView.loadUrl(getSearchUrlBase() + encoded);
                            speechRecognizer = null;
                            recognitionProgressView.stop();
                            recognitionProgressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onEnd() {
                            speechRecognizer = null;
                        }

                        @Override
                        public void onError(int error) {
                            recognitionProgressView.stop();
                            recognitionProgressView.setVisibility(View.GONE);
                        }
                    });
                    Context context = getContext();
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
                    speechRecognizerIntent = new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    speechRecognizer.startListening(speechRecognizerIntent);

                    recognitionProgressView.setVisibility(View.VISIBLE);
                    recognitionProgressView.play();
                    recognitionProgressView.setSpeechRecognizer(speechRecognizer);
                    recognitionProgressView.setRecognitionListener(listener);
                    int[] colors = {
                            ContextCompat.getColor(context, R.color.color1),
                            ContextCompat.getColor(context, R.color.color2),
                            ContextCompat.getColor(context, R.color.color3),
                            ContextCompat.getColor(context, R.color.color4),
                            ContextCompat.getColor(context, R.color.color5)
                    };
                    recognitionProgressView.setColors(colors);
                }
            }
        });


        handleVoiceVisibility();
        ImageButton receiveButton = view.findViewById(R.id.receive_button);
        ImageButton fullScreenButton = view.findViewById(R.id.fullscreen_button);
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
        ImageButton searchYoutubeButton = view.findViewById(R.id.search_youtube_button);
        searchYoutubeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSearchShown) {
                    isSearchShown = true;
                    searchMode = SearchMode.YOUTUBE;
                    carUiController.getSearchController().showSearchBox();
                    carUiController.getSearchController().setSearchHint("YouTube Search");
                } else {
                    isSearchShown = false;
                    carUiController.getSearchController().hideSearchBox();
                    carUiController.getStatusBarController().hideAppHeader();
                }
            }
        });

        ImageButton searchGoogleButton = view.findViewById(R.id.search_google_button);
        searchGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSearchShown) {
                    isSearchShown = true;
                    searchMode = SearchMode.GOOGLE;
                    carUiController.getSearchController().showSearchBox();
                    carUiController.getSearchController().setSearchHint("Google search");
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
                SharedPreferences car = getContext().getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
                String url = car.getString("url", null);
                if (url != null) {
                    webView.loadUrl(url);
                }
            }
        });

        View webViewContainer = view.findViewById(R.id.container);
        ViewGroup fullScreenVideoView = view.findViewById(R.id.full_screen_view);
        webView.setWebViewClient(new CustomWebViewClient());
        VideoEnabledWebChromeClient videoEnabledWebChromeClient = new VideoEnabledWebChromeClient(webViewContainer, fullScreenVideoView, new ProgressBar(getContext()), webView);
        webView.setWebChromeClient(videoEnabledWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
        handler.post(new Runnable() {
            @Override
            public void run() {
                SharedPreferences car = getContext().getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
                final String url = car.getString(HOME_URL, YOUTUBE_HOME_URL_BASE);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(url);
                    }
                });
            }
        });

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

                webView.loadUrl(getSearchUrlBase() + encoded);
                isSearchShown = false;
                carUiController.getSearchController().hideSearchBox();
            }

            @Override
            public boolean onSearchSubmitted(String s) {
                String encoded = null;
                try {
                    encoded = URLEncoder.encode(s, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                webView.loadUrl(getSearchUrlBase() + encoded);
                isSearchShown = false;
                carUiController.getSearchController().hideSearchBox();
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

                        String urlBase = null;
                        if (searchMode == SearchMode.YOUTUBE) {
                            urlBase = YOUTUBE_AUTOSUGGEST_URL_BASE;
                        } else if (searchMode == SearchMode.GOOGLE) {
                            urlBase = GOOGLE_AUTOSUGGEST_URL_BASE;
                        }
                        Request request = new Request.Builder()
                                .url(urlBase + encoded)
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

    private boolean isRecordAudioGranted() {
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    private String getSearchUrlBase() {
        if (searchMode == SearchMode.YOUTUBE) {
            return YOUTUBE_SEARCH_URL_BASE;
        } else if (searchMode == SearchMode.GOOGLE) {
            return GOOGLE_SEARCH_URL_BASE;
        }
        return null;
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
        handleVoiceVisibility();
    }

    private void handleVoiceVisibility() {
        if (getView() != null) {
            ImageButton voiceButton = getView().findViewById(R.id.voice_button);
            if (!isRecordAudioGranted()) {
                voiceButton.setVisibility(View.GONE);
            } else {
                voiceButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        if (webView != null) {
            webView.pauseVideo();
        }
        super.onPause();
    }

    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        webView.destroy();
        handlerThread.quit();
        super.onDestroy();
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            SharedPreferences car = getContext().getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
            car.edit().putString(HOME_URL, url).apply();
        }
    }
}
