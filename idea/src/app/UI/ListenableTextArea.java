package app.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ListenableTextArea extends JTextArea implements KeyListener {
    public ListenableTextArea() {
        this.addKeyListener(this);
    }

    public synchronized void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    public synchronized ActionListener[] getActionListeners() {
        return listenerList.getListeners(ActionListener.class);
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
