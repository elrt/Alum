package com.mojang.rubydung;

import org.lwjgl.opengl.GL11;

public class Clouds {
    private int textureId;
    private float cloudX = 0.0f;
    private float cloudSpeed = 0.0002f;
    private float cloudColorR = 1.0f;
    private float cloudColorG = 1.0f;
    private float cloudColorB = 1.0f;
    private float cloudAlpha = 0.8f;

    public Clouds() {
        textureId = Textures.loadTexture("/clouds.png", GL11.GL_LINEAR);
    }

    public void tick() {
        cloudX += cloudSpeed;
        if(cloudX > 1.0f) cloudX -= 1.0f;
    }

    public void setColor(float r, float g, float b, float alpha) {
        this.cloudColorR = r;
        this.cloudColorG = g;
        this.cloudColorB = b;
        this.cloudAlpha = alpha;
    }

    public void render(Player player, float a) {
        GL11.glDisable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glColor4f(cloudColorR, cloudColorG, cloudColorB, cloudAlpha);

        float x = player.xo + (player.x - player.xo) * a;
        float z = player.zo + (player.z - player.zo) * a;

        float cloudSize = 300.0f;
        float cloudHeight = 55.0f;

        float uOffset = cloudX + x * 0.0001f;
        float vOffset = z * 0.0001f;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(uOffset, vOffset);
        GL11.glVertex3f(x - cloudSize, cloudHeight, z - cloudSize);
        GL11.glTexCoord2f(uOffset + 2.0f, vOffset);
        GL11.glVertex3f(x + cloudSize, cloudHeight, z - cloudSize);
        GL11.glTexCoord2f(uOffset + 2.0f, vOffset + 2.0f);
        GL11.glVertex3f(x + cloudSize, cloudHeight, z + cloudSize);
        GL11.glTexCoord2f(uOffset, vOffset + 2.0f);
        GL11.glVertex3f(x - cloudSize, cloudHeight, z + cloudSize);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_FOG);
    }
}