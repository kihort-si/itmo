package ru.itmo.client.gui.graphics;

import ru.itmo.client.controllers.RemoveLowerController;
import ru.itmo.client.network.ClientManager;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class CountDialog extends JDialog {
    public CountDialog (Frame parent, ResourceBundle resourceBundle, ClientManager clientManager) {
        super(parent, resourceBundle.getString("count"), true);
        setLayout(new BorderLayout());
        setSize(600, 230);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JLabel label = new JLabel(resourceBundle.getString("enterHeight"));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(label);

        JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.add(textField);

        JLabel errorLabel = new JLabel();
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(errorLabel);

        JButton enterButton = new RoundedButton(resourceBundle.getString("countButton"), Color.decode("#1F4BFA"), 0);
        enterButton.setForeground(Color.WHITE);
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(enterButton);

        enterButton.addActionListener(e -> {
            if (!textField.getText().matches("\\d+")) {
                errorLabel.setForeground(Color.decode("#FF4040"));
                errorLabel.setText(resourceBundle.getString("notNumber"));
            } else {
                int status = new RemoveLowerController(clientManager, Double.parseDouble(textField.getText())).execute();
                if (status == Integer.MAX_VALUE) {
                    errorLabel.setForeground(Color.decode("#FF4040"));
                    errorLabel.setText(resourceBundle.getString("serverError"));
                } else {
                    errorLabel.setForeground(Color.BLACK);
                    errorLabel.setText(status + " " + resourceBundle.getString("successCount") + Double.parseDouble(textField.getText()));
                }
            }

        });

        add(panel, BorderLayout.CENTER);

        JButton closeButton = new RoundedButton(resourceBundle.getString("close"), Color.decode("#1F4BFA"), 0);
        closeButton.setForeground(Color.WHITE);
        closeButton.addActionListener(e -> dispose());
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(closeButton, BorderLayout.SOUTH);
    }
}
