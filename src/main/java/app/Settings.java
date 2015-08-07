package app;

public class Settings {
    // Mixer
    public static double FRAMES_PER_SECOND = 30;
    public static double MILLIS_BETWEEN_FRAMES = 1000 / FRAMES_PER_SECOND;
    public static int FFT_BIN_COUNT = (int) Math.pow(2, (int) (Math.log(44100 / FRAMES_PER_SECOND) / Math.log(2) + 1));
    public static double FFT_BIN_FREQUENCY = 44100.0 / FFT_BIN_COUNT;

    // Applet
    public static int APPLET_WIDTH = 1536;
    public static int APPLET_HEIGHT = 1000;

    // Player
    public static int PLAYER_BUFFER_SIZE = 2; // increase to sync early audio w/ late video
    public static float PLAYER_MASTER_VOLUME = 1.0f;
    public static long PLAYER_DELAY_TIME_MS = 17; // 17 @ 30 fps

    // Visualizer
    public static int VISUALIZER_DELAY_TIME_MS = 0;
    public static int VISUALIZER_PARTICLE_MAGNITUDE = 5; // 1 - 5

    // Audio Files
    private static String[] AUDIO_FILE_NAMES = {
            "nara_16.wav",                  /* 0  */
            "truth_be_known_16.wav",        /* 1  */
            "gangnam_style_16.wav",         /* 2  */
            "the_next_episode_16.wav",      /* 3  */
            "light_my_fire_16.wav",         /* 4  */
            "came_to_this_16.wav",          /* 5  */
            "dark_horse_16.wav",            /* 6  */
            "every_time_we_touch_16.wav",   /* 7  */
            "mind_heist_16.wav"             /* 8  */
    };
    public static String AUDIO_FILE_NAME = AUDIO_FILE_NAMES[2];
}
