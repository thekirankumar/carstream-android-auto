package com.thekirankumar.youtubeauto.bookmarks;

import android.support.annotation.IdRes;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 24/01/18.
 */

public class Bookmark extends RealmObject {
    private String title;
    private String faviconPath;
    private @IdRes
    int faviconResource;
    private String url;
    private byte[] thumbnail;
    private byte[] favicon;
    private long createdAt;
    private @IdRes
    int thumbnailResource;
    private boolean preventDelete;
    private OverrideSettings overrideSettings;

    public Bookmark() {
    }

    public Bookmark(String title, String faviconPath, String url) {
        this.title = title;
        this.faviconPath = faviconPath;
        this.url = url;
        this.createdAt = url.hashCode();
    }

    public Bookmark(String title, int faviconRes, String url, int thumbnailRes) {
        this.title = title;
        this.faviconResource = faviconRes;
        this.url = url;
        this.thumbnailResource = thumbnailRes;
        this.preventDelete = true;
        this.createdAt = url.hashCode();
    }

    public OverrideSettings getOverrideSettings() {
        return overrideSettings;
    }

    public void setOverrideSettings(OverrideSettings overrideSettings) {
        this.overrideSettings = overrideSettings;
    }

    public boolean isPreventDelete() {
        return preventDelete;
    }

    public void setPreventDelete(boolean preventDelete) {
        this.preventDelete = preventDelete;
    }

    public int getFaviconResource() {
        return faviconResource;
    }

    public void setFaviconResource(int faviconResource) {
        this.faviconResource = faviconResource;
    }

    public int getThumbnailResource() {
        return thumbnailResource;
    }

    public void setThumbnailResource(int thumbnailResource) {
        this.thumbnailResource = thumbnailResource;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public byte[] getFavicon() {
        return favicon;
    }

    public void setFavicon(byte[] favicon) {
        this.favicon = favicon;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFaviconPath() {
        return faviconPath;
    }

    public void setFaviconPath(String faviconPath) {
        this.faviconPath = faviconPath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
