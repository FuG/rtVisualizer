package app;

public class FrameRegulator implements Runnable {

    private int desiredFPS;
    private double millisBetweenFrames;
    private long frameStartMillis;

    int frameCount = 0;

    public FrameRegulator() {
        desiredFPS = (int) Settings.FRAMES_PER_SECOND;
        millisBetweenFrames = Settings.MILLIS_BETWEEN_FRAMES;
    }

    public FrameRegulator(int fps) {
        desiredFPS = fps;
        millisBetweenFrames = 1000.0 / desiredFPS;
    }

    public FrameRegulator(double millisBetweenFrames) {
        this.millisBetweenFrames = millisBetweenFrames;
    }

    public void start() {
        frameStartMillis = System.currentTimeMillis();
    }

    public void waitForNextFrame() throws InterruptedException {
        frameStartMillis += millisBetweenFrames;
        long timeDelta = (long) (millisBetweenFrames - (System.currentTimeMillis() - frameStartMillis));

//        System.out.println("(" + frameCount++ + ") Sleep Time: " + timeDelta);

        if (timeDelta > 0) {
            Thread.sleep(timeDelta);
        }
    }

    @Override
    public void run() {

    }
}