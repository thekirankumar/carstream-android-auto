package com.thekirankumar.youtubeauto.exoplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.TextInformationFrame;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.fragments.WebViewCarFragment;
import com.thekirankumar.youtubeauto.mediaplayer.PlayerFocusHelper;
import com.thekirankumar.youtubeauto.service.MyMediaBrowserService;
import com.thekirankumar.youtubeauto.utils.BroadcastFromUI;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExoPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ExoPlayerFragment extends Fragment implements Player.EventListener {

    private static final String FILE_PATH = "file_path";
    private OnFragmentInteractionListener mListener;
    private String filePath;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private Uri fileUri;
    private TextView titleView;
    private PlayerQueue playerQueue;
    private TextView albumView;
    private PlayerFocusHelper playerFocusHelper;
    private boolean wasPlayingInBackground;

    public ExoPlayerFragment() {
        // Required empty public constructor
    }

    public static ExoPlayerFragment newInstance(String filePath) {
        Bundle args = new Bundle();
        args.putString(FILE_PATH, filePath);
        ExoPlayerFragment fragment = new ExoPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String filePath = getArguments().getString(FILE_PATH);
        this.filePath = filePath;
        this.fileUri = Uri.parse(filePath);
    }

    public void setAspectRatio(WebViewCarFragment.AspectRatio aspectRatio) {
        if (aspectRatio == WebViewCarFragment.AspectRatio.CONTAIN) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else if (aspectRatio == WebViewCarFragment.AspectRatio.FILL) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        } else if (aspectRatio == WebViewCarFragment.AspectRatio.COVER) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exo_player, container, false);
    }

    @Override
    public void onPause() {
        super.onPause();
        wasPlayingInBackground = player.getPlayWhenReady();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wasPlayingInBackground) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleView = view.findViewById(R.id.player_title);
        albumView = view.findViewById(R.id.player_album);
        playerView = view.findViewById(R.id.exoplayer_view);
        playerView.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                titleView.setVisibility(visibility);
                albumView.setVisibility(visibility);
                mListener.onNativePlayerControlsVisibilityChange(visibility);
            }
        });
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(getContext()),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        playerView.setPlayer(player);
        playerView.requestFocus();
        player.setPlayWhenReady(true);
        playerQueue = new PlayerQueue(filePath);
        MediaSource mediaSource = buildMediaSource(playerQueue);
        player.seekToDefaultPosition(playerQueue.currentIndex());
        player.prepare(mediaSource, true, false);
        player.addListener(this);
        setAspectRatio(mListener.getAspectRatio());
        playerFocusHelper = new PlayerFocusHelper(getContext()) {
            @Override
            protected void onStop() {
                super.onStop();
                player.setPlayWhenReady(false);
            }

            @Override
            protected void onPause() {
                super.onPause();
                player.setPlayWhenReady(false);
            }

            @Override
            public boolean isPlaying() {
                return player.getPlayWhenReady();
            }

            @Override
            protected void onPlay() {
                super.onPlay();
                player.setPlayWhenReady(true);
            }
        };


    }


    private MediaSource buildMediaSource(PlayerQueue playerQueue) {
        ArrayList<MediaSource> mediaSources = new ArrayList<>();
        File[] currentQueue = playerQueue.getCurrentQueue();
        for (File file : currentQueue) {
            Uri fileUri = Uri.fromFile(file);
            String userAgent = Util.getUserAgent(getContext(), "CarStream");
            if (file != null && (file.getName().endsWith(".m3u") || file.getName().endsWith(".m3u8"))) {
                Handler mHandler = new Handler();
                DefaultDataSourceFactory defaultDataSourceFactory = new DefaultDataSourceFactory(getContext(), userAgent);
                HlsMediaSource mediaSource = new HlsMediaSource(fileUri, defaultDataSourceFactory, 1800000,
                        mHandler, null);
                mediaSources.add(mediaSource);
            } else {
                ExtractorMediaSource extractorMediaSource = new ExtractorMediaSource(fileUri,
                        new DefaultDataSourceFactory(getContext(), userAgent),
                        new DefaultExtractorsFactory(), null, null);
                mediaSources.add(extractorMediaSource);
            }
        }
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(mediaSources.toArray(new MediaSource[mediaSources.size()]));
        return concatenatingMediaSource;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Object parent = getParentFragment();
        if (parent == null) {
            parent = getContext();
        }
        if (parent instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) parent;
        } else {
            throw new RuntimeException(parent
                    + " must implement OnFragmentInteractionListener");
        }
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && player != null) {
                    long actionType = intent.getLongExtra(MyMediaBrowserService.ACTION_TYPE, 0);
                    if (actionType == PlaybackState.ACTION_PLAY) {
                        player.setPlayWhenReady(true);
                    } else if (actionType == PlaybackState.ACTION_PAUSE) {
                        player.setPlayWhenReady(false);
                    } else if (actionType == PlaybackState.ACTION_SKIP_TO_NEXT) {
                        if (playerQueue.hasNext()) {
                            playerQueue.next();
                            player.seekToDefaultPosition(playerQueue.currentIndex());
                        }
                    } else if (actionType == PlaybackState.ACTION_SKIP_TO_PREVIOUS) {
                        if (playerQueue.hasPrevious()) {
                            playerQueue.previous();
                            player.seekToDefaultPosition(playerQueue.currentIndex());
                        }
                    } else if (actionType == PlaybackState.ACTION_PLAY_FROM_SEARCH) {

                    }


                }
            }
        }, new IntentFilter(MyMediaBrowserService.PLAYER_EVENT));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        player.stop();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        playerQueue.resetPosition(player.getCurrentWindowIndex());
        String album = null;
        String title = null;
        for (int i = 0; i < trackGroups.length; i++) {
            TrackGroup trackGroup = trackGroups.get(i);
            for (int j = 0; j < trackGroup.length; j++) {
                Metadata trackMetadata = trackGroup.getFormat(j).metadata;
                if (trackMetadata != null) {
                    for (int k = 0; k < trackMetadata.length(); k++) {
                        Metadata.Entry entry = trackMetadata.get(k);
                        if (entry instanceof TextInformationFrame) {
                            TextInformationFrame textInformationFrame = (TextInformationFrame) entry;
                            String id = textInformationFrame.id;
                            if (id != null && id.equals("TALB")) {
                                album = textInformationFrame.value;
                            } else if (id != null && id.equals("TIT2")) {
                                title = textInformationFrame.value;
                            }

                        }
                    }
                }
            }
        }
        if (title != null || album != null) {
            if (title != null) {
                titleView.setText(title);
            }
            if (album != null) {
                albumView.setText(album);
            }
            BroadcastFromUI.broadcastTitle(getContext(), title);
        } else {
            File file = new File(playerQueue.current());
            titleView.setText(file.getName());
            albumView.setText(file.getParent());
        }
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            BroadcastFromUI.broadCastLoading(getContext());
        } else if (playbackState == Player.STATE_ENDED) {
            BroadcastFromUI.broadCastPaused(getContext(), String.valueOf(titleView.getText()));
        } else if (playWhenReady && playbackState == Player.STATE_READY) {
            BroadcastFromUI.broadCastPlaying(getContext(), String.valueOf(titleView.getText()));
            playerFocusHelper.play();
        } else if (playWhenReady) {
            BroadcastFromUI.broadCastLoading(getContext());
        } else {
            BroadcastFromUI.broadCastPaused(getContext(), String.valueOf(titleView.getText()));
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        titleView.setText(error.getLocalizedMessage());
        albumView.setText("Error");
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        playerQueue.resetPosition(player.getCurrentWindowIndex());
        File file = new File(playerQueue.current());
        titleView.setText(file.getName());
        albumView.setText(file.getParent());
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onNativePlayerControlsVisibilityChange(int visibility);

        WebViewCarFragment.AspectRatio getAspectRatio();
    }
}
