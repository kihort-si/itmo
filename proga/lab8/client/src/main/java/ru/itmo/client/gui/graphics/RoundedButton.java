package ru.itmo.client.gui.graphics;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {
    private Color backgroundColor;
    private int cornerRadius;

    public RoundedButton(String text, Color backgroundColor, int cornerRadius) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.cornerRadius = cornerRadius;
        setContentAreaFilled(false); // Позволяет нам управлять фоном и границей
        setBorderPainted(false); // Убирает стандартную рамку
        setFocusPainted(false); // Убирает фокусную рамку
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем закругленный прямоугольник
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Рисуем текст кнопки
        FontMetrics fm = g2.getFontMetrics();
        Rectangle r = getBounds();
        int x = (r.width - fm.stringWidth(getText())) / 2;
        int y = (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(getForeground());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}
