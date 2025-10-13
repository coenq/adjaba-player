package com.adjaba.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

    @Database(entities = {AdEntity.class,ImpressionEntity.class,InfoEntity.class}, version = 5, exportSchema = false)
    public abstract class AdDatabase extends RoomDatabase {

        private static volatile AdDatabase INSTANCE;

        public abstract AdDao adDao();
        public abstract ImpressionDao impDao();
        public abstract InfoDao infoDao();

        public static AdDatabase getInstance(Context context) {
            if (INSTANCE == null) {
                synchronized (AdDatabase.class) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                                context.getApplicationContext(),
                                AdDatabase.class,
                                "ad_database"
                        ).fallbackToDestructiveMigration().build();
                    }
                }
            }
            return INSTANCE;
        }
    }
