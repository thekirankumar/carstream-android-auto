package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

public class ToolbarMode extends RealmObject {
    public static final int MODE_AUTO_HIDE = 0;
    public static final int MODE_ALWAYS_SHOW = 1;

    private int mode = MODE_ALWAYS_SHOW;

    public ToolbarMode(int mode) {
        this.mode = mode;
    }

    public ToolbarMode() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
