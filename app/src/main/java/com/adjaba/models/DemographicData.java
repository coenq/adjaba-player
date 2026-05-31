package com.adjaba.models;

import java.util.List;

/**
 * Model class representing demographic data received from MQTT broker.
 * Published by OnlyCamera analytics on topic: store/{screenId}
 *
 * New nested format (May 2026):
 * {
 *   "version": "1.0",
 *   "storeId": "demo959",
 *   "timestamp": 1779232472751,
 *   "audience": { "count": 2, "maleCount": 2, "femaleCount": 0, "avgDwellSec": 8 },
 *   "profile": { "ageRange": "(32, 43)", "ageBracket": "32-42", "gender": "M",
 *                "dominantEmotion": "neutral", "emotionScores": { "happy": 5, ... } },
 *   "adTarget": { "segment": "32_42_M_neutral", "tags": ["M", "32-42", "neutral"] }
 * }
 */
public class DemographicData {

    private String version;
    private String storeId;
    private long timestamp;
    private Audience audience;
    private Profile profile;
    private AdTarget adTarget;

    // ─── Nested classes ────────────────────────────────────────────────────────

    public static class Audience {
        public int count;
        public int maleCount;
        public int femaleCount;
        public int avgDwellSec;
    }

    public static class EmotionScores {
        public int happy;
        public int neutral;
        public int sad;
        public int angry;
        public int fear;
        public int surprise;
        public int disgust;
    }

    public static class Profile {
        public String ageRange;    // e.g. "(32, 43)"
        public String ageBracket;  // e.g. "32-42" – matches API targetAgeGroup values
        public String gender;      // "M" or "F"
        public String dominantEmotion;
        public EmotionScores emotionScores;
    }

    public static class AdTarget {
        public String segment;        // e.g. "32_42_M_neutral"
        public List<String> tags;     // e.g. ["M", "32-42", "neutral"]
    }

    // ─── Validity check ────────────────────────────────────────────────────────

    /**
     * Returns true if the message contains enough data for ad selection.
     */
    public boolean isValid() {
        return audience != null && audience.count > 0
                && profile != null && profile.gender != null && !profile.gender.isEmpty();
    }

    // ─── Convenience getters (delegates to nested objects) ────────────────────

    public int getCustomerCount() {
        return audience != null ? audience.count : 0;
    }

    public int getMaleCount() {
        return audience != null ? audience.maleCount : 0;
    }

    public int getFemaleCount() {
        return audience != null ? audience.femaleCount : 0;
    }

    public int getAvgDwellSec() {
        return audience != null ? audience.avgDwellSec : 0;
    }

    /** Raw age range string, e.g. "(32, 43)" */
    public String getAgeRange() {
        return profile != null ? profile.ageRange : null;
    }

    /**
     * Normalized age bracket that matches AdContractData.targetAgeGroup values.
     * e.g. "32-42"  →  matches API list entry "32-42"
     */
    public String getAgeBracket() {
        return profile != null ? profile.ageBracket : null;
    }

    /** "M" or "F" */
    public String getGender() {
        return profile != null ? profile.gender : null;
    }

    public String getDominantEmotion() {
        if (profile != null && profile.dominantEmotion != null) {
            return profile.dominantEmotion;
        }
        // Compute from scores if not supplied
        if (profile != null && profile.emotionScores != null) {
            EmotionScores s = profile.emotionScores;
            int max = Math.max(s.happy, Math.max(s.neutral, Math.max(s.sad,
                    Math.max(s.angry, Math.max(s.fear, Math.max(s.surprise, s.disgust))))));
            if (max == 0)         return "neutral";
            if (max == s.happy)   return "happy";
            if (max == s.neutral) return "neutral";
            if (max == s.sad)     return "sad";
            if (max == s.angry)   return "angry";
            if (max == s.fear)    return "fear";
            if (max == s.surprise) return "surprise";
            return "disgust";
        }
        return "neutral";
    }

    public int getHappy() {
        return (profile != null && profile.emotionScores != null) ? profile.emotionScores.happy : 0;
    }

    public int getNeutral() {
        return (profile != null && profile.emotionScores != null) ? profile.emotionScores.neutral : 0;
    }

    /** IOT ad-targeting tags, e.g. ["M", "32-42", "neutral"] */
    public List<String> getAdTags() {
        return adTarget != null ? adTarget.tags : null;
    }

    public String getAdSegment() {
        return adTarget != null ? adTarget.segment : null;
    }

    // ─── Plain getters ─────────────────────────────────────────────────────────

    public String getStoreId()  { return storeId; }
    public long   getTimestamp() { return timestamp; }
    public String getVersion()   { return version; }
    public Audience  getAudience()  { return audience; }
    public Profile   getProfile()   { return profile; }
    public AdTarget  getAdTarget()  { return adTarget; }

    @Override
    public String toString() {
        return "DemographicData{"
                + "storeId='" + storeId + '\''
                + ", count=" + getCustomerCount()
                + ", gender='" + getGender() + '\''
                + ", ageRange='" + getAgeRange() + '\''
                + ", ageBracket='" + getAgeBracket() + '\''
                + ", emotion='" + getDominantEmotion() + '\''
                + ", avgDwellSec=" + getAvgDwellSec()
                + ", tags=" + getAdTags()
                + '}';
    }
}
