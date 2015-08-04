package app;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppletMain extends Applet implements Runnable {
    Image backBuffer;
    Graphics backGraphics;

    AudioFile audioFile;
    Player player;
    Visualizer visualizer;
    Mixer mixer;
    Thread mainThread, playerThread, mixerThread;
    FrameRegulator dspFrameReg, graphicsFrameReg;

    volatile long frameCountResetTime = 0;
    volatile int frameCount = 0, lastFPS = 0;

//    private String filepath = "nara_16.wav";
//    private String filepath = "truth_be_known_16.wav";
    private String filepath = "gangnam_style_16.wav";
//    private String filepath = "the_next_episode_16.wav";
//    private String filepath = "light_my_fire_16.wav";
//    private String filepath = "came_to_this_16.wav";
//    private String filepath = "dark_horse_16.wav";
//    private String filepath = "every_time_we_touch_16.wav";

    public void init() {
        setSize(Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        backBuffer = createImage(Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
        backGraphics = backBuffer.getGraphics();

        initRenderHints(backGraphics);

//        dspFrameReg = new FrameRegulator();
        dspFrameReg = new FrameRegulator();
//        graphicsFrameReg = new FrameRegulator(60); // 16.6666667 milliseconds b/w frames (60 FPS)

        try {
            audioFile = new AudioFile(filepath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

//        setupFreqProgression(audioFile);

        player = new Player(audioFile.getBaseFormat());
        visualizer = new Visualizer();
        try {
            mixer = new Mixer(player, visualizer, audioFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        audioFile.garbageCollect();
    }

    private void setupFreqProgression(AudioFile audioFile) {
        double[] monoFreqProgData = Generator.generateFrequencySpectrumProgression(15.0);
        double[] stereoFreqProgData = Utility.monoToStereo(monoFreqProgData);
        byte[] stereoFreqProgRaw = Utility.doublesToBytes(stereoFreqProgData, 2, true);

        audioFile.setRawStereoBuffer(stereoFreqProgRaw);
    }

    public void start() {
        if (playerThread == null) {
            playerThread = new Thread(player);
            playerThread.start();
        }
        if (mixerThread == null) {
            mixerThread = new Thread(mixer);
            mixerThread.start();
        }
        if (mainThread == null) {
            mainThread = new Thread(this);
            mainThread.start();
        }
    }

    private void initRenderHints(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        Map<RenderingHints.Key, Object> renderingHintsMap = new HashMap<>();
        renderingHintsMap.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHintsMap.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        renderingHintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        renderingHintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHintsMap.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        renderingHintsMap.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2.setRenderingHints(renderingHintsMap);
    }

    // repaint() schedules the AWT thread to call update()
    public void update(Graphics g) {
//        frameCount++;
//        long timeDelta = System.currentTimeMillis() - frameCountResetTime;
//        if (timeDelta >= 1000) {
//            frameCountResetTime = System.currentTimeMillis();
//            lastFPS = frameCount;
//            frameCount = 0;
//        }
//
//        backGraphics.setColor(Color.GREEN);
//        backGraphics.drawString("FPS: " + lastFPS, 10, 10);

        // TODO: extract this fps counter into method

        g.drawImage(backBuffer, 0, 0, this);
        getToolkit().sync();
    }

    @Override
    public void run() {
//        dspFrameReg.setDependentFrameReg(graphicsFrameReg);
//        dspFrameReg.startRegulation();
        dspFrameReg.start();

        frameCountResetTime = System.currentTimeMillis();
        while (true) {
            try {
                long start = System.currentTimeMillis();
//                setupBackBuffer();

                double[] fftResults = visualizer.nextFftData();

//                createAllFrequencyVisual(fftResults);
                createRangedBarsVisual(fftResults);
                backBuffer.getWidth(this);

                repaint();
//                System.out.println(System.currentTimeMillis() - startRegulation + " ms");
//                dspFrameReg.waitForNextFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static boolean needSetup = true;
    static double[] ranges = null; // 14 bands

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

    static double[] lastFFTResults = null;

    private void createRangedBarsVisual(double[] fftResults) {
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

        setupBackBuffer();
        int gradientColor = 0;
        Color gradient = new Color(gradientColor, gradientColor, gradientColor, (int) volume);
        backGraphics.setColor(gradient);
        backGraphics.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);

        // bars
        backGraphics.setColor(Color.ORANGE);

        for (int i = 0; i < rangeMagnitudes.length; i++) {
            double magnitude = Math.sqrt(rangeMagnitudes[i]) * 25 * 10;
//            System.out.println(i + ": " + magnitude);
            backGraphics.fillRect(i * 8 * 6 + 2, (int) (Settings.APPLET_HEIGHT - 1 - magnitude), 8 * 6 - 4, (int) magnitude);
        }
    }

    private void createAllFrequencyVisual(double[] fftResults) {
        // volume background
        backGraphics.setColor(Color.CYAN);
        backGraphics.fillRect(0, 799, 1024, (int) (799 - (64 / fftResults[0])));

        // bars
        backGraphics.setColor(Color.WHITE);
        for (int i = 1; i <= fftResults.length / 2; i++) {
            int startIndex = i * 2;
            double sum = fftResults[startIndex] + fftResults[startIndex -1];
            double normalizedAmplitude = Math.log10(startIndex * Settings.FFT_BIN_FREQUENCY) * (sum / 41.4 * 2);
            int magnitude = (int) (799 * normalizedAmplitude);
            backGraphics.drawLine(i, 799, i, 799 - magnitude);
        }
    }

    private void setupBackBuffer() {
        // wipe the image
        backGraphics.setColor(Color.WHITE);
        backGraphics.fillRect(0, 0, Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);

        // draw grid lines
    }
}