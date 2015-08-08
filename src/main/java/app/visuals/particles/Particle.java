package app.visuals.particles;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Particle {
    static Random rand = new Random();
    static final int SCREEN_CENTER_RADIUS = 30;
    static final float FRICTION_COEFFICIENT = 0.3f; // inverse; i.e. - how much particle will slow over time
    static final float MAX_VARIABILITY = 0.25f;
    static final int FORCE_CONSTANT = 800;
    static final float RADIUS = 2f;

    float x, y;
    float xVector, yVector;

    static int newParticleCount = 0;
    private final static ConcurrentLinkedQueue<Particle> freeParticles = new ConcurrentLinkedQueue<>();

    public synchronized static Particle createParticle(float magnitude) {
        Particle p = freeParticles.poll();

        if (p == null) {
            System.out.println("new Particle(): " + ++newParticleCount);
            return new Particle(magnitude);
        }

        p.init(magnitude);

        return p;
    }

    private Particle(float magnitude) {
        init(magnitude);
    }

    public void init(float magnitude) {
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

        x = xVector * (SCREEN_CENTER_RADIUS + RADIUS) - RADIUS;
        y = yVector * (SCREEN_CENTER_RADIUS + RADIUS) - RADIUS;

        float variability = rand.nextFloat() * MAX_VARIABILITY - MAX_VARIABILITY / 2;

        xVector *= magnitude * FORCE_CONSTANT * (1 - variability);
        yVector *= magnitude * FORCE_CONSTANT * (1 - variability);
    }

    public void free() {
        freeParticles.add(this);
    }

    public void processFrame() {
        xVector *= FRICTION_COEFFICIENT;
        yVector *= FRICTION_COEFFICIENT;

        x += xVector;
        y += yVector;
    }
}
