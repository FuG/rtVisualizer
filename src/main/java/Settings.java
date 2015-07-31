
public class Settings {
    // Mixer
    public static double FRAMES_PER_SECOND = 30;
    public static double MILLIS_BETWEEN_FRAMES = 1000 /FRAMES_PER_SECOND;
    public static int FFT_BIN_COUNT = (int) Math.pow(2, (int) (Math.log(44100 / FRAMES_PER_SECOND) / Math.log(2) + 1));
    public static double FFT_BIN_FREQUENCY = 44100.0 / FFT_BIN_COUNT;

    // Applet
    public static int APPLET_WIDTH = 1536;
    public static int APPLET_HEIGHT = 1000;

    // Player
    public static int PLAYER_BUFFER_SIZE = 2; // increase to sync early audio w/ late video
    public static float PLAYER_MASTER_VOLUME = 0.3f;
}
