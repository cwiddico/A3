package com.csci443.cwiddico.locatr.database;

import android.database.Cursor;
import android.database.CursorWrapper;


import com.csci443.cwiddico.locatr.LMarker;

import java.util.Date;
import java.util.UUID;

public class LocCursor extends CursorWrapper{
    public LocCursor(Cursor cursor) {
        super(cursor);
    }

    public LMarker getMarker() {
        String uuidString = getString(getColumnIndex(LocSchema.LocTable.Cols.UUID));
        String lat = getString(getColumnIndex(LocSchema.LocTable.Cols.LAT));
        String longi = getString(getColumnIndex(LocSchema.LocTable.Cols.LONG));
        long date = getLong(getColumnIndex(LocSchema.LocTable.Cols.DATE));
        long time = getLong(getColumnIndex(LocSchema.LocTable.Cols.TIME));
        String weather = getString(getColumnIndex(LocSchema.LocTable.Cols.WEATHER));
        int tempe = getInt(getColumnIndex(LocSchema.LocTable.Cols.TEMPERATURE));


        LMarker marker = new LMarker(UUID.fromString(uuidString));

        return marker;
    }
}
