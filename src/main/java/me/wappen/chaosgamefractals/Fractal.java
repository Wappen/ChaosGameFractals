package me.wappen.chaosgamefractals;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

import java.util.Random;

import static processing.core.PConstants.RGB;

/**
 * @author LenzK
 * @since 13.06.2021
 */

public class Fractal {
    final Random rng;
    final PApplet parent;
    final boolean mode;
    final PVector[] vertices;
    final long[][] buffer;
    long iters = 0;
    double scale = 1.0;

    public Fractal(PApplet parent, int width, int height, int vertexCount, boolean mode) {
        this.rng = new Random();
        this.parent = parent;
        this.mode = mode;

        this.buffer = new long[width][height];

        float radius = PApplet.min(width, height) / 2f - 5;
        this.vertices = new PVector[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            float x = (PApplet.sin((float)i / vertexCount * 2f * parent.PI) * radius) + width / 2f;
            float y = (PApplet.cos((float)i / vertexCount * 2f * parent.PI) * radius) + height / 2f;

            this.vertices[i] = new PVector(x, y);
        }
    }

    public void simulate(int iterationCount) {
        int x;
        int y;
        int vertex = rng.nextInt(vertices.length);
        int lastVertex = vertex;
        PVector pos = new PVector(vertices[vertex].x, vertices[vertex].y);

        for (int i = 0; i <= (iterationCount + vertices.length); i++) {
            if (mode) {
                do {
                    vertex = rng.nextInt(vertices.length);
                } while (vertex == lastVertex);
                lastVertex = vertex;
            }
            else {
                vertex = rng.nextInt(vertices.length);
            }

            //pos = PVector.lerp(pos, vertices[vertex], 0.5f);
            moveHalf(pos, vertices[vertex]);

            // Prevent clustered points by skipping first iteration
            if (i > vertices.length) {
                x = (int) pos.x;
                y = (int) pos.y;

                buffer[x][y]++;
                iters++;

                if (buffer[x][y] > scale)
                    scale = buffer[x][y];
            }
        }
    }

    private void moveHalf(PVector v1, PVector v2) {
        v1.x += (v2.x - v1.x) / 2f;
        v1.y += (v2.y - v1.y) / 2f;
    }

    public PImage getImage(int[] colors, int width, int height) {
        PImage image = parent.createImage(width, height, RGB);
        image.loadPixels();

        if (width == buffer.length && height == buffer[0].length) {
            for (int x = 0; x < buffer.length; x++) {
                for (int y = 0; y < buffer[x].length; y++) {
                    int colorsIndex = (int) Math.min((buffer[x][y] / scale) * (colors.length - 1), colors.length - 1);
                    int pixelsIndex = y * buffer.length + x;
                    int col = colors[colorsIndex];
                    image.pixels[pixelsIndex] = col;
                }
            }
        }
        else {
            float scaleX = (float) buffer.length / width;
            float scaleY = (float) buffer[0].length / height;

            for (int i = 0; i < image.pixels.length; i++) {
                int x = (int) (i % width * scaleX);
                int y = (int) (i / height * scaleY);

                image.pixels[i] = colors[(int) ((buffer[x][y] / scale) * (colors.length - 1))];
            }
        }

        image.updatePixels();
        return image;
    }

    public long getTotalIters() {
        return iters;
    }
}
