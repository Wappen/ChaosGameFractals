package me.wappen.chaosgamefractals;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author LenzK
 * @since 13.06.2021
 */

public class ChaosGame extends PApplet {
    int resX = 800;
    int resY = 800;
    int iters;
    int n;
    int g; // current gradient index
    boolean mode;
    Fractal fractal;
    PImage gradients;
    int[] gradient;

    boolean run = true;

    private static final Pattern argsPattern = Pattern.compile("\\d+x\\d+");

    @Override
    public void settings() {
        size(800, 800);
    }

    @Override
    public void setup() {
        gradients = loadImage("gradients.bmp");
        gradient = loadGradient(gradients, g);

        if (args != null) {
            if (args.length == 1) {
                if (argsPattern.matcher(args[0]).matches()) {
                    String[] dimens = args[0].split("x");
                    int w = Integer.parseInt(dimens[0]);
                    int h = Integer.parseInt(dimens[1]);

                    resX = w;
                    resY = h;
                } else {
                    println("Invalid argument! Use for example: 800x800");
                    exit();
                }
            }
            else {
                println("Invalid number of arguments!");
                exit();
            }
        }
        else {
            resX = width;
            resY = height;
        }

        n = 5;
        g = 0;
        createNewFractal();

        for (int i = 0; i < 4; i++) {
            thread("simulate");
        }
    }

    @Override
    public void draw() {
        background(0);
        imageMode(CORNER);
        image(fractal.getImage(gradient, width, height), 0, 0);

        stroke(255);
        textAlign(LEFT, TOP);
        text("fps: " + (int)frameRate + "; iters: " + String.format("%,dk", fractal.getTotalIters() / 1000) + "\n" +
                "gradient: " + (g + 1) + "/" + gradients.height + "\n" +
                "n: " + n + "; m: " + mode + "\n" +
                "res: " + resX + "x" + resY, 5, 5);

        textAlign(LEFT, BOTTOM);
        text("up/down: n +/- 1\n" +
                "right/left: gradient +/- 1 \n" +
                "ctrl: toggle mode\n" +
                "s: save image", 5, height - 5);
    }

    public void simulate() {
        while (run) {
            fractal.simulate(iters);
        }
    }

    @Override
    public void keyPressed() {
        if (key == CODED) {
            switch (keyCode) {
                case UP:
                    n = max(2, n + 1);
                    createNewFractal();
                    break;
                case DOWN:
                    n = max(2, n - 1);
                    createNewFractal();
                    break;
                case LEFT:
                    g = wrap(g - 1, 0, gradients.height);
                    gradient = loadGradient(gradients, g);
                    break;
                case RIGHT:
                    g = wrap(g + 1, 0, gradients.height);
                    gradient = loadGradient(gradients, g);
                    break;
                case CONTROL:
                    mode = !mode;
                    createNewFractal();
                    break;
            }
        }
        else {
            switch (key) {
                case 's':
                    String path;
                    try {
                        path = File.createTempFile(resX + "x" + resY + "_n-" + n + "_m-" + mode + "_", ".bmp", new File("./")).getAbsolutePath();
                        fractal.getImage(gradient, resX, resY).save(path);
                        Runtime.getRuntime().exec("explorer.exe /select," + path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void createNewFractal() {
        // min because the fractal has height and width with min
        iters = min(resX, resY) * n;
        fractal = new Fractal(this, resX, resY, n, mode);
    }

    private static int[] loadGradient(PImage image, int index) {
        image.loadPixels();
        int[] gradient = new int[image.width];
        for (int x = 0; x < image.width; x++) {
            gradient[x] = image.get(x, index);
        }
        return gradient;
    }

    private static int wrap(int v, int min, int max) {
        int v2 = min + v % (max - min);
        if (v2 < min) return v2 + max - min;
        return v2;
    }

    private static String getUniqueFileName(String prefix, String suffix) {
        String fileName = UUID.randomUUID().toString();
        return prefix + fileName + suffix;
    }
}
