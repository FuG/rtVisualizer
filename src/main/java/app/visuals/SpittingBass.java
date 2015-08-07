package app.visuals;

import app.Settings;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpittingBass implements IVisual {
    static float[] ranges = null; // 32 bands
    static float maxBassVolume = 0;
    final int MAX_RADIUS = 30;
    float maxBassVolumeEncountered = 0.8f;
    Random rand;

    List<Particle> particles = new ArrayList<>();

    class Particle {
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

        public Particle(float frequency, float magnitude) {
            if (frequency == ranges[16]) {
                radius = 1f;
            }

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
            this.frequency = frequency;
        }

        public void draw(Graphics g) {
            if (framesTilDeath != FRAMES_TO_LIVE) { // suggestion from Tanner Y.
                if (frequency == ranges[16]) {
                    g.setColor(new Color(255, 255, 255, (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * 255)));
                } else {
                    int green = (int) (255 * (frequency / 16384f));
//                    System.out.println("Green: " + green);
                    g.setColor(new Color(255, 255 - green, 0, (int) (((float) framesTilDeath / FRAMES_TO_LIVE) * magnitude * 255)));
                }

                g.fillOval((int) (Settings.APPLET_WIDTH / 2 + x), (int) (Settings.APPLET_HEIGHT / 2 + y), (int)(radius * 2), (int)(radius * 2));
            }
            xVector *= FRICTION_COEFFICIENT;
            yVector *= FRICTION_COEFFICIENT;

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
        float[] rangeMagnitudes = new float[rangeBins];

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

        for (int i = 3; i < 17; i++) { // bass beat ranges
            bassMagnitude += rangeMagnitudes[i] / 8;
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

        for (int i = 0; i < bassMagnitude * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 80; i++) {
            particles.add(new Particle(ranges[16], bassMagnitude));
        }

        for (int i = 17; i < rangeMagnitudes.length; i+=3) {
            float frequency = ranges[i + 2];
            float freqMagnitude = (float) (Math.sqrt((rangeMagnitudes[i] + rangeMagnitudes[i + 1] + rangeMagnitudes[i + 2]) / 5));

            if (freqMagnitude > 0.7f) System.out.println("Freq Mag = " + freqMagnitude);

            float maxFreqMag = 0.9f;
            int particleCount = (int) (freqMagnitude / maxFreqMag * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 20);
//            System.out.println("Particle Count: " + particleCount);
            for (int j = 0; j < particleCount; j++) {
                particles.add(new Particle(frequency, freqMagnitude / maxFreqMag));
            }
        }

        System.out.println("Total Frame Particles: " + particles.size());

        List<Particle> particlesToKill = new ArrayList<>();
        for (Particle p : particles) {
            if (p.framesTilDeath <= 0) {
                particlesToKill.add(p);
                continue;
            }

            p.draw(g);
        }

        for (Particle p : particlesToKill) {
            particles.remove(p);
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
        ranges = new float[binCount];
        ranges[0] = 33.333333333f;

        for (int i = 1; i < binCount; i++) {
            ranges[i] = ranges[i - 1] * 1.221304f;
        }
    }

    private void setupBackBuffer(Color color, Graphics g) {
        // wipe the image
        g.setColor(color);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
    }
}
