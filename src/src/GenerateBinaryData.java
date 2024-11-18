import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GenerateBinaryData {
    public static void main(String[] args) {
        String filename = "data.bin";
        int numElements = 1000000;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

        try (FileOutputStream fos = new FileOutputStream(filename)) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(byteOrder);

            for (int i = 0; i < numElements; i++) {
                int value = i; // or any function to generate sorted data
                buffer.putInt(0, value);
                fos.write(buffer.array());
            }

            System.out.println("Data generation completed.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
