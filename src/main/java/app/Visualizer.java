package app;

import app.delayed.DelayFftDataPacketQueue;

public class Visualizer {
    public volatile double[] fftResults;

    DelayFftDataPacketQueue dfdpQueue;

    public Visualizer() {
        fftResults = null;
        dfdpQueue = new DelayFftDataPacketQueue();
    }

    public void enqueue(double[] fftResults) {
        dfdpQueue.add(fftResults);
    }

    public double[] nextFftData() {
        try {
            return dfdpQueue.next();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
