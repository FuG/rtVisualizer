import java.awt.Frame;

public class FrameRegulator implements Runnable {

    private int desiredFPS;
    private double millisBetweenFrames;
    private long frameStartMillis;

    int framesBeforeRealUpdate = 0;

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

    public void start() {
        frameStartMillis = System.currentTimeMillis();
    }

    public void waitForNextFrame() throws InterruptedException {
        frameStartMillis += millisBetweenFrames;
        long timeDelta = (long) (millisBetweenFrames - (System.currentTimeMillis() - frameStartMillis));

        if (timeDelta > 0) {
            Thread.sleep(timeDelta);
        }
    }

    @Override
    public void run() {

    }

//    public boolean hasNextUpdate() {
//        boolean hasUpdate = false;
//
//        if (framesBeforeRealUpdate == 0) {
//            hasUpdate = true;
//            framesBeforeRealUpdate = Settings.FRAMES_BETWEEN_REAL_UPDATE;
//        }
//        framesBeforeRealUpdate--;
//        return hasUpdate;
//    }
}