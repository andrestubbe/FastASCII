package fastascii;

import java.util.Random;
import fastascii.FastASCIIWriter;

/**
 * 🧨 StringBuilder vs FastASCII Demo
 * 
 * Demonstrates zero-allocation frame building by directly writing integers
 * and UTF-8 codepoints into a reusable byte array, bypassing StringBuilder
 * and String entirely for a massive speedup.
 */
public class Demo {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println(" 🧨 StringBuilder vs FastASCII Demo");
        System.out.println("=========================================");
        System.out.println();
        
        int frames = 10000;
        int numbersPerFrame = 2000;
        int utf8PerFrame = 2000;
        
        System.out.println("[INFO] Generating random frame data...");
        int[][] frameNumbers = new int[frames][numbersPerFrame];
        int[][] frameUtf8 = new int[frames][utf8PerFrame];
        Random rand = new Random(42);
        
        for (int i = 0; i < frames; i++) {
            for (int j = 0; j < numbersPerFrame; j++) {
                frameNumbers[i][j] = rand.nextInt(1000000); // 0 to 999999
            }
            for (int j = 0; j < utf8PerFrame; j++) {
                // Random block character (e.g. █ ░ ▒ ▓)
                frameUtf8[i][j] = 0x2588 - rand.nextInt(4);
            }
        }
        
        System.out.println("[INFO] Data ready. Warmup running...");
        
        // --- Warmup (JIT compilation) ---
        runStringBuilder(100, frameNumbers, frameUtf8);
        runFastASCII(100, frameNumbers, frameUtf8);
        
        System.out.println("[INFO] Benchmarking 10,000 frames...\n");
        
        // --- StringBuilder Benchmark ---
        long startSb = System.nanoTime();
        long totalSbBytes = runStringBuilder(frames, frameNumbers, frameUtf8);
        long timeSb = System.nanoTime() - startSb;
        double msSb = timeSb / 1_000_000.0;
        
        // --- FastASCII Benchmark ---
        long startFa = System.nanoTime();
        long totalFaBytes = runFastASCII(frames, frameNumbers, frameUtf8);
        long timeFa = System.nanoTime() - startFa;
        double msFa = timeFa / 1_000_000.0;
        
        // --- Results ---
        System.out.println("=========================================");
        System.out.println(" 📊 Benchmark Results (10,000 Frames)");
        System.out.println("=========================================");
        System.out.printf("  StringBuilder:   %,10.2f ms%n", msSb);
        System.out.printf("  FastASCII:       %,10.2f ms%n", msFa);
        System.out.println("-----------------------------------------");
        System.out.printf("  Speedup:         %,10.2fx faster!%n", msSb / msFa);
        System.out.println("=========================================");
        System.out.printf("  Processed Bytes: %,d bytes%n", totalFaBytes);
        System.out.println("=========================================\n");
    }
    
    private static long runStringBuilder(int frames, int[][] frameNumbers, int[][] frameUtf8) {
        long totalBytes = 0;
        int numbersPerFrame = frameNumbers[0].length;
        int utf8PerFrame = frameUtf8[0].length;
        
        for (int i = 0; i < frames; i++) {
            // Allocate a new StringBuilder per frame (simulating a rendering loop)
            StringBuilder sb = new StringBuilder(10000);
            
            for (int j = 0; j < numbersPerFrame; j++) {
                sb.append(frameNumbers[i][j]).append(' ');
            }
            for (int j = 0; j < utf8PerFrame; j++) {
                sb.appendCodePoint(frameUtf8[i][j]).append(' ');
            }
            sb.append('\n');
            
            // Standard Java allocation: UTF-16 to UTF-8 conversion array
            byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            totalBytes += bytes.length;
        }
        return totalBytes;
    }
    
    private static long runFastASCII(int frames, int[][] frameNumbers, int[][] frameUtf8) {
        long totalBytes = 0;
        int numbersPerFrame = frameNumbers[0].length;
        int utf8PerFrame = frameUtf8[0].length;
        
        // Pre-allocate a large enough reusable buffer ONCE
        byte[] buffer = new byte[1024 * 128]; 
        
        for (int i = 0; i < frames; i++) {
            int offset = 0;
            
            for (int j = 0; j < numbersPerFrame; j++) {
                offset += FastASCIIWriter.writeInt(buffer, offset, frameNumbers[i][j]);
                buffer[offset++] = ' ';
            }
            for (int j = 0; j < utf8PerFrame; j++) {
                offset += FastASCIIWriter.writeUtf8(buffer, offset, frameUtf8[i][j]);
                buffer[offset++] = ' ';
            }
            buffer[offset++] = '\n';
            
            // In a real engine, we'd do System.out.write(buffer, 0, offset) here
            totalBytes += offset;
        }
        return totalBytes;
    }
}
