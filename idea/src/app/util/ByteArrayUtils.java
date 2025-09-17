package app.util;

import java.nio.charset.StandardCharsets;

public class ByteArrayUtils {
    private ByteArrayUtils() {}

    // Convert hex characters -> ASCII bytes
    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

    // from: https://stackoverflow.com/a/9855338
    public static String bytesToHex(byte[] bytes) {
        // 1 byte = 2 hex digits (0x00 - 0xFF)
        // Because 2 hex digits == 2 characters, we need to make an array
        // that can hold every single byte as a hex character.
        byte[] hexChars = new byte[bytes.length * 2];

        // Iterate through the bytes
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF; // Ensure that the byte is actually byte-sized

            // This gets the first hex digit of the byte, by shifting the bits to the right by 4 bits (0xF == 0b1111).
            hexChars[i * 2] = HEX_ARRAY[value >>> 4];

            // This gets the second hex digit of the byte, by removing everything except for the last 4 bits using an AND operation.
            hexChars[i * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }

        // Convert the byte array to a string
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
