package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

public class UserAgentMode extends RealmObject {
    public static final int MODE_MOBILE = 0;
    public static final int MODE_DESKTOP = 1;
    public static final int MODE_CUSTOM = 2;
    private int mode = MODE_MOBILE;
    private String customUserAgent;

    public UserAgentMode(int mode, String customUserAgent) {
        this.mode = mode;
        this.customUserAgent = customUserAgent;
    }

    public UserAgentMode(int mode) {
        this.mode = mode;
    }

    public UserAgentMode() {
    }

    public int getMode() {
        return mode;
    }

    public String getCustomUserAgent() {
        return customUserAgent;
    }
}
