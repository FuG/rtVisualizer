package app.delayed;

import app.Settings;

import java.util.concurrent.DelayQueue;

public class DelayFftDataPacketQueue {
    DelayQueue delayQueue;
    double packetStartTime = 0;

    public DelayFftDataPacketQueue() {
        delayQueue = new DelayQueue();
    }

    public void add(double[] fftData) {
        if (packetStartTime == 0) {
            packetStartTime = System.currentTimeMillis() + Settings.VISUALIZER_DELAY_TIME_MS;
        }
        DelayFftDataPacket dap = new DelayFftDataPacket(fftData, (long) packetStartTime);
        delayQueue.add(dap);

        packetStartTime += Settings.MILLIS_BETWEEN_FRAMES;
    }

    public double[] next() throws InterruptedException {
        return ((DelayFftDataPacket) delayQueue.take()).getFftData();
    }
}
