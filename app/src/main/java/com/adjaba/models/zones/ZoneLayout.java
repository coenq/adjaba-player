package com.adjaba.models.zones;

/**
 * Represents a multi-zone layout configuration
 * Supports 2-4 zones displaying different content types simultaneously
 * 
 * Use Cases:
 * - Zone 1 (Main): Video/Image ads (60-70%)
 * - Zone 2 (Bottom): Weather widget (15-20%)
 * - Zone 3 (Side): News ticker (15-20%)
 * - Zone 4 (Corner): Clock/QR code (10%)
 */
public class ZoneLayout {
    
    public enum LayoutType {
        SINGLE_FULL_SCREEN,          // Current default (100%)
        SPLIT_HORIZONTAL_2,           // Top 70% + Bottom 30%
        SPLIT_HORIZONTAL_3,           // Top 60% + Middle 20% + Bottom 20%
        SPLIT_VERTICAL_2,             // Left 70% + Right 30%
        GRID_2X2,                     // 4 equal zones (25% each)
        MAIN_PLUS_BOTTOM_BAR,         // Main 75% + Bottom bar 25%
        MAIN_PLUS_SIDE_BAR,           // Main 75% + Right sidebar 25%
        L_SHAPE,                      // Main (top-left 60%) + Bottom 30% + Right 30%
        CUSTOM                        // User-defined zones
    }
    
    public static class Zone {
        public int zoneId;
        public String zoneName;
        public ZoneContentType contentType;
        public float widthPercent;
        public float heightPercent;
        public float xPercent;          // Position from left (0-100)
        public float yPercent;          // Position from top (0-100)
        public String contentSource;     // URL, local path, or content ID
        public int zIndex;               // Layering order (higher = on top)
        
        public Zone(int zoneId, String zoneName, ZoneContentType contentType) {
            this.zoneId = zoneId;
            this.zoneName = zoneName;
            this.contentType = contentType;
            this.zIndex = zoneId; // Default: zone 1 on bottom, zone 4 on top
        }
    }
    
    public enum ZoneContentType {
        VIDEO,
        IMAGE,
        WEB_VIEW,
        WEATHER,
        NEWS_TICKER,
        CLOCK,
        QR_CODE,
        LIVE_STREAM,
        SOCIAL_FEED,
        DATA_WIDGET
    }
    
    private LayoutType layoutType;
    private Zone[] zones;
    private boolean isActive;
    
    public ZoneLayout(LayoutType layoutType) {
        this.layoutType = layoutType;
        this.isActive = false;
        initializeZonesForLayout();
    }
    
    /**
     * Initialize zones based on layout type with predefined dimensions
     */
    private void initializeZonesForLayout() {
        switch (layoutType) {
            case SINGLE_FULL_SCREEN:
                zones = new Zone[1];
                zones[0] = new Zone(1, "Main", ZoneContentType.VIDEO);
                zones[0].widthPercent = 100f;
                zones[0].heightPercent = 100f;
                zones[0].xPercent = 0f;
                zones[0].yPercent = 0f;
                break;
                
            case SPLIT_HORIZONTAL_2:
                zones = new Zone[2];
                // Top zone (main content)
                zones[0] = new Zone(1, "Main", ZoneContentType.VIDEO);
                zones[0].widthPercent = 100f;
                zones[0].heightPercent = 70f;
                zones[0].xPercent = 0f;
                zones[0].yPercent = 0f;
                
                // Bottom zone (weather/news)
                zones[1] = new Zone(2, "Bottom Bar", ZoneContentType.WEATHER);
                zones[1].widthPercent = 100f;
                zones[1].heightPercent = 30f;
                zones[1].xPercent = 0f;
                zones[1].yPercent = 70f;
                break;
                
            case MAIN_PLUS_BOTTOM_BAR:
                zones = new Zone[2];
                // Main content zone
                zones[0] = new Zone(1, "Main Content", ZoneContentType.VIDEO);
                zones[0].widthPercent = 100f;
                zones[0].heightPercent = 75f;
                zones[0].xPercent = 0f;
                zones[0].yPercent = 0f;
                
                // Bottom info bar
                zones[1] = new Zone(2, "Info Bar", ZoneContentType.NEWS_TICKER);
                zones[1].widthPercent = 100f;
                zones[1].heightPercent = 25f;
                zones[1].xPercent = 0f;
                zones[1].yPercent = 75f;
                break;
                
            case L_SHAPE:
                zones = new Zone[3];
                // Main video zone (top-left)
                zones[0] = new Zone(1, "Main Video", ZoneContentType.VIDEO);
                zones[0].widthPercent = 70f;
                zones[0].heightPercent = 70f;
                zones[0].xPercent = 0f;
                zones[0].yPercent = 0f;
                
                // Bottom zone
                zones[1] = new Zone(2, "Bottom", ZoneContentType.WEATHER);
                zones[1].widthPercent = 100f;
                zones[1].heightPercent = 30f;
                zones[1].xPercent = 0f;
                zones[1].yPercent = 70f;
                
                // Right sidebar
                zones[2] = new Zone(3, "Sidebar", ZoneContentType.NEWS_TICKER);
                zones[2].widthPercent = 30f;
                zones[2].heightPercent = 70f;
                zones[2].xPercent = 70f;
                zones[2].yPercent = 0f;
                break;
                
            case GRID_2X2:
                zones = new Zone[4];
                zones[0] = new Zone(1, "Top-Left", ZoneContentType.VIDEO);
                zones[0].widthPercent = 50f;
                zones[0].heightPercent = 50f;
                zones[0].xPercent = 0f;
                zones[0].yPercent = 0f;
                
                zones[1] = new Zone(2, "Top-Right", ZoneContentType.WEATHER);
                zones[1].widthPercent = 50f;
                zones[1].heightPercent = 50f;
                zones[1].xPercent = 50f;
                zones[1].yPercent = 0f;
                
                zones[2] = new Zone(3, "Bottom-Left", ZoneContentType.NEWS_TICKER);
                zones[2].widthPercent = 50f;
                zones[2].heightPercent = 50f;
                zones[2].xPercent = 0f;
                zones[2].yPercent = 50f;
                
                zones[3] = new Zone(4, "Bottom-Right", ZoneContentType.WEB_VIEW);
                zones[3].widthPercent = 50f;
                zones[3].heightPercent = 50f;
                zones[3].xPercent = 50f;
                zones[3].yPercent = 50f;
                break;
                
            default:
                zones = new Zone[1];
                zones[0] = new Zone(1, "Main", ZoneContentType.VIDEO);
                zones[0].widthPercent = 100f;
                zones[0].heightPercent = 100f;
                break;
        }
    }
    
    public LayoutType getLayoutType() {
        return layoutType;
    }
    
    public Zone[] getZones() {
        return zones;
    }
    
    public Zone getZone(int zoneId) {
        if (zones != null) {
            for (Zone zone : zones) {
                if (zone.zoneId == zoneId) {
                    return zone;
                }
            }
        }
        return null;
    }
    
    public int getZoneCount() {
        return zones != null ? zones.length : 0;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Set content source for a specific zone
     */
    public void setZoneContent(int zoneId, String contentSource, ZoneContentType contentType) {
        Zone zone = getZone(zoneId);
        if (zone != null) {
            zone.contentSource = contentSource;
            zone.contentType = contentType;
        }
    }
}

