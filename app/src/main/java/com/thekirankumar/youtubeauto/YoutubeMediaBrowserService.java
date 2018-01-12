package com.thekirankumar.youtubeauto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Collections;
import java.util.List;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE;

/**
 * Created by kiran.kumar on 11/01/18.
 */

public class YoutubeMediaBrowserService extends MediaBrowserServiceCompat {

    public static final String WEBVIEW_EVENT = "com.thekirankumar.youtubeauto.webview.event";
    public static final String PLAYER_EVENT = "com.thekirankumar.youtubeauto.player.event";
    public static final String ACTION_TYPE = "action_type";
    public static final String MEDIA_TITLE = "title";
    public static final String PLAYBACK_STATE = "state";
    private static final String TAG = YoutubeMediaBrowserService.class.getName();
    public static final String QUERY = "query";
    private MediaSessionCompat mediaSessionCompat;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(this, "youtube");
        mediaSessionCompat.setCallback(new MediaCallBack());
        mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//        try {
//            MediaNotificationManager notificationManager = new MediaNotificationManager(this, mediaSessionCompat.getSessionToken());
//            notificationManager.startNotification(this);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

        setSessionToken(mediaSessionCompat.getSessionToken());
        setup();
    }

    private void setup() {
        mediaSessionCompat.setActive(true);
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
        builder.setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1);
        MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder();
        metadata.putString(METADATA_KEY_TITLE, "Start playing from Youtube app first");
        mediaSessionCompat.setMetadata(metadata.build());
        mediaSessionCompat.setPlaybackState(builder.build());
        final MediaPlayer mMediaPlayer;
        mMediaPlayer = MediaPlayer.create(YoutubeMediaBrowserService.this, R.raw.silent);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayer.release();
            }
        });
        mMediaPlayer.start();
        AudioManager audioManager = (AudioManager)
                getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String title = intent.getStringExtra(MEDIA_TITLE);
                MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder();
                metadata.putString(METADATA_KEY_TITLE, title);
                mediaSessionCompat.setMetadata(metadata.build());
                PlaybackStateCompat.Builder stateCompat = new PlaybackStateCompat.Builder();
                stateCompat.setState(intent.getIntExtra(PLAYBACK_STATE, 0), 0, 1);
                stateCompat.setActions(getAvailableActions());
                mediaSessionCompat.setPlaybackState(stateCompat.build());
            }
        }, new IntentFilter(WEBVIEW_EVENT));


    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        return actions;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("youtube", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        result.sendResult(Collections.<MediaBrowserCompat.MediaItem>emptyList());
    }


    public class MediaCallBack extends MediaSessionCompat.Callback {
        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            BroadcastFromPlayer.broadcastPreviousClicked(YoutubeMediaBrowserService.this);

        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            BroadcastFromPlayer.broadcastNextClicked(YoutubeMediaBrowserService.this);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            BroadcastFromPlayer.broadcastPlayClicked(YoutubeMediaBrowserService.this);
        }

        @Override
        public void onPause() {
            super.onPause();
            BroadcastFromPlayer.broadcastPauseClicked(YoutubeMediaBrowserService.this);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();

        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
            if(query!=null) {
                BroadcastFromPlayer.broadcastVoiceSearch(YoutubeMediaBrowserService.this, query);
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            BroadcastFromPlayer.broadcastPauseClicked(YoutubeMediaBrowserService.this);
        }
    }
}
