package com.example.backondeath;

import org.bukkit.Location;

public class DeathData {
    private final Location deathLocation;
    private long nearPlayerStartTime = -1; // -1 nghĩa là chưa có ai ở gần

    public DeathData(Location deathLocation) {
        this.deathLocation = deathLocation;
    }

    public Location getDeathLocation() {
        return deathLocation;
    }

    public long getNearPlayerStartTime() {
        return nearPlayerStartTime;
    }

    public void setNearPlayerStartTime(long nearPlayerStartTime) {
        this.nearPlayerStartTime = nearPlayerStartTime;
    }
}
