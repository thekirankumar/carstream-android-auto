package com.thekirankumar.youtubeauto.bookmarks;

import android.support.annotation.IdRes;

import io.realm.RealmObject;

/**
 * Created by kiran.kumar on 24/01/18.
 */

public class Bookmark extends RealmObject implements IBookmark {
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
    private OverrideSettings overrideSettings = new OverrideSettings();

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

    @Override
    public OverrideSettings getOverrideSettings() {
        return overrideSettings;
    }

    @Override
    public void setOverrideSettings(OverrideSettings overrideSettings) {
        this.overrideSettings = overrideSettings;
    }

    @Override
    public boolean isPreventDelete() {
        return preventDelete;
    }

    @Override
    public void setPreventDelete(boolean preventDelete) {
        this.preventDelete = preventDelete;
    }

    @Override
    public int getFaviconResource() {
        return faviconResource;
    }

    @Override
    public void setFaviconResource(int faviconResource) {
        this.faviconResource = faviconResource;
    }

    @Override
    public int getThumbnailResource() {
        return thumbnailResource;
    }

    @Override
    public void setThumbnailResource(int thumbnailResource) {
        this.thumbnailResource = thumbnailResource;
    }

    @Override
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public byte[] getFavicon() {
        return favicon;
    }

    @Override
    public void setFavicon(byte[] favicon) {
        this.favicon = favicon;
    }

    @Override
    public byte[] getThumbnail() {
        return thumbnail;
    }

    @Override
    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getFaviconPath() {
        return faviconPath;
    }

    @Override
    public void setFaviconPath(String faviconPath) {
        this.faviconPath = faviconPath;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }
}
