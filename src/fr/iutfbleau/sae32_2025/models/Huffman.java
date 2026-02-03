package fr.iutfbleau.sae32_2025.models;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

/**
 * The <code>Huffman</code> class manages the logic for the Huffman compression algorithm.
 * <p>
 * It performs three main tasks:
 * 1. <b>Tree Construction:</b> Builds a binary tree based on character frequencies using a Priority Queue.
 * 2. <b>Code Generation:</b> Traverses the tree to assign initial binary codes (0 for left, 1 for right).
 * 3. <b>Canonical Transformation:</b> Converts the standard Huffman codes into "Canonical Huffman Codes".
 * </p>
 * <p>
 * The Canonical format is essential for the PIF file format because it allows the decoder to reconstruct
 * the exact same tree structure knowing only the code lengths, saving significant space in the file header.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class Huffman {

    /**
     * The root node of the constructed Huffman tree.
     */
    private Node root;

    /**
     * A map storing the frequency count for each character (pixel value).
     */
    private Map<Character, Integer> frequencies;

    /**
     * Stores the standard (initial) Huffman codes generated from the tree.
     */
    private final Map<Character, String> huffmanCodes;

    /**
     * Stores the final Canonical Huffman codes used for the actual compression.
     */
    private Map<Character, String> canonicalCodes;

    /**
     * Constructs a new Huffman processor.
     *
     * @param frequencies The frequency map extracted from the image analysis.
     */
    public Huffman(Map<Character, Integer> frequencies) {
        // Create a defensive copy of the map to avoid external modification issues
        this.frequencies = new HashMap<>(frequencies);
        this.huffmanCodes = new HashMap<>();
        this.canonicalCodes = new HashMap<>();
    }

    /**
     * Builds the Huffman Tree using the classic "Bottom-Up" approach.
     * <p>
     * Algorithm:
     * 1. Create a Leaf node for each character and add it to a Priority Queue (sorted by frequency).
     * 2. While the queue has more than one node:
     * a. Remove the two nodes with the lowest frequency.
     * b. Create a new internal parent node with these two as children.
     * c. Add the parent back to the queue.
     * 3. The last remaining node is the Root.
     * </p>
     */
    public void prepareDictionary() {
        PriorityQueue<Node> queue = new PriorityQueue<>();

        // 1. Initialize Queue with Leaves
        // We iterate over the frequency map safely
        Map<Character, Integer> freqCopy = new HashMap<>(this.frequencies);
        
        while (!freqCopy.isEmpty()) {
            Character c = freqCopy.keySet().iterator().next();
            int f = freqCopy.remove(c);
            queue.add(new Leaf(c, f));
        }

        // Handle edge case: Empty image or single color
        if (queue.isEmpty()) {
            this.root = new Node(0);
            return;
        }

        // 2. Build Tree
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();

            // Safety check
            if (right == null) {
                queue.add(left);
                break;
            }

            // Create parent and re-insert
            queue.add(new Node(left, right));
        }

        // 3. Finalize
        this.root = queue.poll();

        // Generate initial standard codes immediately
        if (this.root != null) {
            generateHuffmanCodes(this.root, "");
        }
    }

    /**
     * Recursively traverses the tree to assign binary codes to leaves.
     *
     * @param node The current node being visited.
     * @param code The binary string accumulated so far ("0" or "1" steps).
     */
    private void generateHuffmanCodes(Node node, String code) {
        // Base case: If we reach a leaf, we map the character to the current code
        if (node instanceof Leaf) {
            Leaf leaf = (Leaf) node;
            huffmanCodes.put(leaf.getCharacter(), code);
            return;
        }

        // Recursive step: 0 for Left, 1 for Right
        if (node.getLeft() != null) {
            generateHuffmanCodes(node.getLeft(), code + "0");
        }
        if (node.getRight() != null) {
            generateHuffmanCodes(node.getRight(), code + "1");
        }
    }

    /**
     * Converts the standard Huffman codes into Canonical Huffman Codes.
     * <p>
     * <b>Why Canonical?</b>
     * Standard Huffman codes depend on the tree structure, which varies.
     * Canonical codes only depend on the code *lengths*. By sorting symbols by length
     * and then lexicographically, we can generate codes sequentially.
     * </p>
     */
    public void generateCanonicalCodes() {
        // Convert the map entries to a list for sorting
        List<Map.Entry<Character, String>> sortedList = new ArrayList<>(huffmanCodes.entrySet());

        // Sort the list using the custom comparator
        Collections.sort(sortedList, new CanonicalComparator());

        int codeInteger = 0;
        int currentLength = sortedList.get(0).getValue().length();

        for (int i = 0; i < sortedList.size(); i++) {
            char value = sortedList.get(i).getKey();
            int targetLength = sortedList.get(i).getValue().length();

            // If the length increases, we shift the bits to the left to "make room"
            if (targetLength > currentLength) {
                codeInteger = codeInteger << (targetLength - currentLength);
                currentLength = targetLength;
            }

            // Convert the integer code to a binary string
            String binary = Integer.toBinaryString(codeInteger);
            
            // Pad with leading zeros to match the target length
            while (binary.length() < currentLength) {
                binary = "0" + binary;
            }

            canonicalCodes.put(value, binary);
            
            // The next code is simply the current one + 1
            codeInteger++;
        }
    }

    /**
     * Retrieves the standard Huffman codes.
     *
     * @return A map of character to binary string.
     */
    public Map<Character, String> getHuffmanCodes() {
        return this.huffmanCodes;
    }

    /**
     * Retrieves the root of the Huffman tree.
     *
     * @return The root Node object.
     */
    public Node getRoot() {
        return this.root;
    }

    /**
     * Retrieves the generated Canonical Huffman codes.
     *
     * @return A map of character to canonical binary string.
     */
    public Map<Character, String> getCanonicalCodes() {
        return this.canonicalCodes;
    }

    // =============================================================================================
    // NAMED INNER CLASSES
    // =============================================================================================

    /**
     * A custom Comparator to sort Huffman codes according to Canonical rules.
     * <p>
     * Sorting Rules:
     * 1. Primary: Code Length (Ascending) - Shortest codes first.
     * 2. Secondary: Symbol Value (Ascending) - Lexicographical order for equal lengths.
     * </p>
     */
    private static class CanonicalComparator implements Comparator<Map.Entry<Character, String>> {
        @Override
        public int compare(Map.Entry<Character, String> e1, Map.Entry<Character, String> e2) {
            int len1 = e1.getValue().length();
            int len2 = e2.getValue().length();

            // First, compare by length
            if (len1 != len2) {
                return Integer.compare(len1, len2);
            }
            
            // If lengths are equal, compare by character value
            return Character.compare(e1.getKey(), e2.getKey());
        }
    }
}