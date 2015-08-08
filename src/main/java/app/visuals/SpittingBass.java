package app.visuals;

import app.Monitor;
import app.Settings;
import app.visuals.particles.ParticleGroup;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SpittingBass implements IVisual {
    static float[] ranges = null;
    static float[] maxMagnitudes = null;
    static float[] magnitudes = null;
    static Color[] colors = null;
    final int MAX_RADIUS = 40;
    Random rand;

    List<SpittingBassWorker> workers;
    Monitor monitor;

    class SpittingBassWorker implements Runnable {
        List<ParticleGroup> particleGroups = new ArrayList<>();
        Graphics g;
        Color color;
        Monitor monitor;

        public SpittingBassWorker(Color color, Graphics g, Monitor monitor) {
            this.color = color;
            this.g = g;
            this.monitor = monitor;
        }

        public synchronized int getParticleCount() {
            int particleCount = 0;
            for (ParticleGroup pg : particleGroups) {
                particleCount += pg.getParticleCount();
            }
            return particleCount;
        }

        public synchronized void create(float magnitude) {
            ParticleGroup pg = ParticleGroup.createParticleGroup(magnitude, color, g);
            particleGroups.add(pg);
        }

        public synchronized void processFrame() {
            notify();
        }

        @Override
        public void run() {
            while (true) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    List<ParticleGroup> pGroupsToKill = new ArrayList<>();
                    for (ParticleGroup pg : particleGroups) {
                        if (pg.isDead()) {
                            pGroupsToKill.add(pg);
                            continue;
                        }

                        pg.processFrame();
                        pg.draw();
                    }

                    // kill dead particle groups
                    for (ParticleGroup pg : pGroupsToKill) {
                        pg.free();
                        particleGroups.remove(pg);
                    }

                    monitor.signal();
                }
            }
        }
    }

    public SpittingBass(Graphics g) {
        init();
        rand = new Random();
        monitor = new Monitor(ranges.length);
        workers = new ArrayList<>();

        for (int i = 0; i < ranges.length; i++) {
            SpittingBassWorker worker = new SpittingBassWorker(colors[i], g, monitor);
            workers.add(worker);
            Thread t = new Thread(worker);
            t.start();
        }
    }

    @Override
    public int process(double[] fftResults, Graphics g) {
        int rangeBins = ranges.length;
        magnitudes = new float[rangeBins];

        int rangeIndex = 0;
        for (int i = 1; i < fftResults.length && rangeIndex < rangeBins; i++) {
            if (i * Settings.FFT_BIN_FREQUENCY > ranges[rangeIndex]) {
                rangeIndex++;
            }

            if (rangeIndex < rangeBins) {
                magnitudes[rangeIndex] += fftResults[i] / Math.sqrt(i); // TODO: make this less expensive
            }
        }

        // Perform magnitude adjustment
        for (int i = 0; i < magnitudes.length; i++) {
            // Finding max volume
//            if ((float) (Math.pow(2, magnitudes[0]) - 1) > maxBassVolume) {
//                maxBassVolume = magnitudes[0];
////            System.out.println("Max Bass Volume: " + maxBassVolume);
//            }
            maxMagnitudes[i] = magnitudes[i] > maxMagnitudes[i] ? magnitudes[i] : maxMagnitudes[i];
            magnitudes[i] = (float) (Math.pow(2, magnitudes[i] / maxMagnitudes[i]) - 1); // adjust bass Magnitude
        }

//        System.out.println(magnitudes[0]);
        setupBackBuffer(Color.BLACK, g);
        setupBassCircle(magnitudes[0], g);

        monitor.reset();
        for (int i = 0; i < workers.size(); i++) {
            SpittingBassWorker worker = workers.get(i);
            worker.create(magnitudes[i]);
            worker.processFrame();
        }

        monitor.awaitCompletion();

        int particleCount = 0;
        for (SpittingBassWorker sbw : workers) {
            particleCount += sbw.getParticleCount();
        }

        return particleCount;
    }

    private void setupBassCircle(float volume, Graphics g) {
        int x = Settings.APPLET_WIDTH / 2 - MAX_RADIUS;
        int y = Settings.APPLET_HEIGHT / 2 - MAX_RADIUS;

        g.setColor(new Color(255, 255, 255, (int) (volume * 255)));
        g.fillOval(x, y, MAX_RADIUS * 2, MAX_RADIUS * 2);
    }

    private void init() {
        ranges = new float[] { 100f, 1000f, 4000f, 20000f };
        maxMagnitudes = new float[ranges.length];
        Arrays.fill(maxMagnitudes, 5.0f);
        magnitudes = new float[ranges.length];
        colors = new Color[] {
                Color.WHITE,
                Color.YELLOW,
                Color.ORANGE,
                Color.RED
        };
    }

    private void setupBackBuffer(Color color, Graphics g) {
        // wipe the image
        g.setColor(color);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
    }
}
