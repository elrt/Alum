package com.mojang.rubydung;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.lwjgl.opengl.GL11;

public class OBJLoader {

    public static class Mesh {
        public float[] vertices;
        public float[] normals;
        public float[] texCoords;
        public int[] indices;
        public int vertexCount;

        public Mesh() {
            vertices = new float[0];
            normals = new float[0];
            texCoords = new float[0];
            indices = new int[0];
            vertexCount = 0;
        }

        public boolean isValid() {
            return vertices != null && vertices.length > 0 && indices != null && indices.length > 0;
        }
    }

    public static Mesh loadModel(String resourceName) {
        Mesh mesh = new Mesh();
        ArrayList<Float> verts = new ArrayList<>();
        ArrayList<Float> norms = new ArrayList<>();
        ArrayList<Float> uvs = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();

        try {
            System.out.println("Attempting to load OBJ: " + resourceName);

            InputStream inputStream = OBJLoader.class.getResourceAsStream(resourceName);
            if (inputStream == null) {
                System.err.println("OBJ file not found in classpath: " + resourceName);
                System.err.println("Trying alternative path...");

                String altPath = resourceName.startsWith("/") ? resourceName.substring(1) : "/" + resourceName;
                inputStream = OBJLoader.class.getResourceAsStream(altPath);
                if (inputStream == null) {
                    System.err.println("OBJ file also not found at: " + altPath);
                    return mesh;
                }
            }

            System.out.println("OBJ file found, reading...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int lineCount = 0;
            int vertexCount = 0;
            int faceCount = 0;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue;

                switch (parts[0]) {
                    case "v":
                        if (parts.length >= 4) {
                            verts.add(Float.parseFloat(parts[1]));
                            verts.add(Float.parseFloat(parts[2]));
                            verts.add(Float.parseFloat(parts[3]));
                            vertexCount++;
                        }
                        break;
                    case "vn":
                        if (parts.length >= 4) {
                            norms.add(Float.parseFloat(parts[1]));
                            norms.add(Float.parseFloat(parts[2]));
                            norms.add(Float.parseFloat(parts[3]));
                        }
                        break;
                    case "vt":
                        if (parts.length >= 3) {
                            uvs.add(Float.parseFloat(parts[1]));
                            uvs.add(Float.parseFloat(parts[2]));
                        }
                        break;
                    case "f":
                        if (parts.length >= 4) {

                            if (parts.length == 4) {

                                for (int i = 1; i <= 3; i++) {
                                    String[] vertData = parts[i].split("/");
                                    int vertIndex = Integer.parseInt(vertData[0]) - 1;
                                    indices.add(vertIndex);
                                }
                                faceCount++;
                            } else if (parts.length == 5) {

                                for (int i = 1; i <= 3; i++) {
                                    String[] vertData = parts[i].split("/");
                                    int vertIndex = Integer.parseInt(vertData[0]) - 1;
                                    indices.add(vertIndex);
                                }

                                for (int i = 3; i <= 4; i++) {
                                    String[] vertData = parts[i].split("/");
                                    int vertIndex = Integer.parseInt(vertData[0]) - 1;
                                    indices.add(vertIndex);
                                }

                                String[] vertData = parts[1].split("/");
                                int vertIndex = Integer.parseInt(vertData[0]) - 1;
                                indices.add(vertIndex);
                                faceCount += 2;
                            }
                        }
                        break;
                }
            }
            reader.close();

            System.out.println("OBJ loaded: " + lineCount + " lines, " + vertexCount + " vertices, " + faceCount + " faces");

            mesh.vertices = new float[verts.size()];
            for (int i = 0; i < verts.size(); i++) {
                mesh.vertices[i] = verts.get(i);
            }

            mesh.normals = new float[norms.size()];
            for (int i = 0; i < norms.size(); i++) {
                mesh.normals[i] = norms.get(i);
            }

            mesh.texCoords = new float[uvs.size()];
            for (int i = 0; i < uvs.size(); i++) {
                mesh.texCoords[i] = uvs.get(i);
            }

            mesh.indices = new int[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                mesh.indices[i] = indices.get(i);
            }

            mesh.vertexCount = indices.size();

            System.out.println("Mesh created: " + mesh.vertices.length / 3 + " verts, " + mesh.indices.length / 3 + " tris");

        } catch (Exception e) {
            System.err.println("Failed to load OBJ: " + resourceName);
            e.printStackTrace();
            return mesh;
        }

        return mesh;
    }

    public static void renderMesh(Mesh mesh, float x, float y, float z, float scale) {
        if (mesh == null || !mesh.isValid()) return;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(scale, scale, scale);

        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < mesh.indices.length; i++) {
            int idx = mesh.indices[i];
            if (idx * 3 + 2 >= mesh.vertices.length) continue;

            float vx = mesh.vertices[idx * 3];
            float vy = mesh.vertices[idx * 3 + 1];
            float vz = mesh.vertices[idx * 3 + 2];

            if (mesh.normals != null && mesh.normals.length > idx * 3 + 2) {
                GL11.glNormal3f(
                        mesh.normals[idx * 3],
                        mesh.normals[idx * 3 + 1],
                        mesh.normals[idx * 3 + 2]
                );
            }

            if (mesh.texCoords != null && mesh.texCoords.length > idx * 2 + 1) {
                GL11.glTexCoord2f(
                        mesh.texCoords[idx * 2],
                        mesh.texCoords[idx * 2 + 1]
                );
            }

            GL11.glVertex3f(vx, vy, vz);
        }
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    public static void renderWireframe(Mesh mesh, float x, float y, float z, float scale) {
        if (mesh == null || !mesh.isValid()) return;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glScalef(scale, scale, scale);

        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < mesh.indices.length; i += 3) {
            for (int j = 0; j < 3; j++) {
                int idx1 = mesh.indices[i + j];
                int idx2 = mesh.indices[i + ((j + 1) % 3)];

                if (idx1 * 3 + 2 >= mesh.vertices.length || idx2 * 3 + 2 >= mesh.vertices.length) continue;

                float x1 = mesh.vertices[idx1 * 3];
                float y1 = mesh.vertices[idx1 * 3 + 1];
                float z1 = mesh.vertices[idx1 * 3 + 2];

                float x2 = mesh.vertices[idx2 * 3];
                float y2 = mesh.vertices[idx2 * 3 + 1];
                float z2 = mesh.vertices[idx2 * 3 + 2];

                GL11.glVertex3f(x1, y1, z1);
                GL11.glVertex3f(x2, y2, z2);
            }
        }
        GL11.glEnd();

        GL11.glPopMatrix();
    }

    public static void renderMeshColored(Mesh mesh, float x, float y, float z, float scale, float r, float g, float b) {
        if (mesh == null || !mesh.isValid()) return;

        GL11.glColor3f(r, g, b);
        renderMesh(mesh, x, y, z, scale);
    }
}