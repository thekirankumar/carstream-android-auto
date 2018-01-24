package com.thekirankumar.youtubeauto.utils;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

/**
 * Created by kiran.kumar on 02/01/18.
 */

public class MyRecognitionListener implements RecognitionListener {
    private static final String TAG = MyRecognitionListener.class.getName();
    private OnCompleteListener listener;

    public MyRecognitionListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    public MyRecognitionListener() {
    }

    public void setListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        if (listener != null) {
            listener.onEnd();
        }
    }

    @Override
    public void onError(int error) {
        if (listener != null) {
            listener.onError(error);
        }
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (data.size() > 0) {
            String query = data.get(0);
            if (listener != null) {
                listener.onVoiceRecognitionComplete(query);
            }
        } else {
            if (listener != null) {
                listener.onError(0);
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public interface OnCompleteListener {
        void onVoiceRecognitionComplete(String text);

        void onEnd();

        void onError(int error);
    }
}
