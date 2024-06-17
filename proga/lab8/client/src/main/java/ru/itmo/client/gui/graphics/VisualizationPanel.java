package ru.itmo.client.gui.graphics;

import ru.itmo.client.controllers.CollectionController;
import ru.itmo.client.network.ClientManager;
import ru.itmo.common.collection.Person;
import ru.itmo.common.user.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ResourceBundle;

public class VisualizationPanel extends JPanel {
    private List<Person> persons;
    private double scale = 1.0;
    private Point dragStartScreen;
    private Point dragStart;
    private final ClientManager clientManager;
    private float opacity = 1.0f;
    
    public VisualizationPanel(List<Person> persons, ClientManager clientManager, ResourceBundle resourceBundle) {
        this.clientManager = clientManager;
        this.persons = persons;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point2D scaledPoint = getScaledPoint(e.getPoint());
                for (Person person : persons) {
                    int x = (int) person.getCoordinates().getX();
                    int y = person.getCoordinates().getY().intValue();
                    int width = (int) (person.getWeight() * scale);
                    int height = (int) (person.getHeight() * scale);
                    if (scaledPoint.getX() >= x && scaledPoint.getX() <= x + width &&
                            scaledPoint.getY() >= y && scaledPoint.getY() <= y + height) {
                        JPanel panel = new JPanel();
                        panel.setLayout(new BorderLayout());
                        panel.add(new JLabel(person.toString()), BorderLayout.NORTH);
                        JButton refreshButton = new RoundedButton(resourceBundle.getString("updateButton"), Color.decode("#1F4BFA"), 0);
                        refreshButton.setForeground(Color.WHITE);
                        refreshButton.setMaximumSize(new Dimension(100, 20));
                        refreshButton.addActionListener(e1 -> {
                            UpdateDialog updateDialog = new UpdateDialog(new JFrame(), resourceBundle, clientManager, person.getId());
                            updateDialog.setVisible(true);
                        });
                        panel.add(refreshButton, BorderLayout.SOUTH);

                        JOptionPane.showOptionDialog(null, panel, "Object Info", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragStartScreen = e.getLocationOnScreen();
                dragStart = getLocation();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int deltaX = e.getLocationOnScreen().x - dragStartScreen.x;
                int deltaY = e.getLocationOnScreen().y - dragStartScreen.y;
                setLocation(dragStart.x + deltaX, dragStart.y + deltaY);
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getPreciseWheelRotation() < 0) {
                    scale *= 1.1;
                } else {
                    scale /= 1.1;
                }
                revalidate();
                repaint();
            }
        });

        Timer timer = new Timer(1000, e -> {
            updatePersons();
            repaint();
        });
        timer.start();
    }

    private void updatePersons() {
        persons = new CollectionController(clientManager).execute();
    }

    private Point2D getScaledPoint(Point p) {
        return new Point2D.Double(p.getX() / scale, p.getY() / scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform originalTransform = g2.getTransform();
        g2.scale(scale, scale);


        Timer timer = new Timer(1000 / 60, e -> {
            long currentTime = System.currentTimeMillis();
            float cycle = (currentTime % 1000) / 1000.0f;

            if (cycle < 0.5) {
                opacity = 1.0f - (cycle * 2);
            } else {
                opacity = (cycle - 0.5f) * 2;
            }
            repaint();
        });
        timer.start();

        for (Person person : persons) {
            int x = (int) person.getCoordinates().getX();
            int y = person.getCoordinates().getY().intValue();
            int width = (int) person.getWeight();
            int height = person.getHeight().intValue();
            g2.setColor(getColorByUser(person.getCreator()));

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

            g2.fillRect(x, y, width, height);

            g2.setTransform(originalTransform);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    private Color getColorByUser(long userId) {
        return new Color((int) (userId * 123456789 % 256), (int) (userId * 987654321 % 256), (int) (userId * 54321 % 256));
    }
}