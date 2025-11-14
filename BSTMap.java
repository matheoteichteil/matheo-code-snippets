import java.util.Iterator;
import java.util.Set;

/**
 * BSTMap
 *
 * A simple Map<K, V> implementation backed by a binary search tree.
 * Keys must be Comparable so that they can be ordered in the tree.
 *
 * Supported operations:
 *  - put(key, value): insert or update a key–value pair
 *  - get(key): look up the value associated with a key
 *  - containsKey(key): test for membership
 *  - size(), clear()
 *
 * This implementation focuses on clarity rather than balancing;
 * performance is O(h) where h is the height of the tree.
 */
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    /** Node in the BST: stores a key–value pair and left/right children. */
    private class Node {
        private K key;
        private V value;
        private Node left;
        private Node right;

        private Node(K k, V v) {
            key = k;
            value = v;
            left = null;
            right = null;
        }
    }

    /** Root of the BST (null if the map is empty). */
    private Node root;
    /** Number of key–value pairs stored in the map. */
    private int size;

    /** Constructs an empty BST map. */
    public BSTMap() {
        this.root = null;
        size = 0;
    }

    /**
     * Insert or update a key–value pair.
     * If the key is new, it is inserted in sorted order.
     * If the key already exists, its value is replaced.
     */
    @Override
    public void put(K key, V value) {
        root = putHelper(root, key, value);
    }

    /** Recursive helper for put. Returns the (possibly new) subtree root. */
    private Node putHelper(Node n, K key, V value) {
        if (n == null) {
            size++;
            return new Node(key, value);
        }
        int cmp = key.compareTo(n.key);
        if (cmp > 0) {
            n.right = putHelper(n.right, key, value);
        } else if (cmp < 0) {
            n.left = putHelper(n.left, key, value);
        } else {
            // Key already present, overwrite existing value.
            n.value = value;
        }
        return n;
    }

    /** Returns the value associated with key, or null if the key is absent. */
    @Override
    public V get(K key) {
        return getHelper(root, key);
    }

    /** Recursive helper for get. */
    private V getHelper(Node n, K key) {
        if (n == null) {
            return null;
        }
        int cmp = key.compareTo(n.key);
        if (cmp == 0) {
            return n.value;
        } else if (cmp > 0) {
            return getHelper(n.right, key);
        } else {
            return getHelper(n.left, key);
        }
    }

    /** Returns true if this map contains the given key. */
    @Override
    public boolean containsKey(K key) {
        return containsKeyHelper(root, key);
    }

    /** Recursive helper for containsKey. */
    private boolean containsKeyHelper(Node n, K key) {
        if (n == null) {
            return false;
        }
        int cmp = key.compareTo(n.key);
        if (cmp == 0) {
            return true;
        } else if (cmp < 0) {
            return containsKeyHelper(n.left, key);
        } else {
            return containsKeyHelper(n.right, key);
        }
    }

    /** Returns the number of key–value pairs in the map. */
    @Override
    public int size() {
        return size;
    }

    /** Removes all mappings from the map. */
    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }
}
