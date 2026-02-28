package com.phasmidsoftware.dsaipg.adt.pq;

import java.util.Comparator;

/**
 * 4-ary Heap implementation of PriorityQueue.
 * Each node has up to 4 children instead of 2.
 *
 * Index rules (root at index 1):
 *   Parent of k  : (k + 2) / 4
 *   First child of k : 4k - 2   (children: 4k-2, 4k-1, 4k, 4k+1)
 */
public class PriorityQueue_4AryHeap<K> implements PriorityQueue<K> {

    private static final int D = 4; // number of children per node

    private final K[] heap;      // heap[1..capacity]
    private int m;               // current size
    private final int capacity;
    private final boolean max;
    private final Comparator<K> comparator;
    private final boolean floyd;

    // --- spill tracking ---
    private K highestSpilled = null;
    private int spillCount = 0;

    /**
     * Main constructor.
     *
     * @param n          maximum capacity
     * @param max        true = max-heap, false = min-heap
     * @param comparator comparator for type K
     * @param floyd      true = use Floyd's trick on insert
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue_4AryHeap(int n, boolean max, Comparator<K> comparator, boolean floyd) {
        this.capacity = n;
        this.max = max;
        this.comparator = comparator;
        this.floyd = floyd;
        this.heap = (K[]) new Object[n + 1]; // index 0 unused
        this.m = 0;
    }

    // ── PriorityQueue interface ──────────────────────────────

    @Override
    public boolean isEmpty() {
        return m == 0;
    }

    @Override
    public int size() {
        return m;
    }

    /**
     * Insert key into the heap.
     * If full, spill the key (record if it's the highest spilled).
     */
    @Override
    public void give(K key) {
        if (m == capacity) {
            spill(key);
            return;
        }
        heap[++m] = key;
        if (floyd) swimFloyd(m);
        else swim(m);
    }

    /**
     * Remove and return the root (highest priority) element.
     */
    @Override
    public K take() throws PQException {
        if (isEmpty()) throw new PQException("Priority queue is empty");
        K result = heap[1];
        swap(1, m);
        heap[m--] = null;
        sink(1);
        return result;
    }

    @Override
    public void heapConstructor() {
        for (int k = parent(m); k >= 1; k--) sink(k);
    }

    @Override
    public K peek(int k) {
        return heap[k];
    }

    @Override
    public boolean getMax() {
        return max;
    }

    // ── Spill info ───────────────────────────────────────────

    public K getHighestSpilled() { return highestSpilled; }
    public int getSpillCount()   { return spillCount; }

    // ── Core heap operations ─────────────────────────────────

    /**
     * Standard swim: bubble up from index k.
     */
    private void swim(int k) {
        while (k > 1) {
            int p = parent(k);
            if (!inverted(p, k)) break;
            swap(p, k);
            k = p;
        }
    }

    /**
     * Floyd's swim: swim straight to root, then sink back to correct position.
     * Reduces comparisons on average.
     */
    private void swimFloyd(int k) {
        K key = heap[k];

        // Phase 1: swim to root (no comparisons, just move up)
        while (k > 1) {
            int p = parent(k);
            heap[k] = heap[p];
            k = p;
        }
        // k is now 1 (root)

        // Phase 2: sink key down to correct position
        while (true) {
            int fc = firstChild(k);
            if (fc > m) break;

            // find the best child among up to D children
            int best = fc;
            for (int i = fc + 1; i <= fc + D - 1 && i <= m; i++)
                if (inverted(best, i)) best = i;

            // if key belongs here, stop
            if (!shouldSwap(key, heap[best])) break;

            heap[k] = heap[best];
            k = best;
        }
        heap[k] = key;
    }

    /**
     * Sink: push element at index k downward.
     */
    private void sink(int k) {
        while (true) {
            int fc = firstChild(k);
            if (fc > m) break;

            // find the best (highest priority) child
            int best = fc;
            for (int i = fc + 1; i <= fc + D - 1 && i <= m; i++)
                if (inverted(best, i)) best = i;

            if (!inverted(k, best)) break;
            swap(k, best);
            k = best;
        }
    }

    // ── Index helpers ────────────────────────────────────────

    /** First child of node k */
    private int firstChild(int k) {
        return D * k - (D - 2); // = 4k - 2
    }

    /** Parent of node k */
    private int parent(int k) {
        return (k + D - 2) / D; // = (k + 2) / 4
    }

    // ── Comparison helpers ───────────────────────────────────

    /** Returns true if heap[i] and heap[j] are out of order */
    private boolean inverted(int i, int j) {
        return (comparator.compare(heap[i], heap[j]) > 0) ^ max;
    }

    /** Returns true if 'key' should be placed below 'child' (i.e., child has higher priority) */
    private boolean shouldSwap(K key, K child) {
        return (comparator.compare(key, child) > 0) ^ max;
    }

    private void swap(int i, int j) {
        K tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void spill(K key) {
        spillCount++;
        if (highestSpilled == null ||
                (max && comparator.compare(key, highestSpilled) > 0) ||
                (!max && comparator.compare(key, highestSpilled) < 0))
            highestSpilled = key;
    }
}