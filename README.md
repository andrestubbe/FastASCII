# FastASCII 0.1.0 [ALPHA-2026-01-11] — Zero-Allocation ASCII and UTF-8 Byte Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastASCII/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastASCII)

**⚡ A high-performance, zero-allocation byte processing library for Java, engineered for direct manipulation of primitive byte arrays without the devastating overhead of String instantiation or UTF-16 transcoding.**

FastASCII is the foundational byte-level standard library for the **FastJava** ecosystem.

To achieve a completely responsive, zero-latency parsing and rendering experience, FastASCII is the invisible backbone designed to power the rest of the **FastJava** ecosystem:

* ⚡ **[FastANSI](https://github.com/andrestubbe/FastANSI)** — Relies on FastASCII for byte-native escape sequence scanning.
* 🚀 **[FastTerminal](https://github.com/andrestubbe/FastTerminal)** — Uses FastASCII to compose ANSI streams directly to memory for 60+ FPS rendering.
* 🖱️ **[FastMouse](https://github.com/andrestubbe/FastMouse)** — Depends on FastASCII for ultra-fast integer tracking directly from standard input.

[**Watch the Demo**](https://youtu.be/5IxTqipmnOE)

---

[![FastASCII Showcase](docs/screenshot.png)](https://youtu.be/5IxTqipmnOE)

---

## Quick Start — Example

```java
import fastascii.FastASCIIWriter;
import fastascii.FastASCIIReader;
import fastascii.FastASCIIScanner;
import fastascii.FastUTF8;

public class ByteProcessingDemo {
    public static void processAnsiSequence(byte[] buffer, int offset, int length) {
        // 1. Write ANSI escape sequence directly to bytes (Zero Allocation!)
        int bytesWritten = FastASCIIWriter.writeAscii(buffer, offset, '\u001B');
        bytesWritten += FastASCIIWriter.writeAscii(buffer, offset + bytesWritten, '[');
        bytesWritten += FastASCIIWriter.writeInt(buffer, offset + bytesWritten, 31);
        bytesWritten += FastASCIIWriter.writeAscii(buffer, offset + bytesWritten, 'm');

        // 2. Write UTF-8 codepoints natively
        bytesWritten += FastASCIIWriter.writeUtf8(buffer, offset + bytesWritten, 0x1F680); // 🚀

        // 3. Parse unsigned integers blazingly fast
        int parsed = FastASCIIReader.parseUInt(buffer, offset, bytesWritten);
        System.out.println("Parsed: " + parsed);

        // 4. Find bytes efficiently
        int targetIndex = FastASCIIScanner.find(buffer, offset, length, (byte) '[');
        System.out.println("Found '[' at index: " + targetIndex);

        // 5. Validate UTF-8 sequence
        int[] codePoint = new int[1];
        int consumed = FastUTF8.decodeCodePoint(buffer, offset, length, codePoint);
        System.out.println("Decoded codepoint: " + codePoint[0] + " (consumed " + consumed + " bytes)");
    }
}
```

---

## Table of Contents

- [Why FastASCII?](#why-fastascii)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [License](#license)

---

## Why FastASCII?

The mission is to build the fastest, most robust byte manipulation kernel on the JVM. Java's standard library forces expensive `String` allocations and UTF-8 to UTF-16 conversions that destroy performance in hot loops. FastASCII operates exclusively on primitives, empowering developers to create parsers and renderers that redefine Java performance by pushing the absolute limits of the HotSpot JIT compiler.

---

## Key Features

* **🚫 Zero Allocations** — Bypasses all `String`, `StringBuilder`, and `Matcher` instantiations.
* **⚡ JIT-Optimized Java** — The core layer is pure Java, written specifically to trigger aggressive HotSpot compiler inlining for small buffers (like ANSI codes).
* **🌐 Native UTF-8 Encoding** — Validates, encodes, and decodes UTF-8 codepoints natively at blazing speeds.
* **🎯 Universal Parsers** — Built-in highly-optimized scalar search functions for `indexOf`, whitespace skipping, and integer parsing.

---

## Real-World Use Cases

- 🖥️ **Terminal Rendering**: Power 60+ FPS zero-latency ANSI rendering in [FastTerminal](https://github.com/andrestubbe/FastTerminal) without JVM Garbage Collection stalls.
- 📡 **Network Protocol Parsing**: Parse TCP/IP packets, HTTP headers, and WebSocket frames directly from raw byte buffers without String conversion overhead.
- 🎮 **Input Processing**: Process terminal mouse coordinates and keyboard events in [FastMouse](https://github.com/andrestubbe/FastMouse) for instant integer parsing from standard input.
- 🔍 **Log Analysis**: Scan multi-gigabyte server logs for threat patterns and critical alerts at memory bus speeds without heap allocation.
- 📊 **Data Streaming**: Process real-time data streams from sensors, databases, or file systems with zero-copy byte operations.
- 📄 **File Processing**: Read and process large text files, configuration files, and data dumps without the overhead of String instantiation.

---

## Performance Benchmarks

`FastASCII` is built for high-throughput byte processing and zero-allocation parsing. In the official [JMH Benchmark](examples/Benchmark), the system measured throughput across various byte operations:

```text
Benchmark                            Mode  Cnt           Score            Error  Units
WriteUtf8                             thrpt    3  1969308910,268 ± 2563663636,053  ops/s
StringGetBytes                       thrpt    3    42257743,812 ±  196393567,708  ops/s
WriteInt                              thrpt    3    73711394,231 ±   53995529,398  ops/s
IntToString                          thrpt    3    45162532,447 ±  212680799,075  ops/s
ParseUInt                             thrpt    3    78883788,685 ±  214179469,644  ops/s
IntegerParseInt                       thrpt    3    60248220,270 ±   30679786,197  ops/s
FindByte                              thrpt    3   170030593,977 ±  805067393,217  ops/s
StringIndexOf                         thrpt    3   313027503,055 ±  802829538,875  ops/s
```

> **46x Faster UTF-8 Encoding**: `FastASCII.writeUtf8()` achieves **1.97 billion operations per second**, 46x faster than Java's `String.getBytes()`. The library excels at byte writing and integer parsing, making it ideal for terminal rendering and data streaming applications.

---

## API Quick Reference

| Method                                | Description                                              | Component |
|---------------------------------------|----------------------------------------------------------|-----------|
| `writeInt(buffer, offset, value)`     | Writes an integer directly into a byte buffer.           | `FastASCIIWriter` |
| `writeUtf8(buffer, offset, cp)`       | Encodes a codepoint directly to UTF-8 bytes.             | `FastASCIIWriter` |
| `parseUInt(buffer, start, end)`       | Blazing fast unsigned integer parsing from bytes.        | `FastASCIIReader` |
| `find(haystack, offset, len, needle)` | Zero-allocation `indexOf` replacement.                   | `FastASCIIScanner` |
| `decodeCodePoint(buf, off, len, out)` | High-throughput UTF-8 to UTF-32 decoding.                | `FastUTF8` |

---

## Documentation

- **[JMH Benchmark](examples/Benchmark)** — Official performance benchmarks comparing FastASCII with standard Java operations.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)** — Engineering rationale for zero-allocation byte processing.
- **[ROADMAP.md](docs/ROADMAP.md)** — Future milestones and ecosystem adoption plans.

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastASCII Library -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastascii</artifactId>
        <version>0.1.0</version>
    </dependency>

</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastascii:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 📦 **[fastascii-0.1.0.jar](https://github.com/andrestubbe/FastASCII/releases/download/0.1.0/fastascii-0.1.0.jar)** (The Core Library)

---

## Platform Support

| Platform      | Status |
|---------------|--------|
| Windows 10/11 | 🚀 Fully Supported |
| Linux         | 🚀 Fully Supported |
| macOS         | 🚀 Fully Supported |


---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastTerminal](https://github.com/andrestubbe/FastTerminal)
- [FastANSI](https://github.com/andrestubbe/FastANSI)
- [FastMouse](https://github.com/andrestubbe/FastMouse)
- [FastJSON](https://github.com/andrestubbe/FastJSON)
- [FastFileScrape](https://github.com/andrestubbe/FastFileScrape)
- [FastGLOB](https://github.com/andrestubbe/FastGLOB)
- [FastCore](https://github.com/andrestubbe/FastCore)

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀💎*
