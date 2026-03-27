package com.mojang.rubydung;

import java.io.File;

public class SaveSlot {
    public static final int MAX_SLOTS = 5;

    private int slotId;
    private String worldName;
    private long lastPlayed;
    private boolean exists;

    public SaveSlot(int slotId) {
        this.slotId = slotId;
        load();
    }

    public void load() {
        File saveFile = new File("save_" + slotId + ".dat");
        exists = saveFile.exists();

        if (exists) {
            worldName = "World " + (slotId + 1);
            lastPlayed = saveFile.lastModified();
        } else {
            worldName = "Empty Slot";
            lastPlayed = 0;
        }
    }

    public void createNewWorld(String name) {
        worldName = name;
        exists = true;
        lastPlayed = System.currentTimeMillis();
    }

    public void setExists(boolean exists) {
        this.exists = exists;
        if (exists) {
            lastPlayed = System.currentTimeMillis();
        }
    }

    public int getSlotId() { return slotId; }
    public String getWorldName() { return worldName; }
    public long getLastPlayed() { return lastPlayed; }
    public boolean exists() { return exists; }

    public String getLastPlayedString() {
        if (!exists) return "Empty";
        long diff = System.currentTimeMillis() - lastPlayed;
        if (diff < 60000) return "Just now";
        if (diff < 3600000) return (diff / 60000) + " minutes ago";
        if (diff < 86400000) return (diff / 3600000) + " hours ago";
        return (diff / 86400000) + " days ago";
    }
}