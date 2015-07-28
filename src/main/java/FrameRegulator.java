public class FrameRegulator implements Runnable {
    private int desiredFPS;
    public double millisBetweenFrames;
    public volatile double nextFrameStartMillis;

    public FrameRegulator() {
        desiredFPS = 60;
        millisBetweenFrames = 1.0 / desiredFPS * 1000;
    }

    public FrameRegulator(int fps) {
        desiredFPS = fps;
        millisBetweenFrames = 1.0 / desiredFPS * 1000;
    }

    public FrameRegulator(double millisBetweenFrames) {
        this.millisBetweenFrames = millisBetweenFrames;
    }

    public void init() {
        nextFrameStartMillis = Utility.getCurrentMillis();
    }

    public synchronized double waitForNextFrame() throws InterruptedException {
        double millisToSleep = millisTilNextFrame();
        sleep(millisToSleep);
        return millisToSleep;
    }

    @Override
    public void run() {
        init();

        while (true) {
            nextFrameStartMillis += millisBetweenFrames;

            sleep(millisTilNextFrame());
        }
    }

    private double millisTilNextFrame() {
        return nextFrameStartMillis - Utility.getCurrentMillis();
    }

    private void sleep(double millisToSleep) {
        if (millisToSleep > 0) {
            long ms = (long) millisToSleep;
            int ns = (int) ((millisToSleep - millisToSleep) * 1000000);

            try {
                Thread.sleep(ms, ns);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}