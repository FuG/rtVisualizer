package app;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

public class TestApplet extends Applet implements Runnable {
    int WIDTH = 800, HEIGHT = 800;

    Image backBuffer;
    Graphics backG;

    ThreadDrawer td1, td2;
    Thread t1, t2, mainThread;

    class ThreadDrawer implements Runnable {
        Graphics g;
        int xMin, xMax;
        Color color;

        public ThreadDrawer(Graphics g, int xMin, int xMax, Color color) {
            this.g = g;
            this.xMin = xMin;
            this.xMax = xMax;
            this.color = color;
        }

        @Override
        public void run() {
            for (int i = xMin; i < xMax; i++) {
                g.setColor(color);
                g.drawLine(i, 0, i, HEIGHT - 1);
            }
        }
    }

    @Override
    public void init() {
        setSize(WIDTH, HEIGHT);

        backBuffer = createImage(WIDTH, HEIGHT);
        backG = backBuffer.getGraphics();

        backG.setColor(Color.BLACK);
        backG.fillRect(0, 0, WIDTH - 1, HEIGHT - 1);

        td1 = new ThreadDrawer(backG, 0, WIDTH, new Color(255, 0, 255));
        td2 = new ThreadDrawer(backG, 0, WIDTH, new Color(0, 255, 25));
    }

    @Override
    public void start() {
        t1 = new Thread(td1);
        t2 = new Thread(td2);
        mainThread = new Thread(this);

        t1.start();
        t2.start();
        mainThread.start();
    }

    @Override
    public void update(Graphics g) {
        g.drawImage(backBuffer, 0, 0, this);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            repaint();
        }
    }
}
