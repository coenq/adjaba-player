package com.rnd.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ReportEntity.class}, version =2, exportSchema = false)
public abstract class ReportDataBase extends RoomDatabase {

    private static volatile ReportDataBase INSTANCE;

    public abstract ReportDao reportDao();

    public static ReportDataBase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ReportDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            ReportDataBase.class,
                            "report_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}