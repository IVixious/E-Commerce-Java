package app.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * A helper class to set different component attributes without making it a separate variable
 * @param <T> The component to modify/build
 */
public class SwingComponentBuilder<T extends JComponent> {
    private final T component;

    public SwingComponentBuilder(T component) {
        this.component = component;
    }

    public SwingComponentBuilder<T> setOpaque(boolean opaque) {
        this.component.setOpaque(opaque);
        return this;
    }

    public SwingComponentBuilder<T> setBackground(Color color) {
        this.component.setBackground(color);
        return this;
    }

    public SwingComponentBuilder<T> setForeground(Color color) {
        this.component.setForeground(color);
        return this;
    }

    public SwingComponentBuilder<T> setFont(Font font) {
        this.component.setFont(font);
        return this;
    }

    public SwingComponentBuilder<T> setBorder(Border border) {
        this.component.setBorder(border);
        return this;
    }

    public T build() {
        return this.component;
    }
}
