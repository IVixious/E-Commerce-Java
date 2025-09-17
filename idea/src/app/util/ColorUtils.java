package app.util;

import java.awt.*;

public class ColorUtils {
    public static final Color NONE = new Color(0f, 0f, 0f, 0f);

    /**
     * Converts a hex value to an AWT Color.
     * @param hexRGB The hex value (formatted in RGB) to convert
     * @return The AWT Color with the specified values
     */
    public static Color fromHex(int hexRGB) {
        return new Color(hexRGB);
    }

    /**
     * Converts RGB color data to an AWT Color.
     * @param r The red value (0-255)
     * @param g The green value (0-255)
     * @param b The blue value (0-255)
     * @return The AWT Color with the specified values
     */
    public static Color fromRGB(int r, int g, int b) {
        return new Color(r, g, b);
    }
}
