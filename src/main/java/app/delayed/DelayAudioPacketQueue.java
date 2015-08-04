package app.delayed;

import app.Settings;

import java.util.concurrent.DelayQueue;

public class DelayAudioPacketQueue {
    DelayQueue delayQueue;
    double packetStartTime = 0;

    public DelayAudioPacketQueue() {
        delayQueue = new DelayQueue();
    }

    public void add(byte[] audioData) {
        if (packetStartTime == 0) {
            packetStartTime = System.currentTimeMillis() + Settings.PLAYER_DELAY_TIME_MS;
        }
        DelayAudioPacket dap = new DelayAudioPacket(audioData, (long) packetStartTime);
        delayQueue.add(dap);

        packetStartTime += Settings.MILLIS_BETWEEN_FRAMES;
    }

    public byte[] next() throws InterruptedException {
        return ((DelayAudioPacket) delayQueue.take()).getAudioData();
    }
}
