package fr.iutfbleau.sae32_2025.models;

/**
 * The <code>Leaf</code> class represents a terminal node in the Huffman Binary Tree.
 * <p>
 * In the context of Huffman coding, a Leaf node is distinct from an internal node because it holds actual data.
 * When traversing the tree using the bits of a code (0 for left, 1 for right), reaching a Leaf indicates
 * that a complete symbol has been decoded.
 * </p>
 * <p>
 * This class extends <code>Node</code> to add a field for storing the specific pixel value (character) associated
 * with the frequency.
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class Leaf extends Node {

    /**
     * The specific symbol (pixel intensity value) represented by this leaf.
     * It is stored as a char (unsigned 16-bit) to handle byte values from 0 to 255 easily.
     */
    private final char character;

    /**
     * Constructs a new Leaf node.
     *
     * @param character The pixel value (symbol) associated with this node.
     * @param frequency The frequency of occurrence of this symbol in the image.
     */
    public Leaf(char character, int frequency) {
        // Initialize the superclass (Node) with the frequency.
        // Leaves have no children, so left and right remain null.
        super(frequency);
        this.character = character;
    }

    /**
     * Retrieves the symbol stored in this leaf.
     *
     * @return The character (pixel value) of this leaf.
     */
    public char getCharacter() {
        return this.character;
    }
    
    /**
     * Checks if this node is a leaf.
     * Overridden for clarity, although the base implementation (checking null children) is sufficient.
     *
     * @return Always true for instances of this class.
     */
    @Override
    public boolean isLeaf() {
        return true;
    }
}