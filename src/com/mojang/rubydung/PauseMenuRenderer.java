package com.mojang.rubydung;

import org.lwjgl.opengl.GL11;

public class PauseMenuRenderer {
    private BitmapFont font;
    private int screenWidth, screenHeight;
    private int woodTextureId;
    private int hoveredButton = -1;

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 50;
    private static final int BUTTON_SPACING = 20;

    public PauseMenuRenderer(BitmapFont font) {
        this.font = font;
        this.woodTextureId = Textures.loadTexture("/wood_tex.png", GL11.GL_LINEAR);
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

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(screenWidth, 0);
        GL11.glVertex2f(screenWidth, screenHeight);
        GL11.glVertex2f(0, screenHeight);
        GL11.glEnd();

        int totalHeight = BUTTON_HEIGHT * 2 + BUTTON_SPACING;
        int startY = (screenHeight - totalHeight) / 2;
        int startX = (screenWidth - BUTTON_WIDTH) / 2;

        int resumeY = startY;
        renderButton(startX, resumeY, BUTTON_WIDTH, BUTTON_HEIGHT, "   Resume", hoveredButton == 0);

        int exitY = startY + BUTTON_HEIGHT + BUTTON_SPACING;
        renderButton(startX, exitY, BUTTON_WIDTH, BUTTON_HEIGHT, "Save & Escepe", hoveredButton == 1);

        GL11.glPopAttrib();
    }

    private void renderButton(int x, int y, int width, int height, String text, boolean hovered) {

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, woodTextureId);

        float r = 0.55f, g = 0.35f, b = 0.20f;
        if (hovered) {
            r = 0.75f; g = 0.55f; b = 0.35f;
        }

        GL11.glColor4f(r, g, b, 0.95f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + width, y);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + height);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.85f, 0.65f, 0.45f, 1.0f);
        GL11.glLineWidth(2.5f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (font != null) {
            int textWidth = (int)(text.length() * 8 * 0.45f);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 12) / 2;
            font.draw(text, textX - 70, textY, 0.45f, 1.0f, 0.95f, 0.75f);
        }
    }

    public void handleMouseMove(int mouseX, int mouseY) {
        int totalHeight = BUTTON_HEIGHT * 2 + BUTTON_SPACING;
        int startY = (screenHeight - totalHeight) / 2;
        int startX = (screenWidth - BUTTON_WIDTH) / 2;

        hoveredButton = -1;

        int resumeY = startY;
        if (mouseX >= startX && mouseX <= startX + BUTTON_WIDTH &&
                mouseY >= resumeY && mouseY <= resumeY + BUTTON_HEIGHT) {
            hoveredButton = 0;
            return;
        }

        int exitY = startY + BUTTON_HEIGHT + BUTTON_SPACING;
        if (mouseX >= startX && mouseX <= startX + BUTTON_WIDTH &&
                mouseY >= exitY && mouseY <= exitY + BUTTON_HEIGHT) {
            hoveredButton = 1;
        }
    }

    public int handleClick(int mouseX, int mouseY) {
        int totalHeight = BUTTON_HEIGHT * 2 + BUTTON_SPACING;
        int startY = (screenHeight - totalHeight) / 2;
        int startX = (screenWidth - BUTTON_WIDTH) / 2;

        int resumeY = startY;
        if (mouseX >= startX && mouseX <= startX + BUTTON_WIDTH &&
                mouseY >= resumeY && mouseY <= resumeY + BUTTON_HEIGHT) {
            return 0;
        }

        int exitY = startY + BUTTON_HEIGHT + BUTTON_SPACING;
        if (mouseX >= startX && mouseX <= startX + BUTTON_WIDTH &&
                mouseY >= exitY && mouseY <= exitY + BUTTON_HEIGHT) {
            return 1;
        }

        return -1;
    }
}