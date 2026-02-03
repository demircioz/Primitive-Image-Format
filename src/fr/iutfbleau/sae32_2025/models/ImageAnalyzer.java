package fr.iutfbleau.sae32_2025.models;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ImageAnalyzer</code> class is responsible for the statistical analysis phase of the compression process.
 * <p>
 * Its primary goal is to scan the entire image and calculate the frequency of occurrence for every possible
 * pixel value (0-255) in each color channel (Red, Green, Blue).
 * </p>
 * <p>
 * This frequency data is the prerequisite for building the Huffman Trees. According to the algorithm,
 * values with higher frequencies will eventually be assigned shorter binary codes, resulting in compression.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class ImageAnalyzer {

    /**
     * Map storing the frequency count for each intensity value (0-255) in the Red channel.
     */
    private final Map<Integer, Integer> redFreq;

    /**
     * Map storing the frequency count for each intensity value (0-255) in the Green channel.
     */
    private final Map<Integer, Integer> greenFreq;

    /**
     * Map storing the frequency count for each intensity value (0-255) in the Blue channel.
     */
    private final Map<Integer, Integer> blueFreq;

    /**
     * Constructs a new ImageAnalyzer and initializes the storage maps.
     */
    public ImageAnalyzer() {
        this.redFreq = new HashMap<>();
        this.greenFreq = new HashMap<>();
        this.blueFreq = new HashMap<>();
    }

    /**
     * Scans the provided image pixel by pixel to populate the frequency maps.
     * <p>
     * For each pixel, the method extracts the Red, Green, and Blue components using bitwise operations.
     * It relies on the standard ARGB integer structure where:
     * - Alpha is bits 24-31
     * - Red is bits 16-23
     * - Green is bits 8-15
     * - Blue is bits 0-7
     * </p>
     *
     * @param image The source {@link BufferedImage} to analyze.
     */
    public void extractFrequencies(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Iterate over every pixel in the image grid
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Retrieve the full ARGB pixel as a 32-bit integer
                int pixel = image.getRGB(x, y);

                // Extract Red component: Shift right by 16 bits, then mask with 0xFF (255) to keep only the last 8 bits
                int r = (pixel >> 16) & 0xFF;
                
                // Extract Green component: Shift right by 8 bits, then mask with 0xFF
                int g = (pixel >> 8) & 0xFF;
                
                // Extract Blue component: No shift needed, just mask with 0xFF
                int b = pixel & 0xFF;

                // Update the respective frequency tables
                incrementFreq(this.redFreq, r);
                incrementFreq(this.greenFreq, g);
                incrementFreq(this.blueFreq, b);
            }
        }
    }

    /**
     * Helper method to safely increment the count for a specific value in a frequency map.
     * If the value is encountered for the first time, it initializes the count to 1.
     *
     * @param map   The frequency map to update.
     * @param value The pixel intensity value (0-255) observed.
     */
    private void incrementFreq(Map<Integer, Integer> map, int value) {
        // Retrieve current count, default to 0 if null, then add 1
        int currentCount = map.getOrDefault(value, 0);
        map.put(value, currentCount + 1);
    }

    /**
     * Retrieves the frequency map for the Red channel.
     *
     * @return A Map associating pixel values (Keys) to their occurrence counts (Values).
     */
    public Map<Integer, Integer> getRedFreq() {
        return this.redFreq;
    }

    /**
     * Retrieves the frequency map for the Green channel.
     *
     * @return A Map associating pixel values (Keys) to their occurrence counts (Values).
     */
    public Map<Integer, Integer> getGreenFreq() {
        return this.greenFreq;
    }

    /**
     * Retrieves the frequency map for the Blue channel.
     *
     * @return A Map associating pixel values (Keys) to their occurrence counts (Values).
     */
    public Map<Integer, Integer> getBlueFreq() {
        return this.blueFreq;
    }
}