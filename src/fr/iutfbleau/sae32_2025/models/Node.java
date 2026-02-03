package fr.iutfbleau.sae32_2025.models;

/**
 * The <code>Node</code> class represents a fundamental element in the Huffman Binary Tree.
 * <p>
 * A node can be of two types:
 * 1. <b>Leaf Node:</b> Contains a specific pixel value (symbol). It has no children.
 * 2. <b>Internal Node:</b> Contains no value but connects two child nodes. Its frequency is the sum of its children's frequencies.
 * </p>
 * <p>
 * This class implements the <code>Comparable</code> interface to define a natural ordering based on frequency.
 * This ordering is crucial for the Priority Queue used during the tree construction phase (bottom-up approach).
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class Node implements Comparable<Node> {

    /**
     * The frequency of occurrence of the character (for leaves) or the cumulative frequency of the subtree (for internal nodes).
     */
    protected final int frequency;

    /**
     * The left child of this node in the binary tree.
     */
    protected Node left;

    /**
     * The right child of this node in the binary tree.
     */
    protected Node right;

    /**
     * Constructs a basic Node with a specific frequency.
     * This constructor is typically used when initializing leaf nodes before linking them.
     *
     * @param frequency The occurrence count of the symbol.
     */
    public Node(int frequency) {
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    /**
     * Constructs an Internal Node by merging two existing child nodes.
     * The frequency of this new node is strictly the sum of the frequencies of its children.
     *
     * @param leftChild  The left branch of the new subtree.
     * @param rightChild The right branch of the new subtree.
     */
    public Node(Node leftChild, Node rightChild) {
        this.frequency = leftChild.getFrequency() + rightChild.getFrequency();
        this.left = leftChild;
        this.right = rightChild;
    }

    /**
     * Retrieves the frequency associated with this node.
     *
     * @return The frequency value.
     */
    public int getFrequency() {
        return this.frequency;
    }

    /**
     * Retrieves the left child of this node.
     *
     * @return The left Node object, or null if it is a leaf.
     */
    public Node getLeft() {
        return this.left;
    }

    /**
     * Retrieves the right child of this node.
     *
     * @return The right Node object, or null if it is a leaf.
     */
    public Node getRight() {
        return this.right;
    }

    /**
     * Sets the left child for this node.
     *
     * @param left The node to attach to the left branch.
     */
    public void setLeft(Node left) {
        this.left = left;
    }

    /**
     * Sets the right child for this node.
     *
     * @param right The node to attach to the right branch.
     */
    public void setRight(Node right) {
        this.right = right;
    }

    /**
     * Checks if the current node is a leaf (i.e., has no children).
     * This helper method simplifies tree traversal logic.
     *
     * @return true if both children are null, false otherwise.
     */
    public boolean isLeaf() {
        return (this.left == null) && (this.right == null);
    }

    /**
     * Compares this node with another node to establish an order.
     * <p>
     * The comparison is based primarily on the frequency.
     * - A lower frequency is considered "smaller" (higher priority in the queue).
     * - A higher frequency is considered "larger".
     * </p>
     *
     * @param other The node to compare against.
     * @return A negative integer, zero, or a positive integer as this node's frequency
     * is less than, equal to, or greater than the specified node's frequency.
     */
    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.frequency, other.getFrequency());
    }
}