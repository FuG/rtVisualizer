package app;

import java.awt.Color;
import java.awt.Graphics;

public class Visualizer {
    static double[] lastRangedMagnitudes = null;
    static boolean needSetup = true;
    static double[] ranges = null; // 14 bands

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

    public static void createRangedBarsVisual(double[] fftResults, Graphics g) {
        setupRanges();

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

        // volume background
        double volume = 0;

        for (int i = 2; i < 10; i++) {
            volume += rangeMagnitudes[i] / 8;
        }
        volume = (Math.pow(2, volume / 4) - 1) * 255;

        setupBackBuffer(Color.WHITE, g);
        int gradientColor = 0;
        Color gradient = new Color(gradientColor, gradientColor, gradientColor, (int) volume);
        g.setColor(gradient);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);

        // current bars
//        g.setColor(Color.ORANGE);
        Color fader = new Color(255, 117, 25, 230);
        Color noFade = new Color(255, 117, 25, 255);
        g.setColor(noFade);
        for (int i = 0; i < rangeMagnitudes.length; i++) {
            double magnitude = Math.sqrt(rangeMagnitudes[i]) * 25 * 10;
            g.setColor(noFade);
            g.fillRect(i * 8 * 6 + 2, (int) (Settings.APPLET_HEIGHT - 1 - magnitude), 8 * 6 - 4, (int) magnitude);
            if (lastRangedMagnitudes != null) {
                double lastMagnitude = Math.sqrt(lastRangedMagnitudes[i]) * 25 * 10;
                double semiMag = lastMagnitude - magnitude;
//                System.out.println(i + ": " + semiMag);
                if (semiMag > 0) {
                    g.setColor(fader);
                    g.fillRect(i * 8 * 6 + 2, (int) (Settings.APPLET_HEIGHT - 1 - semiMag), 8 * 6 - 4, (int) semiMag);
                }
            }
        }

        // full faded bars
        if (lastRangedMagnitudes != null) {
            Color fadest = new Color(255, 117, 25, 200);
            g.setColor(fadest);
            for (int i = 0; i < lastRangedMagnitudes.length; i++) {
                double magnitude = Math.sqrt(lastRangedMagnitudes[i]) * 25 * 10;
//            System.out.println(i + ": " + magnitude);
                g.setColor(fadest);
                g.fillRect(i * 8 * 6 + 2, (int) (Settings.APPLET_HEIGHT - 1 - magnitude), 8 * 6 - 4, (int) magnitude);
            }
        }

        lastRangedMagnitudes = rangeMagnitudes;
    }

    private static void setupRanges() {
        if (needSetup) {
            int binCount = 32;
            ranges = new double[binCount];
            ranges[0] = 33.333333333333333;

            for (int i = 1; i < binCount; i++) {
                ranges[i] = ranges[i - 1] * 1.221304;
            }

            needSetup = false;
        }
    }

    private static void setupBackBuffer(Color color, Graphics g) {
        // wipe the image
        g.setColor(color);
        g.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
    }
}
