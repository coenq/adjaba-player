package com.rnd.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ImpressionEntity.class}, version =1, exportSchema = false)
public abstract class AdDatabase extends RoomDatabase {

    private static volatile AdDatabase INSTANCE;

    public abstract ImpressionDao impDao();

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
