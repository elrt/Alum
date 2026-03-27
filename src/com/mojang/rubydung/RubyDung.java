package com.mojang.rubydung;

import com.mojang.rubydung.character.Cube;
import com.mojang.rubydung.character.Zombie;
import com.mojang.rubydung.inventory.Inventory;
import com.mojang.rubydung.inventory.InventoryRenderer;
import com.mojang.rubydung.level.Chunk;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelRenderer;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class RubyDung implements Runnable {
	private static final boolean FULLSCREEN_MODE = false;
	private int width;
	private int height;
	private FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);
	private Timer timer = new Timer(60.0F);
	private Level level;
	private LevelRenderer levelRenderer;
	private Player player;
	private ArrayList<Zombie> zombies = new ArrayList();
	private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
	private IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);
	private HitResult hitResult = null;
	private BitmapFont font;
	private Clouds clouds;
	private Inventory inventory;
	private InventoryRenderer inventoryRenderer;
	private int armTextureId;
	private int crosshairTextureId;
	private float armSwing = 0.0f;
	private float armSwingX = 0.0f;
	private float armSwingY = 0.0f;
	private float armPunch = 0.0f;
	private boolean punching = false;
	private GameState gameState = GameState.MENU;
	private MenuRenderer menuRenderer;
	private PauseMenuRenderer pauseMenuRenderer;
	private boolean paused = false;

	private SoundManager soundManager;

	private boolean isDay = true;
	private float nightBrightness = 0.3f;
	private float dayBrightness = 1.0f;
	private float currentBrightness = 1.0f;

	private ByteBuffer convertToByteBuffer(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);

		int[] pixels = new int[w * h];
		image.getRGB(0, 0, w, h, pixels, 0, w);

		for (int i = 0; i < pixels.length; i++) {
			int a = (pixels[i] >> 24) & 0xFF;
			int r = (pixels[i] >> 16) & 0xFF;
			int g = (pixels[i] >> 8) & 0xFF;
			int b = pixels[i] & 0xFF;

			buffer.put((byte)r);
			buffer.put((byte)g);
			buffer.put((byte)b);
			buffer.put((byte)a);
		}

		buffer.flip();
		return buffer;
	}

	public void init() throws LWJGLException, IOException {
		float fr = 0.5F;
		float fg = 0.8F;
		float fb = 1.0F;

		this.fogColor.put(new float[]{fr, fg, fb, 1.0F});
		this.fogColor.flip();

		Display.setDisplayMode(new DisplayMode(1024, 768));
		Display.create();

		try {
			//ETO XYITA VOOBSHE NE RABOTAET NAXYA YA ETO PISAL (ya nixochy ybirat vdryg zarabotaet
			BufferedImage icon = ImageIO.read(Textures.class.getResourceAsStream("/icon.png"));
			ByteBuffer[] icons = new ByteBuffer[1];
			icons[0] = convertToByteBuffer(icon);
			Display.setIcon(icons);
			System.out.println("Window icon loded: " + icon.getWidth() + "x" + icon.getHeight());
		} catch (Exception e) {
			System.out.println("Failed to load window icon: " + e.getMessage());
		}

		Keyboard.create();
		Mouse.create();
		this.width = Display.getDisplayMode().getWidth();
		this.height = Display.getDisplayMode().getHeight();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(fr, fg, fb, 0.0F);
		GL11.glClearDepth(1.0D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);

		this.level = new Level(256, 256, 64);
		this.levelRenderer = new LevelRenderer(this.level);
		this.player = new Player(this.level);

		font = new BitmapFont();
		System.out.println("Shrift initialized");

		armTextureId = Textures.loadTexture("/arm.png", GL11.GL_LINEAR);
		System.out.println("Arm texture loaded: " + armTextureId);

		crosshairTextureId = Textures.loadTexture("/circle.png", GL11.GL_LINEAR);
		System.out.println("Kryjochek circle texture loaded: " + crosshairTextureId);

		inventory = new Inventory();
		inventoryRenderer = new InventoryRenderer(inventory, font);
		inventoryRenderer.setScreenSize(width, height);

		menuRenderer = new MenuRenderer(font);
		menuRenderer.setScreenSize(width, height);

		pauseMenuRenderer = new PauseMenuRenderer(font);
		pauseMenuRenderer.setScreenSize(width, height);

		try {
			soundManager = SoundManager.getInstance();
			System.out.println("Sound system RABOTAET");
		} catch (Exception e) {
			System.err.println("FAILED to initialize sound: " + e.getMessage());
		}

		clouds = new Clouds();
		Mouse.setGrabbed(false);
	}

	private void updateTimeOfDay() {
		if (isDay) {
			currentBrightness = dayBrightness;
			GL11.glClearColor(0.5F, 0.8F, 1.0F, 0.0F);
			this.fogColor.put(new float[]{0.5F, 0.8F, 1.0F, 1.0F});
			this.fogColor.flip();

			if (clouds != null) {
				clouds.setColor(1.0f, 1.0f, 1.0f, 0.8f);
			}
			if (soundManager != null) {
				soundManager.playMusic("/background.wav");
			}
		} else {
			currentBrightness = nightBrightness;
			GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
			this.fogColor.put(new float[]{0.0F, 0.0F, 0.0F, 0.0F});
			this.fogColor.flip();

			if (clouds != null) {
				clouds.setColor(1f, 1f, 1f, 0.4f);
			}
			if (soundManager != null) {
				soundManager.playMusic("/night.wav");
			}
		}


	}

	public void destroy() {
		if (level != null) {
			level.save("level.dat");
		}
		if (soundManager != null) {
			soundManager.cleanup();
		}
		Mouse.destroy();
		Keyboard.destroy();
		Display.destroy();
	}

	public void run() {
		try {
			this.init();
		} catch (Exception var9) {
			JOptionPane.showMessageDialog((Component)null, var9.toString(), "Failed to start RubyDung", 0);
			System.exit(0);
		}

		long lastTime = System.currentTimeMillis();
		int frames = 0;

		try {
			while(!Display.isCloseRequested()) {
				this.timer.advanceTime();

				if (gameState == GameState.PLAYING && !paused) {
					for(int e = 0; e < this.timer.ticks; ++e) {
						this.tick();
					}
				}

				if (Display.wasResized()) {
					width = Display.getWidth();
					height = Display.getHeight();
					if (inventoryRenderer != null) {
						inventoryRenderer.setScreenSize(width, height);
					}
					if (menuRenderer != null) {
						menuRenderer.setScreenSize(width, height);
					}
					if (pauseMenuRenderer != null) {
						pauseMenuRenderer.setScreenSize(width, height);
					}
					GL11.glViewport(0, 0, width, height);
				}

				this.render(this.timer.a);
				++frames;

				while(System.currentTimeMillis() >= lastTime + 1000L) {
					System.out.println(frames + " fps, " + Chunk.updates);
					Chunk.updates = 0;
					lastTime += 1000L;
					frames = 0;
				}
			}
		} catch (Exception var10) {
			var10.printStackTrace();
		} finally {
			this.destroy();
		}

	}

	public void tick() {
		for(int i = 0; i < this.zombies.size(); ++i) {
			((Zombie)this.zombies.get(i)).tick();
		}
		this.player.tick();

		if(clouds != null) {
			clouds.tick();
		}

		updateArmAnimation();
	}

	private void createNewWorld(int slotId) {
		this.level = new Level(256, 256, 64);
		this.levelRenderer = new LevelRenderer(this.level);
		this.player = new Player(this.level);
		this.level.save("save_" + slotId + ".dat");
		SaveSlot slot = menuRenderer.getSelectedSaveSlot();
		if (slot != null) {
			slot.createNewWorld("World " + (slotId + 1));
		}

	}

	private void loadWorld(int slotId) {
		this.level = new Level(256, 256, 64);
		this.levelRenderer = new LevelRenderer(this.level);
		this.player = new Player(this.level);
		this.level.load("save_" + slotId + ".dat");

	}

	private void moveCameraToPlayer(float a) {
		GL11.glTranslatef(0.0F, 0.0F, -0.3F);
		GL11.glRotatef(this.player.xRot, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(this.player.yRot, 0.0F, 1.0F, 0.0F);
		float x = this.player.xo + (this.player.x - this.player.xo) * a;
		float y = this.player.yo + (this.player.y - this.player.yo) * a;
		float z = this.player.zo + (this.player.z - this.player.zo) * a;
		GL11.glTranslatef(-x, -y, -z);
	}

	private void setupCamera(float a) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, 1000.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		this.moveCameraToPlayer(a);
	}

	private void setupPickCamera(float a, int x, int y) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		this.viewportBuffer.clear();
		GL11.glGetInteger(GL11.GL_VIEWPORT, this.viewportBuffer);
		this.viewportBuffer.flip();
		this.viewportBuffer.limit(16);
		GLU.gluPickMatrix((float)x, (float)y, 5.0F, 5.0F, this.viewportBuffer);
		GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, 1000.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		this.moveCameraToPlayer(a);
	}

	private void pick(float a) {
		this.selectBuffer.clear();
		GL11.glSelectBuffer(this.selectBuffer);
		GL11.glRenderMode(GL11.GL_SELECT);
		this.setupPickCamera(a, this.width / 2, this.height / 2);
		this.levelRenderer.pick(this.player);
		int hits = GL11.glRenderMode(GL11.GL_RENDER);
		this.selectBuffer.flip();
		this.selectBuffer.limit(this.selectBuffer.capacity());
		long closest = 0L;
		int[] names = new int[10];
		int hitNameCount = 0;

		for(int i = 0; i < hits; ++i) {
			int nameCount = this.selectBuffer.get();
			long minZ = (long)this.selectBuffer.get();
			this.selectBuffer.get();
			int j;
			if(minZ >= closest && i != 0) {
				for(j = 0; j < nameCount; ++j) {
					this.selectBuffer.get();
				}
			} else {
				closest = minZ;
				hitNameCount = nameCount;

				for(j = 0; j < nameCount; ++j) {
					names[j] = this.selectBuffer.get();
				}
			}
		}

		if(hitNameCount > 0) {
			this.hitResult = new HitResult(names[0], names[1], names[2], names[3], names[4]);
		} else {
			this.hitResult = null;
		}

	}

	private void startArmPunch() {
		punching = true;
		armPunch = 0.0f;
	}

	private void updateArmAnimation() {
		if (punching) {
			armPunch += 0.25f;
			if (armPunch >= 1.0f) {
				punching = false;
			}
		} else {
			armPunch *= 0.9f;
		}

		float xa = 0.0f;
		float za = 0.0f;

		if (Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W)) {
			za = -1.0f;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S)) {
			za = 1.0f;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A)) {
			xa = -1.0f;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D)) {
			xa = 1.0f;
		}

		float speed = 0.0f;
		boolean wasMoving = false;
		if (player != null && player.onGround && (Math.abs(xa) > 0.01f || Math.abs(za) > 0.01f)) {
			speed = 0.25f;
			wasMoving = true;
		}

		float oldCycle = armSwing;
		armSwing += speed;
		if (armSwing > Math.PI * 2) {
			armSwing -= Math.PI * 2;
		}

		if (wasMoving && (int)(oldCycle / (Math.PI * 2)) != (int)(armSwing / (Math.PI * 2))) {
			if (soundManager != null && player.onGround) {
				soundManager.playStepSound(player.x, player.y - 0.5f, player.z);
			}
		}

		float swingSin = (float)Math.sin(armSwing);

		float forwardBack = 0.0f;
		float leftRight = 0.0f;

		if (Math.abs(za) > 0.01f) {
			forwardBack = -za * swingSin * 6.0f;
		}

		if (Math.abs(xa) > 0.01f) {
			leftRight = xa * swingSin * 6.0f;
		}

		armSwingX = armSwingX * 0.8f + forwardBack * 0.2f;
		armSwingY = armSwingY * 0.8f + leftRight * 0.2f;

		armSwingX = Math.max(-12.0f, Math.min(12.0f, armSwingX));
		armSwingY = Math.max(-12.0f, Math.min(12.0f, armSwingY));

		if (Math.abs(xa) < 0.01f && Math.abs(za) < 0.01f) {
			armSwingX *= 0.95f;
			armSwingY *= 0.95f;
		}
	}

	private void renderArm() {
		int armWidth = 178;
		int armHeight = 178;
		int armX = width - armWidth - 20;
		int armY = height - armHeight + 20;

		float punchOffset = 0.0f;
		if (punching) {
			float t = armPunch;
			punchOffset = -(float)Math.sin(t * Math.PI) * 25.0f;
		} else {
			punchOffset = -armPunch * 5.0f;
		}

		float bobX = armSwingX;
		float bobY = armSwingY + punchOffset;

		GL11.glPushMatrix();

		GL11.glTranslatef(armX + armWidth / 2, armY + armHeight / 2, 0);
		GL11.glTranslatef(bobX, bobY, 0);
		GL11.glTranslatef(-(armX + armWidth / 2), -(armY + armHeight / 2), 0);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, armTextureId);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0); GL11.glVertex2f(armX, armY);
		GL11.glTexCoord2f(1, 0); GL11.glVertex2f(armX + armWidth, armY);
		GL11.glTexCoord2f(1, 1); GL11.glVertex2f(armX + armWidth, armY + armHeight);
		GL11.glTexCoord2f(0, 1); GL11.glVertex2f(armX, armY + armHeight);
		GL11.glEnd();

		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
	}

	private void renderCrosshair() {
		int size = 32;
		int x = (width - size) / 2;
		int y = (height - size) / 2;

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, crosshairTextureId);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
		GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + size, y);
		GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + size, y + size);
		GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + size);
		GL11.glEnd();

		GL11.glDisable(GL11.GL_BLEND);
	}

	private void renderMenu() {
		while (Keyboard.next()) {
			int key = Keyboard.getEventKey();
			boolean pressed = Keyboard.getEventKeyState();
			if (key == Keyboard.KEY_ESCAPE && pressed) {
				System.exit(0);
			}
		}

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		menuRenderer.render();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
	}

	private void renderGameWorld(float a) {
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		this.setupCamera(a);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glEnable(GL11.GL_FOG);
		GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
		GL11.glFogf(GL11.GL_FOG_DENSITY, isDay ? 0.008F : 0.03F);
		GL11.glFog(GL11.GL_FOG_COLOR, this.fogColor);

		this.levelRenderer.render(this.player, 0);

		for(int i = 0; i < this.zombies.size(); ++i) {
			((Zombie)this.zombies.get(i)).render(a);
		}

		this.levelRenderer.render(this.player, 1);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		if(clouds != null) {
			if (!isDay) {
				GL11.glColor4f(0.3f, 0.3f, 0.4f, 0.8f);
			} else {
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);
			}
			clouds.render(this.player, a);
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		if(this.hitResult != null) {
			this.levelRenderer.renderHit(this.hitResult);
		}
		GL11.glDisable(GL11.GL_BLEND);

		new Cube(0, 0);
	}

	private void renderHUD() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		String coords = "XYZ: " + (int)player.x + " " + (int)player.y + " " + (int)player.z;
		font.draw(coords, 10, 30, 0.4f, 1.0f, 1.0f, 1.0f);

		renderHealthBar();

		if (inventoryRenderer != null) {
			inventoryRenderer.render();
		}

		renderCrosshair();
		renderArm();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
	}

	private void renderHealthBar() {
		int barWidth = 200;
		int barHeight = 20;
		int x = (width - barWidth) / 2;
		int y = 20;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(0.2f, 0.2f, 0.2f, 0.8f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + barWidth, y);
		GL11.glVertex2f(x + barWidth, y + barHeight);
		GL11.glVertex2f(x, y + barHeight);
		GL11.glEnd();

		int healthPercent = player.health * 100 / player.maxHealth;
		int healthWidth = barWidth * healthPercent / 100;

		GL11.glColor4f(0.8f, 0.2f, 0.2f, 0.9f);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + healthWidth, y);
		GL11.glVertex2f(x + healthWidth, y + barHeight);
		GL11.glVertex2f(x, y + barHeight);
		GL11.glEnd();

		GL11.glColor4f(0.6f, 0.6f, 0.6f, 1.0f);
		GL11.glLineWidth(2.0f);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex2f(x, y);
		GL11.glVertex2f(x + barWidth, y);
		GL11.glVertex2f(x + barWidth, y + barHeight);
		GL11.glVertex2f(x, y + barHeight);
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		if (font != null) {
			String hpText = player.health + "";
			int textWidth = (int)(hpText.length() * 8 * 0.5f);
			int textX = x + (barWidth - textWidth) / 2;
			int textY = y + (barHeight - 10) / 2;
			font.draw(hpText, textX, textY, 0.5f, 1.0f, 1.0f, 1.0f);
		}
	}

	private void renderCRTOverlay() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, -1, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		int overlayTex = Textures.loadTexture("/crt_overlay.png", GL11.GL_LINEAR);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlayTex);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.3F);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0); GL11.glVertex2f(0, 0);
		GL11.glTexCoord2f(1, 0); GL11.glVertex2f(width, 0);
		GL11.glTexCoord2f(1, 1); GL11.glVertex2f(width, height);
		GL11.glTexCoord2f(0, 1); GL11.glVertex2f(0, height);
		GL11.glEnd();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
	}

	private void renderGame(float a) {
		updateTimeOfDay();

		if (soundManager != null) {
			float x = player.xo + (player.x - player.xo) * a;
			float y = player.yo + (player.y - player.yo) * a + 1.62f;
			float z = player.zo + (player.z - player.zo) * a;
			soundManager.updateListener(x, y, z, player.yRot, player.xRot);
		}
		if (!paused) {
			float xo = (float)Mouse.getDX();
			float yo = (float)Mouse.getDY();
			this.player.turn(xo, yo);
			this.pick(a);
		}
		while (Mouse.next()) {
			int button = Mouse.getEventButton();
			boolean pressed = Mouse.getEventButtonState();
			int mouseX = Mouse.getEventX();
			int mouseY = height - Mouse.getEventY();

			if (paused) {
				if (pressed && button == 0) {
					int result = pauseMenuRenderer.handleClick(mouseX, mouseY);
					if (result == 0) {
						paused = false;
						Mouse.setGrabbed(true);
					} else if (result == 1) {
						this.level.save("level.dat");
						gameState = GameState.MENU;
						paused = false;
						Mouse.setGrabbed(false);
					}
				}
				pauseMenuRenderer.handleMouseMove(mouseX, mouseY);
			} else {
				if (pressed && button == 0) {
					int slot = inventoryRenderer.getSlotAt(mouseX, mouseY);
					if (slot != -1) {
						inventoryRenderer.handleInventoryClick(slot, 0);
					}
				}

				if (!inventoryRenderer.isGrabbing()) {
					if (button == 0 && pressed && this.hitResult != null) {
						boolean hasEmptySlot = false;
						boolean hasSameSlot = false;
						int blockType = this.level.getTileType(this.hitResult.x, this.hitResult.y, this.hitResult.z);

						if (blockType != 0) {
							if (soundManager != null) {
								soundManager.playBreakSound(blockType,
										this.hitResult.x + 0.5f,
										this.hitResult.y + 0.5f,
										this.hitResult.z + 0.5f);
							}

							for (int i = 0; i < Inventory.SLOT_COUNT; i++) {
								if (inventory.getItem(i) == 0) {
									hasEmptySlot = true;
									break;
								}
								if (inventory.getItem(i) == blockType && inventory.getCount(i) < 2) {
									hasSameSlot = true;
									break;
								}
							}

							if (hasEmptySlot || hasSameSlot) {
								this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
								inventory.takeItem(blockType);
								startArmPunch();
							}
						}
					}

					if (button == 1 && pressed && this.hitResult != null && inventory.hasItemInSelected()) {
						int x = this.hitResult.x;
						int y = this.hitResult.y;
						int z = this.hitResult.z;
						if (this.hitResult.f == 0) { --y; }
						if (this.hitResult.f == 1) { ++y; }
						if (this.hitResult.f == 2) { --z; }
						if (this.hitResult.f == 3) { ++z; }
						if (this.hitResult.f == 4) { --x; }
						if (this.hitResult.f == 5) { ++x; }

						if (this.level.getTileType(x, y, z) == 0) {
							this.level.setTile(x, y, z, inventory.getSelectedItem());
							inventory.useItem();
							startArmPunch();

							if (soundManager != null) {
								soundManager.playBreakSound(inventory.getSelectedItem(), x + 0.5f, y + 0.5f, z + 0.5f);
							}
						}
					}
				}
			}
		}
		while(Keyboard.next()) {
			int key = Keyboard.getEventKey();
			boolean pressed = Keyboard.getEventKeyState();

			if (key == Keyboard.KEY_RETURN && pressed) {
				this.level.save("level.dat");
			}
			if (key == Keyboard.KEY_F5 && pressed) {
				this.level = new Level(256, 256, 64);
				this.levelRenderer = new LevelRenderer(this.level);
				this.player = new Player(this.level);
				System.out.println("!!World regenerated!!");
			}
			if (key == Keyboard.KEY_T && pressed) {
				isDay = !isDay;
				if (isDay) {
					System.out.println("Day mod");
				} else {
					System.out.println("Night mod");
				}
			}

			if (key == Keyboard.KEY_ESCAPE && pressed) {
				if (paused) {
					paused = false;
					Mouse.setGrabbed(true);
				} else {
					paused = true;
					Mouse.setGrabbed(false);
				}
			}

			if (!paused && pressed) {
				if (key == Keyboard.KEY_1) {
					inventory.setSelectedSlot(0);
				}
				if (key == Keyboard.KEY_2) {
					inventory.setSelectedSlot(1);
				}
			}
		}

		if (!paused && !inventoryRenderer.isGrabbing()) {
			int scroll = Mouse.getDWheel();
			if (scroll != 0) {
				int delta = scroll / 120;
				inventory.scrollSelected(delta);
			}
		}

		renderGameWorld(a);
		renderHUD();

		if (paused) {
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glOrtho(0, width, height, 0, -1, 1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			GL11.glLoadIdentity();

			pauseMenuRenderer.render();

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glPopMatrix();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
		}

		renderCRTOverlay();
	}

	public void render(float a) {
		if (gameState == GameState.MENU) {
			while (Mouse.next()) {
				int button = Mouse.getEventButton();
				boolean pressed = Mouse.getEventButtonState();
				int mouseX = Mouse.getEventX();
				int mouseY = height - Mouse.getEventY();

				if (pressed && button == 0) {
					int result = menuRenderer.handleClick(mouseX, mouseY);
					if (result >= 0) {
						SaveSlot slot = menuRenderer.getSelectedSaveSlot();
						if (slot != null && slot.exists()) {
							loadWorld(result);
							gameState = GameState.PLAYING;
							paused = false;
							Mouse.setGrabbed(true);
						} else if (slot != null && !slot.exists()) {
							createNewWorld(result);
							gameState = GameState.PLAYING;
							paused = false;
							Mouse.setGrabbed(true);
						}
					} else if (result == -3) {
						System.exit(0);
					}
				}
			}

			int mouseX = Mouse.getX();
			int mouseY = height - Mouse.getY();
			menuRenderer.handleMouseMove(mouseX, mouseY);

			renderMenu();
		} else {
			renderGame(a);
		}
		Display.update();
	}

	public static void checkError() {
		int e = GL11.glGetError();
		if(e != 0) {
			throw new IllegalStateException(GLU.gluErrorString(e));
		}
	}

	public static void main(String[] args) throws LWJGLException {
		(new Thread(new RubyDung())).start();
	}
}