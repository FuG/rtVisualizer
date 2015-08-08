package app.visuals;

import app.Monitor;
import app.Settings;
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

        public synchronized void create(float magnitude) {
            ParticleGroup pg = new ParticleGroup(magnitude, color, g);
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
                        particleGroups.remove(pg);
                    }

                    monitor.signal();
                }
            }
        }
    }

    public SpittingBass(Graphics g) {
        rand = new Random();
        setupRanges();
        monitor = new Monitor(1);

        SpittingBassWorker worker = new SpittingBassWorker(Color.WHITE, g, monitor);
        Thread t = new Thread(worker);
        t.start();

        workers = new ArrayList<>();
        workers.add(worker);
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
                rangeMagnitudes[rangeIndex] += fftResults[i] / Math.sqrt(i); // TODO: make this less expensive
            }
        }

        float bassMagnitude = 0;
        bassMagnitude += rangeMagnitudes[0];

        // Finding max volume
        if ((float) (Math.pow(2, bassMagnitude) - 1) > maxBassVolume) {
            maxBassVolume = bassMagnitude;
//            System.out.println("Max Bass Volume: " + maxBassVolume);
        }
        maxBassVolumeEncountered = bassMagnitude > maxBassVolumeEncountered ? bassMagnitude : maxBassVolumeEncountered;
        bassMagnitude = (float) (Math.pow(2, bassMagnitude / maxBassVolumeEncountered) - 1); // adjust bass Magnitude

        setupBackBuffer(Color.BLACK, g);
        setupBassCircle(bassMagnitude, g);

        monitor.reset();
        SpittingBassWorker worker = workers.get(0);
        worker.create(bassMagnitude);
        worker.processFrame();

        monitor.awaitCompletion();
    }

    private void setupBassCircle(float volume, Graphics g) {
        int x = Settings.APPLET_WIDTH / 2 - MAX_RADIUS;
        int y = Settings.APPLET_HEIGHT / 2 - MAX_RADIUS;

        g.setColor(new Color(255, 255, 255, (int) (volume * 255)));
        g.fillOval(x, y, MAX_RADIUS * 2, MAX_RADIUS * 2);
    }

    private void setupRanges() {
        ranges = new float[] { 100f, 1000f, 4000f, 20000f };
    }

    private void setupBackBuffer(Color color, Graphics g) {
        // wipe the image
        g.setColor(color);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
    }
}
