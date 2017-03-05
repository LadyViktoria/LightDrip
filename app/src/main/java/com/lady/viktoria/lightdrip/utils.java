package com.lady.viktoria.lightdrip;

public class utils {

    public static Integer convertSrc(final String Src) {
        Integer res = 0;
        String tmpSrc = Src.toUpperCase();
        res |= getSrcValue(tmpSrc.charAt(0)) << 20;
        res |= getSrcValue(tmpSrc.charAt(1)) << 15;
        res |= getSrcValue(tmpSrc.charAt(2)) << 10;
        res |= getSrcValue(tmpSrc.charAt(3)) << 5;
        res |= getSrcValue(tmpSrc.charAt(4));
        return res;
    }

    private static int getSrcValue(char ch) {
        int i;
        char[] cTable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
                'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'W', 'X', 'Y'};
        for (i = 0; i < cTable.length; i++) {
            if (cTable[i] == ch) break;
        }
        return i;
    }

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexRepresentation) {
        int len = hexRepresentation.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexRepresentation.charAt(i), 16) << 4)
                    + Character.digit(hexRepresentation.charAt(i + 1), 16));
        }

        return data;
    }

}