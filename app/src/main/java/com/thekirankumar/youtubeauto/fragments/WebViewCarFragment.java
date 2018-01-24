package com.thekirankumar.youtubeauto.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.CarNotConnectedException;
import android.support.car.hardware.CarSensorEvent;
import android.support.car.hardware.CarSensorManager;
import android.support.car.media.CarAudioManager;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchController;
import com.google.android.apps.auto.sdk.SearchItem;
import com.thekirankumar.youtubeauto.utils.BroadcastFromWebview;
import com.thekirankumar.youtubeauto.webview.JavascriptCallback;
import com.thekirankumar.youtubeauto.activity.MainCarActivity;
import com.thekirankumar.youtubeauto.Manifest;
import com.thekirankumar.youtubeauto.utils.MyExceptionHandler;
import com.thekirankumar.youtubeauto.service.MyMediaBrowserService;
import com.thekirankumar.youtubeauto.utils.MyRecognitionListener;
import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.utils.SearchMode;
import com.thekirankumar.youtubeauto.webview.VideoEnabledWebChromeClient;
import com.thekirankumar.youtubeauto.webview.VideoWebView;
import com.thekirankumar.youtubeauto.utils.WebviewUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WebViewCarFragment extends CarFragment implements MainCarActivity.OnConfigurationChangedListener, JavascriptCallback.JSCallbacks, SafetyWarningFragment.FragmentInteractionListener {
    public static final String YOUTUBE_HOME_URL_BASE = "https://www.youtube.com";
    public static final String YOUTUBE_OFFLINE_URL_BASE = "file:///" + Environment.getExternalStorageDirectory().getPath() + "/";
    public static final String YOUTUBE_SEARCH_URL_BASE = "https://www.youtube.com/results?search_query=";
    public static final String YOUTUBE_AUTOSUGGEST_URL_BASE = "http://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=";
    public static final int AUTOSUGGEST_DEBOUNCE_DELAY_MILLIS = 500;
    public static final String GOOGLE_AUTOSUGGEST_URL_BASE = "http://suggestqueries.google.com/complete/search?client=chrome&q=";
    public static final String GOOGLE_SEARCH_URL_BASE = "https://www.google.com/#q=";
    public static final String PREFS = "car";
    public static final String HOME_URL = "home_url";
    public static final String JAVASCRIPT_INTERFACE = "nativecallbacks";
    public static final String SAFETY_WARNING_FRAGMENT_TAG = "safety";
    private static final String TAG = "WebViewCarFragment";
    private static final String FULLSCREEN_KEY = "fullscreen";
    private static final String ASPECT_RATIO_KEY = "aspect_ratio";
    private HandlerThread handlerThread;
    private Runnable searchRunnable;
    private TextView carSpeedView;
    private boolean isParkingEngaged;
    private boolean isSearchShown = false;
    private CarSensorManager sensorManager;
    private Car car;
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;
    private VideoWebView webView;
    private SearchMode searchMode = SearchMode.YOUTUBE;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private MediaSessionCompat mediaSessionCompat;
    private ProgressBar progressBar;
    private View toolbar;
    private Runnable toolbarHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideToolbar();
        }
    };
    private boolean isNightMode = false;
    private boolean fullScreenRequested;
    private boolean clickFirstVideoAfterPageLoad;
    private boolean warningAccepted;
    private AspectRatio currentAspectRatio;
    private Handler handler;
    private SharedPreferences sharedPrefs;
    private View cornerControls;
    private Runnable cornerControlsHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideCornerControls(true);
        }
    };
    private Button aspectButton;
    private int playerState = PlaybackState.STATE_PAUSED;
    private boolean warningScreenOpen;
    private final CarSensorManager.OnSensorChangedListener mSensorsListener = new CarSensorManager.OnSensorChangedListener() {
        @Override
        public void onSensorChanged(CarSensorManager sensorManager, CarSensorEvent ev) {
            try {
                if (ev.sensorType == CarSensorManager.SENSOR_TYPE_PARKING_BRAKE) {
                    CarSensorEvent.ParkingBrakeData parkingBrakeData = ev.getParkingBrakeData();
                    if (parkingBrakeData != null) {
                        setParkingBrake(parkingBrakeData.isEngaged);
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
    private final CarConnectionCallback mCarConnectionCallback = new CarConnectionCallback() {
        @Override
        public void onConnected(Car car) {
            try {
                CarSensorManager sensorManager = (CarSensorManager) car.getCarManager(Car.SENSOR_SERVICE);
                WebViewCarFragment.this.sensorManager = sensorManager;
                WebViewCarFragment.this.car = car;
                gainAudioFocus();

                sensorManager = (CarSensorManager) car.getCarManager(Car.SENSOR_SERVICE);
                sensorManager.addListener(mSensorsListener, CarSensorManager.SENSOR_TYPE_PARKING_BRAKE,
                        CarSensorManager.SENSOR_RATE_NORMAL);

                if (sensorManager != null && sensorManager.isSensorSupported(CarSensorManager.SENSOR_TYPE_PARKING_BRAKE)) {
                    CarSensorEvent ds = sensorManager.getLatestSensorEvent(CarSensorManager.SENSOR_TYPE_PARKING_BRAKE);
                    if (ds != null) {
                        mSensorsListener.onSensorChanged(sensorManager, ds);
                    } else {
                        setParkingBrake(false);
                    }
                } else {
                    setParkingBrake(false);
                }

                mediaBrowser = new MediaBrowserCompat(getContext(), new ComponentName(getContext(), MyMediaBrowserService.class),
                        new MediaBrowserCompat.ConnectionCallback() {
                            @Override
                            public void onConnected() {
                                try {
                                    MediaSessionCompat.Token token =
                                            mediaBrowser.getSessionToken();
                                    MediaControllerCompat controller =
                                            new MediaControllerCompat(getContext(), token);
                                    controller.registerCallback(new MediaControllerCompat.Callback() {
                                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                        @Override
                                        public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                            super.onPlaybackStateChanged(state);
                                        }
                                    });
                                    mediaController = controller;

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

            } catch (Exception e) {
                e.printStackTrace();
                setParkingBrake(false);
            }
        }

        @Override
        public void onDisconnected(Car car) {
            Log.d(TAG, "Disconnected from car");
        }
    };
    private Integer currentVideoTime;

    public WebViewCarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlerThread = new HandlerThread("autosuggest");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Car mCar = Car.createCar(context, mCarConnectionCallback);
        mCar.connect();
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && webView != null) {
                    long actionType = intent.getLongExtra(MyMediaBrowserService.ACTION_TYPE, 0);
                    if (actionType == PlaybackState.ACTION_PLAY) {
                        webView.playVideo();
                    } else if (actionType == PlaybackState.ACTION_PAUSE) {
                        webView.pauseVideo();
                    } else if (actionType == PlaybackState.ACTION_SKIP_TO_NEXT) {
                        webView.playNextTrack();
                    } else if (actionType == PlaybackState.ACTION_SKIP_TO_PREVIOUS) {
                        webView.goBack();
                    } else if (actionType == PlaybackState.ACTION_PLAY_FROM_SEARCH) {
                        clickFirstVideoAfterPageLoad = true;
                        search(intent.getStringExtra(MyMediaBrowserService.QUERY));
                    }


                }
            }
        }, new IntentFilter(MyMediaBrowserService.PLAYER_EVENT));
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

        webView = view.findViewById(R.id.web_view);
        final MainCarActivity mainCarActivity = (MainCarActivity) getContext();
        mainCarActivity.setIgnoreConfigChanges(512);
        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(mainCarActivity));


        final CarUiController carUiController = mainCarActivity.getCarUiController();
        final SearchController searchController = carUiController.getSearchController();

        webView.addJavascriptInterface(new JavascriptCallback(this), JAVASCRIPT_INTERFACE);
        mainCarActivity.getWindow().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        ImageButton homeButton = view.findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(YOUTUBE_HOME_URL_BASE);
            }
        });
        homeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                webView.loadUrl(YOUTUBE_OFFLINE_URL_BASE);
                return true;
            }
        });
        cornerControls = view.findViewById(R.id.fullscreen_corner_controls);

        aspectButton = view.findViewById(R.id.aspect_button);
        aspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAspectRatio();
                showAndHideCornerControlsAnimation();
                showAndHideToolbarAnimation();
            }
        });
        aspectButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showAndHideCornerControlsAnimation();
                showAndHideToolbarAnimation();
            }
        });
        progressBar = view.findViewById(R.id.progress_bar);
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
                    speechRecognizer.destroy();
                    speechRecognizer = null;
                } else {
                    webView.pauseVideoAndSetFlag();
                    final RecognitionProgressView recognitionProgressView = view.findViewById(R.id.speech_view);
                    recognitionProgressView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            recognitionProgressView.stop();
                            recognitionProgressView.setVisibility(View.GONE);
                        }
                    });
                    MyRecognitionListener listener = new MyRecognitionListener(new MyRecognitionListener.OnCompleteListener() {
                        @Override
                        public void onVoiceRecognitionComplete(String text) {
                            search(text);
                            if (speechRecognizer != null) {
                                speechRecognizer.destroy();
                                speechRecognizer = null;
                            }
                            recognitionProgressView.stop();
                            recognitionProgressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onEnd() {
                        }

                        @Override
                        public void onError(int error) {
                            recognitionProgressView.stop();
                            recognitionProgressView.setVisibility(View.GONE);
                            if (speechRecognizer != null) {
                                speechRecognizer.destroy();
                                speechRecognizer = null;
                            }
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
        final ImageButton fullScreenButton = view.findViewById(R.id.fullscreen_button);
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fullScreenRequested) {
                    fullScreenRequested = false;
                    fullScreenButton.setActivated(false);
                    fullScreenButton.setImageResource(R.drawable.ic_fullscreen);
                    webView.exitFullScreen();
                    SharedPreferences car = getContext().getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
                    car.edit().putBoolean(FULLSCREEN_KEY, false).apply();
                } else {
                    fullScreenRequested = true;
                    fullScreenButton.setActivated(true);
                    webView.requestFullScreen();
                    fullScreenButton.setImageResource(R.drawable.ic_exit_fullscreen);
                    SharedPreferences car = getContext().getSharedPreferences(PREFS, Context.MODE_MULTI_PROCESS);
                    car.edit().putBoolean(FULLSCREEN_KEY, true).apply();
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
                SharedPreferences car = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String url = car.getString("url", null);
                if (url != null) {
                    webView.loadUrl(url);
                }
            }
        });

        toolbar = view.findViewById(R.id.toolbar);
        View webViewContainer = view.findViewById(R.id.webview_container);
        ViewGroup fullScreenVideoView = view.findViewById(R.id.full_screen_view);
        webView.setWebViewClient(new CustomWebViewClient());
        final VideoEnabledWebChromeClient videoEnabledWebChromeClient = new VideoEnabledWebChromeClient(webViewContainer, fullScreenVideoView, new ProgressBar(getContext()), webView);
        webView.setWebChromeClient(videoEnabledWebChromeClient);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        webView.getSettings().setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        webView.getSettings().setAllowFileAccess(true);
        if (isNightMode) {
            webView.setBackgroundColor(Color.BLACK);
        }
        //webView.getSettings().setTextSize(WebSettings.TextSize.LARGER);
        handler.post(new Runnable() {
            @Override
            public void run() {
                final String url = getSharedPrefs().getString(HOME_URL, YOUTUBE_HOME_URL_BASE);
                final boolean fullScreen = getSharedPrefs().getBoolean(FULLSCREEN_KEY, false);
                final int aspectRatio = getSharedPrefs().getInt(ASPECT_RATIO_KEY, 0);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(url);
                        if (fullScreen) {
                            fullScreenRequested = true;
                            fullScreenButton.setActivated(true);
                            fullScreenButton.setImageResource(R.drawable.ic_exit_fullscreen);
                        }
                        if (AspectRatio.values().length > aspectRatio) {
                            setAspectRatio(AspectRatio.values()[aspectRatio]);
                        }
                    }
                });

            }
        });

        webView.requestFocus();
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            if (goBack()) return true;
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            if (webView.isVideoFullscreen()) {
                                showAndHideToolbarAnimation();
                                showAndHideCornerControlsAnimation();
                                return true;
                            }
                            break;
                    }
                    return false;
                }
                return false;
            }
        });


        searchController.setSearchCallback(new SearchCallback() {
            @Override
            public void onSearchItemSelected(SearchItem searchItem) {
                search(String.valueOf(searchItem.getTitle()));
                isSearchShown = false;
                carUiController.getSearchController().hideSearchBox();
            }

            @Override
            public boolean onSearchSubmitted(String s) {
                search(s);
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
                    hideToolbar();
                    hideCornerControls(false);
                } else {
                    showToolbar();
                    hideCornerControls(false);
                }
            }
        });
        videoEnabledWebChromeClient.setVideoTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleToolbarAnimation();
                    toggleCornerControlsAnimation();
                }
                return false;
            }
        });
        mainCarActivity.addConfigurationChangedListener(this);
    }

    private void toggleAspectRatio() {
        int ordinal = currentAspectRatio.ordinal();
        if (ordinal >= AspectRatio.values().length - 1) {
            ordinal = 0;
        } else {
            ordinal++;
        }
        setAspectRatio(AspectRatio.values()[ordinal]);
    }

    private SharedPreferences getSharedPrefs() {
        if (sharedPrefs != null) {
            return sharedPrefs;
        }
        sharedPrefs = getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sharedPrefs;
    }

    private void search(String text) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(text, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        webView.loadUrl(getSearchUrlBase() + encoded);
    }

    private boolean goBack() {
        if (webView.isVideoFullscreen()) {
            webView.exitFullScreen();
            return true;
        }
        if (webView.canGoBack() && webView.hasFocus()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    private void hideToolbar() {
        toolbar.clearAnimation();
        if (toolbar.getTranslationY() == 0) {
            toolbar.animate().setDuration(200).translationY(-toolbar.getMeasuredHeight());
        }
    }

    private void hideCornerControls(boolean animated) {
        cornerControls.clearFocus();
        if (animated) {
            cornerControls.setVisibility(View.VISIBLE);
            cornerControls.clearAnimation();
            if (cornerControls.getTranslationX() == 0) {
                cornerControls.animate().setDuration(200).translationX(cornerControls.getMeasuredWidth());
            }
        } else {
            cornerControls.setTranslationX(500);
        }
    }

    private void showToolbar() {
        toolbar.removeCallbacks(toolbarHideRunnable);
        toolbar.clearAnimation();
        if (toolbar.getTranslationY() < 0) {
            toolbar.animate().setDuration(200).translationY(0);
        }
    }

    private void showCornerControls() {
        cornerControls.setVisibility(View.VISIBLE);
        cornerControls.removeCallbacks(cornerControlsHideRunnable);
        cornerControls.clearAnimation();
        if (cornerControls.getTranslationX() > 0) {
            cornerControls.animate().setDuration(200).translationX(0);
        }
    }

    private void showAndHideToolbarAnimation() {
        toolbar.removeCallbacks(toolbarHideRunnable);
        showToolbar();
        toolbar.postDelayed(toolbarHideRunnable, 3000);
    }

    private void showAndHideCornerControlsAnimation() {
        cornerControls.removeCallbacks(cornerControlsHideRunnable);
        showCornerControls();
        cornerControls.postDelayed(cornerControlsHideRunnable, 3000);
    }

    private void toggleToolbarAnimation() {
        if (toolbar.getTranslationY() == 0) {
            hideToolbar();
        } else {
            showAndHideToolbarAnimation();
        }
    }

    private void toggleCornerControlsAnimation() {
        if (cornerControls.getTranslationX() == 0) {
            hideCornerControls(true);
        } else {
            showAndHideCornerControlsAnimation();
        }
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
            //webView.requestFocus();
            webView.playVideoIfPausedFlag();
        }
        if (car != null) {
            gainAudioFocus();
        }
        handleVoiceVisibility();
    }


    private void gainAudioFocus() {
        final CarAudioManager carAudioManager;
        try {
            carAudioManager = car.getCarManager(CarAudioManager.class);
            final AudioAttributes audioAttributes = carAudioManager.getAudioAttributesForCarUsage(
                    CarAudioManager.CAR_AUDIO_USAGE_MUSIC);
            int ret = carAudioManager.requestAudioFocus(null, audioAttributes,
                    AudioManager.AUDIOFOCUS_GAIN, 0);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void loseAudioFocus() {
        final CarAudioManager carAudioManager;
        try {
            carAudioManager = car.getCarManager(CarAudioManager.class);

            final AudioAttributes audioAttributes = carAudioManager.getAudioAttributesForCarUsage(
                    CarAudioManager.CAR_AUDIO_USAGE_MUSIC);
            carAudioManager.abandonAudioFocus(null, audioAttributes);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
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
            webView.pauseVideoAndSetFlag();
        }

        super.onPause();
        loseAudioFocus();
        final String url = webView.getUrl();

        handler.post(new Runnable() {
            @Override
            public void run() {
                getSharedPrefs().edit().putString(HOME_URL, url).apply();

            }
        });
        //BroadcastFromWebview.setEnableBroadcast(getContext(), true);
    }

    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        if (handlerThread != null) {
            handlerThread.quit();
        }
        if (mediaSessionCompat != null) {
            mediaSessionCompat.release();
        }
        if (car != null && car.isConnected()) {
            car.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final MainCarActivity mainCarActivity = (MainCarActivity) getContext();
        mainCarActivity.removeConfigurationChangedListener(this);

    }

    @Override
    public void onConfigChanged() {
        isNightMode = getResources().getBoolean(R.bool.isNight);
        WebviewUtils.injectNightModeCss(webView, isNightMode);
    }

    @Override
    public void onJSVideoEvent(String event) {
        switch (event) {
            case "playing":
                playerState = PlaybackState.STATE_PLAYING;
                BroadcastFromWebview.broadCastPlaying(getContext(), webView.getTitle());
                break;
            case "pause":
                playerState = PlaybackState.STATE_PAUSED;
                BroadcastFromWebview.broadCastPaused(getContext(), webView.getTitle());
                break;
            case "waiting":
                BroadcastFromWebview.broadCastLoading(getContext());
                break;
            case "error":
                BroadcastFromWebview.broadCastError(getContext(), webView.getTitle());
                break;
        }
    }

    @Override
    public void onVideoElementDiscovered() {
        if (fullScreenRequested) {
            webView.requestFullScreen();
        }
        if (currentAspectRatio != null) {
            webView.setAspectRatio(currentAspectRatio.name().toLowerCase());
        }
        webView.attachVideoListeners();
        if (currentVideoTime != null) {
            webView.seekBySeconds(currentVideoTime);
            currentVideoTime = null;
        }
    }

    @Override
    public void onVideoCurrentTimeResult(int currentTime) {
        this.currentVideoTime = currentTime;
    }

    public void setParkingBrake(boolean parkingBrake) {
        this.isParkingEngaged = parkingBrake;
        if (!isParkingEngaged && !warningAccepted) {
            showWarningScreen();
        } else {
            hideWarningScreen();
        }
    }

    private void hideWarningScreen() {
        if (isAdded()) {
            webView.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            FragmentManager childFragmentManager = getChildFragmentManager();
            Fragment oldFragment = childFragmentManager.findFragmentByTag(SAFETY_WARNING_FRAGMENT_TAG);
            if (oldFragment != null) {
                warningScreenOpen = false;
                FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
                fragmentTransaction.remove(oldFragment);
                fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
        webView.requestFocus();
    }

    private void showWarningScreen() {
        if (isAdded()) {
            webView.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);

            warningScreenOpen = true;
            FragmentManager childFragmentManager = getChildFragmentManager();
            SafetyWarningFragment warningFragment = (SafetyWarningFragment) childFragmentManager.findFragmentByTag(SAFETY_WARNING_FRAGMENT_TAG);
            if (warningFragment == null) {
                warningFragment = new SafetyWarningFragment();
            }
            FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.safety_container, warningFragment, SAFETY_WARNING_FRAGMENT_TAG);
            fragmentTransaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
            fragmentTransaction.commitAllowingStateLoss();
        }

    }

    @Override
    public void onReadyToExitSafetyInstructions(SafetyWarningFragment warningFragment) {
        warningAccepted = true;
        hideWarningScreen();
    }

    public void setAspectRatio(AspectRatio aspectRatio) {
        this.currentAspectRatio = aspectRatio;
        this.aspectButton.setText(getHumanText(aspectRatio));
        webView.setAspectRatio(currentAspectRatio.name().toLowerCase());
        getSharedPrefs().edit().putInt(ASPECT_RATIO_KEY, currentAspectRatio.ordinal()).apply();
    }

    private String getHumanText(AspectRatio aspectRatio) {
        switch (aspectRatio) {
            case FILL:
                return getString(R.string.aspect_ratio_fill);
            case COVER:
                return getString(R.string.aspect_ratio_cover);
            case CONTAIN:
                return getString(R.string.aspect_ratio_contain);
        }
        return "";
    }

    private enum AspectRatio {
        CONTAIN, FILL, COVER
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            BroadcastFromWebview.broadCastLoading(getContext());
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (warningScreenOpen && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                    onReadyToExitSafetyInstructions(null);
                } else {
                    if (webView.isVideoFullscreen()) {
                        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                            showToolbar();
                            showCornerControls();
                            cornerControls.requestFocus(View.FOCUS_UP);
                        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                            cornerControls.clearFocus();
                            hideToolbar();
                            hideCornerControls(true);
                        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                            if (playerState == PlaybackState.STATE_PLAYING) {
                                webView.pauseVideo();
                            } else {
                                webView.playVideo();
                            }
                        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            webView.seekBySeconds(+10);
                        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                            webView.seekBySeconds(-10);
                        }
                    }
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isNightMode = getResources().getBoolean(R.bool.isNight);
            WebviewUtils.injectNightModeCss(webView, isNightMode);
            progressBar.setVisibility(View.GONE);
            getSharedPrefs().edit().putString(HOME_URL, url).commit();
            webView.discoverVideoElements();
            WebviewUtils.injectFileListingHack(webView);
            BroadcastFromWebview.broadcastTitle(getContext(), view.getTitle());

            if (clickFirstVideoAfterPageLoad) {
                clickFirstVideoAfterPageLoad = false;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.findAndClickFirstVideo();
                    }
                }, 1000);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            BroadcastFromWebview.broadCastError(getContext(), error.toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("intent://")) {
                try {
                    Context context = view.getContext();
                    new Intent();
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
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
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
