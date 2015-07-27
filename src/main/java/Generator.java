
public class Generator {
    public static double[] generateSineWave(double frequency, double sampleRate, int totalFrames) {
        double[] sineWaveArray = new double[totalFrames];
        double samplingInterval = sampleRate / frequency;

        for (int i = 0; i < sineWaveArray.length; i++) {
            double angle = (2.0 * Math.PI * i) / samplingInterval;
            sineWaveArray[i] = Math.sin(angle);
        }

        return sineWaveArray;
    }

    public static double[] generateFrequencySpectrumProgression(double duration) {
        int sampleRate = 44100;
        int arrayLength = (int) (sampleRate * duration);
        double minFreq = 20.0, maxFreq = 2000.0;

        double[] sineWaveArray = new double[arrayLength];
        for (int i = 0; i < sineWaveArray.length; i++) {
            double currentFreq = (maxFreq - minFreq) / arrayLength * i;
            double samplingInterval = sampleRate / currentFreq;
            double angle = (2.0 * Math.PI * i) / samplingInterval;
            sineWaveArray[i] = Math.sin(angle);
        }

        return sineWaveArray;
    }
}
