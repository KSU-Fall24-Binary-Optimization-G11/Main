import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

public class MainApp {

    private JFrame frame;
    private JTextArea outputArea;
    private File dataFile;
    private int numElements = 1000000;
    private int elementSize = 4; // 4 bytes for integers
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    private String[] algorithms = {"Binary Search", "Exponential Search", "Interpolation Search"};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Binary Search Optimization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        outputArea = new JTextArea(20, 50);
        outputArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        JButton uploadButton = new JButton("Upload .bin File");
        JButton generateButton = new JButton("Generate New Data File");
        JButton runButton = new JButton("Run Program with Existing Data");
        JButton exitButton = new JButton("Exit");

        uploadButton.addActionListener(new UploadAction());
        generateButton.addActionListener(new GenerateAction());
        runButton.addActionListener(new RunAction());
        exitButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(runButton);
        buttonPanel.add(exitButton);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem exportItem = new JMenuItem("Export Output");
        JMenuItem newRunItem = new JMenuItem("Start New Run");
        menu.add(exportItem);
        menu.add(newRunItem);
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);

        exportItem.addActionListener(new ExportAction());
        newRunItem.addActionListener(e -> outputArea.setText(""));

        frame.getContentPane().add(buttonPanel, "North");
        frame.getContentPane().add(scrollPane, "Center");

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Upload .bin file
    private class UploadAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                dataFile = fileChooser.getSelectedFile();
                outputArea.append("Uploaded file: " + dataFile.getAbsolutePath() + "\n");
            }
        }
    }

    // Generate a new data file
    private class GenerateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                dataFile = new File("data.bin");
                GenerateBinaryData.generateDataFile(dataFile.getName(), numElements, byteOrder);
                outputArea.append("Generated new data file: " + dataFile.getAbsolutePath() + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error generating data file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RunAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (dataFile == null || !dataFile.exists()) {
                JOptionPane.showMessageDialog(frame, "No data file available. Please upload or generate one.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String input = JOptionPane.showInputDialog(frame, "Enter the target value to search for:", "Search Input", JOptionPane.QUESTION_MESSAGE);
                if (input != null && !input.isEmpty()) {
                    long target = Long.parseLong(input);

                    // Algorithm selection
                    String algorithm = (String) JOptionPane.showInputDialog(frame, "Select Search Algorithm:", "Algorithm Selection",
                            JOptionPane.PLAIN_MESSAGE, null, algorithms, algorithms[0]);

                    if (algorithm != null) {
                        outputArea.append("Starting search for target value: " + target + "\n");
                        LargeDatasetSearch searcher = new LargeDatasetSearch(dataFile.getName(), numElements, elementSize, byteOrder, outputArea);

                        // Output initial space usage
                        searcher.outputSpaceComplexity();

                        int index = -1;

                        switch (algorithm) {
                            case "Binary Search":
                                index = searcher.binarySearch(target);
                                break;
                            case "Exponential Search":
                                index = searcher.exponentialSearch(target);
                                break;
                            case "Interpolation Search":
                                index = searcher.interpolationSearch(target);
                                break;
                            default:
                                JOptionPane.showMessageDialog(frame, "Invalid algorithm selection.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                        }

                        if (index != -1) {
                            outputArea.append("Found target " + target + " at index: " + index + "\n");
                        } else {
                            outputArea.append("Target " + target + " not found.\n");
                        }

                        // Output space usage after search
                        searcher.outputSpaceComplexity();

                        searcher.close();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error during search operation.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Export the output
    private class ExportAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Output As");
            int userSelection = fileChooser.showSaveDialog(frame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    Utils.writeToFile(outputArea.getText(), fileToSave);
                    JOptionPane.showMessageDialog(frame, "Output exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error exporting output.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
