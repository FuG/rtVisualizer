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
        nextFrameStartMillis = System.currentTimeMillis();
    }

    public double waitForNextFrame() throws InterruptedException {
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
        return nextFrameStartMillis - System.currentTimeMillis();
    }

    private void sleep(double millisToSleep) {
        Utility.log("FrameRegulator sleep(" + millisToSleep + ")");
//        if (millisToSleep > 0) {
//            long ms = (long) millisToSleep;
//            int ns = (int) ((millisToSleep - ms) * 1000000);
//
//            try {
//                Thread.sleep(ms, ns);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        if (millisToSleep > 0) {
            try {
                Thread.sleep((long) millisToSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}