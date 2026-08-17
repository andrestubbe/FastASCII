# Philosophy of FastASCII

## 1. Zero Allocations
Java's standard library often forces you to instantiate `String` objects, triggering UTF-8 to UTF-16 conversions and filling up the young generation heap. FastASCII operates exclusively on primitives and pre-allocated `byte[]` arrays, eliminating GC pressure during hot loops.

## 2. Pure Java Implementation
FastASCII is implemented entirely in pure Java, focusing on zero-allocation byte processing for maximum JIT inlining and performance without the complexity of native dependencies.
