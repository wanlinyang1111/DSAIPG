package com.phasmidsoftware.dsaipg.sort.elementary;

import java.util.Random;

public class InsertionSortBenchmark {

    public static void main(String[] args) {
        System.out.println("Insertion Sort Benchmark");
        System.out.println("================================================");
        
        int[] sizes = {100, 200, 400, 800, 1600};
        
        System.out.println("Size\tRandom(ms)\tOrdered(ms)\tPartial(ms)\tReverse(ms)");
        System.out.println("----------------------------------------------------------------");
        
        for (int n : sizes) {
            double r = test(n, "random");
            double o = test(n, "ordered");
            double p = test(n, "partial");
            double v = test(n, "reverse");
            System.out.printf("%d\t%.4f\t\t%.4f\t\t%.4f\t\t%.4f%n", n, r, o, p, v);
        }
        
        System.out.println("\nCONCLUSIONS:");
        System.out.println("1. Ordered: O(n) - fastest, already sorted");
        System.out.println("2. Partial: Between O(n) and O(n²) - mostly sorted");
        System.out.println("3. Random: O(n²) - average case");
        System.out.println("4. Reverse: O(n²) - worst case, slowest");
        System.out.println("\nWhen n doubles:");
        System.out.println("- Random/Reverse time approximately quadruples (O(n²))");
        System.out.println("- Ordered time approximately doubles (O(n))");
    }
    
    private static double test(int n, String type) {
        int runs = 100;
        long totalTime = 0;
        
        // Warmup
        for (int i = 0; i < 10; i++) {
            Integer[] arr = makeArray(n, type);
            InsertionSortComparator.sort(arr);
        }
        
        // Actual timing
        for (int i = 0; i < runs; i++) {
            Integer[] arr = makeArray(n, type);
            long start = System.nanoTime();
            InsertionSortComparator.sort(arr);
            long end = System.nanoTime();
            totalTime += (end - start);
        }
        
        return totalTime / 1_000_000.0 / runs; // Convert to milliseconds
    }
    
    private static Integer[] makeArray(int n, String type) {
        Integer[] arr = new Integer[n];
        Random rand = new Random();
        
        if (type.equals("random")) {
            for (int i = 0; i < n; i++) arr[i] = rand.nextInt(n * 10);
        } else if (type.equals("ordered")) {
            for (int i = 0; i < n; i++) arr[i] = i;
        } else if (type.equals("partial")) {
            for (int i = 0; i < n; i++) arr[i] = i;
            for (int i = 0; i < n / 10; i++) {
                int a = rand.nextInt(n);
                int b = rand.nextInt(n);
                int temp = arr[a];
                arr[a] = arr[b];
                arr[b] = temp;
            }
        } else {
            for (int i = 0; i < n; i++) arr[i] = n - i - 1;
        }
        
        return arr;
    }
}
