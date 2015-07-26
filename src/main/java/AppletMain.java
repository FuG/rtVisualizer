import javax.sound.sampled.UnsupportedAudioFileException;
import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
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
    Thread mainThread, playerThread, visualizerThread, mixerThread;
    FrameRegulator frameReg;
    private String filepath = "nara_16.wav";
//    private String filepath = "truth_be_known_16.wav";

    public void init() {
        setSize(1000, 800);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        backBuffer = createImage(getWidth(), getHeight());
        backGraphics = backBuffer.getGraphics();

        initRenderHints(backGraphics);

        frameReg = new FrameRegulator(60.0);

        try {
            audioFile = new AudioFile(filepath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

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
        g.drawImage(backBuffer, 0, 0, this);
        getToolkit().sync();
    }

    @Override
    public void run() {
        frameReg.start();

        while (true) {
            try {
                long start = System.currentTimeMillis();
                setupBackBuffer();
                mixer.processNext();

                while (!visualizer.newResults) {
                    Thread.sleep(5);
                }

                double[] fftResults;
                fftResults = visualizer.getFftResults();

//                createAllFrequencyVisual(fftResults);
                createRangedBarsVisual(fftResults);
                backBuffer.getWidth(this);

                repaint();
                System.out.println(System.currentTimeMillis() - start + " ms");
                frameReg.waitForNextFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static double binHz = 1000.0 / 30; // 660 stop
    static double[] ranges = { binHz, binHz * 2, binHz * 3, binHz * 4, binHz * 5, binHz * 6,
            binHz * 8, binHz * 10, binHz * 12, binHz * 14, binHz * 16, binHz * 18, binHz * 20,
            binHz * 23, binHz * 26, binHz * 29,
            binHz * 33, binHz * 38, binHz * 44, binHz * 51,
            binHz * 59, binHz * 68, binHz * 78, binHz * 89,
            binHz * 101, binHz * 114, binHz * 128, binHz * 143,
            binHz * 159, binHz * 177, binHz * 197, binHz * 219,
            binHz * 244, binHz * 273, binHz * 307, binHz * 347,
            binHz * 394, binHz * 449, binHz * 513, binHz * 587, binHz * 662 }; // 39 bands

    private void createRangedBarsVisual(double[] fftResults) {
        int rangeBins = ranges.length;
        for (int i = 0; i < rangeBins; i++) {
            ranges[i] += 0.01; // so we don't have to deal with rounding errors
        }

        double[] rangeMagnitudes = new double[rangeBins];

        int rangeIndex = 0;
        for (int i = 0; i < fftResults.length; i++) {
            if ((i + 1) * binHz > ranges[rangeIndex]) {
                if (rangeIndex < rangeBins - 1)
                    rangeIndex++;
                else
                    break;
            }

            rangeMagnitudes[rangeIndex] += fftResults[i] / 41.4;
        }

        backGraphics.setColor(Color.WHITE);

        for (int i = 0; i < rangeMagnitudes.length; i++) {
            double magnitude = Math.sqrt(rangeMagnitudes[i]) * 500;
            System.out.println(i + ": " + magnitude);
            backGraphics.fillRect(i * 25, (int) (799 - magnitude), 25, (int) magnitude);
        }
    }

    private void createAllFrequencyVisual(double[] fftResults) {
        backGraphics.setColor(Color.WHITE);
        for (int i = 0; i < fftResults.length; i++) {
            double factor = Math.pow(2.5, 1.0 / (Math.sqrt(i / 1323.0) + 0.1)) / 5;
            int magnitude = (int) (799 * fftResults[i] / factor);
            backGraphics.drawLine(i, 799, i, 799 - magnitude);
        }
    }

    private void setupBackBuffer() {
        // wipe the image
        backGraphics.setColor(Color.BLACK);
        backGraphics.fillRect(0, 0, getWidth(), getHeight());

        // draw grid lines
    }
}
