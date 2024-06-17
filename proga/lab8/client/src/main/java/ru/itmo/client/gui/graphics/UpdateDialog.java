package ru.itmo.client.gui.graphics;

import ru.itmo.client.builders.PersonBuilder;
import ru.itmo.client.controllers.UpdateController;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.collection.Person;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class UpdateDialog extends JDialog {
    private final boolean withId;
    private final Long id;
    public UpdateDialog(JFrame parent, ResourceBundle resourceBundle, ClientManager clientManager, long id) {
        super(parent, resourceBundle.getString("updateButton"), true);
        withId = true;
        this.id = id;
        gui(parent, resourceBundle, clientManager);
    }

    public UpdateDialog(JFrame parent, ResourceBundle resourceBundle, ClientManager clientManager) {
        super(parent, resourceBundle.getString("updateButton"), true);
        withId = false;
        this.id = null;
        gui(parent, resourceBundle, clientManager);
    }

    private void gui(JFrame parent, ResourceBundle resourceBundle, ClientManager clientManager) {
        setLayout(new BorderLayout());
        setSize(950, 700);
        setLocationRelativeTo(parent);
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        JLabel[][] labels = new JLabel[5][2];
        JTextField[][] textFields = new JTextField[5][2];
        JLabel[][] textAreas = new JLabel[5][2];

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 2; j++) {
                JPanel cellPanel = new JPanel();
                cellPanel.setLayout(new BoxLayout(cellPanel, BoxLayout.Y_AXIS));

                labels[i][j] = new JLabel();
                labels[i][j].setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                textFields[i][j] = new JTextField(10);
                textFields[i][j].setBorder(BorderFactory.createEmptyBorder());
                textAreas[i][j] = new JLabel();
                textAreas[i][j].setForeground(Color.decode("#FF4040"));
                textAreas[i][j].setBackground(parent.getBackground());
                textAreas[i][j].setVisible(true);
                textAreas[i][j].setText(" ");

                cellPanel.add(labels[i][j]);
                cellPanel.add(textFields[i][j]);
                cellPanel.add(textAreas[i][j]);

                mainPanel.add(cellPanel);
            }
        }

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        labels[0][0].setText(resourceBundle.getString("name"));
        labels[1][0].setText(resourceBundle.getString("coordinateX"));
        labels[2][0].setText(resourceBundle.getString("coordinateY"));
        labels[3][0].setText(resourceBundle.getString("height"));
        labels[4][0].setText(resourceBundle.getString("weight"));
        labels[0][1].setText(resourceBundle.getString("eyeColorEnum"));
        labels[1][1].setText(resourceBundle.getString("nationalityEnum"));
        labels[2][1].setText(resourceBundle.getString("LocationX"));
        labels[3][1].setText(resourceBundle.getString("LocationY"));
        labels[4][1].setText(resourceBundle.getString("LocationZ"));

        JPanel idBoxPanel = new JPanel();
        idBoxPanel.setLayout(new BoxLayout(idBoxPanel, BoxLayout.Y_AXIS));
        JTextField idField = new JTextField();
        idField.setBorder(BorderFactory.createEmptyBorder());
        JLabel errorPanel = new JLabel();
        idBoxPanel.add(idField);
        idBoxPanel.add(errorPanel);
        idBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        if (withId) {
            idField.setVisible(false);
        }

        JPanel buttonPanel = new JPanel();
        JButton submitButton = new RoundedButton(resourceBundle.getString("updateButton"), Color.decode("#1F4BFA"), 0);
        submitButton.setForeground(Color.WHITE);
        buttonPanel.add(submitButton);

        submitButton.addActionListener(e -> {
            Long idUpdate = null;
            if (!withId) {
                if (idField.getText().isEmpty()) {
                    errorPanel.setForeground(Color.decode("#FF4040"));
                    errorPanel.setText(resourceBundle.getString("notNull"));
                } else {
                    idUpdate = Long.parseLong(idField.getText());
                }
            } else {
                idUpdate = id;
            }
            Person updatedPerson = (new PersonBuilder(textFields, textAreas, resourceBundle)).build();
            if (idUpdate != null) {
                int status = new UpdateController(clientManager, updatedPerson, idUpdate).execute();
                switch (status) {
                    case 0 -> {
                        errorPanel.setForeground(Color.BLACK);
                        errorPanel.setText(resourceBundle.getString("successUpdate"));
                    }
                    case 1 -> {
                        errorPanel.setForeground(Color.decode("#FF4040"));
                        errorPanel.setText(resourceBundle.getString("notPersonWithId"));
                    }
                    case 2 -> {
                        errorPanel.setForeground(Color.decode("#FF4040"));
                        errorPanel.setText(resourceBundle.getString("notYourPerson"));
                    }
                    default -> {
                        errorPanel.setForeground(Color.decode("#FF4040"));
                        errorPanel.setText(resourceBundle.getString("serverError"));
                    }
                }
            }
        });

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(idBoxPanel);
        southPanel.add(buttonPanel);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);
    }
}
