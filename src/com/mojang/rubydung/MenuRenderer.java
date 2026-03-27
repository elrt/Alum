package com.mojang.rubydung;

import org.lwjgl.opengl.GL11;

public class MenuRenderer {
    private BitmapFont font;
    private int screenWidth, screenHeight;
    private SaveSlot[] saveSlots;
    private int selectedSlot = -1;
    private int hoveredSlot = -1;
    private int backgroundTextureId;
    private int woodTextureId;

    private static final int BUTTON_WIDTH = 280;
    private static final int BUTTON_HEIGHT = 55;
    private static final int SLOT_HEIGHT = 65;
    private static final int START_X = 20;
    private static final int START_Y_OFFSET = 180;
    private static final int SLOT_SPACING = 75;

    private static final int SETTINGS_WIDTH = 280;
    private static final int SETTINGS_HEIGHT = 40;
    private static final int BUTTONS_X_OFFSET = 20;
    private static final int BUTTONS_Y_OFFSET = 60;

    public MenuRenderer(BitmapFont font) {
        this.font = font;
        saveSlots = new SaveSlot[SaveSlot.MAX_SLOTS];
        for (int i = 0; i < SaveSlot.MAX_SLOTS; i++) {
            saveSlots[i] = new SaveSlot(i);
        }
        backgroundTextureId = Textures.loadTexture("/bg.png", GL11.GL_LINEAR);
        woodTextureId = Textures.loadTexture("/wood_tex.png", GL11.GL_LINEAR);
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
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, backgroundTextureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(screenWidth, 0);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(screenWidth, screenHeight);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(0, screenHeight);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.55f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(screenWidth, 0);
        GL11.glVertex2f(screenWidth, screenHeight);
        GL11.glVertex2f(0, screenHeight);
        GL11.glEnd();

        if (font != null) {
            font.draw("Alum", screenWidth / 2 - 50, screenHeight / 2 - 20, 1f, 0.95f, 0.75f, 0.45f);
        }
        int totalHeight = SaveSlot.MAX_SLOTS * SLOT_SPACING;
        int startY = (screenHeight - totalHeight) / 2;

        for (int i = 0; i < SaveSlot.MAX_SLOTS; i++) {
            SaveSlot slot = saveSlots[i];
            boolean isHovered = (hoveredSlot == i);
            boolean isSelected = (selectedSlot == i);

            int y = startY + i * SLOT_SPACING;

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, woodTextureId);

            float r = 0.55f, g = 0.35f, b = 0.20f;
            if (isHovered) {
                r = 0.75f; g = 0.55f; b = 0.35f;
            }
            if (isSelected) {
                r = 0.85f; g = 0.65f; b = 0.40f;
            }

            GL11.glColor4f(r, g, b, 0.95f);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(START_X, y);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(START_X + BUTTON_WIDTH, y);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(START_X + BUTTON_WIDTH, y + SLOT_HEIGHT);
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(START_X, y + SLOT_HEIGHT);
            GL11.glEnd();

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.75f, 0.55f, 0.35f, 1.0f);
            GL11.glLineWidth(2.5f);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex2f(START_X, y);
            GL11.glVertex2f(START_X + BUTTON_WIDTH, y);
            GL11.glVertex2f(START_X + BUTTON_WIDTH, y + SLOT_HEIGHT);
            GL11.glVertex2f(START_X, y + SLOT_HEIGHT);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            if (font != null) {
                String title = slot.getWorldName();
                String info = slot.exists() ? slot.getLastPlayedString() : "New world";
                float infoColor = slot.exists() ? 0.85f : 0.95f;

                font.draw(title, START_X + 15, y + 18, 0.48f, 1.0f, 0.95f, 0.75f);
                font.draw(info, START_X + 15, y + 48, 0.32f, infoColor, infoColor * 0.8f, infoColor * 0.6f);

                if (slot.exists()) {
                    font.draw(">", START_X + BUTTON_WIDTH - 28, y + 28, 0.45f, 0.95f, 0.75f, 0.45f);
                }
            }
        }

        int buttonsX = screenWidth - BUTTONS_X_OFFSET - SETTINGS_WIDTH;
        int settingsY = BUTTONS_Y_OFFSET;
        int exitY = settingsY + SETTINGS_HEIGHT + 15;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, woodTextureId);
        GL11.glColor4f(0.55f, 0.35f, 0.20f, 0.95f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(buttonsX, settingsY);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, settingsY);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, settingsY + SETTINGS_HEIGHT);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(buttonsX, settingsY + SETTINGS_HEIGHT);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.75f, 0.55f, 0.35f, 1.0f);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(buttonsX, settingsY);
        GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, settingsY);
        GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, settingsY + SETTINGS_HEIGHT);
        GL11.glVertex2f(buttonsX, settingsY + SETTINGS_HEIGHT);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (font != null) {
            font.draw("Settings", buttonsX + 95, settingsY + 12, 0.4f, 0.95f, 0.85f, 0.65f);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, woodTextureId);
        GL11.glColor4f(0.65f, 0.40f, 0.25f, 0.95f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(buttonsX, exitY);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, exitY);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, exitY + SETTINGS_HEIGHT);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(buttonsX, exitY + SETTINGS_HEIGHT);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.85f, 0.55f, 0.35f, 1.0f);
        GL11.glLineWidth(2.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(buttonsX, exitY);
        GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, exitY);
        GL11.glVertex2f(buttonsX + SETTINGS_WIDTH, exitY + SETTINGS_HEIGHT);
        GL11.glVertex2f(buttonsX, exitY + SETTINGS_HEIGHT);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (font != null) {
            font.draw("Escape", buttonsX + 105, exitY + 12, 0.4f, 1.0f, 0.8f, 0.6f);
        }

        GL11.glPopAttrib();
    }

    public void handleMouseMove(int mouseX, int mouseY) {
        int totalHeight = SaveSlot.MAX_SLOTS * SLOT_SPACING;
        int startY = (screenHeight - totalHeight) / 2;
        hoveredSlot = -1;

        for (int i = 0; i < SaveSlot.MAX_SLOTS; i++) {
            int y = startY + i * SLOT_SPACING;
            if (mouseX >= START_X && mouseX <= START_X + BUTTON_WIDTH &&
                    mouseY >= y && mouseY <= y + SLOT_HEIGHT) {
                hoveredSlot = i;
                break;
            }
        }
    }

    public int handleClick(int mouseX, int mouseY) {
        int totalHeight = SaveSlot.MAX_SLOTS * SLOT_SPACING;
        int startY = (screenHeight - totalHeight) / 2;

        for (int i = 0; i < SaveSlot.MAX_SLOTS; i++) {
            int y = startY + i * SLOT_SPACING;
            if (mouseX >= START_X && mouseX <= START_X + BUTTON_WIDTH &&
                    mouseY >= y && mouseY <= y + SLOT_HEIGHT) {
                selectedSlot = i;
                return i;
            }
        }

        int buttonsX = screenWidth - BUTTONS_X_OFFSET - SETTINGS_WIDTH;
        int settingsY = BUTTONS_Y_OFFSET;

        if (mouseX >= buttonsX && mouseX <= buttonsX + SETTINGS_WIDTH &&
                mouseY >= settingsY && mouseY <= settingsY + SETTINGS_HEIGHT) {
            return -2;
        }

        int exitY = settingsY + SETTINGS_HEIGHT + 15;
        if (mouseX >= buttonsX && mouseX <= buttonsX + SETTINGS_WIDTH &&
                mouseY >= exitY && mouseY <= exitY + SETTINGS_HEIGHT) {
            return -3;
        }

        return -1;
    }

    public SaveSlot getSelectedSaveSlot() {
        if (selectedSlot >= 0 && selectedSlot < SaveSlot.MAX_SLOTS) {
            return saveSlots[selectedSlot];
        }
        return null;
    }

    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < SaveSlot.MAX_SLOTS) {
            selectedSlot = slot;
        }
    }
    public SaveSlot getSaveSlot(int slotId) {
        if (slotId >= 0 && slotId < SaveSlot.MAX_SLOTS) {
            return saveSlots[slotId];
        }
        return null;
    }
}