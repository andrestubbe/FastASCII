package fastascii;

/**
 * High-performance, zero-allocation writer for ASCII and UTF-8 bytes.
 */
public final class FastASCIIWriter {
    
    private FastASCIIWriter() {}

    /**
     * Writes a positive integer to the buffer without String allocation.
     * @return the number of bytes written
     */
    public static int writeInt(byte[] buffer, int offset, int value) {
        if (value == 0) {
            buffer[offset] = '0';
            return 1;
        }
        
        int temp = value;
        int length = 0;
        while (temp > 0) {
            length++;
            temp /= 10;
        }
        
        int pos = offset + length - 1;
        temp = value;
        while (temp > 0) {
            buffer[pos--] = (byte) ('0' + (temp % 10));
            temp /= 10;
        }
        
        return length;
    }

    /**
     * Writes a long to the buffer.
     * @return the number of bytes written
     */
    public static int writeLong(byte[] buffer, int offset, long value) {
        if (value == 0) {
            buffer[offset] = '0';
            return 1;
        }
        
        long temp = value;
        int length = 0;
        while (temp > 0) {
            length++;
            temp /= 10;
        }
        
        int pos = offset + length - 1;
        temp = value;
        while (temp > 0) {
            buffer[pos--] = (byte) ('0' + (temp % 10));
            temp /= 10;
        }
        
        return length;
    }

    /**
     * Encodes and writes a UTF-8 code point to the buffer.
     * @return the number of bytes written (1 to 4)
     */
    public static int writeUtf8(byte[] buffer, int offset, int codePoint) {
        if (codePoint <= 0x7F) {
            buffer[offset] = (byte) codePoint;
            return 1;
        } else if (codePoint <= 0x7FF) {
            buffer[offset]     = (byte) (0xC0 | (codePoint >> 6));
            buffer[offset + 1] = (byte) (0x80 | (codePoint & 0x3F));
            return 2;
        } else if (codePoint <= 0xFFFF) {
            buffer[offset]     = (byte) (0xE0 | (codePoint >> 12));
            buffer[offset + 1] = (byte) (0x80 | ((codePoint >> 6) & 0x3F));
            buffer[offset + 2] = (byte) (0x80 | (codePoint & 0x3F));
            return 3;
        } else if (codePoint <= 0x10FFFF) {
            buffer[offset]     = (byte) (0xF0 | (codePoint >> 18));
            buffer[offset + 1] = (byte) (0x80 | ((codePoint >> 12) & 0x3F));
            buffer[offset + 2] = (byte) (0x80 | ((codePoint >> 6) & 0x3F));
            buffer[offset + 3] = (byte) (0x80 | (codePoint & 0x3F));
            return 4;
        }
        return 0; // Invalid code point
    }

    public static int writeAscii(byte[] buffer, int offset, String ascii) {
        int len = ascii.length();
        for (int i = 0; i < len; i++) {
            buffer[offset + i] = (byte) ascii.charAt(i);
        }
        return len;
    }

    public static int writeChar(byte[] buffer, int offset, char c) {
        return writeUtf8(buffer, offset, c);
    }

    public static int writeNewline(byte[] buffer, int offset) {
        buffer[offset] = '\n';
        return 1;
    }
}
