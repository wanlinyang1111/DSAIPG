package com.phasmidsoftware.dsaipg.sort.par;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * ParSort is a class implementing a parallel sorting algorithm.
 * The sorting is executed using a fork-and-join approach,
 * where large arrays are divided into smaller portions and sorted concurrently.
 * Designed to optimize performance for sorting large integer arrays.
 * This code has been fleshed out by...
 * @author Ziyao Qiao. Thanks very much.
 */
final class ParSort {

    /**
     * Cutoff: if array segment is smaller than this, use Arrays.sort instead.
     * Default: 1000. Can be set from command line.
     */
    public static int cutoff = 1000;

    /**
     * Target number of parallel threads (must be a power of 2).
     * maxDepth = log2(targetThreads).
     * e.g. targetThreads=8 → maxDepth=3 → up to 8 parallel partitions.
     */
    public static int targetThreads = Runtime.getRuntime().availableProcessors();

    /**
     * Compute max recursion depth from targetThreads.
     * e.g. targetThreads=8 → maxDepth=3
     */
    private static int getMaxDepth() {
        return (int) (Math.log(targetThreads) / Math.log(2));
    }

    /**
     * Public entry point for sorting.
     */
    public static void sort(int[] array, int from, int to) {
        sort(array, from, to, 0);
    }

    /**
     * Internal recursive sort with depth tracking.
     * Stops forking new threads when:
     *   (1) segment size < cutoff, OR
     *   (2) depth >= maxDepth (lg t reached)
     */
    private static void sort(int[] array, int from, int to, int depth) {
        if (to - from < cutoff || depth >= getMaxDepth()) {
            Arrays.sort(array, from, to);
            return;
        }

        int mid = from + (to - from) / 2;

        // Fork left half into a new thread
        final int nextDepth = depth + 1;
        CompletableFuture<Void> left = CompletableFuture.runAsync(
                () -> sort(array, from, mid, nextDepth)
        );

        // Run right half on current thread
        sort(array, mid, to, nextDepth);

        // Wait for left half to finish
        left.join();
    }

    /**
     * Recursively sorts a portion of the array and returns a new sorted array.
     */
    static int[] sortRecursive(int[] array, int from, int to) {
        int[] result = new int[to - from];
        System.arraycopy(array, from, result, 0, to - from);
        sort(result, 0, to - from);
        return result;
    }

    /**
     * Merges two sorted arrays into one sorted array.
     */
    static int[] doMerge(int[] xs1, int[] xs2) {
        int[] result = new int[xs1.length + xs2.length];
        int i = 0, j = 0;
        for (int k = 0; k < result.length; k++) {
            if (i >= xs1.length) result[k] = xs2[j++];
            else if (j >= xs2.length) result[k] = xs1[i++];
            else if (xs2[j] < xs1[i]) result[k] = xs2[j++];
            else result[k] = xs1[i++];
        }
        return result;
    }

    /**
     * Asynchronously sorts a portion of the array.
     */
    static CompletableFuture<int[]> asyncSort(int[] array, int from, int to) {
        return CompletableFuture.supplyAsync(
                () -> sortRecursive(array, from, to)
        );
    }
}