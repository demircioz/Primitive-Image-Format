package fr.iutfbleau.sae32_2025.controllers;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.iutfbleau.sae32_2025.models.ImageAnalyzer;
import fr.iutfbleau.sae32_2025.models.Huffman;
import fr.iutfbleau.sae32_2025.models.PifWriter;
import fr.iutfbleau.sae32_2025.views.AppTheme;
import fr.iutfbleau.sae32_2025.views.ConverterFrame;

/**
 * The <code>Converter</code> class acts as the main Controller for the Encoding module.
 * <p>
 * In the MVC architecture, this class is responsible for:
 * 1. Handling user inputs (Menu clicks, Drag and Drop, Command Line Arguments).
 * 2. Orchestrating the Model logic (Analysis, Huffman Tree creation, File Writing).
 * 3. Updating the View (ConverterFrame) with visual feedback and statistical data.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class Converter {

    /** Reference to the currently loaded image. */
    private static BufferedImage image;
    
    /** Canonical Huffman codes for the Red channel. */
    private static Map<Character, String> canonicalR;
    
    /** Canonical Huffman codes for the Green channel. */
    private static Map<Character, String> canonicalG;
    
    /** Canonical Huffman codes for the Blue channel. */
    private static Map<Character, String> canonicalB;
    
    /** The main GUI window. */
    private static ConverterFrame frame;
    
    /** Stores the output path if provided via command line arguments. */
    private static String outputArg = null; 

    /**
     * Main entry point for the Converter application.
     * It sets up the environment and delegates the GUI creation to the Event Dispatch Thread.
     *
     * @param args Command line arguments (Optional input file, Optional output file).
     */
    public static void main(String[] args) {
        AppTheme.setupLookAndFeel();
        
        // Delegate startup logic to a dedicated named class on the EDT
        new GuiInitializer(args).run();
    }

    /**
     * Handles the specific case of opening an image dropped via Drag and Drop.
     *
     * @param file The file dropped onto the application window.
     */
    public static void openImageDragDrop(File file) {
        loadImage(file);
    }

    /**
     * Opens a file chooser dialog to let the user select an image to convert.
     */
    private static void openFileChooser() {
        JFileChooser filechooser = AppTheme.createFileChooser();
        
        // Set default filter to Images (JPG, PNG...)
        for (FileFilter filter : filechooser.getChoosableFileFilters()) {
            if (filter.getDescription().contains("Images")) {
                filechooser.setFileFilter(filter);
                break;
            }
        }
        
        int result = filechooser.showOpenDialog(frame);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            loadImage(filechooser.getSelectedFile());
        }
    }

    /**
     * Core logic: Loads an image, triggers analysis, builds Huffman trees, and updates the UI.
     *
     * @param file The image file to process.
     */
    private static void loadImage(File file) {
        if (!file.exists()) return;
        
        // Strict validation: Prevent opening .pif in Converter
        if (file.getName().toLowerCase().endsWith(".pif")) {
            JOptionPane.showMessageDialog(frame, 
                "This file is a .PIF archive.\nPlease use the Viewer (Decoder) to open it.", 
                "Incorrect Module", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (frame != null) {
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            frame.setStatus("Analyzing image...");
        }

        try {
            String[] formats = ImageIO.getReaderFormatNames();
            BufferedImage temp = ImageIO.read(file);
            if (temp == null) throw new IOException("Unsupported or corrupted image format. Please select a valid image ("+ Arrays.toString(formats) + ").");
            image = temp;
            
            // Step 1: Statistical Analysis
            ImageAnalyzer analyzer = new ImageAnalyzer();
            analyzer.extractFrequencies(image);

            // Step 2: Huffman Tree Creation and Canonical Code Generation
            Huffman hR = createHuffman(analyzer.getRedFreq());
            Huffman hG = createHuffman(analyzer.getGreenFreq());
            Huffman hB = createHuffman(analyzer.getBlueFreq());

            // Store codes for the writing phase
            canonicalR = hR.getCanonicalCodes();
            canonicalG = hG.getCanonicalCodes();
            canonicalB = hB.getCanonicalCodes();

            // Step 3: Update View
            if (frame != null) {
                frame.updateData(image,
                    analyzer.getRedFreq(), hR.getHuffmanCodes(), canonicalR,
                    analyzer.getGreenFreq(), hG.getHuffmanCodes(), canonicalG,
                    analyzer.getBlueFreq(), hB.getHuffmanCodes(), canonicalB
                );
                frame.setStatus("Image loaded: " + file.getName());
            }
        } catch (IOException e) { 
            if (frame != null) frame.setStatus("Load error.");
            JOptionPane.showMessageDialog(frame, "Error loading image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (frame != null) frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Helper method to initialize and process a Huffman tree for a frequency map.
     *
     * @param freqMap Map of integer values to frequencies.
     * @return A fully prepared Huffman object.
     */
    private static Huffman createHuffman(Map<Integer, Integer> freqMap) {
        // Convert Integer keys to Character keys for the Huffman model
        Map<Character, Integer> huffInput = new HashMap<>();
        for (Map.Entry<Integer, Integer> e : freqMap.entrySet()) {
            if (e.getValue() > 0) {
                huffInput.put((char) e.getKey().intValue(), e.getValue());
            }
        }
        
        Huffman h = new Huffman(huffInput);
        h.prepareDictionary();
        h.generateCanonicalCodes();
        return h;
    }

    /**
     * Initiates the process of writing the compressed PIF file to disk.
     */
    private static void startSave() {
        if (image == null) return;
        String path;

        // Determine output path: either from CLI args or via Save Dialog
        if (outputArg != null) {
            path = outputArg;
            outputArg = null; // Reset after usage
        } else {
            JFileChooser fc = AppTheme.createFileChooser();
            fc.setSelectedFile(new File("converted.pif"));
            fc.setFileFilter(new FileNameExtensionFilter("PIF Files (.pif)", "pif"));

            if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                path = fc.getSelectedFile().getAbsolutePath();
            } else {
                return; // User cancelled
            }
        }

        // Enforce extension
        if (!path.toLowerCase().endsWith(".pif")) {
            path += ".pif";
        }

        if (frame != null) {
            frame.setStatus("Writing PIF file...");
            frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }

        try {
            // Perform the writing operation using the Model
            PifWriter.save(image, canonicalR, canonicalG, canonicalB, path);
            
            // Calculate compression statistics
            File f = new File(path);
            long pifSize = f.length();
            long rawSize = (long) image.getWidth() * image.getHeight() * 3;
            double gain = 100.0 * (1.0 - ((double)pifSize / rawSize));
            
            String info = String.format("Size: %.2f MB (Gain %.1f%%)", pifSize / 1048576.0, gain);
            
            if (frame != null) frame.setStatus("Success: " + info);
            
            JOptionPane.showMessageDialog(frame, 
                "Conversion successful!\n" + info + "\nSaved to: " + path, 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            if (frame != null) frame.setStatus("Write error.");
            JOptionPane.showMessageDialog(frame, "Write error: " + e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (frame != null) frame.setCursor(Cursor.getDefaultCursor());
        }
    }

    // =============================================================================================
    // NAMED INNER CLASSES
    // =============================================================================================

    /**
     * Runnable class responsible for initializing the GUI on the Event Dispatch Thread.
     * It also handles the logic for command-line arguments (automatic loading/saving).
     */
    private static class GuiInitializer implements Runnable {
        private final String[] args;

        public GuiInitializer(String[] args) {
            this.args = args;
        }

        @Override
        public void run() {
            // Create the main frame with specific ActionListeners for its buttons
            frame = new ConverterFrame(new OpenAction(), new SaveAction());
            frame.setVisible(true);

            // Logic for Command Line Arguments
            if (args.length > 0) {
                File inputFile = new File(args[0]);
                
                // Store output path if provided
                if (args.length > 1) {
                    outputArg = args[1];
                }

                if (inputFile.exists()) {
                    loadImage(inputFile);
                    
                    // If an output argument was provided, trigger auto-save after a short delay
                    if (outputArg != null && image != null) {
                        Timer timer = new Timer(500, new AutoSaveAction());
                        timer.setRepeats(false);
                        timer.start();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "File not found: " + args[0], "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // If no args, open the file chooser immediately for better UX
                openFileChooser();
            }
        }
    }

    /**
     * Listener for the "Open File" action (Menu Item).
     */
    private static class OpenAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            openFileChooser();
        }
    }

    /**
     * Listener for the "Convert/Save" action (Menu Item).
     */
    private static class SaveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            startSave();
        }
    }

    /**
     * Listener used by the Timer to trigger an automatic save when running in CLI mode.
     */
    private static class AutoSaveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            startSave();
        }
    }
}