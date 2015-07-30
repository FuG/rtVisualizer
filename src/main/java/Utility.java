import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class Utility {

    private static byte[] getBytes(double normalizedValue, int sampleSizeInBytes, boolean signed) {
        // sampleSizeInBytes = 1
        if (sampleSizeInBytes == 1) {
            return new byte[] { (byte) (normalizedValue * 255) };
        }

        // sampleSizeInBytes = (2, 3, 4)
        byte[] bytes = new byte[sampleSizeInBytes];
        int maxValue = (int) Math.pow(2, sampleSizeInBytes * 8);
        int offset = 0;
        if (signed) {
            int baseValue = (int) (normalizedValue * (maxValue / 2 - 1));
            for (int i = 0; i < sampleSizeInBytes; i++) {
                bytes[i] = (byte) ((baseValue >> offset) & 0xFF);
                offset += 8;
            }
        } else {
            // TODO: figure this out
        }

        return bytes;
    }

    private static double getNormalizedFloat64(byte[] bytes, int sampleSizeInBytes, boolean signed) {
        if (bytes.length != sampleSizeInBytes) {
            throw new IllegalArgumentException("Could not parse double from byte[]: bytes.length did not match sampleSizeInBytes");
        }

        // sampleSizeInBytes = 1
        if (sampleSizeInBytes == 1) {
            return (double) bytes[0] / 255;
        }

        // sampleSizeInBytes = (2, 3, 4)
        double normalizedValue = 0;
        int maxValue = (int) Math.pow(2, sampleSizeInBytes * 8);
        int offset = 0;
        if (signed) {
            int baseValue = 0;
            for (int i = 0; i < sampleSizeInBytes; i++) {
                if (i == sampleSizeInBytes - 1) {
                    baseValue |= (bytes[i] << offset);
                } else {
                    baseValue |= ((bytes[i] & 0xFF) << offset) ;
                }
                offset += 8;
            }

            normalizedValue = (double) baseValue / (maxValue / 2);
        } else {
            // TODO: figure this out
        }

        return normalizedValue;
    }

    public static double[] bytesToDoubles(byte[] bytes, int bytesPerDouble, boolean signed) {
        checkBytesPerDoubleArg(bytesPerDouble);

        int doubleArraySize = bytes.length / bytesPerDouble;
        double[] doubleArray = new double[doubleArraySize];
        for (int i = 0; i < doubleArraySize; i++) {
            byte[] sampleBytes = new byte[bytesPerDouble];
            for (int j = 0; j < bytesPerDouble; j++) {
                int dataIndex = i * bytesPerDouble + j;
                sampleBytes[j] = bytes[dataIndex];
            }
            doubleArray[i] = getNormalizedFloat64(sampleBytes, bytesPerDouble, signed);
        }

        return doubleArray;
    }

    public static byte[] doublesToBytes(double[] data, int bytesPerDouble, boolean signed) {
        checkBytesPerDoubleArg(bytesPerDouble);

        int byteArraySize = data.length * bytesPerDouble;
        byte[] byteArray = new byte[byteArraySize];
        for (int i = 0; i < data.length; i++) {
            byte[] doubleAsBytes = getBytes(data[i], bytesPerDouble, signed);
            for (int j = 0; j < bytesPerDouble; j++) {
                int byteArrayIndex = i * bytesPerDouble + j;
                byteArray[byteArrayIndex] = doubleAsBytes[j];
            }
        }

        return byteArray;
    }

    public static double[] monoToStereo(double[] monoData) {
        double[] stereoData = new double[monoData.length * 2];

        for (int i = 0; i < monoData.length; i++) {
            stereoData[i * 2] = monoData[i];
            stereoData[i * 2 + 1] = monoData[i];
        }

        return stereoData;
    }

    public static void printArrayToFile(double[] array, String fileName) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("C:\\Users\\Gary\\Desktop\\" + fileName, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < array.length; i++) {
            writer.println(array[i]);
        }
        writer.close();
    }

    private static void checkBytesPerDoubleArg(int bytesPerDouble) {
        if (bytesPerDouble < 1 || bytesPerDouble > 4) {
            throw new IllegalArgumentException("bytesPerDouble must be one of the following values: (1, 2, 3, 4)");
        }
    }


    public static double getCurrentMillis() {
        return System.nanoTime() / 1000000.0;
    }

    public static long getRemainingNanosFromMillis(double millis) {
        long ms = (long) millis;
        double remainder = millis - ms;
        return (long) (remainder * 1000000);
    }

    public static void log(String msg) {
//        System.out.println(System.currentTimeMillis() + " : " + msg);
    }
}
