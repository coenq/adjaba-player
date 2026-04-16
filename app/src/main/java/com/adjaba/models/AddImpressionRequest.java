package com.adjaba.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddImpressionRequest {

    @SerializedName("impressionId")
    @Expose
    private String impressionId;
    @SerializedName("advertId")
    @Expose
    private String advertId;
    @SerializedName("amountSettled")
    @Expose
    private Boolean amountSettled;
    @SerializedName("contractId")
    @Expose
    private String contractId;
    @SerializedName("currency")
    @Expose
    private String currency;
    @SerializedName("dayHour")
    @Expose
    private Integer dayHour;
    @SerializedName("playSec")
    @Expose
    private Integer playSec;
    @SerializedName("female20")
    @Expose
    private Integer female20;
    @SerializedName("female32")
    @Expose
    private Integer female32;
    @SerializedName("female40")
    @Expose
    private Integer female40;
    @SerializedName("female50")
    @Expose
    private Integer female50;
    @SerializedName("female50plus")
    @Expose
    private Integer female50plus;
    @SerializedName("format")
    @Expose
    private String format;
    @SerializedName("impressionCost")
    @Expose
    private Integer impressionCost;
    @SerializedName("isActiveContract")
    @Expose
    private Boolean isActiveContract;
    @SerializedName("locationType")
    @Expose
    private String locationType;
    @SerializedName("male20")
    @Expose
    private Integer male20;
    @SerializedName("male32")
    @Expose
    private Integer male32;
    @SerializedName("male40")
    @Expose
    private Integer male40;
    @SerializedName("male50")
    @Expose
    private Integer male50;
    @SerializedName("male50plus")
    @Expose
    private Integer male50plus;
    @SerializedName("maxBid")
    @Expose
    private Integer maxBid;
    @SerializedName("orientation")
    @Expose
    private String orientation;
    @SerializedName("playTimeStamp")
    @Expose
    private String playTimeStamp;
    @SerializedName("screenDevice")
    @Expose
    private String screenDevice;
    @SerializedName("screenPlayer")
    @Expose
    private String screenPlayer;
    @SerializedName("screenId")
    @Expose
    private String screenId;

    @SerializedName("viewCount")
    @Expose
    private Integer viewCount;

    public String getImpressionId() {
        return impressionId;
    }

    public void setImpressionId(String impressionId) {
        this.impressionId = impressionId;
    }

    public String getAdvertId() {
        return advertId;
    }

    public void setAdvertId(String advertId) {
        this.advertId = advertId;
    }

    public Boolean getAmountSettled() {
        return amountSettled;
    }

    public void setAmountSettled(Boolean amountSettled) {
        this.amountSettled = amountSettled;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getDayHour() {
        return dayHour;
    }

    public void setDayHour(Integer dayHour) {
        this.dayHour = dayHour;
    }

    public Integer getPlaySec() {
        return playSec;
    }

    public void setPlaySec(Integer playSec) {
        this.playSec = playSec;
    }

    public Integer getFemale20() {
        return female20;
    }

    public void setFemale20(Integer female20) {
        this.female20 = female20;
    }

    public Integer getFemale32() {
        return female32;
    }

    public void setFemale32(Integer female32) {
        this.female32 = female32;
    }

    public Integer getFemale40() {
        return female40;
    }

    public void setFemale40(Integer female40) {
        this.female40 = female40;
    }

    public Integer getFemale50() {
        return female50;
    }

    public void setFemale50(Integer female50) {
        this.female50 = female50;
    }

    public Integer getFemale50plus() {
        return female50plus;
    }

    public void setFemale50plus(Integer female50plus) {
        this.female50plus = female50plus;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getImpressionCost() {
        return impressionCost;
    }

    public void setImpressionCost(Integer impressionCost) {
        this.impressionCost = impressionCost;
    }

    public Boolean getIsActiveContract() {
        return isActiveContract;
    }

    public void setIsActiveContract(Boolean isActiveContract) {
        this.isActiveContract = isActiveContract;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Integer getMale20() {
        return male20;
    }

    public void setMale20(Integer male20) {
        this.male20 = male20;
    }

    public Integer getMale32() {
        return male32;
    }

    public void setMale32(Integer male32) {
        this.male32 = male32;
    }

    public Integer getMale40() {
        return male40;
    }

    public void setMale40(Integer male40) {
        this.male40 = male40;
    }

    public Integer getMale50() {
        return male50;
    }

    public void setMale50(Integer male50) {
        this.male50 = male50;
    }

    public Integer getMale50plus() {
        return male50plus;
    }

    public void setMale50plus(Integer male50plus) {
        this.male50plus = male50plus;
    }

    public Integer getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(Integer maxBid) {
        this.maxBid = maxBid;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getPlayTimeStamp() {
        return playTimeStamp;
    }

    public void setPlayTimeStamp(String playTimeStamp) {
        this.playTimeStamp = playTimeStamp;
    }

    public String getScreenDevice() {
        return screenDevice;
    }

    public void setScreenDevice(String screenDevice) {
        this.screenDevice = screenDevice;
    }

    public String getScreenPlayer() {
        return screenPlayer;
    }

    public void setScreenPlayer(String screenPlayer) {
        this.screenPlayer = screenPlayer;
    }

    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

}
