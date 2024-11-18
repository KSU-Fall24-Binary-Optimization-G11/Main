import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteOrder;

public class LargeDatasetBinarySearch {

    private MappedByteBuffer buffer;
    private int numElements;
    private int elementSize;
    private ByteOrder byteOrder;

    public LargeDatasetBinarySearch(String filename, int numElements, int elementSize, ByteOrder byteOrder) throws IOException {
        this.numElements = numElements;
        this.elementSize = elementSize;
        this.byteOrder = byteOrder;

        // Open the file and map it into memory
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        FileChannel channel = file.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buffer.order(this.byteOrder);
        channel.close();
        file.close();
    }

    private long readElement(int index) {
        int position = index * elementSize;
        buffer.position(position);

        switch (elementSize) {
            case 1:
                return buffer.get() & 0xFF; // Unsigned byte
            case 2:
                return buffer.getShort() & 0xFFFF; // Unsigned short
            case 4:
                return buffer.getInt() & 0xFFFFFFFFL; // Unsigned int
            case 8:
                return buffer.getLong(); // Java doesn't have unsigned long
            default:
                throw new IllegalArgumentException("Unsupported element size.");
        }
    }

    public int search(long target) {
        int left = 0;
        int right = numElements - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            long midValue = readElement(mid);

            if (midValue == target) {
                return mid;
            } else if (midValue < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        return -1; // Target not found
    }

    public void close() {
        // Clean up buffer
        buffer = null;
    }

    public static void main(String[] args) {
        try {
            // A sorted dataset of 1,000,000 unsigned integers in 'data.bin'
            String filename = "data.bin";
            int numElements = 1000000;
            int elementSize = 4; // 4 bytes for integers
            ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

            LargeDatasetBinarySearch searcher = new LargeDatasetBinarySearch(filename, numElements, elementSize, byteOrder);

            long target = 123456; // The value target

            int index = searcher.search(target);

            if (index != -1) {
                System.out.println("Found target at index: " + index);
            } else {
                System.out.println("Target not found.");
            }

            // Close the searcher when done
            searcher.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
