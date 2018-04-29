package com.csci443.cwiddico.locatr;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class LMarker {
    private UUID mId;
    private int lat;
    private int longitude;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;

    public LMarker() {
        this(UUID.randomUUID());
    }
    public LMarker(UUID id) {
        mDate = Calendar.getInstance().getTime();
        mId = id;
    }
}
