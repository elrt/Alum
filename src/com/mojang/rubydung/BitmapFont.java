package com.mojang.rubydung;

import org.lwjgl.opengl.GL11;

public class BitmapFont {
    private int textureId;
    private int charWidth = 32;
    private int charHeight = 32;
    private int charsPerRow = 10;
    private int firstChar = 32;//probel
    private int lastChar = 126; //~

    public BitmapFont() {
        textureId = Textures.loadTexture("/all_32x32.png", GL11.GL_NEAREST);
    }

    public void draw(String text, int x, int y, float scale, float r, float g, float b) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor3f(r, g, b);

        int currentX = x;
        int currentY = y;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                currentX = x;
                currentY += (int)(charHeight * scale);
                continue;
            }

            int code = (int)c;
            if (code < firstChar || code > lastChar) {
                code = firstChar;
            }
            int index = code - firstChar;
            int row = index / charsPerRow;
            int col = index % charsPerRow;

            float u0 = (float)col / charsPerRow;
            float u1 = (float)(col + 1) / charsPerRow;
            float v0 = (float)row / charsPerRow;
            float v1 = (float)(row + 1) / charsPerRow;

            float x0 = currentX;
            float y0 = currentY;
            float x1 = currentX + charWidth * scale;
            float y1 = currentY + charHeight * scale;

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(u0, v0); GL11.glVertex2f(x0, y0);
            GL11.glTexCoord2f(u1, v0); GL11.glVertex2f(x1, y0);
            GL11.glTexCoord2f(u1, v1); GL11.glVertex2f(x1, y1);
            GL11.glTexCoord2f(u0, v1); GL11.glVertex2f(x0, y1);
            GL11.glEnd();

            currentX += charWidth * scale;
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public int getWidth(String text, float scale) {
        return (int)(text.length() * charWidth * scale);
    }

    public int getHeight(float scale) {
        return (int)(charHeight * scale);
    }
}