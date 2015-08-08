package app;

import app.visuals.IVisual;
import app.visuals.SpittingBass;

import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Visualizer {
    IVisual visualEffects;
    long startTime, endTime;
    long allowedTime = (long) Settings.MILLIS_BETWEEN_FRAMES;
    double runningAvgTime = 0;
    long maxFrameTime = 0;
    int framesProcessed = 0;
    int maxParticleCount = 0;
    NumberFormat formatter = new DecimalFormat("#0.00");

    public Visualizer(Graphics g) {
//        visualEffects = new VerticalBars();
        visualEffects = new SpittingBass(g);
    }

    public void process(double[] fftResults, Graphics g) {
        startTime = System.currentTimeMillis();

        int particleCount = visualEffects.process(fftResults, g);

        endTime = System.currentTimeMillis();
        long frameTime = endTime - startTime;

        if (frameTime > maxFrameTime) {
            maxFrameTime = frameTime;
        }

        runningAvgTime = runningAvgTime * framesProcessed + frameTime;
        framesProcessed++;
        runningAvgTime /= framesProcessed;

        if (particleCount > maxParticleCount) {
            maxParticleCount = particleCount;
        }

        System.out.print("Frame Time (Avg: " + formatter.format(runningAvgTime) + " / Max: " + maxFrameTime + ")" +
                " | Particle Count (Max: " + maxParticleCount + ")" +
                "\r"
        );

//        System.out.println("Frame Time: " + frameTime + " / " + allowedTime + " ms");
    }

    // TODO: move these
    public static void createAllFrequencyVisual(double[] fftResults, Graphics g) {
        setupBackBuffer(Color.BLACK, g);

        // bars
        g.setColor(Color.WHITE);
        for (int i = 1; i <= fftResults.length / 2; i++) {
            int startIndex = i * 2;
            double sum = fftResults[startIndex] + fftResults[startIndex -1];
            double normalizedAmplitude = Math.log10(startIndex * Settings.FFT_BIN_FREQUENCY) * (sum / 41.4 * 2);
            int magnitude = (int) ((Settings.APPLET_HEIGHT - 1) * normalizedAmplitude);
            g.drawLine(i, Settings.APPLET_HEIGHT - 1, i, Settings.APPLET_HEIGHT - 1 - magnitude);
        }
    }

    private static void setupBackBuffer(Color color, Graphics g) {
        // wipe the image
        g.setColor(color);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
    }
}
