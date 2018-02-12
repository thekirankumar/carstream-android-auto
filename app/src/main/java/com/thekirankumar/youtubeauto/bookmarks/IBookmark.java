package com.thekirankumar.youtubeauto.bookmarks;

/**
 * Created by kiran.kumar on 09/02/18.
 */

public interface IBookmark {
    OverrideSettings getOverrideSettings();

    void setOverrideSettings(OverrideSettings overrideSettings);

    boolean isPreventDelete();

    void setPreventDelete(boolean preventDelete);

    int getFaviconResource();

    void setFaviconResource(int faviconResource);

    int getThumbnailResource();

    void setThumbnailResource(int thumbnailResource);

    long getCreatedAt();

    void setCreatedAt(long createdAt);

    byte[] getFavicon();

    void setFavicon(byte[] favicon);

    byte[] getThumbnail();

    void setThumbnail(byte[] thumbnail);

    String getTitle();

    void setTitle(String title);

    String getFaviconPath();

    void setFaviconPath(String faviconPath);

    String getUrl();

    void setUrl(String url);
}
