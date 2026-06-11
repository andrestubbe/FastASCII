# FastASCII 🚀

High-performance, zero-allocation ASCII and UTF-8 byte processing library for Java.

FastASCII is the foundational byte-level standard library for the entire FastJava ecosystem. It is designed to replace expensive `String` allocations and UTF-16 conversions with blazing-fast, direct byte array manipulation.

## Core Architecture

FastASCII uses a dual-layer architecture:
1. **Pure Java Layer**: The default implementation relies on heavily JIT-optimized scalar operations. Perfect for parsing short escape sequences without JNI boundary overhead.
2. **FastASCII.Native (Optional JNI)**: For bulk parsing (e.g., >1KB JSON files or logs), FastASCII can dynamically bridge to SIMD-accelerated C++ (AVX2/SSE4.2) for extreme throughput.

## The 5 Pillars of FastASCII

- `FastASCIIWriter`: Zero-allocation writing of integers, characters, and UTF-8 to byte buffers.
- `FastASCIIReader`: Blazing-fast integer parsing and stream reading.
- `FastASCIIScanner`: SIMD-friendly byte searching, substring matching, and whitespace skipping.
- `FastUTF8`: High-throughput UTF-8 validation, encoding, and decoding.
- `FastASCIIChar`: ASCII character classification (digits, letters, whitespace).
