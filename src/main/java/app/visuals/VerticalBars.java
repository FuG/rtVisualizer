package app.visuals;

import app.Settings;

import java.awt.Color;
import java.awt.Graphics;

public class VerticalBars implements IVisual {
    static double[] lastRangedMagnitudes = null;
    static double[] ranges = null; // 32 bands

    public VerticalBars() {
        setupRanges();
    }

    @Override
    public void process(double[] fftResults, Graphics g) {
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
