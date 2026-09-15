package fastascii.benchmark;

import fastascii.FastASCIIReader;
import fastascii.FastASCIIScanner;
import fastascii.FastASCIIWriter;
import fastascii.FastUTF8;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class Benchmark {

    private byte[] smallBuffer;
    private byte[] largeBuffer;
    private byte[] integerBuffer;
    private byte[] ansiBuffer;
    private String smallString;
    private String largeString;
    private String integerString;
    private String ansiString;

    @Setup
    public void setup() {
        // Small buffer (typical ANSI code size)
        smallBuffer = "Hello World FastASCII!".getBytes(StandardCharsets.UTF_8);
        smallString = new String(smallBuffer, StandardCharsets.UTF_8);

        // Large buffer (log file size)
        largeBuffer = """
            [INFO] 2024-01-15 10:30:45,123 main.java.Server - Starting server on port 8080
            [INFO] 2024-01-15 10:30:45,124 main.java.Server - Loading configuration from config.yml
            [WARN] 2024-01-15 10:30:45,125 main.java.Config - Missing optional setting: max_connections
            [INFO] 2024-01-15 10:30:45,126 main.java.Server - Database connection established
            [INFO] 2024-01-15 10:30:45,127 main.java.Server - HTTP listener started
            [DEBUG] 2024-01-15 10:30:45,128 main.java.Request - Incoming request: GET /api/users
            [INFO] 2024-01-15 10:30:45,129 main.java.Request - Request processed in 2ms
            [ERROR] 2024-01-15 10:30:45,130 main.java.Auth - Invalid token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
            [WARN] 2024-01-15 10:30:45,131 main.java.Auth - Rate limit exceeded for IP: 192.168.1.100
            [INFO] 2024-01-15 10:30:45,132 main.java.Server - Server running. Uptime: 0 days, 0 hours, 0 minutes
            """.getBytes(StandardCharsets.UTF_8);
        largeString = new String(largeBuffer, StandardCharsets.UTF_8);

        // Integer parsing
        integerBuffer = "1234567890".getBytes(StandardCharsets.UTF_8);
        integerString = new String(integerBuffer, StandardCharsets.UTF_8);

        // ANSI escape sequence
        ansiBuffer = "\u001B[31mError:\u001B[0m Connection failed".getBytes(StandardCharsets.UTF_8);
        ansiString = new String(ansiBuffer, StandardCharsets.UTF_8);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIIWriteInt() {
        byte[] buffer = new byte[16];
        return FastASCIIWriter.writeInt(buffer, 0, 12345);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaIntToString() {
        return Integer.toString(12345).getBytes(StandardCharsets.UTF_8).length;
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIIParseUInt() {
        return FastASCIIReader.parseUInt(integerBuffer, 0, integerBuffer.length);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaIntegerParseInt() {
        return Integer.parseInt(integerString);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIIFindByte() {
        return FastASCIIScanner.find(smallBuffer, 0, smallBuffer.length, (byte) 'F');
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaStringIndexOf() {
        return smallString.indexOf('F');
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIIFindSubstring() {
        return FastASCIIScanner.find(smallBuffer, 0, smallBuffer.length, "Fast".getBytes(StandardCharsets.UTF_8));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaStringIndexOfSubstring() {
        return smallString.indexOf("Fast");
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIIWriteUtf8() {
        byte[] buffer = new byte[16];
        return FastASCIIWriter.writeUtf8(buffer, 0, 0x1F680); // 🚀
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaStringGetBytes() {
        return "🚀".getBytes(StandardCharsets.UTF_8).length;
    }

    @org.openjdk.jmh.annotations.Benchmark
    public boolean benchmarkFastASCIIValidateUtf8() {
        return FastUTF8.validate(largeBuffer, 0, largeBuffer.length);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public boolean benchmarkJavaUtf8Validation() {
        try {
            new String(largeBuffer, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIIDecodeCodePoint() {
        int[] codePoint = new int[1];
        return FastUTF8.decodeCodePoint(ansiBuffer, 0, ansiBuffer.length, codePoint);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaStringCodePointAt() {
        return ansiString.codePointAt(0);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkFastASCIILargeBufferSearch() {
        return FastASCIIScanner.find(largeBuffer, 0, largeBuffer.length, (byte) 'E');
    }

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkJavaLargeBufferSearch() {
        return largeString.indexOf('E');
    }
}
