package fr.iutfbleau.sae32_2025.controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import fr.iutfbleau.sae32_2025.models.PifReader;
import fr.iutfbleau.sae32_2025.views.AppTheme;
import fr.iutfbleau.sae32_2025.views.ViewerFrame;

/**
 * The <code>Decoder</code> class acts as the specific Controller for the "Viewer" module of the application.
 * <p>
 * In the Model-View-Controller (MVC) architecture, this class orchestrates the decoding process:
 * 1. It determines the input file (either from command-line arguments or via a file chooser dialog).
 * 2. It triggers the <b>Model</b> (PifReader) to parse and decode the binary data.
 * 3. It instantiates the <b>View</b> (ViewerFrame) to display the reconstructed image to the user.
 * </p>
 * <p>
 * This class serves as a secondary entry point for the application, specifically focused on viewing .pif files.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class Decoder {

    /**
     * The main entry point for the Decoder module.
     * <p>
     * It handles two scenarios:
     * 1. <b>Argument provided:</b> The application attempts to open the specified file immediately.
     * 2. <b>No argument:</b> The application opens a graphical file chooser to let the user select a file.
     * </p>
     *
     * @param args Command line arguments. The first argument is expected to be the file path.
     */
    public static void main(String[] args) {
        // Initialize the UI look and feel
        AppTheme.setupLookAndFeel();

        if (args.length > 0) {
            // Scenario 1: File path passed as argument
            File targetFile = new File(args[0]);
            loadAndDisplay(targetFile);
        } else {
            // Scenario 2: Ask user to select a file
            JFileChooser fc = AppTheme.createFileChooser();
            
            // Set default filter to .pif
            for (FileFilter filter : fc.getChoosableFileFilters()) {
                if (filter.getDescription().contains("PIF")) {
                    fc.setFileFilter(filter);
                    break;
                }
            }
            // Remove "All files" to enforce strict selection
            fc.setAcceptAllFileFilterUsed(false);
            
            int result = fc.showOpenDialog(null);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                loadAndDisplay(fc.getSelectedFile());
            } else {
                // User cancelled the operation
                System.out.println("No file selected. Exiting.");
                System.exit(0);
            }
        }
    }

    /**
     * Static utility method used by the ViewerFrame to reload a PIF image explicitly.
     * This acts as a bridge between the View and the Model.
     *
     * @param file The .pif file to read.
     * @return The reconstructed BufferedImage.
     * @throws IOException If the file format is invalid or unreadable.
     */
    public static BufferedImage openPifImage(File file) throws IOException {
        if (!file.getName().toLowerCase().endsWith(".pif")) {
            throw new IOException("Invalid file format. Expected .pif");
        }
        return PifReader.load(file);
    }

    /**
     * Orchestrates the loading and displaying of an image.
     * <p>
     * This method runs the I/O operation (loading) on the current thread (or main thread),
     * but ensures that the GUI creation (ViewerFrame) happens on the Swing Event Dispatch Thread (EDT).
     * </p>
     *
     * @param file The file to load.
     */
    private static void loadAndDisplay(File file) {
        // Strict validation: Ensure the file is a .pif file
        if (!file.getName().toLowerCase().endsWith(".pif")) {
            JOptionPane.showMessageDialog(null, 
                "Invalid file format.\nThe Decoder only supports .pif files.\nPlease select a valid PIF archive.", 
                "Invalid Format", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Delegate the complex decoding logic to the Model
            BufferedImage img = PifReader.load(file);
            
            // Once loaded, update the View on the EDT using a named inner class
            new ViewerLauncher(img).run();
            
        } catch (IOException e) {
            // Handle errors gracefully by informing the user
            JOptionPane.showMessageDialog(null, 
                "Error reading the file: " + e.getMessage(), 
                "Read Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // =========================================================================
    // NAMED INNER CLASSES
    // =========================================================================

    /**
     * A Runnable task responsible for creating and showing the Viewer window.
     * This ensures strict thread safety with Swing components.
     */
    private static class ViewerLauncher implements Runnable {
        
        private final BufferedImage image;

        /**
         * Constructs the launcher with the image to display.
         *
         * @param image The decoded image.
         */
        public ViewerLauncher(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void run() {
            ViewerFrame frame = new ViewerFrame();
            frame.displayImage(image);
            frame.setVisible(true);
        }
    }
}