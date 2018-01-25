package com.thekirankumar.youtubeauto.bookmarks;

/**
 * Created by kiran.kumar on 24/01/18.
 */

public interface BookmarksClickCallback {
    void onBookmarkSelected(Bookmark bookmark);
    void onBookmarkAddCurrentPage();
    void onBookmarkDelete(Bookmark bookmark);
    void onBookmarkFragmentClose();
}
