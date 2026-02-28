package com.phasmidsoftware.dsaipg.adt.pq;

import com.phasmidsoftware.dsaipg.util.benchmark.Benchmark_Timer;
import com.phasmidsoftware.dsaipg.util.config.Config;

import java.io.IOException;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.phasmidsoftware.dsaipg.util.config.Config.getConfig;

/**
 * Assignment 4: Heap Benchmark
 *
 * Compares 4 heap implementations using Benchmark_Timer:
 *   1. Binary Heap (basic)
 *   2. Binary Heap + Floyd's trick
 *   3. 4-ary Heap (basic)
 *   4. 4-ary Heap + Floyd's trick
 *
 * Settings: M=4095, insert 16,000 elements, remove 4,000 elements
 */
public class HeapBenchmark {

    static final int M       = 4095;
    static final int INSERTS = 16_000;
    static final int REMOVES = 4_000;
    static final int RUNS    = 20;

    private final Config config;
    private final int[] data;

    public HeapBenchmark() throws IOException {
        this.config = getConfig(HeapBenchmark.class);
        this.data   = generateData(INSERTS);
    }

    public static void main(String[] args) throws IOException, PQException {
        System.out.println("========================================");
        System.out.println("   Assignment 4: Heap Benchmark");
        System.out.println("========================================");
        System.out.printf("M=%d | Inserts=%d | Removes=%d | Runs=%d%n%n",
                M, INSERTS, REMOVES, RUNS);

        HeapBenchmark hb = new HeapBenchmark();

        double t1 = hb.run("1. Binary Heap (basic)   ", false, false);
        double t2 = hb.run("2. Binary Heap (Floyd)   ", false, true);
        double t3 = hb.run("3. 4-ary  Heap (basic)   ", true,  false);
        double t4 = hb.run("4. 4-ary  Heap (Floyd)   ", true,  true);

        hb.printSummary(t1, t2, t3, t4);
        hb.printSpillReport();
    }

    // ─────────────────────────────────────────────────────────
    // 核心：用 Benchmark_Timer 計時
    // ─────────────────────────────────────────────────────────

    private double run(String label, boolean fourAry, boolean floyd) {

        // Supplier：每次 run 建立一個新的空 heap
        Supplier<PriorityQueue<Integer>> supplier =
                () -> createHeap(fourAry, floyd);

        // Consumer：對這個 heap 執行 insert + remove（這部分被計時）
        Consumer<PriorityQueue<Integer>> consumer = heap -> {
            try {
                for (int x : data) heap.give(x);
                for (int i = 0; i < REMOVES; i++)
                    if (!heap.isEmpty()) heap.take();
            } catch (PQException e) {
                throw new RuntimeException(e);
            }
        };

        // 建立 Benchmark_Timer 並執行
        Benchmark_Timer<PriorityQueue<Integer>> timer =
                new Benchmark_Timer<>(label, config, consumer);

        double ms = timer.runFromSupplier(supplier, RUNS);
        System.out.printf("%s : %.3f ms%n", label, ms);
        return ms;
    }

    // ─────────────────────────────────────────────────────────
    // Heap 工廠
    // ─────────────────────────────────────────────────────────

    private static PriorityQueue<Integer> createHeap(boolean fourAry, boolean floyd) {
        Comparator<Integer> comp = Integer::compareTo;
        if (fourAry)
            return new PriorityQueue_4AryHeap<>(M, true, comp, floyd);
        else
            return new PriorityQueue_BinaryHeap<>(M, true, comp, floyd);
    }

    // ─────────────────────────────────────────────────────────
    // Spill 報告
    // ─────────────────────────────────────────────────────────

    private void printSpillReport() {
        System.out.println("\n========================================");
        System.out.println("   Spill Report");
        System.out.println("========================================");

        PriorityQueue_4AryHeap<Integer> heap =
                new PriorityQueue_4AryHeap<>(M, true, Integer::compareTo, false);
        for (int x : data) heap.give(x);

        System.out.printf("Total spilled elements  : %d%n", heap.getSpillCount());
        System.out.printf("Highest priority spilled: %d%n", heap.getHighestSpilled());
        System.out.printf("Expected spill count    : %d  (= %d inserts - %d capacity)%n",
                INSERTS - M, INSERTS, M);
    }

    // ─────────────────────────────────────────────────────────
    // 結果表格
    // ─────────────────────────────────────────────────────────

    private void printSummary(double t1, double t2, double t3, double t4) {
        System.out.println("\n========================================");
        System.out.println("   Results Summary");
        System.out.println("========================================");
        System.out.println("Implementation          | Time (ms) | vs Binary");
        System.out.println("------------------------|-----------|----------");
        System.out.printf("Binary Heap (basic)     | %9.3f | 1.00x%n", t1);
        System.out.printf("Binary Heap (Floyd)     | %9.3f | %.2fx%n", t2, t1 / t2);
        System.out.printf("4-ary  Heap (basic)     | %9.3f | %.2fx%n", t3, t1 / t3);
        System.out.printf("4-ary  Heap (Floyd)     | %9.3f | %.2fx%n", t4, t1 / t4);
    }

    // ─────────────────────────────────────────────────────────
    // 產生隨機資料
    // ─────────────────────────────────────────────────────────

    private static int[] generateData(int n) {
        Random rng = new Random(42);
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rng.nextInt(1_000_000);
        return arr;
    }
}