package app.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

public class MultilineTextLabel extends JComponent implements MouseWheelListener {
    private String text = "";
    private int currentScrollY = 0;
    private boolean allowsScroll = false;

    public MultilineTextLabel() {
        this.addMouseWheelListener(this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g;
        var fontMetrics = g2d.getFontMetrics();
        // Otherwise, the text looks terrible
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        var currentY = this.getY() + fontMetrics.getHeight() - currentScrollY;
        var lines = new ArrayList<String>();

        var x = this.getX() + this.getInsets().left;
        var width = this.getWidth() - this.getInsets().right;

        // Split the lines and try to wrap them if it gets too long.
        for (String line : this.getText().split("\n")) {
            if (fontMetrics.stringWidth(line) < width) {
                lines.add(line);
            } else {
                var builtLine = new StringBuilder();

                for (String word : line.split(" ")) {
                    if (fontMetrics.stringWidth(builtLine + word) > width) {
                        lines.add(builtLine.toString());
                        builtLine = new StringBuilder();
                    }

                    builtLine.append(word);
                    builtLine.append(" ");
                }

                lines.add(builtLine.toString());
            }
        }

        for (String line : lines) {
            g2d.drawString(line, x, currentY);
            currentY += fontMetrics.getHeight() + 2;
        }

        // Enable scrolling if it's needed
        if (lines.size() * (fontMetrics.getHeight() + 2) > this.getHeight()) {
            allowsScroll = true;

            // Calculate the ratio between the amount of lines there are per the height of the box
            var totalLineHeight = ((double) lines.size() * (fontMetrics.getHeight() + 2));
            var linesPerHeight = ((double) this.getHeight() / totalLineHeight);

            // The scrollbar height should be dependent on the amount of lines, with a minimum of 3.
            var scrollbarHeight = (int) Math.max(
                linesPerHeight * (double) this.getHeight(),
                3.0
            );

            // Draw scrollbar
            g2d.fillRect(this.getX() + this.getWidth() - 2, (int) (((double) currentScrollY / totalLineHeight) * (double) (this.getHeight() - scrollbarHeight)),
                2, scrollbarHeight);

            // ensure that the parent is also repainted, otherwise ghosting will occur
            this.getParent().repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!allowsScroll)
            return;

        var scrollAmount = e.getScrollAmount();

        if (e.getPreciseWheelRotation() < 0)
            scrollAmount = -scrollAmount;

        if (currentScrollY + scrollAmount <= 0) {
            currentScrollY = 0;
        } else if (currentScrollY + scrollAmount >= this.getHeight()) {
            currentScrollY = this.getHeight();
        } else {
            currentScrollY += scrollAmount;
        }

        this.revalidate();
        this.repaint();
    }
}
