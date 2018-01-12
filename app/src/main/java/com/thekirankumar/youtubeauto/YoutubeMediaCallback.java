package com.thekirankumar.youtubeauto;

import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by kiran.kumar on 11/01/18.
 */

class YoutubeMediaCallback extends MediaSessionCompat.Callback {
    private VideoWebView webView;
    private MediaSessionCompat mediaSessionCompat;

    public YoutubeMediaCallback(VideoWebView webView) {
        this.webView = webView;
        //this.mediaSessionCompat = mediaSessionCompat;
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        webView.playNextTrack();
    }

    @Override
    public void onSkipToPrevious() {
        super.onSkipToPrevious();
        webView.goBack();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.pauseVideo();
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
        builder.setActions(getAvailableActions());
        builder.setState(PlaybackStateCompat.STATE_PAUSED, 0, 1);
        //mediaSessionCompat.setPlaybackState(builder.build());
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
    public void onPlay() {
        super.onPlay();
        webView.playVideo();
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
        builder.setActions(getAvailableActions());
        builder.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1);
        //mediaSessionCompat.setPlaybackState(builder.build());
    }

    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        return super.onMediaButtonEvent(mediaButtonEvent);
    }
}
