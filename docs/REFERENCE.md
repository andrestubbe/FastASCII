# FastASCII Reference Guide

## Writers (`FastASCIIWriter`)
- `writeInt(byte[] buffer, int offset, int value)`: Writes an integer directly into a byte buffer.
- `writeUtf8(byte[] buffer, int offset, int codePoint)`: Encodes a codepoint directly to UTF-8 bytes.

## Readers (`FastASCIIReader`)
- `parseUInt(byte[] buffer, int start, int end)`: Fast unsigned integer parsing.
- `readUntil(byte[] buffer, int start, int end, byte target)`: Scans for a target byte.

## Scanners (`FastASCIIScanner`)
- `find(byte[] haystack, int offset, int length, byte needle)`: Zero-allocation `indexOf`.
- `skipWhitespace(byte[] buffer, int offset, int limit)`: Advances the offset past ASCII whitespace.

## UTF-8 (`FastUTF8`)
- `decodeCodePoint(byte[] buffer, int offset, int length, int[] outCodePoint)`: Decodes one UTF-8 character.
- `validate(byte[] buffer, int offset, int length)`: High-speed UTF-8 validation.
