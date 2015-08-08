package app;

public class Monitor {
    int threadCount;
    volatile int count;

    public Monitor(int threadCount) {
        this.threadCount = threadCount;
    }

    public void reset() {
        count = threadCount;
    }

    public synchronized void signal() {
        count--;
        if (count == 0) {
            notify();
        }
    }

    public synchronized void awaitCompletion() {
        while (count > 0) {
            try {
                wait(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
