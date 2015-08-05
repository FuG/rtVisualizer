package app;

import app.visuals.IVisual;
import app.visuals.SpittingBass;
import app.visuals.VerticalBars;

import java.awt.Color;
import java.awt.Graphics;

public class Visualizer {
    IVisual visualEffects;
    long startTime, endTime;
    long allowedTime = (long) Settings.MILLIS_BETWEEN_FRAMES;

    public Visualizer() {
//        visualEffects = new VerticalBars();
        visualEffects = new SpittingBass();
    }

    public void process(double[] fftResults, Graphics g) {
        startTime = System.currentTimeMillis();

        visualEffects.process(fftResults, g);

        endTime = System.currentTimeMillis();
        System.out.println("Frame Time: " + (endTime - startTime) + " / " + allowedTime + " ms");
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
