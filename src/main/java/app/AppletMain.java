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
    FFTResultQueue fftResultQueue;
    Mixer mixer;
    Thread mainThread, playerThread, mixerThread;
    FrameRegulator frameReg;

    volatile long frameCountResetTime = 0;
    volatile int frameCount = 0, lastFPS = 0;

    private String filepath = Settings.AUDIO_FILE_NAME;

    public void init() {
        setSize(Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);

        backBuffer = createImage(Settings.APPLET_WIDTH, Settings.APPLET_HEIGHT);
        backGraphics = backBuffer.getGraphics();

        initRenderHints(backGraphics);

        frameReg = new FrameRegulator();

        try {
            audioFile = new AudioFile(filepath);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

//        setupFreqProgression(audioFile);

        player = new Player(audioFile.getBaseFormat());
        fftResultQueue = new FFTResultQueue();
        try {
            mixer = new Mixer(player, fftResultQueue, audioFile);
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

    private void displayFrameRate() {
        frameCount++;
        long timeDelta = System.currentTimeMillis() - frameCountResetTime;
        if (timeDelta >= 1000) {
            frameCountResetTime = System.currentTimeMillis();
            lastFPS = frameCount;
            frameCount = 0;
        }

        backGraphics.setColor(Color.GREEN);
        backGraphics.drawString("FPS: " + lastFPS, 10, 10);
    }

    // repaint() schedules the AWT thread to call update()
    public void update(Graphics g) {
//        displayFrameRate();

        // TODO: extract this fps counter into method

        g.drawImage(backBuffer, 0, 0, this);
        getToolkit().sync();
    }

    @Override
    public void run() {
        frameReg.start();

        frameCountResetTime = System.currentTimeMillis();
        while (true) {
            try {
                long start = System.currentTimeMillis();
//                setupBackBuffer();

                double[] fftResults = fftResultQueue.nextFftData();

//                Visualizer.createAllFrequencyVisual(fftResults, backGraphics);
                Visualizer.createRangedBarsVisual(fftResults, backGraphics);
                backBuffer.getWidth(this);

                repaint();
//                System.out.println(System.currentTimeMillis() - startRegulation + " ms");
//                frameReg.waitForNextFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
