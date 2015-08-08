package app.visuals.particles;

import app.Settings;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParticleGroup {
    static final int RGBA_MAX_VALUE = 255;
    static final int FRAMES_TO_LIVE = 10;

    List<Particle> particles;
    float magnitude;
    Color color;
    int framesTilDeath;

    Graphics g;
    Random rand;

    private static ConcurrentLinkedQueue<ParticleGroup> freeParticleGroups = new ConcurrentLinkedQueue<>();

    public synchronized static ParticleGroup createParticleGroup(float magnitude, Color color, Graphics g) {
        ParticleGroup pg = freeParticleGroups.poll();

        if (pg == null) {
            return new ParticleGroup(magnitude, color, g);
        }

        pg.init(magnitude, color, g);

        return pg;
    }

    private ParticleGroup(float magnitude, Color color, Graphics g) {
        init(magnitude, color, g);
    }

    public void init(float magnitude, Color color, Graphics g) {
        if (particles == null) {
            particles = new ArrayList<>();
        } else {
            particles.clear();
        }
        this.magnitude = magnitude;
        this.color = color;
        framesTilDeath = FRAMES_TO_LIVE;
        rand = new Random();
        this.g = g;

        int particleCount = (int) (magnitude * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 20);
        for (int i = 0; i < particleCount; i++) {
            particles.add(Particle.createParticle(magnitude));
        }
    }

    public void draw() {
        if (framesTilDeath != FRAMES_TO_LIVE) { // skip first frame
            int alpha = (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * RGBA_MAX_VALUE + 0.5f);
            alpha = alpha > RGBA_MAX_VALUE ? RGBA_MAX_VALUE : alpha; // adjust to max if overflow
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));

            for (Particle p : particles) {
                g.fillOval((int) (Settings.APPLET_WIDTH / 2 + p.x), (int) (Settings.APPLET_HEIGHT / 2 + p.y), (int) (Particle.RADIUS * 2), (int) (Particle.RADIUS * 2));
            }
        }
    }

    public void free() {
        for (Particle p : particles) {
            p.free();
        }
        freeParticleGroups.add(this);
    }

    public boolean isDead() {
        return framesTilDeath <= 0;
    }

    public int getParticleCount() {
        return particles.size();
    }

    public void processFrame() {
        if (framesTilDeath > 0) {
            for (Particle p : particles) {
                p.processFrame();
            }

            framesTilDeath--;
        }
    }
}
