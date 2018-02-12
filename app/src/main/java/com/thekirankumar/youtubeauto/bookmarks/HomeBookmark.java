package com.thekirankumar.youtubeauto.bookmarks;

/**
 * Created by kiran.kumar on 09/02/18.
 */

public class HomeBookmark implements IBookmark {
    public HomeBookmark() {
       setUrl("https://youtube.com");
    }

    @Override
    public OverrideSettings getOverrideSettings() {
        return new OverrideSettings();
    }

    @Override
    public void setOverrideSettings(OverrideSettings overrideSettings) {

    }

    @Override
    public boolean isPreventDelete() {
        return false;
    }

    @Override
    public void setPreventDelete(boolean preventDelete) {

    }

    @Override
    public int getFaviconResource() {
        return 0;
    }

    @Override
    public void setFaviconResource(int faviconResource) {

    }

    @Override
    public int getThumbnailResource() {
        return 0;
    }

    @Override
    public void setThumbnailResource(int thumbnailResource) {

    }

    @Override
    public long getCreatedAt() {
        return 0;
    }

    @Override
    public void setCreatedAt(long createdAt) {

    }

    @Override
    public byte[] getFavicon() {
        return new byte[0];
    }

    @Override
    public void setFavicon(byte[] favicon) {

    }

    @Override
    public byte[] getThumbnail() {
        return new byte[0];
    }

    @Override
    public void setThumbnail(byte[] thumbnail) {

    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public String getFaviconPath() {
        return null;
    }

    @Override
    public void setFaviconPath(String faviconPath) {

    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public void setUrl(String url) {

    }
}
