package com.thekirankumar.youtubeauto.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by kiran.kumar on 11/01/18.
 */

public class VideoWebView extends VideoEnabledWebView {
    private float lastDownY;
    private float lastDownX;
    private float TOUCH_TOLERANCE = 20;

    public VideoWebView(Context context) {
        super(context);
        init();
    }

    public VideoWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
            showKeyboardIfInput();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastDownX = event.getX();
            lastDownY = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (Math.abs(lastDownX - event.getX()) < TOUCH_TOLERANCE && Math.abs(lastDownY - event.getY()) < TOUCH_TOLERANCE)
                showKeyboardIfInput();
        }
        return super.onTouchEvent(event);
    }

    public void playNextTrack() {
        loadUrl("javascript:var v = document.querySelector(\"video\"); v.currentTime = v.getDuration();\n" +
                "var next = document.querySelectorAll('a[aria-label=\"Play next video\"]'); next[0].click();\n");
    }

    public void pauseVideo() {
        loadUrl("javascript:var v = document.querySelector(\"video\"); v.pause();");
    }

    public void playVideo() {
        loadUrl("javascript:var v = document.querySelector(\"video\"); v.play();");
    }

    public void scrollActiveElementIntoView(boolean alignToTop) {
        loadUrl("javascript:setTimeout(function() { const element = document.activeElement;\n" +
                "const elementRect = element.getBoundingClientRect();\n" +
                "const absoluteElementTop = elementRect.top + window.pageYOffset;\n" +
                "const middle = absoluteElementTop - (window.innerHeight / 2);\n" +
                "window.scrollTo(0, middle);},500);");
    }

    public void requestFullScreen() {
        loadUrl("javascript: var vid = document.querySelector('video');  " +
                "if (vid.requestFullscreen) {\n" +
                "      vid.requestFullscreen();\n" +
                "    } else if (vid.mozRequestFullScreen) {\n" +
                "      vid.mozRequestFullScreen();\n" +
                "    } else if (vid.webkitRequestFullscreen) {\n" +
                "      vid.webkitRequestFullscreen();\n" +
                "    }");
    }

    public void discoverVideoElements() {
        loadUrl("javascript: function waitForElementToDisplay(selector, time, fn) {\n" +
                "var el = document.querySelector(selector);" +
                "if(el!=null) {" +
                "            fn(el);" +
                "            return;" +
                "        }" +
                "        else {" +
                "            setTimeout(function() {" +
                "                waitForElementToDisplay(selector, time, fn);" +
                "            }, time);\n" +
                "        }" +
                "    }" +
                "waitForElementToDisplay('video', 1000, function(el) {" +
                "window.nativecallbacks.onVideoDiscovered();" +
                "})");

    }

    public void playVideoIfPausedFlag() {
        loadUrl("javascript: " +
                "if(window.pausedDueToMinimize) {" +
                "var el = document.getElementsByTagName('video')[0];" +
                "    el.play();" +
                "window.pausedDueToMinimize = false;" +
                "}");
    }

    public void pauseVideoAndSetFlag() {
        loadUrl("javascript: var el = document.getElementsByTagName('video')[0];\n" +
                "if(!el.paused) {" +
                "el.pause();" +
                "window.pausedDueToMinimize = true;" +
                "}" +
                "");
    }

    public void attachVideoListeners() {
        String s = ("javascript: var el = document.querySelector('video'); " +
                "el.addEventListener('playing', function(e){window.nativecallbacks.onVideoEvent('playing');});" +
                "el.addEventListener('pause', function(e){window.nativecallbacks.onVideoEvent('pause');});" +
                "el.addEventListener('progress', function(e){window.nativecallbacks.onVideoEvent('progress');});" +
                "el.addEventListener('waiting', function(e){window.nativecallbacks.onVideoEvent('waiting');});" +
                "");
        loadUrl(s);
    }

    public void findAndClickFirstVideo() {
        String s = ("javascript: var el = document.querySelector(\"a[href^='/watch'\"); " +
                "el.click();" +
                "");
        loadUrl(s);
    }

    public void setAspectRatio(String mode) {
        String s = "javascript:document.querySelector('video').setAttribute('style','object-fit:" + mode + "');";
        loadUrl(s);
    }

    public void showKeyboardIfInput() {
        String s = "javascript:setTimeout(function() {" +
                "if(document.activeElement instanceof HTMLInputElement) {" +
                "window.nativecallbacks.showKeyboard(document.activeElement.value);" +
                "} else if(document.activeElement.getAttribute(\"contenteditable\") == \"true\") {" +
                "window.nativecallbacks.showKeyboard(document.activeElement.innerText);" +
                "} else {" +
                "" +
                "}" +
                "}, 500);"; // delay so that document.activeElement gets set
        loadUrl(s);
    }

    public void sendKeyboardEnterEvent() {
        loadUrl("javascript: var keyboardEvent = document.createEvent(\"KeyboardEvent\");\n" +
                "var initMethod = typeof keyboardEvent.initKeyboardEvent !== 'undefined' ? \"initKeyboardEvent\" : \"initKeyEvent\";\n" +
                "keyboardEvent[initMethod](\n" +
                "                   \"keyup\"," +
                "                    true, " +
                "                    true, " +
                "                    window, " +
                "                    false, " +
                "                    false, " +
                "                    false, " +
                "                    false, " +
                "                    13, " +
                "                    0" +
                ");\n" +
                "document.activeElement.dispatchEvent(keyboardEvent);");
    }


    public void seekBySeconds(int seconds) {
        loadUrl("javascript:var v = document.querySelector(\"video\"); v.currentTime = v.currentTime + " + seconds + ";");
    }

    public void getCurrentVideoTimeResult() {
        loadUrl("javascript:var v = document.querySelector(\"video\"); window.nativecallbacks.onVideoCurrentTimeResult(v.currentTime);");
    }

    public void enterEditText(String s) {
        String js = "var changeEvent = new Event('change'); " +
                "if(document.activeElement.isContentEditable)" +
                "{" +
                "document.activeElement.innerText = \"" + s + "\";" +
                "}" +
                "" +
                "else " +
                "{" +
                "document.activeElement.value = \"" + s + "\";" +
                "} " +
                "document.activeElement.dispatchEvent(changeEvent);";
        loadUrl("javascript:" + js);

    }
}
