package fr.iutfbleau.sae32_2025.models;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

/**
 * The <code>PifWriter</code> class is responsible for encoding and writing the image data into the proprietary PIF format.
 * <p>
 * The PIF format structure is defined as follows:
 * 1. <b>Header:</b> Two 16-bit integers representing the Width and Height of the image.
 * 2. <b>Code Tables:</b> Three sequences of 256 bytes. Each byte represents the bit-length of the Huffman code for a pixel value (0-255).
 * This allows the decoder to reconstruct the Canonical Huffman Tree.
 * 3. <b>Compressed Data:</b> A continuous stream of bits representing the pixels (Red, Green, Blue sequences).
 * </p>
 * <p>
 * This class handles the complexity of "Bit Packing", allowing variable-length codes to be written
 * into standard 8-bit bytes for file storage.
 * </p>
 *
 * @version 1.3
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class PifWriter {

    /**
     * Encodes the provided image using Canonical Huffman codes and saves it to the specified file path.
     *
     * @param image             The source image to be compressed.
     * @param canonicalCodesRed   The map associating Red channel values (0-255) to their binary Huffman string.
     * @param canonicalCodesGreen The map associating Green channel values (0-255) to their binary Huffman string.
     * @param canonicalCodesBlue  The map associating Blue channel values (0-255) to their binary Huffman string.
     * @param path              The destination file path on the disk.
     * @throws IOException If an error occurs while creating or writing to the file.
     */
    public static void save(BufferedImage image,
                            Map<Character, String> canonicalCodesRed,
                            Map<Character, String> canonicalCodesGreen,
                            Map<Character, String> canonicalCodesBlue,
                            String path) throws IOException {

        // Use a BufferedOutputStream for performance, wrapped in DataOutputStream for primitive type writing
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            
            // --- STEP 1: WRITE HEADER ---
            // Write image dimensions as 16-bit integers (short)
            dos.writeShort(image.getWidth());
            dos.writeShort(image.getHeight());

            // --- STEP 2: WRITE CANONICAL CODE TABLES ---
            // We only need to write the length of the code for each value (0-255).
            // The decoder will be able to reconstruct the exact same tree from these lengths.
            writeChannelTable(dos, canonicalCodesRed);
            writeChannelTable(dos, canonicalCodesGreen);
            writeChannelTable(dos, canonicalCodesBlue);

            // --- STEP 3: WRITE COMPRESSED PIXEL DATA ---
            // This variables acts as a "Bit Buffer". We accumulate bits here until we have a full byte (8 bits).
            int bitBuffer = 0;
            int bitsCount = 0;

            int width = image.getWidth();
            int height = image.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    
                    // Extract color components (0-255)
                    char r = (char) ((pixel >> 16) & 0xFF);
                    char g = (char) ((pixel >> 8) & 0xFF);
                    char b = (char) (pixel & 0xFF);

                    // Retrieve the binary string representation for each component
                    String codeR = canonicalCodesRed.get(r);
                    String codeG = canonicalCodesGreen.get(g);
                    String codeB = canonicalCodesBlue.get(b);

                    // We put the codes in an array to iterate cleanly
                    String[] pixelCodes = { codeR, codeG, codeB };

                    for (String code : pixelCodes) {
                        if (code != null) {
                            for (int i = 0; i < code.length(); i++) {
                                char bitChar = code.charAt(i);
                                int bitValue = (bitChar == '1') ? 1 : 0;

                                // Shift the buffer to the left and add the new bit
                                bitBuffer = (bitBuffer << 1) | bitValue;
                                bitsCount++;
                                
                                // If the buffer is full (8 bits), write it to the stream
                                if (bitsCount == 8) {
                                    dos.writeByte(bitBuffer);
                                    bitBuffer = 0; // Reset buffer
                                    bitsCount = 0; // Reset counter
                                }
                            }
                        }
                    }
                }
            }

            // --- STEP 4: FLUSH REMAINING BITS ---
            // If the total number of bits is not a multiple of 8, some bits are left in the buffer.
            // We shift them to the "left" of the byte (padding with zeros on the right) and write the final byte.
            if (bitsCount > 0) {
                bitBuffer = bitBuffer << (8 - bitsCount);
                dos.writeByte(bitBuffer);
            }

            dos.flush();
        }
    }

    /**
     * Helper method to write the code lengths table for a single color channel.
     * It iterates from 0 to 255 and writes the length of the Huffman code for each value.
     *
     * @param dos            The output stream.
     * @param canonicalCodes The map containing the Huffman codes.
     * @throws IOException If a writing error occurs.
     */
    private static void writeChannelTable(DataOutputStream dos, Map<Character, String> canonicalCodes) throws IOException {
        for (int i = 0; i < 256; i++) {
            String code = canonicalCodes.get((char) i);
            
            // If a value (color intensity) is not present in the image, its length is 0.
            int length = (code != null) ? code.length() : 0;
            
            dos.writeByte(length);
        }
    }
}