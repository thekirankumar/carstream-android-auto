package com.thekirankumar.youtubeauto.bookmarks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thekirankumar.youtubeauto.R;
import com.thekirankumar.youtubeauto.utils.GridAutofitLayoutManager;
import com.thekirankumar.youtubeauto.utils.MemoryStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kiran.kumar on 24/01/18.
 */

public class BookmarksFragment extends Fragment implements BookmarksClickCallback {
    public static final float BOOKMARK_VIEW_SIZE_IN_DP = 180;
    private BookmarksClickCallback listener;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bookmarks_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBookmarkFragmentClose();
            }
        });
        toolbar.setTitle(R.string.bookmarks);

        recyclerView = view.findViewById(R.id.recycler_view);
        Resources r = view.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOOKMARK_VIEW_SIZE_IN_DP, r.getDisplayMetrics());

        recyclerView.setLayoutManager(new GridAutofitLayoutManager(getContext(), (int) px));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Bookmark> bookmarks = realm.where(Bookmark.class).sort("createdAt").findAll();
        if (bookmarks.size() == 0) {
            handleEmptyBookmarks(realm);
            bookmarks = realm.where(Bookmark.class).findAll();
        }
        final BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(bookmarks);
        bookmarksAdapter.setBookmarksClickCallback(this);
        recyclerView.setAdapter(bookmarksAdapter);
        bookmarks.addChangeListener(new OrderedRealmCollectionChangeListener<RealmResults<Bookmark>>() {
            @Override
            public void onChange(@NonNull RealmResults<Bookmark> bookmarks, @javax.annotation.Nullable OrderedCollectionChangeSet changeSet) {
                bookmarksAdapter.setBookmarks(bookmarks);
                bookmarksAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.requestFocus();
    }

    private void handleEmptyBookmarks(final Realm realm) {
        final ArrayList<Bookmark> bookmarks = new ArrayList<>();
        bookmarks.add(new Bookmark("YouTube", R.drawable.youtube_favicon, "http://youtube.com", R.drawable.youtube_bookmark));
        Map<String, File> allStorageLocations = MemoryStorage.getAllStorageLocations();
        for (Map.Entry<String, File> stringFileEntry : allStorageLocations.entrySet()) {
            String title;
            if (stringFileEntry.getKey().equals(MemoryStorage.SD_CARD)) {
                title = getString(R.string.internal_storage);
                bookmarks.add(new Bookmark(title, 0, "file://" + stringFileEntry.getValue().getPath(), R.drawable.external_storage));
            } else if (stringFileEntry.getKey().equals(MemoryStorage.EXTERNAL_SD_CARD)) {
                title = getString(R.string.external_storage);
                bookmarks.add(new Bookmark(title, 0, "file://" + stringFileEntry.getValue().getPath(), R.drawable.external_storage));
            } else {
                title = getString(R.string.generic_storage);
                bookmarks.add(new Bookmark(title, 0, "file://" + stringFileEntry.getValue().getPath(), R.drawable.external_storage));
            }

        }
        bookmarks.add(new Bookmark("Plex.tv", 0, "http://app.plex.tv", R.drawable.plex));
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm1) {
                realm1.insert(bookmarks);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        findParentListener(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        findParentListener(activity);
    }

    private void findParentListener(Context context) {
        if (getParentFragment() != null && getParentFragment() instanceof BookmarksClickCallback) {
            listener = (BookmarksClickCallback) getParentFragment();
        } else if (context instanceof BookmarksClickCallback) {
            listener = (BookmarksClickCallback) context;
        } else {
            throw new IllegalStateException(context + " should implement " + BookmarksClickCallback.class.getSimpleName());
        }
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        listener.onBookmarkSelected(bookmark);
        listener.onBookmarkFragmentClose();
    }

    @Override
    public void onBookmarkAddCurrentPage() {
        listener.onBookmarkAddCurrentPage();
    }

    @Override
    public void onBookmarkDelete(Bookmark bookmark) {
        listener.onBookmarkDelete(bookmark);
    }

    @Override
    public void onBookmarkFragmentClose() {

    }
}
