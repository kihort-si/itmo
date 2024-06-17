package ru.itmo.client.gui.graphics;

import ru.itmo.client.controllers.RemoveController;
import ru.itmo.client.network.ClientManager;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class RemoveDialog extends JDialog {
    public RemoveDialog(Frame parent, ResourceBundle resourceBundle, ClientManager clientManager) {
        super(parent, resourceBundle.getString("remove"), true);
        setLayout(new BorderLayout());
        setSize(600, 230);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        JLabel label = new JLabel(resourceBundle.getString("enterId"));
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(label);

        JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        panel.add(textField);

        JLabel errorLabel = new JLabel();
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panel.add(errorLabel);

        JButton enterButton = new RoundedButton(resourceBundle.getString("removeButton"), Color.decode("#1F4BFA"), 0);
        enterButton.setForeground(Color.WHITE);
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(enterButton);

        enterButton.addActionListener(e -> {
            if (!textField.getText().matches("\\d+")) {
                errorLabel.setForeground(Color.decode("#FF4040"));
                errorLabel.setText(resourceBundle.getString("notNumber"));
            } else {
                int status = new RemoveController(clientManager, Integer.parseInt(textField.getText())).execute();
                switch (status) {
                    case 0 -> {
                        errorLabel.setForeground(Color.BLACK);
                        errorLabel.setText(resourceBundle.getString("successRemoveById") + Integer.parseInt(textField.getText()));
                    }
                    case 1 -> {
                        errorLabel.setForeground(Color.decode("#FF4040"));
                        errorLabel.setText(resourceBundle.getString("notPersonWithId") + Integer.parseInt(textField.getText()));
                    }
                    case 2 -> {
                        errorLabel.setForeground(Color.decode("#FF4040"));
                        errorLabel.setText(resourceBundle.getString("notYourPerson"));
                    }
                    case 3 -> {
                        errorLabel.setForeground(Color.decode("#FF4040"));
                        errorLabel.setText(resourceBundle.getString(""));
                    }
                    default -> {
                        errorLabel.setForeground(Color.decode("#FF4040"));
                        errorLabel.setText(resourceBundle.getString("serverError"));
                    }
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
