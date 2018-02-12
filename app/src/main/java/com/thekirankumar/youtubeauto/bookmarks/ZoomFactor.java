package com.thekirankumar.youtubeauto.bookmarks;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 08/02/18.
 */

public class ZoomFactor extends RealmObject {
    private float pageZoom = 1f;
    private float textSize = 1f;

    public ZoomFactor() {
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getPageZoom() {
        return pageZoom;
    }

    public void setPageZoom(float pageZoom) {
        this.pageZoom = pageZoom;
    }
}
