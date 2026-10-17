package com.ismail.nooktv;

import java.nio.charset.StandardCharsets;

/**
 * Pure Java self-contained QR Code generator (Versions 1-4, Byte mode, Error Correction Level L).
 * Encodes LAN pairing URLs without third-party libraries or platform stubs.
 */
public final class QrCode {
    private static final int[] EXP = new int[512];
    private static final int[] LOG = new int[256];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP[i] = x;
            EXP[i + 255] = x;
            LOG[x] = i;
            x <<= 1;
            if ((x & 0x100) != 0) x ^= 0x11d;
        }
    }

    private static int gmul(int a, int b) {
        return (a == 0 || b == 0) ? 0 : EXP[LOG[a] + LOG[b]];
    }

    private static int[] generator(int k) {
        int[] g = new int[]{1};
        for (int i = 0; i < k; i++) {
            int root = EXP[i];
            int[] next = new int[g.length + 1];
            for (int j = 0; j < g.length; j++) {
                next[j] ^= g[j];
                next[j + 1] ^= gmul(g[j], root);
            }
            g = next;
        }
        return g;
    }

    private static int[] computeEC(int[] data, int k) {
        int[] gen = generator(k);
        int[] res = new int[data.length + k];
        System.arraycopy(data, 0, res, 0, data.length);
        for (int i = 0; i < data.length; i++) {
            int factor = res[i];
            if (factor != 0) {
                for (int j = 0; j <= k; j++) res[i + j] ^= gmul(gen[j], factor);
            }
        }
        int[] ec = new int[k];
        System.arraycopy(res, data.length, ec, 0, k);
        return ec;
    }

    public static boolean[][] encode(String text) {
        if (text == null || text.trim().isEmpty()) throw new IllegalArgumentException("QR text required");
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int ver = 1, dataCap = 19, ecCount = 7, align = 0;
        if (bytes.length > 17) { ver = 2; dataCap = 34; ecCount = 10; align = 18; }
        if (bytes.length > 32) { ver = 3; dataCap = 55; ecCount = 15; align = 22; }
        if (bytes.length > 53) { ver = 4; dataCap = 80; ecCount = 20; align = 26; }
        if (bytes.length > 78) throw new IllegalArgumentException("Address exceeds QR capacity (max 78 chars)");

        int totalBits = dataCap * 8;
        int[] bitBuf = new int[totalBits];
        int bitLen = 0;

        // Byte mode indicator: 0100 (4)
        bitBuf[bitLen++] = 0; bitBuf[bitLen++] = 1; bitBuf[bitLen++] = 0; bitBuf[bitLen++] = 0;

        // Character count indicator (8 bits for Version 1-4 byte mode)
        for (int i = 7; i >= 0; i--) bitBuf[bitLen++] = (bytes.length >> i) & 1;

        // Data bytes
        for (byte b : bytes) {
            int val = b & 0xff;
            for (int i = 7; i >= 0; i--) bitBuf[bitLen++] = (val >> i) & 1;
        }

        // Terminator up to 4 bits
        for (int i = 0; i < 4 && bitLen < totalBits; i++) bitBuf[bitLen++] = 0;

        // Pad to 8-bit boundary
        while (bitLen % 8 != 0 && bitLen < totalBits) bitBuf[bitLen++] = 0;

        // Pad bytes: 0xEC, 0x11
        int[] pads = new int[]{0xec, 0x11};
        int padIdx = 0;
        while (bitLen < totalBits) {
            int pad = pads[padIdx++ % 2];
            for (int i = 7; i >= 0; i--) bitBuf[bitLen++] = (pad >> i) & 1;
        }

        int[] dataCodewords = new int[dataCap];
        for (int i = 0; i < dataCap; i++) {
            int val = 0;
            for (int j = 0; j < 8; j++) val = (val << 1) | bitBuf[i * 8 + j];
            dataCodewords[i] = val;
        }

        int[] ecCodewords = computeEC(dataCodewords, ecCount);
        int[] allCodewords = new int[dataCap + ecCount];
        System.arraycopy(dataCodewords, 0, allCodewords, 0, dataCap);
        System.arraycopy(ecCodewords, 0, allCodewords, dataCap, ecCount);

        int totalCodewordBits = allCodewords.length * 8;
        int[] allBits = new int[totalCodewordBits];
        int abIdx = 0;
        for (int c : allCodewords) {
            for (int i = 7; i >= 0; i--) allBits[abIdx++] = (c >> i) & 1;
        }

        int size = 17 + 4 * ver;
        Boolean[][] mat = new Boolean[size][size];

        // Finder patterns
        setFinder(mat, 0, 0);
        setFinder(mat, 0, size - 7);
        setFinder(mat, size - 7, 0);

        // Separators
        for (int i = 0; i < 8; i++) {
            mat[7][i] = Boolean.FALSE;
            mat[i][7] = Boolean.FALSE;
            mat[7][size - 1 - i] = Boolean.FALSE;
            mat[i][size - 8] = Boolean.FALSE;
            mat[size - 8][i] = Boolean.FALSE;
            mat[size - 1 - i][7] = Boolean.FALSE;
        }

        // Alignment pattern
        if (align > 0) {
            for (int r = -2; r <= 2; r++) {
                for (int c = -2; c <= 2; c++) {
                    boolean border = Math.abs(r) == 2 || Math.abs(c) == 2 || (r == 0 && c == 0);
                    mat[align + r][align + c] = border ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }

        // Timing patterns
        for (int i = 8; i <= size - 9; i++) {
            if (mat[6][i] == null) mat[6][i] = (i % 2 == 0) ? Boolean.TRUE : Boolean.FALSE;
            if (mat[i][6] == null) mat[i][6] = (i % 2 == 0) ? Boolean.TRUE : Boolean.FALSE;
        }

        // Dark module
        mat[4 * ver + 9][8] = Boolean.TRUE;

        // Format info (Level L, Mask 0): 0x77c4 = 0b111011111000100
        int fmt = 0x77c4;
        for (int i = 0; i < 15; i++) {
            boolean bit = ((fmt >> (14 - i)) & 1) == 1;
            int r, c;
            if (i < 6) { r = 8; c = i; }
            else if (i == 6) { r = 8; c = 7; }
            else if (i == 7) { r = 8; c = 8; }
            else if (i == 8) { r = 7; c = 8; }
            else { r = 14 - i; c = 8; }
            mat[r][c] = bit ? Boolean.TRUE : Boolean.FALSE;

            boolean splitBit = ((fmt >> i) & 1) == 1;
            if (i < 7) {
                mat[size - 1 - i][8] = splitBit ? Boolean.TRUE : Boolean.FALSE;
            } else {
                mat[8][size - 15 + i] = splitBit ? Boolean.TRUE : Boolean.FALSE;
            }
        }

        // Data placement with Mask 0 ((r + c) % 2 == 0)
        int bitIdx = 0;
        boolean upward = true;
        for (int right = size - 1; right > 0; right -= 2) {
            if (right == 6) right = 5;
            for (int v = 0; v < size; v++) {
                int r = upward ? (size - 1 - v) : v;
                for (int c = right; c >= right - 1; c--) {
                    if (mat[r][c] == null) {
                        int bit = (bitIdx < allBits.length) ? allBits[bitIdx++] : 0;
                        int mask = ((r + c) % 2 == 0) ? 1 : 0;
                        mat[r][c] = ((bit ^ mask) == 1) ? Boolean.TRUE : Boolean.FALSE;
                    }
                }
            }
            upward = !upward;
        }

        boolean[][] result = new boolean[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                result[r][c] = Boolean.TRUE.equals(mat[r][c]);
            }
        }
        return result;
    }

    private static void setFinder(Boolean[][] mat, int r0, int c0) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                boolean black = (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4));
                mat[r0 + r][c0 + c] = black ? Boolean.TRUE : Boolean.FALSE;
            }
        }
    }
}
