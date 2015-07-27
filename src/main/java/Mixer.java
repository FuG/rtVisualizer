import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class Mixer implements Runnable {
    FrameRegulator frameRegulator;
    Player player;
    Visualizer visualizer;
    AudioFile audioFile;
    double[] leftBuffer, rightBuffer;
    double[] normalStereoBuffer;

    FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);

    public boolean paused;

    public Mixer(Player player, Visualizer visualizer, AudioFile audioFile) throws IOException, UnsupportedAudioFileException {
        frameRegulator = new FrameRegulator(10);
        this.player = player;
        this.visualizer = visualizer;
        this.audioFile = audioFile;
        normalStereoBuffer = audioFile.getNormalBufferInstance();
        leftBuffer = audioFile.left.getNormalBufferInstance();
        rightBuffer = audioFile.right.getNormalBufferInstance();
        paused = false;
    }

    public synchronized void processNext() {
        notify();
    }

    @Override
    public void run() {
        process();
    }

    private synchronized void process() {
        int bufferSize = 2646; // samples per frame * 2 bytes (16 bit rate) at 60 fps (single channel)
        double[] dspBuffer = new double[bufferSize];
        double[] playBuffer = new double[bufferSize * 2]; // to get stereo input

        for (int i = 0; i < leftBuffer.length; i++) {
            if (i % bufferSize == 0) {
                byte[] finalPlayBuffer = Utility.doublesToBytes(playBuffer, 2, true);
                double[] fftResults = transform(dspBuffer);

                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                visualizer.process(fftResults);
                player.enqueue(finalPlayBuffer);
            }
            dspBuffer[i % bufferSize] = leftBuffer[i];
            int playBufferIndex = (i % bufferSize) * 2;
            playBuffer[playBufferIndex] = normalStereoBuffer[i * 2];
            playBuffer[playBufferIndex + 1] = normalStereoBuffer[i * 2 + 1];
        }
    }

    static boolean written = false;
    static int counter = 5;
    public double[] transform(double[] input) {
        double[] paddedInput = getPaddedArray(input);

        Complex[] complexResults;
        double[] tempConversion = new double[input.length / 2];
        double[] tempConversion2 = new double[paddedInput.length];
        try {
            complexResults = transformer.transform(paddedInput, TransformType.FORWARD);
            System.out.println(complexResults.length);

            for (int i = 0; i < input.length / 2; i++) { // take first half of mirrored results
                tempConversion[i] = complexResults[i + 1].abs(); // first bin is *noise*, offset by 1
            }

            for (int i = 0; i < paddedInput.length; i++) {
                tempConversion2[i] = complexResults[i].abs();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (!written) {
            if (counter-- == 0) {
                Utility.printArrayToFile(tempConversion2, "transformed.csv");
                written = true;
            }
        }
        return tempConversion;
    }

    private double[] getPaddedArray(double[] input) {
        int paddedLength = 4096;
        double[] paddedInput = new double[paddedLength];

        System.arraycopy(input, 0, paddedInput, 0, input.length);
        return paddedInput;
    }

    private void experimentalReverb() {
        int bufferSize = 4410;
//        int speakerDistanceOffset = 0;
        int speakerDistanceOffset = (int) (648 * 1.5);
        double volumeFactor = 0.7;
        double[] leftMixInput = new double[bufferSize];
        double[] rightMixInput = new double[bufferSize];
        double[] leftOverflow = new double[bufferSize];
        double[] rightOverflow = new double[bufferSize];
        for (int i = 0; i < leftBuffer.length; i++) {
            if (i % bufferSize == 0) {
                double[] leftMix = mix(leftMixInput, leftOverflow, speakerDistanceOffset, volumeFactor);
                double[] rightMix = mix(rightMixInput, rightOverflow, speakerDistanceOffset, volumeFactor);
                double[] stereoMix = mixToStereo(leftMix, rightMix);
                byte[] stereoMixBytes = Utility.doublesToBytes(stereoMix, 2, true);
                pauseCheck();
                player.enqueue(stereoMixBytes);
                leftMixInput = new double[bufferSize];
                rightMixInput = new double[bufferSize];
//                System.out.println("Enqueued: " + i);
            }

            leftMixInput[i % bufferSize] = leftBuffer[i];
            rightMixInput[i % bufferSize] = rightBuffer[i];
        }
        System.out.println("Data: Fully Loaded!");
    }

    public synchronized void pauseCheck() {
        if (paused) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void togglePause() {
        if (paused) {
            paused = false;
            notify();
        } else {
            paused = true;
        }
    }

    public static double[] mixToStereo(double[] left, double[] right) {
        int length = left.length + right.length;
        double[] result = new double[length];

        for (int i = 0; i < length / 2; i++) {
            int resultIndex = i * 2;
            result[resultIndex] = left[i];
            result[resultIndex + 1] = right[i];
        }

        return result;
    }

    public static double[] mix(double[] primary, double[] carryover, int offset, double volumeFactor) {
        double[] result = new double[primary.length];
        for (int i = 0; i < offset; i++) {
            result[i] = mix(primary[i], carryover[i], volumeFactor);
            carryover[i] = primary[primary.length - 1 - offset - i];
        }

        for (int i = 0; i < primary.length - offset; i++) {
            result[i + offset] = mix(primary[i + offset], primary[i], volumeFactor);
        }

        return result;
    }

    private static double mix(double primary, double secondary, double volumeFactor) {
        return (primary + secondary * volumeFactor) / 2;
    }

    public static double[] reverb(double[] primary, double[] secondary, int offset, double volumeFactor) {
        // need to use a queue for carryover
        return null;
    }
}
