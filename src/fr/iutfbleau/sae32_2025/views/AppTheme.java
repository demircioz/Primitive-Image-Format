package fr.iutfbleau.sae32_2025.views;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * The <code>AppTheme</code> class is a static utility class responsible for managing the visual style of the application.
 * <p>
 * It centralizes the configuration of:
 * <ul>
 * <li>The "Look and Feel" (visual theme) to match the native operating system.</li>
 * <li>Standard typography (Fonts) used across all windows for consistency.</li>
 * <li>Resource loading mechanisms (Images and Icons) that work both in IDEs and JARs.</li>
 * <li>Creation of standardized file choosers with appropriate filters.</li>
 * </ul>
 * </p>
 * <p>
 * This architecture ensures visual consistency and simplifies asset management throughout the project.
 * </p>
 *
 * @version 1.4
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class AppTheme {

    /** Standard font for regular text (Modern System Font). */
    public static final Font FONT_ROBOTO = new Font("Segoe UI", Font.PLAIN, 13);
    
    /** Standard font for emphasized text or headers (Modern System Font Bold). */
    public static final Font FONT_ROBOTO_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    
    /** Monospace font used for displaying binary data or code. */
    public static final Font FONT_MONOSPACE = new Font("Consolas", Font.PLAIN, 13);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AppTheme() {
        // Utility class, no instance needed.
    }

    /**
     * Configures the application's Look and Feel.
     * <p>
     * It attempts to apply the native System Look and Feel (Windows, macOS, or GTK)
     * to ensure the application looks modern and integrates well with the OS.
     * </p>
     */
    public static void setupLookAndFeel() {
        try {
            // FORCE SYSTEM LOOK AND FEEL (Modern Windows/Mac style)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If styling fails, the application continues with the default Java look
            System.err.println("Warning: Failed to apply System Look and Feel.");
            e.printStackTrace();
        }
    }

    /**
     * Loads an image from the application's resources (classpath).
     * <p>
     * This method is designed to work both when running from an IDE and from a packaged JAR file.
     * It handles the file path resolution robustly.
     * </p>
     *
     * @param path The relative path to the image file (e.g., "icons/icon.png").
     * @return The loaded <code>Image</code> object, or <code>null</code> if the resource was not found.
     */
    public static Image loadAssetImage(String path) {
        // Ensure path starts with a slash for absolute classpath lookup
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        
        URL imgUrl = AppTheme.class.getResource(resourcePath);
        
        if (imgUrl != null) {
            try {
                return ImageIO.read(imgUrl);
            } catch (IOException e) {
                System.err.println("Error reading image resource: " + resourcePath);
                e.printStackTrace();
            }
        } else {
            System.err.println("Resource not found: " + resourcePath);
        }
        return null;
    }

    /**
     * Sets the application icon for a specific window (JFrame).
     * <p>
     * It attempts to load "icons/icon.png". If successful, it applies it to the window's title bar.
     * </p>
     *
     * @param frame The window (JFrame) to update.
     */
    public static void setAppIcon(JFrame frame) {
        Image icon = loadAssetImage("icons/icon.png");
        if (icon != null) {
            frame.setIconImage(icon);
        }
    }

    /**
     * Creates a standard FileChooser configured for the application.
     * <p>
     * This helper method ensures that file dialogs across the application are consistent.
     * It pre-configures filters for:
     * <ul>
     * <li>PIF Archives (.pif)</li>
     * <li>Standard Images (.jpg, .png, .bmp, .gif)</li>
     * </ul>
     * </p>
     *
     * @return A configured JFileChooser instance.
     */
    public static JFileChooser createFileChooser() {
        JFileChooser chooser = new JFileChooser(new File("."));
        FileNameExtensionFilter pifFilter = new FileNameExtensionFilter("PIF Files (.pif)", "pif");
        FileNameExtensionFilter imgFilter = new FileNameExtensionFilter("Images (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif");
        
        chooser.addChoosableFileFilter(pifFilter);
        chooser.addChoosableFileFilter(imgFilter);
        return chooser;
    }
}