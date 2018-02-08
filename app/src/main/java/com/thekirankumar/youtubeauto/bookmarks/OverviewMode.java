package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

class OverviewMode extends RealmObject {
    public static final int MODE_OVERVIEW_ENABLED = 1;
    public static final int MODE_OVERVIEW_DISABLED = 0;
    private int mode;

    public OverviewMode(int mode) {

        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
