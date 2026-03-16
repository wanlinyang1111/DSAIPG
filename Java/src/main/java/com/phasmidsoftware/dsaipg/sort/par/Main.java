package com.phasmidsoftware.dsaipg.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * This code has been fleshed out by Ziyao Qiao. Thanks very much.
 * CONSIDER tidy it up a bit.
 */
public class Main {

    public static void main(String[] args) {
        // 從 command line 第一個參數讀取 cutoff
if (args.length > 0) {
    ParSort.cutoff = Integer.parseInt(args[0]);
    System.out.println("Cutoff set from command line: " + ParSort.cutoff);
}
// 從 command line 第二個參數讀取 targetThreads（選填）
if (args.length > 1) {
    ParSort.targetThreads = Integer.parseInt(args[1]);
    System.out.println("Target threads set from command line: " + ParSort.targetThreads);
}
processArgs(args);
System.out.println("Degree of parallelism: " + ForkJoinPool.getCommonPoolParallelism());
        Random random = new Random();

        // 測試不同的陣列大小
        int[] sizes = {100000, 500000, 1000000, 2000000, 5000000};

        // 測試不同的 cutoff 值
        int[] cutoffs = {1000, 5000, 10000, 50000, 100000};

        // 測試不同的執行緒數（2 的次方）
        int[] threadCounts = {1, 2, 4, 8, 16};

        try {
            FileOutputStream fis = new FileOutputStream("./src/result.csv");
            OutputStreamWriter isr = new OutputStreamWriter(fis);
            BufferedWriter bw = new BufferedWriter(isr);

            // 寫 CSV 標題
            bw.write("arraySize,type,cutoff,threads,time(ms)\n");
            bw.flush();

            for (int size : sizes) {
                int[] array = new int[size];
                System.out.println("\n=== Array size: " + size + " ===");

                // 1. 先測序列排序（做基準）
                long seqStart = System.currentTimeMillis();
                for (int t = 0; t < 10; t++) {
                    for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                    Arrays.sort(array);
                }
                long seqEnd = System.currentTimeMillis();
                double seqTime = (double)(seqEnd - seqStart) / 10;
                System.out.println("sequential\t\t\t avg time: " + seqTime + "ms");
                bw.write(size + ",sequential,N/A,1," + seqTime + "\n");
                bw.flush();

                // 2. 測平行排序：不同 cutoff，預設執行緒數
                for (int cutoff : cutoffs) {
                    ParSort.cutoff = cutoff;
                    long startTime = System.currentTimeMillis();
                    for (int t = 0; t < 10; t++) {
                        for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                        ParSort.sort(array, 0, array.length);
                    }
                    long endTime = System.currentTimeMillis();
                    double avgTime = (double)(endTime - startTime) / 10;
                    System.out.println("parallel cutoff=" + cutoff + "\t\t avg time: " + avgTime + "ms");
                    bw.write(size + ",parallel," + cutoff + "," + ForkJoinPool.getCommonPoolParallelism() + "," + avgTime + "\n");
                    bw.flush();
                }

                // 3. 測平行排序：不同執行緒數，固定 cutoff=10000
                System.out.println("--- thread count test (cutoff=10000) ---");
                for (int threads : threadCounts) {
                    ForkJoinPool pool = new ForkJoinPool(threads);
                    ParSort.cutoff = 10000;
                    final int[] arr = new int[size];
                    long startTime = System.currentTimeMillis();
                    for (int t = 0; t < 10; t++) {
                        for (int i = 0; i < arr.length; i++) arr[i] = random.nextInt(10000000);
                        pool.submit(() -> ParSort.sort(arr, 0, arr.length)).join();
                    }
                    long endTime = System.currentTimeMillis();
                    double avgTime = (double)(endTime - startTime) / 10;
                    System.out.println("threads=" + threads + "\t\t\t avg time: " + avgTime + "ms");
                    bw.write(size + ",parallel-threads,10000," + threads + "," + avgTime + "\n");
                    bw.flush();
                    pool.shutdown();
                }
            }

            bw.close();
            System.out.println("\nDone! Results saved to result.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processArgs(String[] args) {
        String[] xs = args;
        while (xs.length > 0)
            if (xs[0].startsWith("-")) xs = processArg(xs);
    }

    private static String[] processArg(String[] xs) {
        String[] result = new String[0];
        System.arraycopy(xs, 2, result, 0, xs.length - 2);
        processCommand(xs[0], xs[1]);
        return result;
    }

    private static void processCommand(String x, String y) {
        if (x.equalsIgnoreCase("N")) setConfig(x, Integer.parseInt(y));
        else
            if (x.equalsIgnoreCase("P"))
                ForkJoinPool.getCommonPoolParallelism();
    }

    private static void setConfig(String x, int i) {
        configuration.put(x, i);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<String, Integer> configuration = new HashMap<>();
}