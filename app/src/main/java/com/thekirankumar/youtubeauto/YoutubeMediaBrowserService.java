package com.thekirankumar.youtubeauto;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE;

/**
 * Created by kiran.kumar on 11/01/18.
 */

public class YoutubeMediaBrowserService extends MediaBrowserServiceCompat {

    private MediaSessionCompat mediaSessionCompat;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSessionCompat = new MediaSessionCompat(this, "youtube");
        mediaSessionCompat.setCallback(new MediaCallBack());

        mediaSessionCompat.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        try {
            MediaNotificationManager notificationManager = new MediaNotificationManager(this, mediaSessionCompat.getSessionToken());
            notificationManager.startNotification(this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        AudioManager audioManager = (AudioManager)
                getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result != AudioManager.AUDIOFOCUS_GAIN) {
            return; //Failed to gain audio focus
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaSessionCompat.setActive(true);
                PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
                builder.setActions(
                        PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE |
                                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID | PlaybackState.ACTION_PAUSE |
                                PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS);
                builder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1);
                MediaMetadataCompat.Builder metadata = new MediaMetadataCompat.Builder();
                metadata.putString(METADATA_KEY_TITLE, "Youtube Auto");
                mediaSessionCompat.setMetadata(metadata.build());
                setSessionToken(mediaSessionCompat.getSessionToken());
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
            }
        },2000);

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



    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("youutube",new Bundle());
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d("", "onLoadChildren() called with: parentId = [" + parentId + "], result = [" + result + "]");
    }


    public class MediaCallBack extends MediaSessionCompat.Callback{
        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();

        }
    }
}
