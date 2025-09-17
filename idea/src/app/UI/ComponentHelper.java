package app.ui;

import app.util.ColorUtils;
import app.util.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;

public class ComponentHelper {
    /**
     * Adds a listener to make a component change foreground colours when hovered.
     * @param component
     * @param hoverColor
     */
    public static void setForegroundHoverColor(Component component, Color hoverColor) {
        var originalColor = component.getForeground();

        component.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                component.setForeground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                component.setForeground(originalColor);
            }
        });
    }

    public static JEditorPane loadView(String id) {
        var editor = new JEditorPane();
        editor.setContentType("text/html");
        editor.setText(Utils.loadView(id));
        editor.setEditable(false);
        editor.addCaretListener(e -> {
            editor.getCaret().setVisible(false);
            editor.setOpaque(false);
        });

        return editor;
    }

    /**
     * Prevents whitespace from being entered into a field.
     * @param field The text field to disallow whitespace into.
     */
    public static void disallowWhitespace(JTextComponent field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (Character.isWhitespace(e.getKeyChar())) {
                    e.consume();
                }
            }
        });
    }

    /**
     * Adds default padding and margins to a text field
     * @param field
     */
    public static void makePaddedAndMarginedTextField(JTextComponent field) {
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                LineBorder.createBlackLineBorder()
            ),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
    }

    /**
     * Makes a button follow the formatting of a hyperlink
     * @param button
     */
    public static void makeHyperlink(JButton button) {
        button.setOpaque(false);

        // Do not set the generic types, as otherwise an error occurs in compilation.
        Map attributes = button.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        button.setFont(button.getFont().deriveFont(attributes));

        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(2, 2, 2, 2));
        button.setBackground(ColorUtils.NONE);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        setForegroundHoverColor(button, ColorUtils.fromHex(0xFEFF00));
    }
}
