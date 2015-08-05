package app.visuals;

import app.Settings;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpittingBass implements IVisual {
    static double[] ranges = null; // 32 bands
    static float maxBassVolume = 0;
    final int MAX_RADIUS = 50;
    float maxBassVolumeEncountered = 1.0f;
    Random rand;

    List<Particle> particles = new ArrayList<>();

    class Particle {
        float x, y;
        float xVector, yVector;
        float frCoeff; // friction coefficient, i.e. - how much particle will slow over time
        final int FRAMES_TO_LIVE = 20;
        public int framesTilDeath = FRAMES_TO_LIVE;
        int radius = 1;
        float magnitude;

        public Particle(float magnitude) {
            float xVectorRatio = rand.nextFloat();
            float yVectorRatio = (float) Math.sqrt(1 - xVectorRatio * xVectorRatio);

            xVector = xVectorRatio * (rand.nextBoolean() == true ? 1 : -1);
            yVector = yVectorRatio * (rand.nextBoolean() == true ? 1 : -1);

            x = xVector * (MAX_RADIUS + radius) - radius;
            y = yVector * (MAX_RADIUS + radius) - radius;

            xVector *= magnitude * 50;
            yVector *= magnitude * 50;

            this.magnitude = magnitude;
        }

        public void draw(Graphics g) {
            g.setColor(new Color(255, 255, 255, (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * 255)));

            g.fillOval((int) (Settings.APPLET_WIDTH / 2 + x), (int) (Settings.APPLET_HEIGHT / 2 + y), radius * 2, radius * 2);

            x += xVector;
            y += yVector;
            framesTilDeath--;
        }
    }

    public SpittingBass() {
        rand = new Random();
        setupRanges();
    }

    @Override
    public void process(double[] fftResults, Graphics g) {
        int rangeBins = ranges.length;
        double[] rangeMagnitudes = new double[rangeBins];

        int rangeIndex = 0;
        for (int i = 1; i < fftResults.length && rangeIndex < rangeBins; i++) {
            if (i * Settings.FFT_BIN_FREQUENCY > ranges[rangeIndex]) {
                rangeIndex++;
            }

            if (rangeIndex < rangeBins) {
                rangeMagnitudes[rangeIndex] += fftResults[i] / Math.sqrt(i);
            }
        }

        float bassMagnitude = 0;

        for (int i = 3; i < 14; i++) { // bass beat ranges
            bassMagnitude += rangeMagnitudes[i] / 11;
        }
        // Finding max volume
        if ((float) (Math.pow(2, bassMagnitude) - 1) > maxBassVolume) {
            maxBassVolume = bassMagnitude;
            System.out.println("Max Bass Volume: " + maxBassVolume);
        }
        maxBassVolumeEncountered = bassMagnitude > maxBassVolumeEncountered ? bassMagnitude : maxBassVolumeEncountered;
        bassMagnitude = (float) (Math.pow(2, bassMagnitude / maxBassVolumeEncountered) - 1); // adjust bass Magnitude

        setupBackBuffer(Color.BLACK, g);
        setupBassCircle(bassMagnitude, g);

//        for (int i = 0; i < rangeMagnitudes.length; i++) {
//             // TODO: do something with frequency
//        }

        for (int i = 0; i < bassMagnitude * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 80; i++) {
            particles.add(new Particle(bassMagnitude));
        }

        List<Particle> particlesToKill = new ArrayList<>();
        for (Particle p : particles) {
            if (p.framesTilDeath <= 0) {
                particlesToKill.add(p);
                continue;
            }

            p.draw(g);
        }
    }

//    private void setupBassCircle(float volume, Graphics g) {
//        int currentRadius = (int) (volume * MAX_RADIUS);
//        int x = Settings.APPLET_WIDTH / 2 - currentRadius;
//        int y = Settings.APPLET_HEIGHT / 2 - currentRadius;
//
//        g.setColor(Color.WHITE);
//        g.fillOval(x, y, currentRadius * 2, currentRadius * 2);
//    }

    private void setupBassCircle(float volume, Graphics g) {
        int x = Settings.APPLET_WIDTH / 2 - MAX_RADIUS;
        int y = Settings.APPLET_HEIGHT / 2 - MAX_RADIUS;

        g.setColor(new Color(255, 255, 255, (int) (volume * 255)));
        g.fillOval(x, y, MAX_RADIUS * 2, MAX_RADIUS * 2);
    }

    private void setupRanges() {
        int binCount = 32;
        ranges = new double[binCount];
        ranges[0] = 33.333333333333333;

        for (int i = 1; i < binCount; i++) {
            ranges[i] = ranges[i - 1] * 1.221304;
        }
    }

    private void setupBackBuffer(Color color, Graphics g) {
        // wipe the image
        g.setColor(color);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
    }
}
