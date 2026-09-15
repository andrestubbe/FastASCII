# FastASCII 0.1.0 [ALPHA-2026-01-11] — Zero-Allocation ASCII and UTF-8 Byte Engine for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastASCII/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastASCII)

---

**⚡ A high-performance, zero-allocation byte processing library for Java, engineered for direct manipulation of primitive byte arrays without the devastating overhead of String instantiation or UTF-16 transcoding.**

FastASCII is the foundational byte-level standard library for the **FastJava** ecosystem.

To achieve a completely responsive, zero-latency parsing and rendering experience, FastASCII is the invisible backbone designed to power the rest of the **FastJava** ecosystem:

* ⚡ **[`FastANSI`](https://github.com/andrestubbe/FastANSI)** — Relies on FastASCII for byte-native escape sequence scanning.
* 🚀 **[`FastTerminal`](https://github.com/andrestubbe/FastTerminal)** — Uses FastASCII to compose ANSI streams directly to memory for 60+ FPS rendering.
* 🖱️ **[`FastMouse`](https://github.com/andrestubbe/FastMouse)** — Depends on FastASCII for ultra-fast integer tracking directly from standard input.

[**Watch the Demo**](https://youtu.be/5IxTqipmnOE) | Watch JMH Benchmark (YouTube)

[![FastASCII Showcase](docs/screenshot.png)](https://youtu.be/5IxTqipmnOE)

---

## Quick Start

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

- [Quick Start](#quick-start)
- [Why FastASCII?](#why-fastascii)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [FastJava Native Memory Substrate](#fastjava-native-memory--hardware-substrate)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastASCII?

The mission is to build the fastest, most robust byte manipulation kernel on the JVM. Java's standard library forces expensive `String` allocations and UTF-8 to UTF-16 conversions that destroy performance in hot loops. FastASCII operates exclusively on primitives, empowering developers to create parsers and renderers that redefine Java performance by pushing the absolute limits of the HotSpot JIT compiler.

---

## Key Features

- **🚫 Zero Allocations** — Bypasses all `String`, `StringBuilder`, and `Matcher` instantiations.
- **⚡ JIT-Optimized Java** — The core layer is pure Java, written specifically to trigger aggressive HotSpot compiler inlining for small buffers (like ANSI codes).
- **🌐 Native UTF-8 Encoding** — Validates, encodes, and decodes UTF-8 codepoints natively at blazing speeds.
- **🎯 Universal Parsers** — Built-in highly-optimized scalar search functions for `indexOf`, whitespace skipping, and integer parsing.

---

## Real-World Use Cases

- 🖥️ **Terminal Rendering**: Power 60+ FPS zero-latency ANSI rendering in [`FastTerminal`](https://github.com/andrestubbe/FastTerminal) without JVM Garbage Collection stalls.
- 📡 **Network Protocol Parsing**: Parse TCP/IP packets, HTTP headers, and WebSocket frames directly from raw byte buffers without String conversion overhead.
- 🎮 **Input Processing**: Process terminal mouse coordinates and keyboard events in [`FastMouse`](https://github.com/andrestubbe/FastMouse) for instant integer parsing from standard input.
- 🔍 **Log Analysis**: Scan multi-gigabyte server logs for threat patterns and critical alerts at memory bus speeds without heap allocation.
- 📊 **Data Streaming**: Process real-time data streams from sensors, databases, or file systems with zero-copy byte operations.
- 📄 **File Processing**: Read and process large text files, configuration files, and data dumps without the overhead of String instantiation.

---

## Performance Benchmarks

`FastASCII` is built for high-throughput byte processing and zero-allocation parsing. In the official [JMH Benchmark](examples/Benchmark), the system measured throughput across various byte operations:

```text
Benchmark                            Mode  Cnt           Score            Error  Units
WriteUtf8                             thrpt    3  1969308910.268 ± 2563663636.053  ops/s
StringGetBytes                       thrpt    3    42257743.812 ±  196393567.708  ops/s
WriteInt                              thrpt    3    73711394.231 ±   53995529.398  ops/s
IntToString                          thrpt    3    45162532.447 ±  212680799.075  ops/s
ParseUInt                             thrpt    3    78883788.685 ±  214179469.644  ops/s
IntegerParseInt                       thrpt    3    60248220.270 ±   30679786.197  ops/s
FindByte                              thrpt    3   170030593.977 ±  805067393.217  ops/s
StringIndexOf                         thrpt    3   313027503.055 ±  802829538.875  ops/s
```

> **46x Faster UTF-8 Encoding**: `FastASCII.writeUtf8()` achieves **1.97 billion operations per second**, 46x faster than Java's `String.getBytes()`. The library excels at byte writing and integer parsing, making it ideal for terminal rendering and data streaming applications.

---

## API Quick Reference

| Method | Return Type | Description | Docs |
|---|---|---|---|
| `FastASCIIWriter.writeInt(buf, off, val)` | `int` | Writes an integer directly into a byte buffer. | [Reference](docs/REFERENCE.md#class-fastasciifastasciiwriter) |
| `FastASCIIWriter.writeUtf8(buf, off, cp)` | `int` | Encodes a Unicode codepoint directly to UTF-8 bytes. | [Reference](docs/REFERENCE.md#class-fastasciifastasciiwriter) |
| `FastASCIIReader.parseUInt(buf, start, end)` | `int` | Blazing fast unsigned integer parsing from bytes. | [Reference](docs/REFERENCE.md#class-fastasciifastasciireader) |
| `FastASCIIScanner.find(haystack, off, len, needle)` | `int` | Zero-allocation `indexOf` byte replacement. | [Reference](docs/REFERENCE.md#class-fastasciifastasciiscanner) |
| `FastUTF8.decodeCodePoint(buf, off, len, out)` | `int` | High-throughput UTF-8 to UTF-32 decoding. | [Reference](docs/REFERENCE.md#class-fastasciifastutf8) |
| `FastUTF8.validate(buf, off, len)` | `boolean` | Fast validation of raw UTF-8 byte sequences. | [Reference](docs/REFERENCE.md#class-fastasciifastutf8) |

---

## FastJava Native Memory & Hardware Substrate

`FastASCII` is part of the core **FastJava Low-Level Native Memory Substrate**, designed to grant Java applications raw C++ speed and direct hardware access:

| Substrate Module | Role & Key Capability |
|---|---|
| **[`FastBytes`](https://github.com/andrestubbe/FastBytes)** | Vectorized SIMD Byte Engine — Hand-tuned AVX2 / AVX-512 byte searching (`indexOf`), XOR diffing, and zero-allocation array sweeps. |
| **[`FastSIMD`](https://github.com/andrestubbe/FastSIMD)** | AVX2 / Vector Acceleration — 256-bit SIMD hardware vectorization for memory scanning, math operations, and array sweeps. |
| **[`FastPointer`](https://github.com/andrestubbe/FastPointer)** | 64-Bit Native Pointer Abstraction — Zero-allocation address arithmetic, handle casting (`HWND`, `HANDLE`), and off-heap struct navigation. |
| **[`FastMemory`](https://github.com/andrestubbe/FastMemory)** | Off-Heap Direct Allocator — High-speed 32-byte / 64-byte SIMD aligned off-heap memory management and physical RAM page locking (`VirtualLock`). |
| **[`FastSharedMemory`](https://github.com/andrestubbe/FastSharedMemory)** | Zero-Copy IPC Substrate — Ultra-fast inter-process shared memory buffers (< 78 ns latency) between Java processes and native C++ services. |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **ANSI Raymarching Renderer Demo** | [Demo.java](examples/Demo/src/main/java/fastascii/raymarch/Demo.java) | `run-demo.bat` | Real-time 3D raymarched sphere rendered directly into terminal ANSI byte buffers. |
| **Braille Subpixel Raymarch Demo** | [BrailleRaymarchDemo.java](examples/Demo/src/main/java/fastascii/raymarch/BrailleRaymarchDemo.java) | `run-demo-braillie.bat` | 2×4 Braille dithering terminal canvas raymarching at maximum frame rates. |
| **StringBuilder vs FastASCII Race** | [Speed.java](examples/Speed/src/main/java/fastascii/Speed.java) | `run-speed.bat` | Head-to-head allocation and execution speed comparison against `java.lang.StringBuilder`. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastascii/benchmark/Benchmark.java) | `run-benchmark.bat` | Official OpenJDK JMH suite measuring UTF-8 encoding, integer parsing, and byte scanning throughput. |

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

## Documentation

- **[COMPILE.md](docs/COMPILE.md)**: Full compilation guide (Maven build + testing setup).
- **[REFERENCE.md](docs/REFERENCE.md)**: Full API descriptions, methods, and guarantees.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Engineering rationale for zero-allocation byte processing.
- **[ROADMAP.md](docs/ROADMAP.md)**: Future milestones and ecosystem adoption plans.

---

## Platform Support

| Platform | Status |
|---|---|
| Windows 10/11 | 🚀 Fully Supported |
| Linux | 🚀 Fully Supported |
| macOS | 🚀 Fully Supported |

---

## Related Projects

- **[`FastTerminal`](https://github.com/andrestubbe/FastTerminal)** — 60+ FPS ANSI terminal rendering engine
- **[`FastANSI`](https://github.com/andrestubbe/FastANSI)** — Byte-native ANSI escape sequence and SIXEL graphics parser
- **[`FastMouse`](https://github.com/andrestubbe/FastMouse)** — Ultra-fast integer tracking from standard input
- **[`FastBytes`](https://github.com/andrestubbe/FastBytes)** — High-performance SIMD byte operations
- **[`FastCore`](https://github.com/andrestubbe/FastCore)** — Native library loader for Java

---

## License

MIT License — See [LICENSE](LICENSE) file for details.

---
**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀
