package app;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AudioFile {
    private File file;
    private AudioFormat baseFormat;

    private byte[] rawStereoBuffer;
    private double[] normalStereoBuffer;

    public Channel left, right;

    public class Channel {
        public static final int LEFT = 0, RIGHT = 1;

        int orientation;
        private byte[] rawBuffer;
        private double[] normalBuffer;

        public Channel(int orientation) {
            this.orientation = orientation;
        }

        public byte[] getRawBufferInstance() throws IOException, UnsupportedAudioFileException {
            if (rawBuffer == null) {
                loadRawBuffer();
            }

            byte[] bufferInstance = new byte[rawBuffer.length];
            System.arraycopy(rawBuffer, 0, bufferInstance, 0, rawBuffer.length);
            return bufferInstance;
        }

        public double[] getNormalBufferInstance() throws IOException, UnsupportedAudioFileException {
            if (normalBuffer == null) {
                loadNormalBuffer();
            }

            double[] bufferInstance = new double[normalBuffer.length];
            System.arraycopy(normalBuffer, 0, bufferInstance, 0, normalBuffer.length);
            return bufferInstance;
        }

        private void loadRawBuffer() throws IOException, UnsupportedAudioFileException {
            int rawBufferSize = rawStereoBuffer.length / baseFormat.getChannels();
            rawBuffer = new byte[rawBufferSize];

            int orientationOffset = orientation * getChannels();
            for (int i = 0; i < rawBufferSize; i++) {
                if (i % getChannels() == 0) {
                    for (int offset = 0; offset < getSampleSizeInBytes(); offset++) {
                        rawBuffer[i + offset] = rawStereoBuffer[i * getSampleSizeInBytes() + orientationOffset + offset];
                    }
                }
            }
        }

        private void loadNormalBuffer() throws IOException, UnsupportedAudioFileException {
            if (rawBuffer == null) {
                loadRawBuffer();
            }

            int sampleSizeInBytes = getSampleSizeInBytes();
            boolean isSigned = encodingIsSigned();

            normalBuffer = Utility.bytesToDoubles(rawBuffer, sampleSizeInBytes, isSigned);
        }
    }

    public AudioFile(String filepath) {
        ClassLoader classLoader = getClass().getClassLoader();
        file = new File(classLoader.getResource(filepath).getFile());

        try {
            baseFormat = AudioSystem.getAudioInputStream(file).getFormat();
            printFormatInfo();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        left = new Channel(Channel.LEFT);
        right = new Channel(Channel.RIGHT);
    }

    public AudioFormat getBaseFormat() {
        return baseFormat;
    }

    public int getChannels() {
        return baseFormat.getChannels();
    }

    public AudioFormat.Encoding getEncoding() {
        return baseFormat.getEncoding();
    }

    public boolean encodingIsSigned() {
        return getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) ? true : false;
    }

    public int getSampleSizeInBytes() {
        return baseFormat.getSampleSizeInBits() / 8;
    }

    public float getSampleRate() {
        return baseFormat.getSampleRate();
    }

    private int getFrameSize() {
        return baseFormat.getFrameSize();
    }

    private float getFrameRate() {
        return baseFormat.getFrameRate();
    }

    private boolean isLittleEndian() {
        return !baseFormat.isBigEndian();
    }

    public void printFormatInfo() {
        System.out.println("\nAudio File Info");
        System.out.println("---------------");
        System.out.println("Channels: " + getChannels());
        System.out.println("Encoding: " + getEncoding());
        System.out.println("Sample Size (bytes): " + getSampleSizeInBytes());
        System.out.println("Sample Rate: " + getSampleRate());
        System.out.println("Frame Size: " + getFrameSize());
        System.out.println("Frame Rate: " + getFrameRate());
        System.out.println("Little Endian: " + isLittleEndian() + "\n");
    }

    private void loadRawStereoBuffer() throws IOException, UnsupportedAudioFileException {
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[4096];

        int nBytesRead = 0;
        while(nBytesRead != -1) {
            nBytesRead = inputStream.read(data, 0, data.length);
            if (nBytesRead != -1) {
                out.write(data, 0, nBytesRead);
            }
        }

        rawStereoBuffer = out.toByteArray();
    }

    private void loadNormalStereoBuffer() throws IOException, UnsupportedAudioFileException {
        if (rawStereoBuffer == null) {
            loadRawStereoBuffer();
        }

        int sampleSizeInBytes = getSampleSizeInBytes();
        boolean isSigned = encodingIsSigned();

        normalStereoBuffer = Utility.bytesToDoubles(rawStereoBuffer, sampleSizeInBytes, isSigned);
    }

    public byte[] getRawBufferInstance() throws IOException, UnsupportedAudioFileException {
        if (rawStereoBuffer == null) {
            loadRawStereoBuffer();
        }

        byte[] bufferInstance = new byte[rawStereoBuffer.length];
        System.arraycopy(rawStereoBuffer, 0, bufferInstance, 0, rawStereoBuffer.length);
        return bufferInstance;
    }

    public double[] getNormalBufferInstance() throws IOException, UnsupportedAudioFileException {
        if (normalStereoBuffer == null) {
            loadNormalStereoBuffer();
        }

        double[] bufferInstance = new double[normalStereoBuffer.length];
        System.arraycopy(normalStereoBuffer, 0, bufferInstance, 0, normalStereoBuffer.length);
        return bufferInstance;
    }

    public byte[] getRawBufferLeft() throws IOException, UnsupportedAudioFileException {
        if (rawStereoBuffer == null) {
            loadRawStereoBuffer();
        }

        return left.getRawBufferInstance();
    }

    public byte[] getRawBufferRight() throws IOException, UnsupportedAudioFileException {
        if (rawStereoBuffer == null) {
            loadRawStereoBuffer();
        }

        return right.getRawBufferInstance();
    }

    public void garbageCollect() {
        rawStereoBuffer = null;
        left.rawBuffer = null;
        right.rawBuffer = null;
    }


    /* EVERYTHING BELOW THIS COMMENT => FOR TEST USAGE ONLY */
    public AudioFile() {
        left = new Channel(Channel.LEFT);
        right = new Channel(Channel.RIGHT);
    }

    public void setBaseFormat(AudioFormat audioFormat) {
        baseFormat = audioFormat;
    }

    public void setRawStereoBuffer(byte[] buffer) {
        rawStereoBuffer = buffer;
    }
}
