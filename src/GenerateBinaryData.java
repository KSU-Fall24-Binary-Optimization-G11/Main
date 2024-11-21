import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

public class GenerateBinaryData {

    public static void generateDataFile(String filename, int numElements, ByteOrder byteOrder) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(byteOrder);

            Random rand = new Random();
            int[] data = new int[numElements];

            // Generate unbalanced/non-uniform data
            for (int i = 0; i < numElements; i++) {
                if (i < numElements * 0.7) {
                    data[i] = rand.nextInt(1000); // Majority of data in lower range
                } else {
                    data[i] = rand.nextInt(Integer.MAX_VALUE - 1000) + 1000; // Sparse higher range
                }
            }

            // Sort the data
            java.util.Arrays.sort(data);

            // Write data to file
            for (int value : data) {
                buffer.putInt(0, value);
                fos.write(buffer.array());
            }
        }
    }
}
