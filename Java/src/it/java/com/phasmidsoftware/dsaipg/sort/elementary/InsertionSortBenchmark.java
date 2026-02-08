package com.phasmidsoftware.dsaipg.sort.elementary;

import com.phasmidsoftware.dsaipg.sort.generic.Sort;
import com.phasmidsoftware.dsaipg.util.benchmark.Benchmark_Timer;
import com.phasmidsoftware.dsaipg.util.config.Config;

import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InsertionSortBenchmark {

    public static void main(String[] args) throws IOException {
        System.out.println("Insertion Sort Benchmark");
        System.out.println("=".repeat(80));

        // Test with doubling sizes
        int[] sizes = {100, 200, 400, 800, 1600};
        int runs = 100; // Number of runs for each test

        System.out.println("\nArray Size\tRandom(ms)\tOrdered(ms)\tPartial(ms)\tReverse(ms)");
        System.out.println("-".repeat(80));

        for (int n : sizes) {
            double randomTime = benchmarkRandom(n, runs);
            double orderedTime = benchmarkOrdered(n, runs);
            double partialTime = benchmarkPartiallyOrdered(n, runs);
            double reverseTime = benchmarkReverse(n, runs);

            System.out.printf("%d\t\t%.4f\t\t%.4f\t\t%.4f\t\t%.4f%n",
                    n, randomTime, orderedTime, partialTime, reverseTime);
        }

        System.out.println("\n" + "=".repeat(80));
        printConclusions();
    }

    // Benchmark random array
    private static double benchmarkRandom(int n, int runs) throws IOException {
        Config config = Config.load(InsertionSortBenchmark.class);
        Supplier<Integer[]> supplier = () -> generateRandomArray(n);
        Consumer<Integer[]> sorter = array -> {
            InsertionSortComparator.sort(array);
        };

        Benchmark_Timer<Integer[]> timer = new Benchmark_Timer<>("Random", sorter);
        return timer.runFromSupplier(supplier, runs);
    }

    // Benchmark ordered array
    private static double benchmarkOrdered(int n, int runs) throws IOException {
        Config config = Config.load(InsertionSortBenchmark.class);
        Supplier<Integer[]> supplier = () -> generateOrderedArray(n);
        Consumer<Integer[]> sorter = array -> {
            InsertionSortComparator.sort(array);
        };

        Benchmark_Timer<Integer[]> timer = new Benchmark_Timer<>("Ordered", sorter);
        return timer.runFromSupplier(supplier, runs);
    }

    // Benchmark partially ordered array
    private static double benchmarkPartiallyOrdered(int n, int runs) throws IOException {
        Config config = Config.load(InsertionSortBenchmark.class);
        Supplier<Integer[]> supplier = () -> generatePartiallyOrderedArray(n);
        Consumer<Integer[]> sorter = array -> {
            InsertionSortComparator.sort(array);
        };

        Benchmark_Timer<Integer[]> timer = new Benchmark_Timer<>("Partially Ordered", sorter);
        return timer.runFromSupplier(supplier, runs);
    }

    // Benchmark reverse ordered array
    private static double benchmarkReverse(int n, int runs) throws IOException {
        Config config = Config.load(InsertionSortBenchmark.class);
        Supplier<Integer[]> supplier = () -> generateReverseOrderedArray(n);
        Consumer<Integer[]> sorter = array -> {
            InsertionSortComparator.sort(array);
        };

        Benchmark_Timer<Integer[]> timer = new Benchmark_Timer<>("Reverse", sorter);
        return timer.runFromSupplier(supplier, runs);
    }

    // Generate random array
    private static Integer[] generateRandomArray(int n) {
        Random random = new Random();
        Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            array[i] = random.nextInt(n * 10);
        }
        return array;
    }

    // Generate ordered array
    private static Integer[] generateOrderedArray(int n) {
        Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            array[i] = i;
        }
        return array;
    }

    // Generate partially ordered array (90% sorted, 10% shuffled)
    private static Integer[] generatePartiallyOrderedArray(int n) {
        Integer[] array = generateOrderedArray(n);
        Random random = new Random();
        int swaps = n / 10; // Swap 10% of elements
        for (int i = 0; i < swaps; i++) {
            int idx1 = random.nextInt(n);
            int idx2 = random.nextInt(n);
            Integer temp = array[idx1];
            array[idx1] = array[idx2];
            array[idx2] = temp;
        }
        return array;
    }

    // Generate reverse ordered array
    private static Integer[] generateReverseOrderedArray(int n) {
        Integer[] array = new Integer[n];
        for (int i = 0; i < n; i++) {
            array[i] = n - i - 1;
        }
        return array;
    }

    // Print conclusions
    private static void printConclusions() {
        System.out.println("CONCLUSIONS:");
        System.out.println("-".repeat(80));
        System.out.println("1. BEST CASE - Ordered Array:");
        System.out.println("   Time complexity: O(n)");
        System.out.println("   The array is already sorted, fastest performance.");

        System.out.println("\n2. AVERAGE CASE - Random Array:");
        System.out.println("   Time complexity: O(n²)");
        System.out.println("   Performance grows quadratically with input size.");

        System.out.println("\n3. NEARLY SORTED - Partially Ordered Array:");
        System.out.println("   Time complexity: Between O(n) and O(n²)");
        System.out.println("   Better than random, worse than fully sorted.");

        System.out.println("\n4. WORST CASE - Reverse Ordered Array:");
        System.out.println("   Time complexity: O(n²)");
        System.out.println("   Slowest performance, maximum comparisons and swaps.");

        System.out.println("\n5. ORDER OF GROWTH:");
        System.out.println("   When n doubles:");
        System.out.println("   - Random/Reverse: time approximately quadruples (O(n²))");
        System.out.println("   - Ordered: time approximately doubles (O(n))");
    }
}