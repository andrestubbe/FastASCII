package fastascii;

import java.util.Arrays;

/**
 * Treats each Braille character as a 2×4 black-and-white bitmap.
 * Gray is produced by ordered dithering, not by varying dot counts.
 */
public final class FastBrailleDither {
    public static final char BASE = '\u2800';

    // Unicode braille dot layout (dx, dy) -> bit mask:
    // (0,0) (1,0)
    // (0,1) (1,1)
    // (0,2) (1,2)
    // (0,3) (1,3)
    private static final int[] DOT_BITS = {
        0x01, 0x08, 0x02, 0x10, 0x04, 0x20, 0x40, 0x80
    };

    private static final int[] BAYER = {
         0, 32,  8, 40,  2, 34, 10, 42,
        48, 16, 56, 24, 50, 18, 58, 26,
        12, 44,  4, 36, 14, 46,  6, 38,
        60, 28, 52, 20, 62, 30, 54, 22,
         3, 35, 11, 43,  1, 33,  9, 41,
        51, 19, 59, 27, 49, 17, 57, 25,
        15, 47,  7, 39, 13, 45,  5, 37,
        63, 31, 55, 23, 61, 29, 53, 21
    };

    private final int charCols;
    private final int charRows;
    private final int pixelWidth;
    private final int pixelHeight;
    private final int[] dots;

    public FastBrailleDither(int charCols, int charRows) {
        if (charCols <= 0 || charRows <= 0) {
            throw new IllegalArgumentException("charCols and charRows must be positive");
        }
        this.charCols = charCols;
        this.charRows = charRows;
        this.pixelWidth = charCols * 2;
        this.pixelHeight = charRows * 4;
        this.dots = new int[charCols * charRows];
    }

    public int getCharCols() { return charCols; }
    public int getCharRows() { return charRows; }
    public int getPixelWidth() { return pixelWidth; }
    public int getPixelHeight() { return pixelHeight; }

    public void clear() {
        Arrays.fill(dots, 0);
    }

    /** Sets one sub-pixel using 8×8 Bayer ordered dithering. Luminance is 0..1. */
    public void setPixel(int px, int py, float luminance) {
        if (px < 0 || py < 0 || px >= pixelWidth || py >= pixelHeight) return;
        if (luminance <= 0.0f) return;

        int threshold = BAYER[((py & 7) << 3) | (px & 7)];
        if (luminance * 64.0f < threshold) return;

        int cell = (py >> 2) * charCols + (px >> 1);
        int bit = DOT_BITS[((py & 3) << 1) | (px & 1)];
        dots[cell] |= bit;
    }

    public void appendTo(StringBuilder sb) {
        for (int y = 0; y < charRows; y++) {
            int row = y * charCols;
            for (int x = 0; x < charCols; x++) {
                sb.append((char) (BASE + dots[row + x]));
            }
            if (y < charRows - 1) sb.append('\n');
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(charCols * charRows + charRows);
        appendTo(sb);
        return sb.toString();
    }
}
