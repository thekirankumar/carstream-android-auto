package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

public class OverviewMode extends RealmObject {
    public static final int MODE_OVERVIEW_ENABLED = 1;
    public static final int MODE_OVERVIEW_DISABLED = 0;
    private int mode = MODE_OVERVIEW_DISABLED;

    public OverviewMode(int mode) {
        this.mode = mode;
    }

    public OverviewMode() {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
