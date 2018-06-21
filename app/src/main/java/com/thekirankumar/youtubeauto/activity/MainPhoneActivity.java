package com.thekirankumar.youtubeauto.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.fragments.WebViewCarFragment;
import com.thekirankumar.youtubeauto.exoplayer.ExoPlayerFragment;


public class MainPhoneActivity extends AppCompatActivity implements ExoPlayerFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_main);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onNativePlayerControlsVisibilityChange(int visibility) {

    }

    @Override
    public WebViewCarFragment.AspectRatio getAspectRatio() {
        return WebViewCarFragment.AspectRatio.CONTAIN;
    }
}
