package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

class ZoomFactor extends RealmObject {
    private float factor = 1f;

    public ZoomFactor(float factor) {

        this.factor = factor;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }
}
