package ru.itmo.client.gui;

import ru.itmo.client.controllers.*;
import ru.itmo.client.gui.graphics.*;
import ru.itmo.client.network.ClientManager;
import ru.itmo.client.utils.Runner;
import ru.itmo.common.collection.Person;
import ru.itmo.common.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class GuiManager {
    private final JFrame frame;
    private JPanel headerPanel;
    private JPanel panel;
    private static Locale locale = new Locale("ru");
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("LocalizationBundle", GuiManager.getLocale());
    private User user;
    private JButton userLabel;
    private JButton logoutButton;
    private TableModel tableModel;
    private JTable table;
    private final ClientManager clientManager;
    private String filePathField;
    private final Runner runner;

    public GuiManager(User user, ClientManager clientManager, Runner runner) {
        this.user = user;
        this.clientManager = clientManager;
        this.runner = runner;
        this.frame = new JFrame();
        setUIFont(new javax.swing.plaf.FontUIResource("Open Sans", Font.PLAIN, 16));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 900);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);
        frame.setTitle(resourceBundle.getString("frameName"));

        panel = new JPanel();
        headerPanel = new JPanel();
        headerPanel.setBackground(Color.DARK_GRAY);
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 50));

        frame.setLayout(new BorderLayout());
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);

        frame.setVisible(true);

        createHeader();
        run();
    }

    private void createHeader() {
        headerPanel.setLayout(new BorderLayout());
        userLabel = new JButton(user != null ? user.getLogin() : "");
        userLabel.setForeground(Color.WHITE);
        userLabel.setBackground(Color.DARK_GRAY);
        userLabel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
        userLabel.setBorderPainted(false);
        userLabel.setFocusPainted(false);

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setOpaque(false);

        logoutButton = new JButton(resourceBundle.getString("logout"));
        logoutButton.setVisible(false);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(Color.DARK_GRAY);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);

        userLabel.addActionListener(e -> {
            logoutButton.setVisible(!logoutButton.isVisible());
        });

        logoutButton.addActionListener(e -> {
            user = null;
            EventQueue.invokeLater(this::run);
            userLabel.setText("");
            logoutButton.setVisible(false);
        });

        logoutPanel.add(userLabel);
        logoutPanel.add(logoutButton);

        headerPanel.add(logoutPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        JButton languageButton = new JButton(resourceBundle.getString("changeLanguage"));
        languageButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 30));
        languageButton.setForeground(Color.WHITE);
        languageButton.setBackground(Color.DARK_GRAY);
        languageButton.setBorderPainted(false);
        languageButton.setFocusPainted(false);

        String[] languages = {
                resourceBundle.getString("russian"),
                resourceBundle.getString("macedonian"),
                resourceBundle.getString("ukrainian"),
                resourceBundle.getString("spanish")
        };
        ImageIcon[] icons = {
                createIcon("/icons/russian_flag.png"),
                createIcon("/icons/macedonian_flag.png"),
                createIcon("/icons/ukrainian_flag.png"),
                createIcon("/icons/spanish_PR_flag.png")
        };
        JComboBox<LanguageItem> languageComboBox = new JComboBox<>();
        for (int i = 0; i < languages.length; i++) {
            languageComboBox.addItem(new LanguageItem(languages[i], icons[i]));
        }
        languageComboBox.setRenderer(new LanguageComboBoxRenderer());
        languageComboBox.setVisible(false);
        languageComboBox.setBorder(BorderFactory.createEmptyBorder());
        languageComboBox.setBackground(Color.LIGHT_GRAY);

        languageButton.addActionListener(e -> languageComboBox.setVisible(!languageComboBox.isVisible()));

        languageComboBox.addActionListener(e -> {
            LanguageItem selectedItem = (LanguageItem) languageComboBox.getSelectedItem();
            if (selectedItem != null) {
                String selectedLanguage = resourceBundle.getString("language." + selectedItem.getText());
                switch (selectedLanguage) {
                    case "ru":
                        setLocale(new Locale("ru"));
                        break;
                    case "mk":
                        setLocale(new Locale("mk"));
                        break;
                    case "uk":
                        setLocale(new Locale("uk"));
                        break;
                    case "es_PR":
                        setLocale(new Locale("es", "PR"));
                        break;
                }
            }
        });

        rightPanel.add(languageButton);
        rightPanel.add(languageComboBox);
        headerPanel.add(rightPanel, BorderLayout.EAST);
    }

    private ImageIcon createIcon(String path) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            Image image = icon.getImage();
            Image scaledImage = image.getScaledInstance(30, 20, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledImage);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public void run() {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        if (user == null) loginAuth();
        else mainForm();
        panel.revalidate();
        panel.repaint();
    }

    public void loginAuth() {
        panel.removeAll();
        JPanel loginPanel = new JPanel();
        GroupLayout layout = new GroupLayout(loginPanel);
        loginPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel welcome = new JLabel(resourceBundle.getString("welcome"));
        welcome.setFont(new Font("Open Sans", Font.BOLD, 20));
        JLabel loginTextLabel = new JLabel(resourceBundle.getString("enterLogin"));
        JTextField loginField = new JTextField();
        JLabel passwordTextLabel = new JLabel(resourceBundle.getString("enterPass"));
        JPasswordField passwordField = new JPasswordField();
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.decode("#FF4040"));
        JButton loginButton = new RoundedButton(resourceBundle.getString("getLogin"), Color.decode("#1F4BFA"), 15);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel registerLabel = new JLabel(resourceBundle.getString("NotHave"));
        JLabel registerLink = new JLabel(resourceBundle.getString("WantRegister"));
        registerLink.setForeground(Color.decode("#1F4BFA"));
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                register();
            }
        });

        JPanel registerPanel = new JPanel();
        registerPanel.add(registerLabel);
        registerPanel.add(registerLink);

        Dimension welcomeSize = new Dimension(200, 40);
        Dimension labelSize = new Dimension(200, 25);
        Dimension fieldSize = new Dimension(290, 30);
        Dimension buttonSize = new Dimension(290, 35);

        welcome.setPreferredSize(welcomeSize);
        welcome.setMaximumSize(welcomeSize);
        welcome.setMaximumSize(welcomeSize);

        loginTextLabel.setPreferredSize(labelSize);
        loginTextLabel.setMinimumSize(labelSize);
        loginTextLabel.setMaximumSize(labelSize);

        passwordTextLabel.setPreferredSize(labelSize);
        passwordTextLabel.setMinimumSize(labelSize);
        passwordTextLabel.setMaximumSize(labelSize);

        loginField.setPreferredSize(fieldSize);
        loginField.setMinimumSize(fieldSize);
        loginField.setMaximumSize(fieldSize);

        passwordField.setPreferredSize(fieldSize);
        passwordField.setMinimumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);

        errorLabel.setPreferredSize(labelSize);
        errorLabel.setMinimumSize(labelSize);
        errorLabel.setMaximumSize(labelSize);

        loginButton.setPreferredSize(buttonSize);
        loginButton.setMinimumSize(buttonSize);
        loginButton.setMaximumSize(buttonSize);

        loginButton.addActionListener(e -> {
            if (checkFields(loginField, passwordField, errorLabel)) {
                User loginUser = new User(Integer.MAX_VALUE, loginField.getText(), String.valueOf(passwordField.getPassword()));
                int authResult = new AuthorizationController(clientManager, loginUser).execute();
                switch (authResult) {
                    case 0 -> {
                        user = loginUser;
                        userLabel.setText(user.getLogin());
                        errorLabel.setText("");
                        panel.removeAll();
                        mainForm();
                    }
                    case 1 -> errorLabel.setText(resourceBundle.getString("invalidUser") + loginField.getText());
                    case 2 -> errorLabel.setText(resourceBundle.getString("invalidPassword"));
                    case 3 -> errorLabel.setText(resourceBundle.getString("dbError"));
                    case 4 -> errorLabel.setText(resourceBundle.getString("wtfError"));
                    case 5 -> errorLabel.setText(resourceBundle.getString("serverError"));
                }
            }
        });

        loginPanel.setOpaque(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(welcome)
                .addComponent(loginTextLabel)
                .addComponent(loginField)
                .addComponent(passwordTextLabel)
                .addComponent(passwordField)
                .addComponent(errorLabel)
                .addComponent(loginButton)
                .addComponent(registerPanel));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(welcome)
                .addComponent(loginTextLabel)
                .addComponent(loginField)
                .addComponent(passwordTextLabel)
                .addComponent(passwordField)
                .addComponent(errorLabel)
                .addComponent(loginButton)
                .addComponent(registerPanel));

        loginButton.setForeground(Color.WHITE);

        loginField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setBorder(BorderFactory.createEmptyBorder());

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(loginPanel, new GridBagConstraints());

        panel.add(wrapperPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void register() {
        panel.removeAll();
        JPanel registerPanel = new JPanel();
        GroupLayout layout = new GroupLayout(registerPanel);
        registerPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel welcome = new JLabel(resourceBundle.getString("welcome"));
        welcome.setFont(new Font("Open Sans", Font.BOLD, 20));
        JLabel registerTextLabel = new JLabel(resourceBundle.getString("enterLogin"));
        JTextField registerField = new JTextField();
        JLabel passwordTextLabel = new JLabel(resourceBundle.getString("enterPass"));
        JPasswordField passwordField = new JPasswordField();
        JLabel confirmPasswordTextLabel = new JLabel(resourceBundle.getString("confirmPass"));
        JPasswordField confirmPasswordField = new JPasswordField();
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.decode("#FF4040"));
        JButton registerButton = new RoundedButton(resourceBundle.getString("WantRegister"), Color.decode("#1F4BFA"), 15);
        JLabel loginLabel = new JLabel(resourceBundle.getString("Have"));
        JLabel loginLink = new JLabel(resourceBundle.getString("getLogin"));
        loginLink.setForeground(Color.decode("#1F4BFA"));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                loginAuth();
            }
        });

        JPanel loginPanel = new JPanel();
        loginPanel.add(loginLabel);
        loginPanel.add(loginLink);

        Dimension welcomeSize = new Dimension(200, 40);
        Dimension labelSize = new Dimension(200, 25);
        Dimension fieldSize = new Dimension(290, 30);
        Dimension buttonSize = new Dimension(290, 35);

        welcome.setPreferredSize(welcomeSize);
        welcome.setMaximumSize(welcomeSize);
        welcome.setMaximumSize(welcomeSize);

        registerTextLabel.setPreferredSize(labelSize);
        registerTextLabel.setMinimumSize(labelSize);
        registerTextLabel.setMaximumSize(labelSize);

        passwordTextLabel.setPreferredSize(labelSize);
        passwordTextLabel.setMinimumSize(labelSize);
        passwordTextLabel.setMaximumSize(labelSize);

        registerField.setPreferredSize(fieldSize);
        registerField.setMinimumSize(fieldSize);
        registerField.setMaximumSize(fieldSize);

        passwordField.setPreferredSize(fieldSize);
        passwordField.setMinimumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);

        confirmPasswordTextLabel.setPreferredSize(labelSize);
        confirmPasswordTextLabel.setMinimumSize(labelSize);
        confirmPasswordTextLabel.setMaximumSize(labelSize);

        confirmPasswordField.setPreferredSize(fieldSize);
        confirmPasswordField.setMinimumSize(fieldSize);
        confirmPasswordField.setMaximumSize(fieldSize);

        errorLabel.setPreferredSize(labelSize);
        errorLabel.setMinimumSize(labelSize);
        errorLabel.setMaximumSize(labelSize);

        registerButton.setPreferredSize(buttonSize);
        registerButton.setMinimumSize(buttonSize);
        registerButton.setMaximumSize(buttonSize);

        registerButton.addActionListener(e -> {
            if (checkRegisterFields(registerField, passwordField, confirmPasswordField, errorLabel)) {
                User registerLogin = new User(Integer.MAX_VALUE, registerField.getText(), String.valueOf(passwordField.getPassword()));
                int registerResult = new RegisterController(clientManager, registerLogin).execute();
                switch (registerResult) {
                    case 0 -> {
                        errorLabel.setText("");
                        loginAuth();
                    }
                    case 1 -> errorLabel.setText(resourceBundle.getString("userExist"));
                    case 2 -> errorLabel.setText(resourceBundle.getString("dbError"));
                    case 3 -> errorLabel.setText(resourceBundle.getString("wtfError"));
                    case 4 -> errorLabel.setText(resourceBundle.getString("serverError"));
                }
            }
        });
        registerPanel.setOpaque(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(welcome)
                .addComponent(registerTextLabel)
                .addComponent(registerField)
                .addComponent(passwordTextLabel)
                .addComponent(passwordField)
                .addComponent(confirmPasswordTextLabel)
                .addComponent(confirmPasswordField)
                .addComponent(errorLabel)
                .addComponent(registerButton)
                .addComponent(loginPanel));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(welcome)
                .addComponent(registerTextLabel)
                .addComponent(registerField)
                .addComponent(passwordTextLabel)
                .addComponent(passwordField)
                .addComponent(confirmPasswordTextLabel)
                .addComponent(confirmPasswordField)
                .addComponent(errorLabel)
                .addComponent(registerButton)
                .addComponent(loginPanel));

        registerButton.setForeground(Color.WHITE);

        registerField.setBorder(BorderFactory.createEmptyBorder());
        passwordField.setBorder(BorderFactory.createEmptyBorder());
        confirmPasswordField.setBorder(BorderFactory.createEmptyBorder());

        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.add(registerPanel, new GridBagConstraints());

        panel.add(wrapperPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private boolean checkFields(JTextField loginField, JPasswordField passwordField, JLabel errorLabel) {
        if (loginField.getText().isEmpty()) {
            errorLabel.setText(resourceBundle.getString("LoginNotNull"));
            errorLabel.setForeground(Color.decode("#FF4040"));
            return false;
        } else if (String.valueOf(passwordField.getPassword()).isEmpty()) {
            errorLabel.setText(resourceBundle.getString("PassNotNull"));
            errorLabel.setForeground(Color.decode("#FF4040"));
            return false;
        }
        return true;
    }

    private boolean checkRegisterFields(JTextField registerField, JPasswordField passwordField, JPasswordField confirmPasswordField, JLabel errorLabel) {
        if (registerField.getText().isEmpty()) {
            errorLabel.setText(resourceBundle.getString("RegisterNotNull"));
            errorLabel.setForeground(Color.decode("#FF4040"));
            return false;
        } else if (String.valueOf(passwordField.getPassword()).isEmpty()) {
            errorLabel.setText(resourceBundle.getString("PassNotNull"));
            errorLabel.setForeground(Color.decode("#FF4040"));
            return false;
        } else if (!String.valueOf(passwordField.getPassword()).equals(String.valueOf(confirmPasswordField.getPassword()))) {
            errorLabel.setText(resourceBundle.getString("PassMismatch"));
            errorLabel.setForeground(Color.decode("#FF4040"));
            return false;
        }
        return true;
    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    public static Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        GuiManager.locale = locale;
        ResourceBundle.clearCache();
        resourceBundle = ResourceBundle.getBundle("LocalizationBundle", GuiManager.getLocale());
        run();
        frame.setTitle(resourceBundle.getString("frameName"));
        frame.revalidate();
        frame.repaint();
        this.run();
    }

    static class LanguageComboBoxRenderer extends JLabel implements ListCellRenderer<LanguageItem> {
        @Override
        public Component getListCellRendererComponent(JList<? extends LanguageItem> list, LanguageItem value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.getText());
            setIcon(value.getIcon());
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            if (isSelected) {
                setBackground(Color.LIGHT_GRAY);
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(Color.GRAY);
                setForeground(list.getForeground());
            }
            setOpaque(true);
            return this;
        }
    }

    public void mainForm() {
        panel.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorLabel.setPreferredSize(new Dimension(Window.WIDTH, 40));
        errorLabel.setMinimumSize(new Dimension(Window.WIDTH, 40));
        errorLabel.setMaximumSize(new Dimension(Window.WIDTH, 40));
        errorLabel.setForeground(Color.decode("#FF4040"));
        mainPanel.add(errorLabel);

        String[] columnNames = {
                resourceBundle.getString("id"),
                resourceBundle.getString("name"),
                resourceBundle.getString("coordinateX"),
                resourceBundle.getString("coordinateY"),
                resourceBundle.getString("creationDate"),
                resourceBundle.getString("height"),
                resourceBundle.getString("weight"),
                resourceBundle.getString("eyeColor"),
                resourceBundle.getString("nationality"),
                resourceBundle.getString("LocationX"),
                resourceBundle.getString("LocationY"),
                resourceBundle.getString("LocationZ"),
                resourceBundle.getString("creator")
        };

        java.util.List<Person> initialData = new CollectionController(clientManager).execute();
        tableModel = new TableModel(initialData, columnNames);
        table = new JTable(tableModel);

        JTextField filterTextField = new JTextField(15);
        JComboBox<String> columnComboBox = new JComboBox<>(columnNames);
        JButton filterButton = new RoundedButton(resourceBundle.getString("Filter"), Color.decode("#1F4BFA"), 15);
        JButton resetButton = new RoundedButton(resourceBundle.getString("Reset"), Color.decode("#1F4BFA"), 15);
        filterButton.setForeground(Color.WHITE);
        resetButton.setForeground(Color.WHITE);

        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel(resourceBundle.getString("FilterButton")));
        controlPanel.add(filterTextField);
        controlPanel.add(columnComboBox);
        controlPanel.add(filterButton);
        controlPanel.add(resetButton);

        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String keyword = filterTextField.getText();
                tableModel.filter(keyword);
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.resetFilter();
            }
        });

        boolean[] sortOrder;
        sortOrder = new boolean[columnNames.length];

        table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int column = table.columnAtPoint(evt.getPoint());
                boolean ascending = sortOrder[column];
                tableModel.sort(column, ascending);
                sortOrder[column] = !ascending;
            }
        });

        mainPanel.add(controlPanel);

        JScrollPane tableScrollPane = new JScrollPane(table);

        tableScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        mainPanel.add(tableScrollPane);

        startAutoUpdate(new CollectionController(clientManager).execute());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 4, 10, 10));
        JButton addButton = new RoundedButton(resourceBundle.getString("addButton"), Color.decode("#1F4BFA"), 15);
        JButton clearButton = new RoundedButton(resourceBundle.getString("clearButton"), Color.decode("#1F4BFA"), 15);
        JButton countButton = new RoundedButton(resourceBundle.getString("countButton"), Color.decode("#1F4BFA"), 15);
        JButton infoButton = new RoundedButton(resourceBundle.getString("infoButton"), Color.decode("#1F4BFA"), 15);
        JButton maxButton = new RoundedButton(resourceBundle.getString("maxButton"), Color.decode("#1F4BFA"), 15);
        JButton removeButton = new RoundedButton(resourceBundle.getString("removeButton"), Color.decode("#1F4BFA"), 15);
        JButton removeLowerButton = new RoundedButton(resourceBundle.getString("removeLowerButton"), Color.decode("#1F4BFA"), 15);
        JButton updateButton = new RoundedButton(resourceBundle.getString("updateButton"), Color.decode("#1F4BFA"), 15);
        JButton chooseScriptButton = new RoundedButton(resourceBundle.getString("chooseScriptButton"), Color.decode("#1F4BFA"), 15);
        JButton executeScriptButton = new RoundedButton(resourceBundle.getString("executeScriptButton"), Color.decode("#1F4BFA"), 15);
        JButton visualizationButton = new RoundedButton(resourceBundle.getString("showAnimated"), Color.decode("#1F4BFA"), 15);

        addButton.setForeground(Color.WHITE);
        clearButton.setForeground(Color.WHITE);
        countButton.setForeground(Color.WHITE);
        infoButton.setForeground(Color.WHITE);
        maxButton.setForeground(Color.WHITE);
        removeButton.setForeground(Color.WHITE);
        removeLowerButton.setForeground(Color.WHITE);
        updateButton.setForeground(Color.WHITE);
        chooseScriptButton.setForeground(Color.WHITE);
        executeScriptButton.setForeground(Color.WHITE);
        visualizationButton.setForeground(Color.WHITE);

        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        countButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        infoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        maxButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeLowerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseScriptButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        executeScriptButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        visualizationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buttonPanel.add(addButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(countButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(maxButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(removeLowerButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(chooseScriptButton);
        buttonPanel.add(executeScriptButton);
        buttonPanel.add(visualizationButton);

        addButton.addActionListener(e -> {
            AddDialog addDialog = new AddDialog(frame, resourceBundle, clientManager);
            addDialog.setVisible(true);
        });

        clearButton.addActionListener(e -> {
            new ClearController(clientManager).execute();
        });

        countButton.addActionListener(e -> {
            CountDialog countDialog = new CountDialog(frame, resourceBundle, clientManager);
            countDialog.setVisible(true);
        });

        infoButton.addActionListener(e -> {
            String[] info = new InfoController(clientManager).execute();
            InfoDialog infoDialog = new InfoDialog(frame, resourceBundle, info);
            infoDialog.setVisible(true);
        });

        maxButton.addActionListener(e -> {
            Person max = new MaxByLocationController(clientManager).execute();
            MaxDialog maxDialog = new MaxDialog(frame, resourceBundle, max);
            maxDialog.setVisible(true);
        });

        removeButton.addActionListener(e -> {
            RemoveDialog removeDialog = new RemoveDialog(frame, resourceBundle, clientManager);
            removeDialog.setVisible(true);
        });

        removeLowerButton.addActionListener(e -> {
            RemoveLowerDialog removeLowerDialog = new RemoveLowerDialog(frame, resourceBundle, clientManager);
            removeLowerDialog.setVisible(true);
        });

        chooseScriptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        executeScriptButton.addActionListener(e -> {
            if (!filePathField.isEmpty()) {
                Thread fileModeThread = new Thread(() -> {
                    runner.fileMode(filePathField);
                });
                fileModeThread.start();
            }
        });

        updateButton.addActionListener(e -> {
            UpdateDialog updateDialog = new UpdateDialog(frame, resourceBundle, clientManager);
            updateDialog.setVisible(true);
        });

        visualizationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openVisualizationWindow();
            }
        });

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        mainPanel.add(buttonPanel);

        panel.add(mainPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void openVisualizationWindow() {
        JFrame visualizationFrame = new JFrame("Visualization");
        VisualizationPanel visualizationPanel = new VisualizationPanel(new CollectionController(clientManager).execute(), clientManager, resourceBundle);
        JScrollPane scrollPane = new JScrollPane(visualizationPanel);
        visualizationPanel.setPreferredSize(new Dimension(1600, 1200));
        visualizationFrame.add(scrollPane);
        visualizationFrame.setSize(800, 600);
        visualizationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        visualizationFrame.setLocationRelativeTo(null);
        visualizationFrame.setVisible(true);
    }

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField = selectedFile.getAbsolutePath();
        }
    }

    private void startAutoUpdate(java.util.List<Person> dataList) {
        Thread updateThread = new Thread(() -> {
            Timer timer = new Timer(1000, e -> {
                java.util.List<Person> updatedList = new CollectionController(clientManager).execute();
                if (updatedList != null) {
                    dataList.clear();
                    dataList.addAll(updatedList);
                    tableModel.updateData(dataList);
                }
            });
            timer.start();
        });
        updateThread.start();
    }

}
