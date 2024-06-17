package ru.itmo.client.gui.graphics;

import ru.itmo.client.builders.PersonBuilder;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.collection.Person;
import ru.itmo.common.network.request.AddIfMaxRequest;
import ru.itmo.common.network.request.AddIfMinRequest;
import ru.itmo.common.network.request.AddRequest;
import ru.itmo.common.network.response.AddIfMaxResponse;
import ru.itmo.common.network.response.AddIfMinResponse;
import ru.itmo.common.network.response.AddResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

public class AddDialog extends JDialog {
    public AddDialog(JFrame parent, ResourceBundle resourceBundle, ClientManager clientManager) {
        super(parent, resourceBundle.getString("addButton"), true);
        setLayout(new BorderLayout());
        setSize(950, 600);
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

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        JCheckBox checkBox1 = new JCheckBox(resourceBundle.getString("addIfMax"));
        JCheckBox checkBox2 = new JCheckBox(resourceBundle.getString("addIfMin"));

        checkBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBox1.isSelected()) {
                    checkBox2.setSelected(false);
                }
            }
        });

        checkBox2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (checkBox2.isSelected()) {
                    checkBox1.setSelected(false);
                }
            }
        });

        checkBoxPanel.add(checkBox1);
        checkBoxPanel.add(checkBox2);

        JPanel buttonPanel = new JPanel();
        JButton submitButton = new RoundedButton(resourceBundle.getString("addButton"), Color.decode("#1F4BFA"), 0);
        submitButton.setForeground(Color.WHITE);
        buttonPanel.add(submitButton);

        submitButton.addActionListener(e -> {
            Person newPerson = (new PersonBuilder(textFields, textAreas, resourceBundle)).build();
            try {
                if (checkBox1.isSelected()) {
                    var response = (AddIfMaxResponse) clientManager.sendAndReceiveCommand(new AddIfMaxRequest(newPerson));
                } else if (checkBox2.isSelected()) {
                    var response = (AddIfMinResponse) clientManager.sendAndReceiveCommand(new AddIfMinRequest(newPerson));
                } else {
                    var response = (AddResponse) clientManager.sendAndReceiveCommand(new AddRequest(newPerson));
                }
            } catch (IOException | ClassNotFoundException ex) {

            }
        });

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(checkBoxPanel);
        southPanel.add(buttonPanel);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);
    }
}
