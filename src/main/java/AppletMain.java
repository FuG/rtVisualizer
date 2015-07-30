import javax.rmi.CORBA.Util;
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

    public static int WIDTH = 256 * 4, HEIGHT = 200 * 4;

    Image backBuffer;
    Graphics backGraphics;

    AudioFile audioFile;
    Player player;
    Visualizer visualizer;
    Mixer mixer;
    Thread mainThread, playerThread, visualizerThread, mixerThread;
    Thread frameRegThread;
    FrameRegulator graphicsFrameReg;
//    private String filepath = "nara_16.wav";
//    private String filepath = "truth_be_known_16.wav";
    private String filepath = "gangnam_style_16.wav";
//    private String filepath = "the_next_episode_16.wav";
//    private String filepath = "light_my_fire_16.wav";

    public void init() {
        setSize(WIDTH, HEIGHT);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        backBuffer = createImage(getWidth(), getHeight());
        backGraphics = backBuffer.getGraphics();

        initRenderHints(backGraphics);

        graphicsFrameReg = new FrameRegulator(60); // 16.6666667 milliseconds b/w frames (60 FPS)

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
        if (visualizerThread == null) {
            visualizerThread = new Thread(visualizer);
            visualizerThread.start();
        }
        if (mixerThread == null) {
            mixerThread = new Thread(mixer);
        }
        if (mainThread == null) {
            mainThread = new Thread(this);
            mainThread.start();
        }
        if (frameRegThread == null) {
            frameRegThread = new Thread(graphicsFrameReg);
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
        g.drawImage(backBuffer, 0, 0, this);
        getToolkit().sync();
    }

    final static int SLEEP_NANOS = (int) (0.2 * 1000000); // (1/5 of a millisecond)
    double[] barMagnitudes = null;

    // TODO: separate the frame regulators and have separate thread write to graphics
    @Override
    public void run() {
        while (frameRegThread == null || mixerThread == null) {
            try {
                Thread.sleep(0, SLEEP_NANOS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mixerThread.start();
        frameRegThread.start();

        Utility.log("AppletMain.run() loop start");
        boolean freshStart = true;
        boolean interpolateFrame;
        while (true) {
            try {
                long start = System.currentTimeMillis();
                setupBackBuffer();
                if (freshStart) {
                    while (!visualizer.newResults) {
//                        Thread.sleep(0, SLEEP_NANOS);
                        Thread.sleep(1);
                    }
                    freshStart = false;
                }
//                graphicsFrameReg.waitForNextFrame();

                if (visualizer.newResults) {
                    double[] fftResults  = visualizer.getFftResults();

                    Utility.log("AppletMain new results");
                    convertToRangeMagnitudes(fftResults);
                    Utility.log("AppletMain converted new results");
                    interpolateFrame = false;
                } else {
                    Utility.log("AppletMain interpolating frame");
                    interpolateFrame = true;
                }

                drawGraphics(interpolateFrame);

//                backBuffer.getWidth(this); // not sure if necessary
                repaint();
                System.out.println(System.currentTimeMillis() - start + " ms");
                Utility.log("AppletMain waitForNextFrame");
                graphicsFrameReg.waitForNextFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void drawGraphics(boolean interpolate) {
        final double decreaseFactor = 1;

        if (interpolate) {
            for (int i = 0; i < barMagnitudes.length; i++) {
                barMagnitudes[i] *= decreaseFactor;
            }
        }

        // volume background
        double volume = 0;

        for (int i = 2; i < 10; i++) {
            volume += barMagnitudes[i] / 8;
        }
//        System.out.println(volume);
        volume = (Math.pow(2, volume / 4) - 1) * 255;
        int gradientColor = 0;
        Color gradient = new Color(gradientColor, gradientColor, gradientColor, (int) volume);
        backGraphics.setColor(gradient);
        backGraphics.fillRect(0, 0, getWidth(), getHeight());

        // bars
        backGraphics.setColor(Color.MAGENTA);

        for (int i = 0; i < barMagnitudes.length; i++) {
            double magnitude = Math.sqrt(barMagnitudes[i]) * 25 * 8;
//            System.out.println(i + ": " + magnitude);
            backGraphics.fillRect(i * 8 * 4, (int) (getHeight() - 1 - magnitude), 8 * 4 - 1, (int) magnitude);
        }
    }

    static double binHz = 44100 / 2 / 2048; // 10.767
    static double[] ranges = null; // 32 bands

    private static void setupRanges() {
        if (ranges == null) {
            int binCount = 32;
            ranges = new double[binCount];
            ranges[0] = 33.333333333333333;

            for (int i = 1; i < binCount; i++) {
                ranges[i] = ranges[i - 1] * 1.221304;
            }
        }
    }

    private void convertToRangeMagnitudes(double[] fftResults) {
        setupRanges();

        int rangeBins = ranges.length;
        double[] rangeMagnitudes = new double[rangeBins];

        int rangeIndex = 0;
        for (int i = 1; i < fftResults.length && rangeIndex < rangeBins; i++) {
            if (i * binHz > ranges[rangeIndex]) {
                rangeIndex++;
            }

            if (rangeIndex < rangeBins) {
                rangeMagnitudes[rangeIndex] += fftResults[i] / Math.sqrt(i);
            }
        }
        barMagnitudes = rangeMagnitudes;

//        // volume background
//        double volume = 0;
//
//        for (int i = 2; i < 10; i++) {
//            volume += rangeMagnitudes[i] / 8;
//        }
//        System.out.println(volume);
//        volume = (Math.pow(2, volume / 4) - 1) * 255;
//        int gradientColor = 0;
//        Color gradient = new Color(gradientColor, gradientColor, gradientColor, (int) volume);
//        backGraphics.setColor(gradient);
//        backGraphics.fillRect(0, 0, getWidth(), getHeight());
//
//        // bars
//        backGraphics.setColor(Color.MAGENTA);
//
//        for (int i = 0; i < rangeMagnitudes.length; i++) {
//            double magnitude = Math.sqrt(rangeMagnitudes[i]) * 25 * 8;
////            System.out.println(i + ": " + magnitude);
//            backGraphics.fillRect(i * 8 * 4, (int) (getHeight() - 1 - magnitude), 8 * 4 - 1, (int) magnitude);
//        }
    }

    private void createAllFrequencyVisual(double[] fftResults) {
        // volume background
        backGraphics.setColor(Color.CYAN);
        backGraphics.fillRect(0, 799, 1024, (int) (799 - (64 / fftResults[0])));

        // bars
        backGraphics.setColor(Color.WHITE);
//        System.out.println(fftResults.length);
        for (int i = 1; i <= fftResults.length / 2; i++) {
            int startIndex = i * 2;
            double sum = fftResults[startIndex] + fftResults[startIndex -1];
            double normalizedAmplitude = Math.log10(startIndex * binHz) * (sum / 41.4 * 2);
            int magnitude = (int) (799 * normalizedAmplitude);
            backGraphics.drawLine(i, 799, i, 799 - magnitude);
        }
    }

    private void setupBackBuffer() {
        // wipe the image
        backGraphics.setColor(Color.WHITE);
        backGraphics.fillRect(0, 0, getWidth(), getHeight());

        // draw grid lines
    }
}
