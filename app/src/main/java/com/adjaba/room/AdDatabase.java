package com.adjaba.room;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

    @Database(entities = {AdEntity.class,ImpressionEntity.class,InfoEntity.class,SlideshowImageEntity.class}, version = 10, exportSchema = false)
    public abstract class AdDatabase extends RoomDatabase {

        private static volatile AdDatabase INSTANCE;

        public abstract AdDao adDao();
        public abstract ImpressionDao impDao();
        public abstract InfoDao infoDao();
        public abstract SlideshowImageDao slideshowImageDao();

        /**
         * Every real installed build today is on v8 (v9 never shipped — it landed earlier in
         * this same v1.3.0 branch). Unlike the `ads` cache, `impressions` holds unsent
         * proof-of-play data that must survive an app update, so this path is a real migration,
         * not the destructive fallback below. MIGRATION_9_10 covers the (currently theoretical,
         * but cheap to keep correct) case of a device already upgraded to exactly v9.
         */
        private static final Migration MIGRATION_8_10 = new Migration(8, 10) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE ads ADD COLUMN zoneId INTEGER NOT NULL DEFAULT 0");
                database.execSQL("ALTER TABLE ads ADD COLUMN zoneLayoutType TEXT");
                database.execSQL("ALTER TABLE impressions ADD COLUMN sentToBackend INTEGER NOT NULL DEFAULT 0");
            }
        };

        private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE impressions ADD COLUMN sentToBackend INTEGER NOT NULL DEFAULT 0");
            }
        };

        public static AdDatabase getInstance(Context context) {
            if (INSTANCE == null) {
                synchronized (AdDatabase.class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AdDatabase.class,
                                "ad_database"
                        ).addMigrations(MIGRATION_8_10, MIGRATION_9_10)
                         .fallbackToDestructiveMigration()
                         .build();
                    }
                }
            }
            return INSTANCE;
        }
    }
