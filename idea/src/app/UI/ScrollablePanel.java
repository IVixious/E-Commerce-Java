package app.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class ScrollablePanel extends JPanel implements MouseWheelListener {
    private int currentScrollY = 0;
    private boolean allowsScroll = false;

    public ScrollablePanel() {
        this.addMouseWheelListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Enable scrolling if it's needed
        if (this.getHeight() > this.getParent().getHeight()) {
            allowsScroll = true;

            // Calculate the ratio between the amount of lines there are per the height of the box
            var linesPerHeight = ((double) this.getParent().getHeight() / this.getHeight());

            // The scrollbar height should be dependent on the amount of lines, with a minimum of 3.
            var scrollbarHeight = (int) Math.max(
                linesPerHeight * (double) this.getParent().getHeight(),
                3.0
            );

            // Draw scrollbar
            g2d.fillRect(this.getX() + this.getWidth() - 2, (int) (((double) currentScrollY / this.getHeight()) * (double) (this.getParent().getHeight() - scrollbarHeight)),
                2, scrollbarHeight);

            // ensure that the parent is also repainted, otherwise ghosting will occur
            this.getParent().repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!allowsScroll)
            return;

        var scrollAmount = e.getScrollAmount() * 12;

        if (e.getPreciseWheelRotation() < 0)
            scrollAmount = -scrollAmount;

        var maxScroll = Math.max(this.getHeight() - this.getParent().getHeight(), 0);

        if (currentScrollY + scrollAmount <= 0) {
            currentScrollY = 0;
        } else if (currentScrollY + scrollAmount >= maxScroll) {
            currentScrollY = maxScroll;
        } else {
            currentScrollY += scrollAmount;
        }

        ((JViewport) this.getParent()).setViewPosition(new Point(0, Math.min(currentScrollY, maxScroll)));
    }
}
