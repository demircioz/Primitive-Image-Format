package fr.iutfbleau.sae32_2025.models;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The <code>PifReader</code> class provides the logic to interpret and decode files encoded in the PIF format.
 * <p>
 * Its primary responsibility is to reconstruct the original image from a compressed binary stream.
 * The decoding process follows these distinct phases:
 * 1. <b>Header Parsing:</b> Reading metadata (Width, Height).
 * 2. <b>Tree Reconstruction:</b> Reading the code lengths for each color channel and rebuilding the Canonical Huffman Trees.
 * 3. <b>Pixel Decoding:</b> navigating these trees bit-by-bit to resolve the original Red, Green, and Blue values.
 * </p>
 * <p>
 * This class implements the Canonical Huffman reconstruction algorithm, which allows generating the exact
 * same binary tree used during compression solely based on the lengths of the codes.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class PifReader {

    /**
     * Loads a specified .pif file and decodes it into a standard BufferedImage.
     *
     * @param file The PIF file object to read from the disk.
     * @return The reconstructed BufferedImage ready for display.
     * @throws IOException If the file cannot be read or if the format is invalid.
     */
    public static BufferedImage load(File file) throws IOException {
        // Use DataInputStream for reading primitive types (short, byte)
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            // --- PHASE 1: READ HEADER ---
            // The first 4 bytes contain the image dimensions (2 shorts)
            int width = dis.readShort();
            int height = dis.readShort();

            // --- PHASE 2: READ CODE LENGTH TABLES ---
            // We read 3 blocks of 256 bytes each. Each byte represents the code length for a value (0-255).
            int[] lengthsRed = readTable(dis);
            int[] lengthsGreen = readTable(dis);
            int[] lengthsBlue = readTable(dis);

            // --- PHASE 3: RECONSTRUCT CANONICAL TREES ---
            // Using the lengths, we rebuild the decoding trees in memory.
            Node rootRed = reconstructTree(lengthsRed);
            Node rootGreen = reconstructTree(lengthsGreen);
            Node rootBlue = reconstructTree(lengthsBlue);

            // --- PHASE 4: DECODE PIXEL DATA ---
            // We prepare the target image and a helper to read individual bits from the stream
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            BitReader br = new BitReader(dis);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Traverse the tree for each channel to find the leaf value (0-255)
                    int r = br.readValue(rootRed);
                    int g = br.readValue(rootGreen);
                    int b = br.readValue(rootBlue);
                    
                    // Combine components into a single ARGB integer
                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(x, y, rgb);
                }
            }
            return image;
        }
    }

    /**
     * Helper method to read a frequency/length table of 256 bytes.
     *
     * @param dis The input stream.
     * @return An integer array containing the length of the code for each index.
     * @throws IOException If reading fails.
     */
    private static int[] readTable(DataInputStream dis) throws IOException {
        int[] lengths = new int[256];
        for (int i = 0; i < 256; i++) {
            // readUnsignedByte() is crucial here to get values 0-255 instead of -128 to 127
            lengths[i] = dis.readUnsignedByte();
        }
        return lengths;
    }

    /**
     * Reconstructs a Canonical Huffman Tree from an array of code lengths.
     * <p>
     * <b>Algorithm Explanation:</b>
     * The canonical format ensures that:
     * 1. Shorter codes are assigned to values sorted by length.
     * 2. For codes of the same length, values are sorted lexicographically.
     * 3. Consecutive codes are derived by incrementing the integer value of the previous code.
     * </p>
     *
     * @param lengths An array where index i is the value and lengths[i] is the bit-length.
     * @return The root Node of the generated Huffman tree.
     */
    private static Node reconstructTree(int[] lengths) {
        // 1. Convert the raw array into a list of ValueLength objects
        List<ValueLength> list = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            if (lengths[i] > 0) {
                list.add(new ValueLength((char) i, lengths[i]));
            }
        }
        
        // Edge case: Empty image or solid color might result in an empty list
        if (list.isEmpty()) {
            return new Node(0);
        }

        // 2. Sort the list to respect Canonical property (Length first, then Value)
        Collections.sort(list);

        // 3. Generate the tree
        Node root = new Node(0);
        int currentCode = 0;
        int currentLength = list.get(0).length;

        for (ValueLength vl : list) {
            // If the length increases, we must shift the code to the left (append zeros)
            // to match the new bit-depth.
            while (currentLength < vl.length) {
                currentCode <<= 1;
                currentLength++;
            }
            
            // Insert this specific code path into the tree
            insertIntoTree(root, vl.value, currentCode, vl.length);
            
            // Increment the code for the next value at this level
            currentCode++;
        }
        return root;
    }

    /**
     * Inserts a value into the binary tree based on the integer representation of its Huffman code.
     * <p>
     * It converts the integer 'code' into a binary path (0=Left, 1=Right) of 'length' steps.
     * </p>
     *
     * @param root   The root of the tree.
     * @param value  The character (pixel intensity) to store at the leaf.
     * @param code   The integer representation of the Huffman code.
     * @param length The number of bits in the code (depth of the leaf).
     */
    private static void insertIntoTree(Node root, char value, int code, int length) {
        Node current = root;
        
        // Traverse down for the first (length - 1) bits
        // We start from the most significant bit relevant to our length
        for (int i = length - 1; i >= 1; i--) {
            int bit = (code >> i) & 1;
            
            if (bit == 0) {
                if (current.getLeft() == null) {
                    current.setLeft(new Node(0)); // Create internal node
                }
                current = current.getLeft();
            } else {
                if (current.getRight() == null) {
                    current.setRight(new Node(0)); // Create internal node
                }
                current = current.getRight();
            }
        }
        
        // The last bit determines where we place the Leaf
        int lastBit = code & 1;
        if (lastBit == 0) {
            current.setLeft(new Leaf(value, 0));
        } else {
            current.setRight(new Leaf(value, 0));
        }
    }
}