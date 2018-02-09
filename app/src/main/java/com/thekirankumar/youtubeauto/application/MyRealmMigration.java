package com.thekirankumar.youtubeauto.application;

import android.support.annotation.NonNull;

import com.thekirankumar.youtubeauto.bookmarks.OverrideSettings;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by kiran.kumar on 08/02/18.
 */

class MyRealmMigration implements RealmMigration {
    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        // Migrate to version 1: Add Overrides to bookmark object.
        if (oldVersion == 0) {
            RealmObjectSchema overrideSettings = schema.get("OverrideSettings");
            schema.get("Bookmark")
                    .addRealmListField("overrideSettings", overrideSettings);
            oldVersion++;
        }

    }
}
