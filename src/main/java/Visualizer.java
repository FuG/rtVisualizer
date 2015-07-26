public class Visualizer implements Runnable {

    public volatile double[] fftResults;
    public volatile boolean newResults = false;

    public void Visualizer() {
        fftResults = null;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void process(double[] fftResults) {
        int fftBins = 2646 / 2; // 1323
        double hzPerBin = 44100.0 / fftBins; // 33.3333333

        this.fftResults = fftResults;
        newResults = true;
        notify();
    }

    public synchronized double[] getFftResults() {
        double[] result = new double[fftResults.length];
        System.arraycopy(fftResults, 0, result, 0, fftResults.length);
        newResults = false;
        return result;
    }
}
