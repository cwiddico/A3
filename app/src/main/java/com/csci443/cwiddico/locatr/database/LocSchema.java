package com.csci443.cwiddico.locatr.database;

public class LocSchema {

    public static final class LocTable {
        public static final String NAME = "locations";
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String LAT = "lat";
            public static final String LONG = "long";
            public static final String DATE = "date";
            public static final String TIME = "time";
            public static final String WEATHER = "weather";
            public static final String TEMPERATURE = "temperature";
        }
    }
}
