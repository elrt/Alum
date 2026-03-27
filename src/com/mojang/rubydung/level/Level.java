package com.mojang.rubydung.level;

import com.mojang.rubydung.phys.AABB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Level {
	public final int width;
	public final int height;
	public final int depth;
	private byte[] blocks;
	private int[] lightDepths;
	private ArrayList<LevelListener> levelListeners = new ArrayList();
	private Random random;
	private float worldOffsetX;
	private float worldOffsetZ;

	private int getIndex(int x, int y, int z) {
		return (y * height + z) * width + x;
	}

	public Level(int w, int h, int d) {
		this.width = w;
		this.height = h;
		this.depth = d;
		this.blocks = new byte[w * h * d];
		this.lightDepths = new int[w * h];
		this.random = new Random();

		this.worldOffsetX = random.nextFloat() * 1000.0f;
		this.worldOffsetZ = random.nextFloat() * 1000.0f;

			System.out.println("GENERATING world " + w + "x" + h + "x" + d + " with seed offset: " + worldOffsetX);

		for(int x = 0; x < w; ++x) {
			for(int z = 0; z < h; ++z) {
				int surfaceHeight = getSmoothHeight(x, z);

				for(int y = 0; y < d; ++y) {
					int i = getIndex(x, y, z);

					if(y < surfaceHeight - 3) {
						this.blocks[i] = 2;
					}
					else if(y < surfaceHeight) {
						this.blocks[i] = 1;
					}
					else {
						this.blocks[i] = 0;
					}
				}
			}
		}

		System.out.println("GENERATING trees...");
		int treeCount = 0;
		for(int x = 4; x < w - 4; x++) {
			for(int z = 4; z < h - 4; z++) {
				int surfaceHeight = getSmoothHeight(x, z);
				int groundY = surfaceHeight - 1;
				int groundType = getTileType(x, groundY, z);

				if(groundType == 1) {
					boolean clear = true;
					for(int dy = 1; dy <= 6; dy++) {
						int aboveType = getTileType(x, groundY + dy, z);
						if(aboveType != 0) {
							clear = false;
							break;
						}
					}
				}
			}
		}

		for(int i = 0; i < this.levelListeners.size(); ++i) {
			((LevelListener)this.levelListeners.get(i)).allChanged();
		}

		this.calcLightDepths(0, 0, w, h);
	}

	private int getSmoothHeight(int x, int z) {
		int baseHeight = 32;
		float freq1 = 0.02f;
		float amp1 = 8.0f;

		float h1 = (float)(Math.sin((x + worldOffsetX) * freq1) * Math.cos((z + worldOffsetZ) * freq1)) * amp1;
		float height = baseHeight + h1;

		if(height < 20) height = 20;
		if(height > 50) height = 50;

		return (int)height;
	}

	private void generateTree(int x, int y, int z) {

		for(int i = 0; i < 4; i++) {
			int yi = y + i;
			if(yi < depth) {
				setTile(x, yi, z, 3);
			}
		}
		setTile(x, y + 4, z, 4);
		int crownY = y + 2;
		for(int dx = -2; dx <= 2; dx++) {
			for(int dz = -2; dz <= 2; dz++) {
				int dist = Math.abs(dx) + Math.abs(dz);
				if(dist <= 2) {
					int xc = x + dx;
					int zc = z + dz;
					if(xc >= 0 && xc < width && zc >= 0 && zc < height) {
						if(getTileType(xc, crownY, zc) == 0) {
							setTile(xc, crownY, zc, 4);
						}
					}
				}
			}
		}
		int crownY2 = y + 3;
		for(int dx = -1; dx <= 1; dx++) {
			for(int dz = -1; dz <= 1; dz++) {
				if(dx != 0 || dz != 0) {
					int xc = x + dx;
					int zc = z + dz;
					if(xc >= 0 && xc < width && zc >= 0 && zc < height) {
						if(getTileType(xc, crownY2, zc) == 0) {
							setTile(xc, crownY2, zc, 4);
						}
					}
				}
			}
		}
	}

	public void load() {
		load("level.dat");
	}

	public void load(String filename) {
		try {
			DataInputStream e = new DataInputStream(new GZIPInputStream(new FileInputStream(new File(filename))));
			e.readFully(this.blocks);
			this.calcLightDepths(0, 0, this.width, this.height);
			for(int i = 0; i < this.levelListeners.size(); ++i) {
				((LevelListener)this.levelListeners.get(i)).allChanged();
			}
			e.close();
		} catch (Exception var3) {
			var3.printStackTrace();
		}
	}

	public void save() {
		save("level.dat");
	}

	public void save(String filename) {
		try {
			DataOutputStream e = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File(filename))));
			e.write(this.blocks);
			e.close();
			System.out.println("Game SAVED to: " + filename);
		} catch (Exception var2) {
			System.err.println("FAILED to SAVE game: " + var2.getMessage());
			var2.printStackTrace();
		}
	}

	public void calcLightDepths(int x0, int y0, int x1, int y1) {
		for(int x = x0; x < x0 + x1; ++x) {
			for(int z = y0; z < y0 + y1; ++z) {
				int oldDepth = this.lightDepths[x + z * this.width];
				int y;
				for(y = this.depth - 1; y > 0 && !this.isLightBlocker(x, y, z); --y) {
				}
				this.lightDepths[x + z * this.width] = y;
				if(oldDepth != y) {
					int yl0 = oldDepth < y ? oldDepth : y;
					int yl1 = oldDepth > y ? oldDepth : y;
					for(int i = 0; i < this.levelListeners.size(); ++i) {
						((LevelListener)this.levelListeners.get(i)).lightColumnChanged(x, z, yl0, yl1);
					}
				}
			}
		}
	}

	public void addListener(LevelListener levelListener) {
		this.levelListeners.add(levelListener);
	}

	public void removeListener(LevelListener levelListener) {
		this.levelListeners.remove(levelListener);
	}

	public boolean isSolidTile(int x, int y, int z) {
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			byte block = this.blocks[getIndex(x, y, z)];
			return block == 1 || block == 2 || block == 3 || block == 4;
		}
		return false;
	}

	public boolean isTile(int x, int y, int z) {
		return isSolidTile(x, y, z);
	}

	public boolean isLightBlocker(int x, int y, int z) {
		return isSolidTile(x, y, z);
	}

	public int getTileType(int x, int y, int z) {
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			return this.blocks[getIndex(x, y, z)];
		}
		return 0;
	}

	public ArrayList<AABB> getCubes(AABB aABB) {
		ArrayList aABBs = new ArrayList();
		int x0 = (int)aABB.x0;
		int x1 = (int)(aABB.x1 + 1.0F);
		int y0 = (int)aABB.y0;
		int y1 = (int)(aABB.y1 + 1.0F);
		int z0 = (int)aABB.z0;
		int z1 = (int)(aABB.z1 + 1.0F);
		if(x0 < 0) x0 = 0;
		if(y0 < 0) y0 = 0;
		if(z0 < 0) z0 = 0;
		if(x1 > this.width) x1 = this.width;
		if(y1 > this.depth) y1 = this.depth;
		if(z1 > this.height) z1 = this.height;
		for(int x = x0; x < x1; ++x) {
			for(int y = y0; y < y1; ++y) {
				for(int z = z0; z < z1; ++z) {
					if(this.isSolidTile(x, y, z)) {
						aABBs.add(new AABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1)));
					}
				}
			}
		}
		return aABBs;
	}

	public float getBrightness(int x, int y, int z) {
		float dark = 0.8F;
		float light = 1.0F;
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			int block = getTileType(x, y, z);
			if(block == 4) {
				int yy = y + 1;
				while(yy < depth && getTileType(x, yy, z) == 4) {
					yy++;
				}
				if(yy < depth && isSolidTile(x, yy, z)) {
					return (yy < this.lightDepths[x + z * this.width] ? dark : light);
				}
				return light;
			}
			return (y < this.lightDepths[x + z * this.width] ? dark : light);
		}
		return light;
	}

	public void setTile(int x, int y, int z, int type) {
		if(x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
			this.blocks[getIndex(x, y, z)] = (byte)type;
			this.calcLightDepths(x, z, 1, 1);
			for(int i = 0; i < this.levelListeners.size(); ++i) {
				((LevelListener)this.levelListeners.get(i)).tileChanged(x, y, z);
			}
		}
	}
	public int getSurfaceHeight(int x, int z) {
		if (x < 0) x = 0;
		if (z < 0) z = 0;
		if (x >= width) x = width - 1;
		if (z >= height) z = height - 1;

		for (int y = depth - 1; y > 0; y--) {
			if (isSolidTile(x, y, z)) {
				return y + 1;
			}
		}
		return 32;
	}
}