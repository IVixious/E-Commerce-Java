package app.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class PlaceholderPasswordTextField extends JPasswordField implements KeyListener {
    private String placeholder;

    public PlaceholderPasswordTextField(String placeholder) {
        super("");
        this.placeholder = placeholder;
        this.addKeyListener(this);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(2, 2, this.getWidth() - 4, this.getHeight() - 4);

        super.paintComponent(g);

        if (placeholder == null || placeholder.isBlank() || !this.getText().isBlank())
            return;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(this.getDisabledTextColor());
        g2d.drawString(this.getPlaceholder(), this.getInsets().left, g2d.getFontMetrics().getMaxAscent() + this.getInsets().top);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Force action events on these fields
        for (ActionListener listener : this.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, 0, ""));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Force action events on these fields
        for (ActionListener listener : this.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, 0, ""));
        }
    }
}
