package ru.itmo.client.gui.graphics;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class InfoDialog extends JDialog {
    public InfoDialog(Frame parent, ResourceBundle resourceBundle, String[] info) {
        super(parent, resourceBundle.getString("collectionInfo"), true);
        setLayout(new BorderLayout());
        setSize(700, 200);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JLabel label1 = new JLabel(resourceBundle.getString("info1") + info[0]);
        JLabel label2 = new JLabel(resourceBundle.getString("info2") + info[1]);
        JLabel label3 = new JLabel(resourceBundle.getString("info3") + info[2]);
        JLabel label4 = new JLabel(resourceBundle.getString("info4") + info[3]);

        label1.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        label2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        label3.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        label4.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        panel.add(label1);
        panel.add(label2);
        panel.add(label3);
        panel.add(label4);

        add(panel, BorderLayout.CENTER);

        JButton closeButton = new RoundedButton(resourceBundle.getString("close"), Color.decode("#1F4BFA"), 0);
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);
    }
}
