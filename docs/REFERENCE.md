# FastASCII Reference Manual

`FastASCII` is the zero-allocation byte manipulation, integer parsing, and UTF-8 encoding/decoding substrate of the FastJava ecosystem.

---

## 1. Core Vocabulary

* **Zero-Allocation Byte Pipeline**: Directly creates, scans, and parses primitives without allocating `java.lang.String` or `StringBuilder` objects.
* **Direct Buffer Slicing**: Works across arbitrary offsets and slice ranges within byte arrays.
* **JIT-Inlining Optimization**: Pure-Java core routines structured specifically to trigger aggressive HotSpot compiler C2 inlining.
* **UTF-8 Native Codepoints**: Encodes and decodes 21-bit Unicode codepoints directly to/from UTF-8 byte streams.

---

## 2. FastASCII Architecture & Classes

### Class: `fastascii.FastASCIIWriter`
Direct high-speed primitive-to-byte serialization.

- `public static int writeInt(byte[] buffer, int offset, int value)`  
  Encodes integer `value` directly as ASCII decimal digits into `buffer` starting at `offset`. Returns number of bytes written.
- `public static int writeAscii(byte[] buffer, int offset, char c)`  
  Writes a single ASCII character directly into `buffer[offset]`. Returns `1`.
- `public static int writeUtf8(byte[] buffer, int offset, int codePoint)`  
  Encodes a Unicode codepoint (1 to 4 bytes) directly into `buffer` at `offset`. Returns byte length written.

### Class: `fastascii.FastASCIIReader`
Zero-allocation primitive byte deserialization.

- `public static int parseUInt(byte[] buffer, int start, int end)`  
  Parses an unsigned decimal integer from `buffer[start..end]` with zero heap allocations.
- `public static int readInt(byte[] buffer, int start, int end)`  
  Parses signed decimal integers supporting negative signs (`-`).
- `public static int readUntil(byte[] buffer, int start, int end, byte target)`  
  Scans buffer until `target` byte is reached.

### Class: `fastascii.FastASCIIScanner`
High-throughput scalar pattern searching.

- `public static int find(byte[] haystack, int offset, int length, byte needle)`  
  Zero-allocation `indexOf` byte scanner returning the absolute index of `needle` or `-1`.
- `public static int find(byte[] haystack, int offset, int length, byte[] needle)`  
  Locates byte subarray pattern `needle` within the specified haystack slice.
- `public static int skipWhitespace(byte[] buffer, int offset, int limit)`  
  Advances the offset pointer past standard ASCII whitespace (`' '`, `'\t'`, `'\r'`, `'\n'`).

### Class: `fastascii.FastUTF8`
UTF-8 codepoint stream operations.

- `public static int decodeCodePoint(byte[] buffer, int offset, int length, int[] outCodePoint)`  
  Decodes a single UTF-8 codepoint into `outCodePoint[0]`. Returns number of bytes consumed (1-4), or `-1` if invalid.
- `public static boolean validate(byte[] buffer, int offset, int length)`  
  High-speed validation verifying whether the byte slice conforms to valid UTF-8 grammar.

### Class: `fastascii.FastBrailleDither`
Braille character matrix dithering and terminal rasterization.

- `public FastBrailleDither(int charCols, int charRows)`  
  Initializes a 2×4 Braille subpixel dithering canvas.
- `public void setPixel(int x, int y, boolean on)`  
  Sets an individual subpixel state inside the Braille cell matrix.
- `public char getChar(int col, int row)`  
  Returns the Unicode Braille character (`U+2800..U+28FF`) corresponding to the 2×4 dot pattern.

---

## 3. Guarantees & Memory Contracts

* **100% Zero-Allocation**: Never creates temporary heap garbage during read, write, or scanning routines.
* **Deterministic Bounds Checks**: Operations check caller-supplied slices against array limits.
* **HotSpot Inlining**: Critical writer and reader loops are under 35 bytecodes to ensure C2 JIT inlining in performance-critical paths.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀
