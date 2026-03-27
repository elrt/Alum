package com.mojang.rubydung.inventory;

import com.mojang.rubydung.BitmapFont;
import com.mojang.rubydung.Textures;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class InventoryRenderer {
    public static final int SLOT_SIZE = 48;
    public static final int SLOT_MARGIN = 12;
    public static final int HOTBAR_X = 20;
    public static final int HOTBAR_Y_OFFSET = 80;

    private Inventory inventory;
    private BitmapFont font;
    private int screenWidth, screenHeight;
    private int terrainTextureId;
    private int grabbedSlot = -1;
    private int grabbedItem = 0;
    private int grabbedCount = 0;

    private static final float TEX_SIZE = 16.0f;
    private static final float TEX_WIDTH = 16.0f;

    public InventoryRenderer(Inventory inventory, BitmapFont font) {
        this.inventory = inventory;
        this.font = font;
        this.terrainTextureId = Textures.loadTexture("/terrain.png", GL11.GL_NEAREST);
    }

    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void render() {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderHotbar();

        if (grabbedItem != 0 && grabbedCount > 0) {
            int mouseX = Mouse.getX();
            int mouseY = screenHeight - Mouse.getY();
            renderCursorItem(mouseX - 24, mouseY - 24, 48, grabbedItem, grabbedCount);
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopAttrib();
    }

    private void renderHotbar() {
        int y = screenHeight - HOTBAR_Y_OFFSET;

        int totalWidth = Inventory.SLOT_COUNT * (SLOT_SIZE + SLOT_MARGIN) - SLOT_MARGIN;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.1f, 0.08f, 0.05f, 0.8f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(HOTBAR_X - 8, y - 8);
        GL11.glVertex2f(HOTBAR_X + totalWidth + 8, y - 8);
        GL11.glVertex2f(HOTBAR_X + totalWidth + 8, y + SLOT_SIZE + 8);
        GL11.glVertex2f(HOTBAR_X - 8, y + SLOT_SIZE + 8);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        for (int i = 0; i < Inventory.SLOT_COUNT; i++) {
            int x = HOTBAR_X + i * (SLOT_SIZE + SLOT_MARGIN);

            if (i == inventory.getSelectedSlot() && grabbedSlot != i) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glColor4f(1.0f, 0.9f, 0.4f, 0.8f);
                GL11.glLineWidth(3.0f);
                GL11.glBegin(GL11.GL_LINE_LOOP);
                GL11.glVertex2f(x - 2, y - 2);
                GL11.glVertex2f(x + SLOT_SIZE + 2, y - 2);
                GL11.glVertex2f(x + SLOT_SIZE + 2, y + SLOT_SIZE + 2);
                GL11.glVertex2f(x - 2, y + SLOT_SIZE + 2);
                GL11.glEnd();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.08f, 0.06f, 0.04f, 0.9f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(x, y);
            GL11.glVertex2f(x + SLOT_SIZE, y);
            GL11.glVertex2f(x + SLOT_SIZE, y + SLOT_SIZE);
            GL11.glVertex2f(x, y + SLOT_SIZE);
            GL11.glEnd();

            GL11.glColor4f(0.5f, 0.4f, 0.25f, 0.8f);
            GL11.glLineWidth(1.5f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(x, y);
            GL11.glVertex2f(x + SLOT_SIZE, y);
            GL11.glVertex2f(x + SLOT_SIZE, y + SLOT_SIZE);
            GL11.glVertex2f(x, y + SLOT_SIZE);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            int itemId = inventory.getItem(i);
            if (itemId != 0 && !(grabbedSlot == i)) {
                renderItemTexture(x + 4, y + 4, SLOT_SIZE - 8, itemId);

                int count = inventory.getCount(i);
                if (font != null && count > 0) {
                    font.draw(String.valueOf(count), x + SLOT_SIZE - 16, y + SLOT_SIZE - 16,
                            0.5f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
    }

    private void renderCursorItem(int x, int y, int size, int itemId, int count) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.08f, 0.06f, 0.04f, 0.95f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + size, y);
        GL11.glVertex2f(x + size, y + size);
        GL11.glVertex2f(x, y + size);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        renderItemTexture(x + 4, y + 4, size - 8, itemId);

        if (font != null && count > 1) {
            font.draw(String.valueOf(count), x + size - 16, y + size - 16, 0.5f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderItemTexture(int x, int y, int iconSize, int itemId) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTextureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        float u0, v0, u1, v1;

        switch (itemId) {
            case 1:
                u0 = 0.0f / TEX_WIDTH;
                v0 = 0.0f / TEX_SIZE;
                u1 = 1.0f / TEX_WIDTH;
                v1 = 1.0f / TEX_SIZE;
                break;
            case 2:
                u0 = 1.0f / TEX_WIDTH;
                v0 = 0.0f / TEX_SIZE;
                u1 = 2.0f / TEX_WIDTH;
                v1 = 1.0f / TEX_SIZE;
                break;
            case 3:
                u0 = 4.0f / TEX_WIDTH;
                v0 = 1.0f / TEX_SIZE;
                u1 = 5.0f / TEX_WIDTH;
                v1 = 2.0f / TEX_SIZE;
                break;
            case 4:
                u0 = 4.0f / TEX_WIDTH;
                v0 = 3.0f / TEX_SIZE;
                u1 = 5.0f / TEX_WIDTH;
                v1 = 4.0f / TEX_SIZE;
                break;
            default:
                u0 = 0;
                v0 = 0;
                u1 = 1.0f / TEX_WIDTH;
                v1 = 1.0f / TEX_SIZE;
                break;
        }

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x + iconSize, y);
        GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x + iconSize, y + iconSize);
        GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x, y + iconSize);
        GL11.glEnd();
    }

    public void handleInventoryClick(int slot, int button) {
        if (button == 0) {
            if (grabbedItem == 0) {
                if (slot != -1 && inventory.getItem(slot) != 0) {
                    grabbedItem = inventory.getItem(slot);
                    grabbedCount = inventory.getCount(slot);
                    grabbedSlot = slot;
                    inventory.setSlot(slot, 0, 0);
                }
            } else {
                if (slot == -1) {
                    if (grabbedSlot != -1) {
                        inventory.setSlot(grabbedSlot, grabbedItem, grabbedCount);
                    }
                    grabbedItem = 0;
                    grabbedCount = 0;
                    grabbedSlot = -1;
                } else if (inventory.getItem(slot) == 0) {
                    inventory.setSlot(slot, grabbedItem, grabbedCount);
                    grabbedItem = 0;
                    grabbedCount = 0;
                    grabbedSlot = -1;
                } else if (inventory.getItem(slot) == grabbedItem && inventory.getCount(slot) < 2) {
                    int space = 2 - inventory.getCount(slot);
                    int add = Math.min(grabbedCount, space);
                    inventory.setSlot(slot, grabbedItem, inventory.getCount(slot) + add);
                    grabbedCount -= add;
                    if (grabbedCount <= 0) {
                        grabbedItem = 0;
                        grabbedCount = 0;
                        grabbedSlot = -1;
                    } else {
                        inventory.setSlot(grabbedSlot, grabbedItem, grabbedCount);
                        grabbedItem = 0;
                        grabbedCount = 0;
                        grabbedSlot = -1;
                    }
                } else {
                    int tempItem = inventory.getItem(slot);
                    int tempCount = inventory.getCount(slot);
                    inventory.setSlot(slot, grabbedItem, grabbedCount);
                    grabbedItem = tempItem;
                    grabbedCount = tempCount;
                    grabbedSlot = slot;
                }
            }
        }
    }

    public int getSlotAt(int mouseX, int mouseY) {
        int y = screenHeight - HOTBAR_Y_OFFSET;

        for (int i = 0; i < Inventory.SLOT_COUNT; i++) {
            int x = HOTBAR_X + i * (SLOT_SIZE + SLOT_MARGIN);
            if (mouseX >= x && mouseX <= x + SLOT_SIZE &&
                    mouseY >= y && mouseY <= y + SLOT_SIZE) {
                return i;
            }
        }

        return -1;
    }

    public boolean isGrabbing() {
        return grabbedItem != 0;
    }
}