package com.rnd.others;


import java.util.ArrayList;
import java.util.List;

public class DataHolder {
    private static DataHolder instance;
    public String time, orient, screenID, location;

    public int displayFlag;

    public int targetHoursFlag;
    public String locationTypes;
    public String screenPlayer;
    public String screenDevice;
    public List<String> tags;
    public List<String> advertIds = new ArrayList<>();
    public int isData;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }
}
