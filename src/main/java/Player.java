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

    boolean hasBufferFrame = false;

    public Player(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
        rawQueue = new Vector<>();
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
        sourceLine = getSourceLine();
        targetLine = getTargetLine();

        if (sourceLine != null) {
            sourceLine.start();

            sourceLine.write(rawBuffer, 0, rawBuffer.length);

            sourceLine.drain();
            sourceLine.stop();
            sourceLine.close();
        }
    }

    private synchronized void playFromQueue() throws LineUnavailableException {
        sourceLine = getSourceLine();
        targetLine = getTargetLine();

        if (sourceLine != null) {
            sourceLine.start();

            while (true) {
                if (rawQueue.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                byte[] buffer = rawQueue.remove(0);

                sourceLine.write(buffer, 0, buffer.length);
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

    public synchronized void enqueue(byte[] buffer) {
        rawQueue.add(buffer);
        notify();
//        if (hasBufferFrame) {
//            notify();
//        } else {
//            hasBufferFrame = true;
//        }
    }

    public void setMasterGain(float volume) {
        float db=(float)(Math.log(volume) / Math.log(10.0) * 20.0);
        FloatControl gainControl = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(db);
    }
}
