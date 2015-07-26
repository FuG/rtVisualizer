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

    public void init() {
        setSize(1323, 800);
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

                if (fftResults != null) {
                    backGraphics.setColor(Color.WHITE);
                    for (int i = 0; i < fftResults.length; i++) {
                        int magnitude = (int) (799 * fftResults[i]);
                        backGraphics.drawLine(i, 799, i, 799 - magnitude);
                    }
                }

                repaint();
                System.out.println(System.currentTimeMillis() - start + " ms");
                frameReg.waitForNextFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupBackBuffer() {
        // wipe the image
        backGraphics.setColor(Color.BLACK);
        backGraphics.fillRect(0, 0, getWidth(), getHeight());

        // draw grid lines
    }
}
