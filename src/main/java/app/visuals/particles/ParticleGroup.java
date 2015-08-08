package app.visuals.particles;

import app.Settings;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleGroup implements Runnable {
    final int RGBA_MAX_VALUE = 255;
    final int FRAMES_TO_LIVE = 10;
    final float FRICTION_COEFFICIENT = 0.3f; // inverse; i.e. - how much particle will slow over time
    final float MAX_VARIABILITY = 0.25f;
    final int FORCE_CONSTANT = 800;

    List<Particle> particles;
    float magnitude;
    float radius;
    Color color;
    int framesTilDeath;

    Random rand;
    public Thread myThread;

    public ParticleGroup(float magnitude, Color color) {
        particles = new ArrayList<>();
        this.magnitude = magnitude;
        this.color = color;
        radius = 1f;
        framesTilDeath = FRAMES_TO_LIVE;
        rand = new Random();

        int particleCount = (int) (magnitude * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 80);
        for (int i = 0; i < particleCount; i++) {
            particles.add(new Particle(magnitude));
        }
    }

    public synchronized void draw(Graphics g) {
        if (framesTilDeath != FRAMES_TO_LIVE) { // skip first frame
            int alpha = (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * RGBA_MAX_VALUE + 0.5f);
            alpha = alpha > RGBA_MAX_VALUE ? RGBA_MAX_VALUE : alpha; // adjust to max if overflow
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

            for (Particle p : particles) {
                g.fillOval((int) (Settings.APPLET_WIDTH / 2 + p.x), (int) (Settings.APPLET_HEIGHT / 2 + p.y), (int) (radius * 2), (int) (radius * 2));
            }
        }

        notify();
    }

    public boolean isDead() {
        return framesTilDeath <= 0;
    }

    @Override
    public void run() {
        while (framesTilDeath > 0) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (Particle p : particles) {
                    p.processFrame();
                }

                framesTilDeath--;
            }
        }
    }
}
