import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.JTextArea;

public class LargeDatasetSearch {

    private MappedByteBuffer buffer;
    private int numElements;
    private int elementSize;
    private ByteOrder byteOrder;
    private JTextArea outputArea;

    public LargeDatasetSearch(String filename, int numElements, int elementSize, ByteOrder byteOrder, JTextArea outputArea) throws IOException {
        this.numElements = numElements;
        this.elementSize = elementSize;
        this.byteOrder = byteOrder;
        this.outputArea = outputArea;

        // Open the file and open into memory
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
                return buffer.getLong(); // Java unsigned long
            default:
                throw new IllegalArgumentException("Unsupported element size.");
        }
    }

    public int binarySearch(long target) {
        outputArea.append("Algorithm: Binary Search\n");
        outputArea.append("Time Complexity: O(log n)\n");

        long startTime = System.nanoTime();

        int left = 0;
        int right = numElements - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            long midValue = readElement(mid);
            outputArea.append(String.format("Checking index %d: value = %d\n", mid, midValue));

            if (midValue == target) {
                long endTime = System.nanoTime();
                outputExecutionTime(startTime, endTime);
                return mid;
            } else if (midValue < target) {
                left = mid + 1;
                outputArea.append(String.format("Target greater than value at index %d. New left index: %d\n", mid, left));
            } else {
                right = mid - 1;
                outputArea.append(String.format("Target less than value at index %d. New right index: %d\n", mid, right));
            }
        }

        long endTime = System.nanoTime();
        outputExecutionTime(startTime, endTime);
        return -1; // Target not found
    }

    public int exponentialSearch(long target) {
        outputArea.append("Algorithm: Exponential Search\n");
        outputArea.append("Time Complexity: O(log n)\n");

        long startTime = System.nanoTime();

        if (readElement(0) == target) {
            outputArea.append("Found target at index 0\n");
            long endTime = System.nanoTime();
            outputExecutionTime(startTime, endTime);
            return 0;
        }

        int range = 1;
        while (range < numElements && readElement(range) <= target) {
            outputArea.append(String.format("Range doubled to %d\n", range));
            range *= 2;
        }

        int left = range / 2;
        int right = Math.min(range, numElements - 1);
        outputArea.append(String.format("Performing Binary Search between indexes %d and %d\n", left, right));

        int result = binarySearchInRange(target, left, right);

        long endTime = System.nanoTime();
        outputExecutionTime(startTime, endTime);
        return result;
    }

    private int binarySearchInRange(long target, int left, int right) {
        while (left <= right) {
            int mid = left + (right - left) / 2;
            long midValue = readElement(mid);
            outputArea.append(String.format("Checking index %d: value = %d\n", mid, midValue));

            if (midValue == target) {
                return mid;
            } else if (midValue < target) {
                left = mid + 1;
                outputArea.append(String.format("Target greater than value at index %d. New left index: %d\n", mid, left));
            } else {
                right = mid - 1;
                outputArea.append(String.format("Target less than value at index %d. New right index: %d\n", mid, right));
            }
        }
        return -1;
    }

    public int interpolationSearch(long target) {
        outputArea.append("Algorithm: Interpolation Search\n");
        outputArea.append("Time Complexity: O(log log n) on average, O(n) worst-case\n");

        long startTime = System.nanoTime();

        int left = 0;
        int right = numElements - 1;

        while (left <= right && target >= readElement(left) && target <= readElement(right)) {
            if (left == right) {
                if (readElement(left) == target) {
                    outputArea.append("Found target at index " + left + "\n");
                    long endTime = System.nanoTime();
                    outputExecutionTime(startTime, endTime);
                    return left;
                }
                break;
            }

            long leftVal = readElement(left);
            long rightVal = readElement(right);

            // Avoid division by zero
            if (rightVal == leftVal) {
                break;
            }

            // Estimate the position
            int pos = left + (int)(((double)(right - left) / (rightVal - leftVal)) * (target - leftVal));

            // Ensure pos is within array bounds
            if (pos < left || pos > right) {
                break;
            }

            long posValue = readElement(pos);
            outputArea.append(String.format("Estimated position %d: value = %d\n", pos, posValue));

            if (posValue == target) {
                long endTime = System.nanoTime();
                outputExecutionTime(startTime, endTime);
                return pos;
            } else if (posValue < target) {
                left = pos + 1;
                outputArea.append(String.format("Target greater than value at index %d. New left index: %d\n", pos, left));
            } else {
                right = pos - 1;
                outputArea.append(String.format("Target less than value at index %d. New right index: %d\n", pos, right));
            }
        }

        long endTime = System.nanoTime();
        outputExecutionTime(startTime, endTime);
        return -1;
    }
    public int hybridSearch(long target) {
        outputArea.append("Algorithm: Hybrid Search (Exponential + Interpolation + Binary)\n");
    
        long startTime = System.nanoTime();
    
        // Step 1: Exponential Search to narrow the range
        if (readElement(0) == target) {
            outputArea.append("Found target at index 0\n");
            long endTime = System.nanoTime();
            outputExecutionTime(startTime, endTime);
            return 0;
        }
    
        int range = 1;
        while (range < numElements && readElement(range) < target) {
            outputArea.append(String.format("Exponential step: range = %d\n", range));
            range *= 2;
        }
    
        int left = range / 2;
        int right = Math.min(range, numElements - 1);
        outputArea.append(String.format("Range narrowed to: left = %d, right = %d\n", left, right));
    
        // Step 2: Interpolation Search within the range
        while (left <= right && target >= readElement(left) && target <= readElement(right)) {
            if (left == right) {
                if (readElement(left) == target) {
                    outputArea.append("Found target at index " + left + "\n");
                    long endTime = System.nanoTime();
                    outputExecutionTime(startTime, endTime);
                    return left;
                }
                break;
            }
    
            long leftVal = readElement(left);
            long rightVal = readElement(right);
    
            // Avoid division by zero
            if (rightVal == leftVal) {
                break;
            }
    
            // Estimate the position
            int pos = left + (int)(((double)(right - left) / (rightVal - leftVal)) * (target - leftVal));
    
            // Ensure pos is within array bounds
            if (pos < left || pos > right) {
                break;
            }
    
            long posValue = readElement(pos);
            outputArea.append(String.format("Estimated position %d: value = %d\n", pos, posValue));
    
            if (posValue == target) {
                outputArea.append("Found target at index " + pos + "\n");
                long endTime = System.nanoTime();
                outputExecutionTime(startTime, endTime);
                return pos;
            } else if (posValue < target) {
                left = pos + 1;
                outputArea.append(String.format("Target greater than value at index %d. New left index: %d\n", pos, left));
            } else {
                right = pos - 1;
                outputArea.append(String.format("Target less than value at index %d. New right index: %d\n", pos, right));
            }
        }
    
        // Step 3: Binary Search fallback
        outputArea.append("Switching to Binary Search.\n");
        int result = binarySearchInRange(target, left, right);
    
        long endTime = System.nanoTime();
        outputExecutionTime(startTime, endTime);
        return result;
    }
    

    private void outputExecutionTime(long startTime, long endTime) {
        long duration = endTime - startTime;
        double milliseconds = duration / 1_000_000.0;
        outputArea.append(String.format("Execution Time: %.4f ms\n", milliseconds));
    }

    public void outputSpaceComplexity() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // Total memory in JVM
        long freeMemory = runtime.freeMemory();   // Free memory in JVM
        long usedMemory = totalMemory - freeMemory;

        outputArea.append(String.format("Space Complexity: Approximately %d bytes used\n", usedMemory));
    }

    public void close() {
        // Clean up buffer
        buffer = null;
    }
}
