package com.ismail.nooktv;

public final class QrTests {
    private static int count;
    private static void check(boolean pass, String msg) { count++; if (!pass) throw new AssertionError(msg); }

    public static void main(String[] args) {
        // Null / empty validation
        boolean error = false;
        try { QrCode.encode(null); } catch (IllegalArgumentException e) { error = true; }
        check(error, "null QR text rejected");

        error = false;
        try { QrCode.encode("   "); } catch (IllegalArgumentException e) { error = true; }
        check(error, "empty QR text rejected");

        error = false;
        try {
            StringBuilder longStr = new StringBuilder();
            for (int i = 0; i < 85; i++) longStr.append("x");
            QrCode.encode(longStr.toString());
        } catch (IllegalArgumentException e) { error = true; }
        check(error, "oversized QR text rejected");

        // Version 1 check (< 18 chars)
        boolean[][] v1 = QrCode.encode("http://10.0.0.1");
        check(v1.length == 21, "version 1 size is 21x21");
        verifyFinders(v1, 21);

        // Version 2 check (typical local TV LAN address)
        String url = "http://192.168.1.100:8765";
        boolean[][] mat = QrCode.encode(url);
        check(mat.length == 25, "version 2 size is 25x25 for 26-char LAN URL");
        verifyFinders(mat, 25);

        // Verify alignment pattern at (18, 18) for Version 2
        check(mat[18][18], "alignment center is black");
        check(!mat[17][18] && !mat[19][18] && !mat[18][17] && !mat[18][19], "alignment ring is white");
        for (int r = -2; r <= 2; r++) {
            for (int c = -2; c <= 2; c++) {
                if (Math.abs(r) == 2 || Math.abs(c) == 2) {
                    check(mat[18 + r][18 + c], "alignment outer border is black");
                }
            }
        }

        // Verify timing patterns on row 6 and col 6
        for (int i = 8; i <= 25 - 9; i++) {
            check(mat[6][i] == (i % 2 == 0), "row 6 timing pattern alternates");
            check(mat[i][6] == (i % 2 == 0), "col 6 timing pattern alternates");
        }

        // Verify dark module at (17, 8)
        check(mat[17][8], "dark module is black");

        // Version 3 check (33-53 chars)
        boolean[][] v3 = QrCode.encode("http://nook-livingroom-tv.local:8765/pair-browser");
        check(v3.length == 29, "version 3 size is 29x29");
        verifyFinders(v3, 29);
        check(v3[22][22], "v3 alignment center at (22, 22)");

        System.out.println("PASS: " + count + " QR Code generation and matrix structure assertions");
    }

    private static void verifyFinders(boolean[][] mat, int size) {
        // Top-left, top-right, bottom-left
        int[][] finders = new int[][]{{0, 0}, {0, size - 7}, {size - 7, 0}};
        for (int[] pos : finders) {
            int r0 = pos[0], c0 = pos[1];
            // Outer 7x7
            for (int r = 0; r < 7; r++) {
                for (int c = 0; c < 7; c++) {
                    boolean expected = (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4));
                    check(mat[r0 + r][c0 + c] == expected, "finder module at " + (r0 + r) + "," + (c0 + c));
                }
            }
        }
    }
}
