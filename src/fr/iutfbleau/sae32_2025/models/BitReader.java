package fr.iutfbleau.sae32_2025.models;

import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>BitReader</code> class provides a mechanism to read data from an Input Stream bit by bit.
 * <p>
 * Standard Java InputStreams read data byte by byte (8 bits). However, Huffman compression generates
 * variable-length codes (e.g., 3 bits, 12 bits) that do not align with byte boundaries.
 * </p>
 * <p>
 * This class acts as a buffer:
 * 1. It reads a full byte from the underlying stream.
 * 2. It stores this byte internally.
 * 3. It serves individual bits (0 or 1) to the caller until the byte is exhausted, then reads the next one.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class BitReader {

    /**
     * The source input stream from which bytes are read.
     */
    private final InputStream in;

    /**
     * The current byte being processed (buffer).
     */
    private int currentByte;

    /**
     * The number of bits remaining in the current byte that have not yet been read.
     * When this reaches 0, a new byte must be fetched from the stream.
     */
    private int remainingBits = 0;

    /**
     * Constructs a new BitReader.
     *
     * @param in The input stream to read compressed data from.
     */
    public BitReader(InputStream in) {
        this.in = in;
    }

    /**
     * Decodes a single pixel value by traversing the Huffman Tree using bits from the stream.
     * <p>
     * The method starts at the root of the tree and reads bits one by one:
     * - If the bit is 0, it moves to the Left child.
     * - If the bit is 1, it moves to the Right child.
     * This process repeats until a Leaf node is reached, which contains the actual pixel value.
     * </p>
     *
     * @param root The root node of the Canonical Huffman Tree.
     * @return The decoded integer value (0-255) represented by the leaf.
     * @throws IOException If the end of the stream is reached unexpectedly before finding a leaf.
     */
    public int readValue(Node root) throws IOException {
        Node current = root;
        
        // Traverse until we hit a Leaf node (which contains the data)
        while (!current.isLeaf()) {
            int bit = readBit();
            
            if (bit == -1) {
                throw new IOException("Error: Unexpected end of binary stream while decoding.");
            }
            
            // Move down the tree: 0 -> Left, 1 -> Right
            if (bit == 0) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }
        
        // Cast the node to Leaf to retrieve the character value
        return (int) ((Leaf) current).getCharacter();
    }

    /**
     * Reads the next single bit from the stream.
     * <p>
     * If the internal 8-bit buffer is empty, it triggers a read operation from the underlying InputStream
     * to fetch the next byte.
     * </p>
     *
     * @return The bit value (0 or 1), or -1 if the end of the stream has been reached.
     * @throws IOException If an I/O error occurs during the byte read.
     */
    private int readBit() throws IOException {
        // If no bits are left in the buffer, read a new byte from the file
        if (remainingBits == 0) {
            currentByte = in.read();
            
            // End of stream check
            if (currentByte == -1) {
                return -1;
            }
            
            // Reset counter for the new byte (8 bits available)
            remainingBits = 8;
        }

        // Decrement the counter to point to the next bit
        remainingBits--;

        // Extract the specific bit using a right shift and a bitwise AND
        // Example: if remainingBits is 7, we want the most significant bit.
        return (currentByte >> remainingBits) & 1;
    }
}