package com.adjaba.models.newmodels;

import com.google.gson.annotations.SerializedName;

public class TvPollAuthResponse {
    public String status;
    public String token;
    public TvUser user;

    /**
     * Optional. When present (and at minimum {@link ScreenSetup#screenId} and
     * {@link ScreenSetup#orientation} are set), the TV skips manual setup entirely: it applies
     * every field here and starts playing immediately, letting a partner configure a whole
     * screen from their phone during the same QR-code login instead of using the TV remote.
     * Absent/null = today's behavior is unchanged (plain login, manual setup on the TV).
     *
     * NOT YET IMPLEMENTED ON THE BACKEND — this is the field contract for whoever builds the
     * mobile-facing setup form: populate this object in the tvPollAuth response once the
     * device code has been approved with these values filled in on the activation page.
     */
    public ScreenSetup screenSetup;

    public static class TvUser {
        @SerializedName("userId")
        public String userId;
        public String email;
        public String userType;
        public boolean isPartner;
        public boolean isAdmin;
        public String businessName;
    }

    /** See {@link #screenSetup}. Mirrors every field on the Select Screen setup page. */
    public static class ScreenSetup {
        /** Required. A screenId belonging to this account (same values SelectScreens lists). */
        public String screenId;
        /** Required. One of: "Landscape", "Portrait", "Forced Portrait", "Split Screen". */
        public String orientation;
        /** Optional. Position of the Data Refresh Interval spinner as a string: "0"=1min,
         *  "1"=5min, "2"=30min, "3"=60min (see R.array.interval_arrays). Defaults to "0". */
        public String refreshInterval;
        public Boolean displayTextEnabled;
        public Boolean businessRulesEnabled;
        /** Defaults to true (shown) if omitted, matching the TV checkbox's own default. */
        public Boolean weatherEnabled;
        /** Defaults to true (shown) if omitted, matching the TV checkbox's own default. */
        public Boolean newsEnabled;
        public Boolean iotEnabled;
        /** Cloud Slideshow — independent of the CMS, see SlideshowManager. */
        public Boolean slideshowEnabled;
        /** Public Google Drive folder link, e.g. https://drive.google.com/drive/folders/<id>?usp=sharing */
        public String slideshowFolderUrl;
        /** Seconds each slideshow photo stays on screen. Defaults to 5 if omitted. */
        public Integer slideshowIntervalSeconds;
    }
}
