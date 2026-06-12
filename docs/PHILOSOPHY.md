# Philosophy of FastASCII

## 1. Zero Allocations
Java's standard library often forces you to instantiate `String` objects, triggering UTF-8 to UTF-16 conversions and filling up the young generation heap. FastASCII operates exclusively on primitives and pre-allocated `byte[]` arrays, eliminating GC pressure during hot loops.

## 2. JIT over JNI for the Small Stuff
Invoking JNI for small sequences (like reading a 10-byte ANSI mouse coordinate) introduces context-switching overhead that outweighs native speed. FastASCII defaults to Pure Java for maximum JIT inlining on small reads.

## 3. SIMD for the Big Stuff
When reading megabytes of logs or parsing large JSON files, JIT isn't enough. FastASCII provides an optional native backend (`FastASCII.Native`) that uses vectorized instructions (AVX2, SSE4.2) to match C++ parse speeds.
