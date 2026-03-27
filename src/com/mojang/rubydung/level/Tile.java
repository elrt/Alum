package com.mojang.rubydung.level;

public class Tile {
	private static final int TEX_SIZE = 16;
	private static final int TEX_WIDTH = 16;

	public static Tile grass = new Tile(1, 0, 0, true);
	public static Tile rock = new Tile(2, 1, 0, true);
	public static Tile wood = new Tile(3, 4, 1, true);
	public static Tile leaves = new Tile(4, 4, 3, true);

	private int id;
	private int texX;
	private int texY;
	private boolean solid;

	private Tile(int id, int texX, int texY, boolean solid) {
		this.id = id;
		this.texX = texX;
		this.texY = texY;
		this.solid = solid;
	}

	public static Tile getTile(int id) {
		if(id == 1) return grass;
		if(id == 2) return rock;
		if(id == 3) return wood;
		if(id == 4) return leaves;
		return null;
	}

	public boolean isSolid() { return solid; }

	public void render(Tesselator t, Level level, int layer, int x, int y, int z) {
		int tileId = level.getTileType(x, y, z);
		Tile tile = getTile(tileId);
		if(tile == null) return;

		renderCube(t, level, layer, x, y, z, tile);
	}

	private void renderCube(Tesselator t, Level level, int layer, int x, int y, int z, Tile tile) {
		float u0 = (float)tile.texX / TEX_SIZE;
		float u1 = u0 + 0.999F / TEX_SIZE;
		float v0 = (float)tile.texY / TEX_SIZE;
		float v1 = v0 + 0.999F / TEX_SIZE;

		float c1 = 1F;
		float c2 = 0.7F;
		float c3 = 0.6F;
		float x0 = (float)x + 0.0F;
		float x1 = (float)x + 1.0F;
		float y0 = (float)y + 0.0F;
		float y1 = (float)y + 1.0F;
		float z0 = (float)z + 0.0F;
		float z1 = (float)z + 1.0F;
		float br = level.getBrightness(x, y - 1, z) * c1;

		if(!level.isSolidTile(x, y - 1, z)) {
			br = level.getBrightness(x, y - 1, z) * c1;
			if(br == c1 ^ layer == 1) {
				t.color(br, br, br);
				t.tex(u0, v1);
				t.vertex(x0, y0, z1);
				t.tex(u0, v0);
				t.vertex(x0, y0, z0);
				t.tex(u1, v0);
				t.vertex(x1, y0, z0);
				t.tex(u1, v1);
				t.vertex(x1, y0, z1);
			}
		}

		if(!level.isSolidTile(x, y + 1, z)) {
			br = level.getBrightness(x, y, z) * c1;
			if(br == c1 ^ layer == 1) {
				t.color(br, br, br);
				t.tex(u1, v1);
				t.vertex(x1, y1, z1);
				t.tex(u1, v0);
				t.vertex(x1, y1, z0);
				t.tex(u0, v0);
				t.vertex(x0, y1, z0);
				t.tex(u0, v1);
				t.vertex(x0, y1, z1);
			}
		}

		if(!level.isSolidTile(x, y, z - 1)) {
			br = level.getBrightness(x, y, z - 1) * c2;
			if(br == c2 ^ layer == 1) {
				t.color(br, br, br);
				t.tex(u1, v0);
				t.vertex(x0, y1, z0);
				t.tex(u0, v0);
				t.vertex(x1, y1, z0);
				t.tex(u0, v1);
				t.vertex(x1, y0, z0);
				t.tex(u1, v1);
				t.vertex(x0, y0, z0);
			}
		}

		if(!level.isSolidTile(x, y, z + 1)) {
			br = level.getBrightness(x, y, z + 1) * c2;
			if(br == c2 ^ layer == 1) {
				t.color(br, br, br);
				t.tex(u0, v0);
				t.vertex(x0, y1, z1);
				t.tex(u0, v1);
				t.vertex(x0, y0, z1);
				t.tex(u1, v1);
				t.vertex(x1, y0, z1);
				t.tex(u1, v0);
				t.vertex(x1, y1, z1);
			}
		}

		if(!level.isSolidTile(x - 1, y, z)) {
			br = level.getBrightness(x - 1, y, z) * c3;
			if(br == c3 ^ layer == 1) {
				t.color(br, br, br);
				t.tex(u1, v0);
				t.vertex(x0, y1, z1);
				t.tex(u0, v0);
				t.vertex(x0, y1, z0);
				t.tex(u0, v1);
				t.vertex(x0, y0, z0);
				t.tex(u1, v1);
				t.vertex(x0, y0, z1);
			}
		}

		if(!level.isSolidTile(x + 1, y, z)) {
			br = level.getBrightness(x + 1, y, z) * c3;
			if(br == c3 ^ layer == 1) {
				t.color(br, br, br);
				t.tex(u0, v1);
				t.vertex(x1, y0, z1);
				t.tex(u1, v1);
				t.vertex(x1, y0, z0);
				t.tex(u1, v0);
				t.vertex(x1, y1, z0);
				t.tex(u0, v0);
				t.vertex(x1, y1, z1);
			}
		}
	}

	public void renderFace(Tesselator t, int x, int y, int z, int face) {
		float u0 = (float)texX / TEX_SIZE;
		float u1 = u0 + 0.999F / TEX_SIZE;
		float v0 = (float)texY / TEX_SIZE;
		float v1 = v0 + 0.999F / TEX_SIZE;

		float x0 = (float)x + 0.0F;
		float x1 = (float)x + 1.0F;
		float y0 = (float)y + 0.0F;
		float y1 = (float)y + 1.0F;
		float z0 = (float)z + 0.0F;
		float z1 = (float)z + 1.0F;

		if(face == 0) {
			t.tex(u0, v1); t.vertex(x0, y0, z1);
			t.tex(u0, v0); t.vertex(x0, y0, z0);
			t.tex(u1, v0); t.vertex(x1, y0, z0);
			t.tex(u1, v1); t.vertex(x1, y0, z1);
		}
		if(face == 1) {
			t.tex(u1, v1); t.vertex(x1, y1, z1);
			t.tex(u1, v0); t.vertex(x1, y1, z0);
			t.tex(u0, v0); t.vertex(x0, y1, z0);
			t.tex(u0, v1); t.vertex(x0, y1, z1);
		}
		if(face == 2) {
			t.tex(u1, v0); t.vertex(x0, y1, z0);
			t.tex(u0, v0); t.vertex(x1, y1, z0);
			t.tex(u0, v1); t.vertex(x1, y0, z0);
			t.tex(u1, v1); t.vertex(x0, y0, z0);
		}
		if(face == 3) {
			t.tex(u0, v0); t.vertex(x0, y1, z1);
			t.tex(u0, v1); t.vertex(x0, y0, z1);
			t.tex(u1, v1); t.vertex(x1, y0, z1);
			t.tex(u1, v0); t.vertex(x1, y1, z1);
		}
		if(face == 4) {
			t.tex(u1, v0); t.vertex(x0, y1, z1);
			t.tex(u0, v0); t.vertex(x0, y1, z0);
			t.tex(u0, v1); t.vertex(x0, y0, z0);
			t.tex(u1, v1); t.vertex(x0, y0, z1);
		}
		if(face == 5) {
			t.tex(u0, v1); t.vertex(x1, y0, z1);
			t.tex(u1, v1); t.vertex(x1, y0, z0);
			t.tex(u1, v0); t.vertex(x1, y1, z0);
			t.tex(u0, v0); t.vertex(x1, y1, z1);
		}
	}
}