package app.visuals;

import app.Settings;
import app.visuals.particles.Particle;
import app.visuals.particles.ParticleGroup;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpittingBass implements IVisual {
    static float[] ranges = null; // 32 bands
    static float maxBassVolume = 0;
    final int MAX_RADIUS = 40;
    float maxBassVolumeEncountered = 0.8f;
    Random rand;

    List<Particle> particles = new ArrayList<>();
    List<ParticleGroup> particleGroups = new ArrayList<>();

    public SpittingBass() {
        rand = new Random();
        setupRanges();
    }

//    @Override
//    public void process(double[] fftResults, Graphics g) {
//        int rangeBins = ranges.length;
//        float[] rangeMagnitudes = new float[rangeBins];
//
//        int rangeIndex = 0;
//        for (int i = 1; i < fftResults.length && rangeIndex < rangeBins; i++) {
//            if (i * Settings.FFT_BIN_FREQUENCY > ranges[rangeIndex]) {
//                rangeIndex++;
//            }
//
//            if (rangeIndex < rangeBins) {
//                rangeMagnitudes[rangeIndex] += fftResults[i] / Math.sqrt(i);
//            }
//        }
//
//        float bassMagnitude = 0;
//
//        for (int i = 3; i < 17; i++) { // bass beat ranges
//            bassMagnitude += rangeMagnitudes[i] / 8;
//        }
//        // Finding max volume
//        if ((float) (Math.pow(2, bassMagnitude) - 1) > maxBassVolume) {
//            maxBassVolume = bassMagnitude;
////            System.out.println("Max Bass Volume: " + maxBassVolume);
//        }
//        maxBassVolumeEncountered = bassMagnitude > maxBassVolumeEncountered ? bassMagnitude : maxBassVolumeEncountered;
//        bassMagnitude = (float) (Math.pow(2, bassMagnitude / maxBassVolumeEncountered) - 1); // adjust bass Magnitude
//
//        setupBackBuffer(Color.BLACK, g);
//        setupBassCircle(bassMagnitude, g);
//
//        for (int i = 0; i < bassMagnitude * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 80; i++) {
//            particles.add(new Particle(ranges[16], bassMagnitude));
//        }
//
//        for (int i = 17; i < rangeMagnitudes.length; i+=3) {
//            float frequency = ranges[i + 2];
//            float freqMagnitude = (float) (Math.sqrt((rangeMagnitudes[i] + rangeMagnitudes[i + 1] + rangeMagnitudes[i + 2]) / 5));
//
////            if (freqMagnitude > 0.7f) System.out.println("Freq Mag = " + freqMagnitude);
//
//            float maxFreqMag = 0.9f;
//            int particleCount = (int) (freqMagnitude / maxFreqMag * Settings.VISUALIZER_PARTICLE_MAGNITUDE * 20);
////            System.out.println("Particle Count: " + particleCount);
//            for (int j = 0; j < particleCount; j++) {
//                particles.add(new Particle(frequency, freqMagnitude / maxFreqMag));
//            }
//        }
//
////        System.out.println("Total Frame Particles: " + particles.size());
//
//        List<Particle> particlesToKill = new ArrayList<>();
//        for (Particle p : particles) {
//            if (p.framesTilDeath <= 0) {
//                particlesToKill.add(p);
//                continue;
//            }
//
//            p.draw(g);
//        }
//
//        for (Particle p : particlesToKill) {
//            particles.remove(p);
//        }
//    }

//    private void setupBassCircle(float volume, Graphics g) {
//        int currentRadius = (int) (volume * MAX_RADIUS);
//        int x = Settings.APPLET_WIDTH / 2 - currentRadius;
//        int y = Settings.APPLET_HEIGHT / 2 - currentRadius;
//
//        g.setColor(Color.WHITE);
//        g.fillOval(x, y, currentRadius * 2, currentRadius * 2);
//    }

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

        for (int i = 2; i < 11; i++) { // bass beat ranges
            bassMagnitude += rangeMagnitudes[i] / 9;
        }
        // Finding max volume
        if ((float) (Math.pow(2, bassMagnitude) - 1) > maxBassVolume) {
            maxBassVolume = bassMagnitude;
//            System.out.println("Max Bass Volume: " + maxBassVolume);
        }
        maxBassVolumeEncountered = bassMagnitude > maxBassVolumeEncountered ? bassMagnitude : maxBassVolumeEncountered;
        bassMagnitude = (float) (Math.pow(2, bassMagnitude / maxBassVolumeEncountered) - 1); // adjust bass Magnitude

        setupBackBuffer(Color.BLACK, g);
        setupBassCircle(bassMagnitude, g);

        ParticleGroup pGroup = new ParticleGroup(bassMagnitude, Color.WHITE);
        particleGroups.add(pGroup);
        Thread t = new Thread(pGroup); // TODO: use lightweight threads, currently starting a new thread makes perf. worse
        t.start();
        pGroup.myThread = t; // necessary?

        List<ParticleGroup> pGroupsToKill = new ArrayList<>();
        for (ParticleGroup pg : particleGroups) {
            pg.draw(g);
            if (pg.isDead()) {
                pGroupsToKill.add(pg);
            }
        }

        // kill dead particle groups
        for (ParticleGroup pg : pGroupsToKill) {
            particleGroups.remove(pg);
        }
    }
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
