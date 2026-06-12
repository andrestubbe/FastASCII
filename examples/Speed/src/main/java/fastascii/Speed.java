package fastascii;

import java.util.Random;

public class Speed {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println(" 🧨 StringBuilder vs FastASCII Demo");
        System.out.println("=========================================");
        System.out.println();
        
        int frames = 200000;
        int numbersPerFrame = 50000;
        int utf8PerFrame = 50000;
        
        System.out.println("[INFO] Generating pre-calculated data...");
        int[] numbers = new int[numbersPerFrame];
        char[] codepoints = new char[utf8PerFrame];
        Random rand = new Random(42);
        
        for (int i = 0; i < numbersPerFrame; i++) {
            numbers[i] = rand.nextInt(1000000);
        }
        for (int i = 0; i < utf8PerFrame; i++) {
            codepoints[i] = (char) (32 + rand.nextInt(90)); // Basic latin-1 chars to avoid utf16 overhead
        }
        
        System.out.println("[INFO] Data ready. Warmup running...");
        
        runStringBuilder(10, numbers, codepoints);
        runFastASCII(10, numbers, codepoints);
        
        com.sun.management.ThreadMXBean mxBean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        
        System.out.println("[INFO] Benchmarking " + frames + " frames...\n");
        
        // --- StringBuilder Benchmark ---
        long startSbAlloc = mxBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        long startSb = System.nanoTime();
        long totalSbBytes = runStringBuilder(frames, numbers, codepoints);
        long timeSb = System.nanoTime() - startSb;
        long endSbAlloc = mxBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        double msSb = timeSb / 1_000_000.0;
        long mbSb = (endSbAlloc - startSbAlloc) / (1024 * 1024);
        
        // --- FastASCII Benchmark ---
        long startFaAlloc = mxBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        long startFa = System.nanoTime();
        long totalFaBytes = runFastASCII(frames, numbers, codepoints);
        long timeFa = System.nanoTime() - startFa;
        long endFaAlloc = mxBean.getThreadAllocatedBytes(Thread.currentThread().getId());
        double msFa = timeFa / 1_000_000.0;
        long mbFa = (endFaAlloc - startFaAlloc) / (1024 * 1024);
        
        // --- Results ---
        System.out.println("=========================================");
        System.out.println(" 📊 Benchmark Results (" + frames + " Frames)");
        System.out.println("=========================================");
        System.out.printf("  StringBuilder:   %,10.2f ms%n", msSb);
        System.out.printf("  FastASCII:       %,10.2f ms%n", msFa);
        System.out.println("-----------------------------------------");
        System.out.printf("  Speedup:         %,10.2fx faster!%n", msSb / msFa);
        System.out.println("=========================================");
        System.out.println(" 🗑️ GC Pressure / Allocations");
        System.out.println("=========================================");
        System.out.printf("  [GC] StringBuilder allocated: %,6d MB%n", mbSb);
        System.out.printf("  [GC] FastASCII allocated:     %,6d MB%n", mbFa);
        System.out.println("=========================================\n");
    }
    
    private static long runStringBuilder(int frames, int[] numbers, char[] codepoints) {
        long totalBytes = 0;
        
        for (int i = 0; i < frames; i++) {
            StringBuilder sb = new StringBuilder(1000000); // Pre-size
            
            for (int j = 0; j < numbers.length; j++) {
                sb.append(numbers[j]).append(' ');
            }
            for (int j = 0; j < codepoints.length; j++) {
                sb.append(codepoints[j]).append(' ');
            }
            sb.append('\n');
            
            String result = sb.toString();
            totalBytes += result.length(); 
        }
        return totalBytes;
    }
    
    private static long runFastASCII(int frames, int[] numbers, char[] codepoints) {
        long totalBytes = 0;
        
        byte[] buffer = new byte[1024 * 1024]; 
        
        for (int i = 0; i < frames; i++) {
            int offset = 0;
            
            for (int j = 0; j < numbers.length; j++) {
                offset += FastASCIIWriter.writeInt(buffer, offset, numbers[j]);
                buffer[offset++] = ' ';
            }
            for (int j = 0; j < codepoints.length; j++) {
                offset += FastASCIIWriter.writeUtf8(buffer, offset, codepoints[j]);
                buffer[offset++] = ' ';
            }
            buffer[offset++] = '\n';
            
            totalBytes += offset;
        }
        return totalBytes;
    }
}
