package app.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GraphBuilder {
    private final int width;
    private final int height;
    private final Color lineColor;
    private final Color markingColor;
    private final Color backgroundColor;

    private final List<Integer> keyPoints = new ArrayList<>();

    public GraphBuilder(int width, int height, Color lineColor, Color markingColor, Color backgroundColor) {
        this.width = width;
        this.height = height;
        this.lineColor = lineColor;
        this.markingColor = markingColor;
        this.backgroundColor = backgroundColor;
    }

    public void addKeypoint(int point) {
        this.keyPoints.add(point);
    }

    public BufferedImage createImage() {
        var image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_ARGB);

        var g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, this.width, this.height);

        if (keyPoints.isEmpty()) { // Just return a text notifying empty data
            g2d.setColor(lineColor);
            g2d.drawString("No data available!", this.width / 2 - 50, this.height / 2);
            g2d.dispose();

            return image;
        }

        int lowest = keyPoints.getFirst(), highest = keyPoints.getFirst();

        for (Integer keyPoint : keyPoints) {
            if (keyPoint < lowest)
                lowest = keyPoint;

            if (keyPoint > highest)
                highest = keyPoint;
        }

        g2d.setColor(this.lineColor);

        g2d.dispose();
        return image;
    }
}
