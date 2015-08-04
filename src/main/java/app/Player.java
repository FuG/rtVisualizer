package app;

import app.delayed.DelayAudioPacketQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.Vector;

public class Player implements Runnable {
    Vector<byte[]> rawQueue;
    public byte[] rawBuffer; // use alternating buffers

    AudioFormat audioFormat;
    SourceDataLine sourceLine;
    TargetDataLine targetLine;

    boolean hasBufferFrames = false;
    int bufferCount = Settings.PLAYER_BUFFER_SIZE;

    DelayAudioPacketQueue dapQueue;

    public Player(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        rawQueue = new Vector<>();

        dapQueue = new DelayAudioPacketQueue();

        try {
            sourceLine = getSourceLine();
            targetLine = getTargetLine();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        setMasterGain(Settings.PLAYER_MASTER_VOLUME);
    }

    @Override
    public void run() {
        try {
            if (rawBuffer != null) {
                playFromBuffer();
            } else {
                playFromQueue();
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private synchronized void playFromBuffer() throws LineUnavailableException {
        if (sourceLine != null) {
            sourceLine.start();

            sourceLine.write(rawBuffer, 0, rawBuffer.length);

            sourceLine.drain();
            sourceLine.stop();
            sourceLine.close();
        }
    }

    private synchronized void playFromQueue() throws LineUnavailableException {
        if (sourceLine != null) {
            sourceLine.start();

            while (true) {
                try {
                    byte[] buffer = dapQueue.next();
                    sourceLine.write(buffer, 0, buffer.length);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private SourceDataLine getSourceLine() throws LineUnavailableException {
        SourceDataLine line;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
        return line;
    }

    private TargetDataLine getTargetLine() throws LineUnavailableException {
        TargetDataLine line;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat); // format is an AudioFormat object
        if (!AudioSystem.isLineSupported(info)) throw new UnsupportedOperationException();
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(audioFormat);
        return line;
    }

    public void enqueue(byte[] buffer) {
        dapQueue.add(buffer);
    }

    public void setMasterGain(float volume) {
        float db=(float)(Math.log(volume) / Math.log(10.0) * 20.0);

        if (sourceLine != null) {
            FloatControl gainControl = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(db);
        }
    }
}
