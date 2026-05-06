package com.adjaba.activities.viewmodel;

import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.others.TargetHours;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {
    private static DataHolder instance;
    public String time, orient, screenID, location;
    public List<MediaModel> allAds;
    public int displayFlag;
    public List<TargetHours> targetHours;
    public int targetHoursFlag;
    public String locationTypes;
    public String screenPlayer;
    public String screenDevice;
    public List<String> tags;
    public List<String> advertIds = new ArrayList<>();

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }
}
