package com.mojang.rubydung.inventory;

public class Inventory {
    public static final int SLOT_COUNT = 2;
    public static final int SLOT_1 = 0;
    public static final int SLOT_2 = 1;

    private int[] items;
    private int[] counts;
    private int selectedSlot = 0;

    public Inventory() {
        items = new int[SLOT_COUNT];
        counts = new int[SLOT_COUNT];

        for (int i = 0; i < SLOT_COUNT; i++) {
            items[i] = 0;
            counts[i] = 0;
        }

        items[SLOT_1] = 1;
        counts[SLOT_1] = 2;
    }

    public void setSlot(int slot, int itemId, int count) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            items[slot] = itemId;
            counts[slot] = count;
            if (counts[slot] > 2) counts[slot] = 2;
            if (counts[slot] < 0) counts[slot] = 0;
            if (counts[slot] == 0) items[slot] = 0;
        }
    }

    public int getItem(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            return items[slot];
        }
        return 0;
    }

    public int getCount(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            return counts[slot];
        }
        return 0;
    }

    public int getSelectedItem() {
        return items[selectedSlot];
    }

    public int getSelectedCount() {
        return counts[selectedSlot];
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            selectedSlot = slot;
        }
    }

    public void scrollSelected(int delta) {
        selectedSlot -= delta;
        if (selectedSlot < 0) selectedSlot += SLOT_COUNT;
        if (selectedSlot >= SLOT_COUNT) selectedSlot -= SLOT_COUNT;
    }

    public boolean hasItemInSelected() {
        return items[selectedSlot] != 0 && counts[selectedSlot] > 0;
    }

    public void takeItem(int itemId) {
        if (!hasItemInSelected()) {
            items[selectedSlot] = itemId;
            counts[selectedSlot] = 1;
        } else if (items[selectedSlot] == itemId && counts[selectedSlot] < 2) {
            counts[selectedSlot]++;
        } else {
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (items[i] == itemId && counts[i] < 2) {
                    counts[i]++;
                    return;
                }
            }
            for (int i = 0; i < SLOT_COUNT; i++) {
                if (items[i] == 0) {
                    items[i] = itemId;
                    counts[i] = 1;
                    return;
                }
            }
        }
    }

    public void useItem() {
        if (hasItemInSelected()) {
            counts[selectedSlot]--;
            if (counts[selectedSlot] <= 0) {
                items[selectedSlot] = 0;
                counts[selectedSlot] = 0;
            }
        }
    }

    public void swapSlots(int slotA, int slotB) {
        int tempItem = items[slotA];
        int tempCount = counts[slotA];
        items[slotA] = items[slotB];
        counts[slotA] = counts[slotB];
        items[slotB] = tempItem;
        counts[slotB] = tempCount;
    }
}