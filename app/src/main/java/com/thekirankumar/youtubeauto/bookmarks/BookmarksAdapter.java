package com.thekirankumar.youtubeauto.bookmarks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.utils.AspectRatioFrameLayout;

import java.util.ArrayList;

import io.realm.RealmResults;

/**
 * Created by kiran.kumar on 24/01/18.
 */

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkViewHolder> {
    private ArrayList<Bookmark> bookmarks;
    private Bookmark addCurrentBookmark = new Bookmark();
    private BookmarksClickCallback bookmarksClickCallback;
    private boolean deleteMode;

    public BookmarksAdapter(ArrayList<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
        setHasStableIds(true);
    }

    public void setBookmarksClickCallback(BookmarksClickCallback bookmarksClickCallback) {
        this.bookmarksClickCallback = bookmarksClickCallback;
    }

    @Override
    public long getItemId(int position) {
        if (position < bookmarks.size()) {
            Bookmark bookmark = bookmarks.get(position);
            if(bookmark.isValid()) {
                return bookmark.getCreatedAt();
            }
        }
        return 0;
    }

    @Override
    public BookmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BookmarkViewHolder(parent.getContext(), parent);
    }


    @Override
    public void onBindViewHolder(BookmarkViewHolder holder, int position) {
        if (position < bookmarks.size()) {
            final Bookmark bookmark = bookmarks.get(position);
            holder.setBookmark(bookmark);
            holder.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                bookmarksClickCallback.onBookmarkFragmentClose();
                                return true;
                        }
                        return false;
                    }
                    return false;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!deleteMode && bookmarksClickCallback != null) {
                        bookmarksClickCallback.onBookmarkSelected(bookmark);
                    }
                    if (deleteMode) {
                        deleteMode = false;
                    }
                    notifyDataSetChanged();
                }
            });
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookmarksClickCallback.onBookmarkDelete(bookmark);
                }
            });
            holder.itemView.setLongClickable(true);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (deleteMode) {
                        deleteMode = false;
                    } else {
                        deleteMode = true;
                    }
                    notifyDataSetChanged();
                    return true;
                }
            });
            if (bookmark.isPreventDelete()) {
                holder.setDeleteMode(false);
            } else {
                holder.setDeleteMode(deleteMode);
            }
        } else {
            holder.title.setText(R.string.add_bookmark);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (deleteMode) {
                        deleteMode = false;
                    }
                    notifyDataSetChanged();
                    if (bookmarksClickCallback != null) {
                        bookmarksClickCallback.onBookmarkAddCurrentPage();
                    }
                }
            });
            holder.thumbnail.setImageResource(R.drawable.add_bookmark);
            holder.setDeleteMode(false);
        }

    }

    @Override
    public int getItemCount() {
        return bookmarks.size() + 1;
    }

    public void setBookmarks(ArrayList<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public class BookmarkViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView thumbnail;
        private final ImageView favicon;
        private final ImageView delete;

        public BookmarkViewHolder(Context context, ViewGroup parent) {
            super(((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bookmark_layout, parent, false));
            AspectRatioFrameLayout aspectFrameLayout = (AspectRatioFrameLayout) itemView;
            aspectFrameLayout.setAspectRatio(1);
            aspectFrameLayout.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            title = itemView.findViewById(R.id.bookmark_title);
            thumbnail = itemView.findViewById(R.id.bookmark_thumbnail);
            favicon = itemView.findViewById(R.id.bookmark_favicon);
            delete = itemView.findViewById(R.id.bookmark_delete);
        }

        public void setBookmark(Bookmark bookmark) {
            this.title.setText(bookmark.getTitle());
            byte[] thumbnail = bookmark.getThumbnail();
            int thumbnailResource = bookmark.getThumbnailResource();
            int faviconResource = bookmark.getFaviconResource();
            if (thumbnail != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;
                Bitmap bitmap = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length, options);
                this.thumbnail.setImageBitmap(bitmap);
            } else {
                this.thumbnail.setImageResource(thumbnailResource);
            }
            byte[] favicon = bookmark.getFavicon();
            if (favicon != null) {
                this.favicon.setVisibility(View.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeByteArray(favicon, 0, favicon.length);
                this.favicon.setImageBitmap(bitmap);
            } else if (faviconResource > 0) {
                this.favicon.setVisibility(View.VISIBLE);
                this.favicon.setImageResource(faviconResource);
            } else {
                this.favicon.setVisibility(View.GONE);
                this.favicon.setImageBitmap(null);
            }
        }

        public void setDeleteMode(boolean deleteMode) {
            if (deleteMode) {
                delete.setVisibility(View.VISIBLE);
            } else {
                delete.setVisibility(View.GONE);
            }
        }
    }
}
