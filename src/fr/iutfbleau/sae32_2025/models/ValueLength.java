package fr.iutfbleau.sae32_2025.models;

/**
 * The <code>ValueLength</code> class is a fundamental data structure used in the Canonical Huffman process.
 * <p>
 * It acts as a tuple associating a specific byte value (symbol) with the bit-length of its Huffman code.
 * This association is the key to reconstructing a Canonical Huffman Tree without needing to store the
 * tree structure itself.
 * </p>
 * <p>
 * This class implements <code>Comparable</code> to enforce a strict ordering rule required by the
 * canonical format: symbols must be processed primarily by code length, and secondarily by their
 * natural value.
 * </p>
 *
 * @version 1.1
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class ValueLength implements Comparable<ValueLength> {

    /**
     * The character (byte value) represented as a char.
     * In the context of image processing, this corresponds to a pixel color intensity (0-255).
     */
    public char value;

    /**
     * The length (in bits) of the Huffman code generated for this value.
     */
    public int length;

    /**
     * Constructs a new Value-Length pair.
     *
     * @param v The symbol value (e.g., a pixel intensity).
     * @param l The length of the associated binary code.
     */
    public ValueLength(char v, int l) {
        this.value = v;
        this.length = l;
    }

    /**
     * Compares this object with another <code>ValueLength</code> object.
     * <p>
     * The sorting logic is critical for the Canonical Huffman algorithm:
     * 1. Primary Sort Key: <b>Code Length</b> (Ascending). Shorter codes come first.
     * 2. Secondary Sort Key: <b>Value</b> (Ascending). If lengths are equal, the symbol value is used.
     * </p>
     *
     * @param o The object to be compared.
     * @return A negative integer, zero, or a positive integer as this object is less than,
     * equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(ValueLength o) {
        // First comparison: Sort by bit length
        if (this.length != o.length) {
            return Integer.compare(this.length, o.length);
        }
        
        // Second comparison: Sort lexicographically by value if lengths are identical
        return Character.compare(this.value, o.value);
    }
}