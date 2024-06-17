package ru.itmo.client.gui.graphics;

import ru.itmo.common.collection.Person;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class MaxDialog extends JDialog {
    public MaxDialog(Frame parent, ResourceBundle resourceBundle, Person person) {
        super(parent, resourceBundle.getString("max"), true);
        setLayout(new BorderLayout());
        setSize(700, 200);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(false);
        if (person != null) {
            textArea.setText(person.toString());
        } else {
            textArea.setText(resourceBundle.getString("nullCollection"));
        }

        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panel.add(textArea);

        add(panel, BorderLayout.CENTER);

        JButton closeButton = new RoundedButton(resourceBundle.getString("close"), Color.decode("#1F4BFA"), 0);
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);
    }
}
