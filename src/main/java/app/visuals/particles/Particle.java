package app.visuals.particles;

import app.Settings;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Particle {
    // TODO: remove these 2
    static Random rand = new Random();
    final int MAX_RADIUS = 30;

    final int FRAMES_TO_LIVE = 10;
    final float FRICTION_COEFFICIENT = 0.3f; // inverse; i.e. - how much particle will slow over time
    final float MAX_VARIABILITY = 0.25f;
    final int FORCE_CONSTANT = 800;

    float x, y;
    float xVector, yVector;
    public int framesTilDeath = FRAMES_TO_LIVE;
    float radius = 1;
    float magnitude;
    float frequency;

    public Particle(float magnitude) {
//        if (frequency < 817) {
//            radius = 1f;
//        }

        float xVectorRatio;
        float yVectorRatio;
        if (rand.nextBoolean()) {
            xVectorRatio = rand.nextFloat();
            yVectorRatio = (float) Math.sqrt(1 - xVectorRatio * xVectorRatio);
        } else {
            yVectorRatio = rand.nextFloat();
            xVectorRatio = (float) Math.sqrt(1 - yVectorRatio * yVectorRatio);
        }

        xVector = xVectorRatio * (rand.nextBoolean() ? 1 : -1);
        yVector = yVectorRatio * (rand.nextBoolean() ? 1 : -1);

        x = xVector * (MAX_RADIUS + radius) - radius;
        y = yVector * (MAX_RADIUS + radius) - radius;

        float variability = rand.nextFloat() * MAX_VARIABILITY - MAX_VARIABILITY / 2;

        xVector *= magnitude * FORCE_CONSTANT * (1 - variability);
        yVector *= magnitude * FORCE_CONSTANT * (1 - variability);

        this.magnitude = magnitude;
//        this.frequency = frequency;
    }

//    public void draw(Graphics g) {
//        if (framesTilDeath != FRAMES_TO_LIVE) { // suggestion from Tanner Y.
//            if (frequency < 817) {
//                g.setColor(new Color(255, 255, 255, (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * 255)));
//            } else {
//                int green = (int) (255 * (frequency / 16384f));
//                g.setColor(new Color(255, 255 - green, 0, (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * 255)));
//            }
//
//            g.fillOval((int) (Settings.APPLET_WIDTH / 2 + x), (int) (Settings.APPLET_HEIGHT / 2 + y), (int)(radius * 2), (int)(radius * 2));
//        }
//        xVector *= FRICTION_COEFFICIENT;
//        yVector *= FRICTION_COEFFICIENT;
//
//        x += xVector;
//        y += yVector;
//        framesTilDeath--;
//    }

    public void processFrame() {
        xVector *= FRICTION_COEFFICIENT;
        yVector *= FRICTION_COEFFICIENT;

        x += xVector;
        y += yVector;
    }
}
