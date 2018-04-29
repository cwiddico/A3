package com.csci443.cwiddico.locatr.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "locations.db";

    public LocHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
                db.execSQL("create table " + LocSchema.LocTable.NAME + "(" +
                        " _id integer primary key autoincrement, " +
                        LocSchema.LocTable.Cols.LAT + ", " +
                        LocSchema.LocTable.Cols.LONG + ", " +
                        LocSchema.LocTable.Cols.TIME + ", " +
                        LocSchema.LocTable.Cols.DATE + ", " +
                        LocSchema.LocTable.Cols.WEATHER + ", " +
                        LocSchema.LocTable.Cols.TEMPERATURE +
                        ")"
                );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

