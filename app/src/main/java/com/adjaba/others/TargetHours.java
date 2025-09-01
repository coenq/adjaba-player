package com.adjaba.others;

import java.util.List;

public class TargetHours {
    String id;
    List<Integer>targetHours;

    public TargetHours(String id, List<Integer> targetHours) {
        this.id = id;
        this.targetHours = targetHours;
    }

    public String getId() {
        return id;
    }

    public List<Integer> getTargetHours() {
        return targetHours;
    }
}
