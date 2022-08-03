package minigames.client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JButton;

import minigames.client.backgrounds.Starfield;
import minigames.rendering.GameMetadata;
import minigames.rendering.GameServerDetails;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import java.util.List;
import java.util.Optional;

/**
 * The main window that appears.
 * 
 * For simplicity, we give it a BorderLayout with panels for north, south, east, west, and center.
 * 
 * This makes it simpler for games to load up the UI however they wish, though the default expectation
 * is that the centre just has an 800x600 canvas.
 */
public class MinigameNetworkClientWindow {

    MinigameNetworkClient networkClient;

    JFrame frame;

    JPanel parent;
    JPanel north;
    JPanel center;
    JPanel south;
    JPanel west;
    JPanel east;    

    JLabel messageLabel;

    // We hang on to this one for registering in servers
    JTextField nameField;

    public MinigameNetworkClientWindow(MinigameNetworkClient networkClient) {
        this.networkClient = networkClient;

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        parent = new JPanel(new BorderLayout());        

        north = new JPanel();
        parent.add(north, BorderLayout.NORTH);
        center = new JPanel();
        center.setPreferredSize(new Dimension(800, 600));
        parent.add(center, BorderLayout.CENTER);
        south = new JPanel();
        parent.add(south, BorderLayout.SOUTH);
        east = new JPanel();
        parent.add(east, BorderLayout.EAST);
        west = new JPanel();
        parent.add(west, BorderLayout.WEST);

        frame.add(parent);

        nameField = new JTextField(20);
        nameField.setText("Algernon");
    }

    /** Removes all components from the south panel */
    public void clearSouth() {
        south.removeAll();
    }

    /** Clears all sections of the UI  */
    public void clearAll() {
        for (JPanel p : new JPanel[] { north, south, east, west, center }) {
            p.removeAll();
        }
    }

    /** Adds a component to the north part of the main window */
    public void addNorth(java.awt.Component c) {
        north.add(c);
    }

    /** Adds a component to the south part of the main window */
    public void addSouth(java.awt.Component c) {
        south.add(c);
    }

    /** Adds a component to the east part of the main window */
    public void addEast(java.awt.Component c) {
        east.add(c);
    }

    /** Adds a component to the west part of the main window */
    public void addWest(java.awt.Component c) {
        west.add(c);
    }

    /** Adds a component to the center of the main window */
    public void addCenter(java.awt.Component c) {
        center.add(c);
    }

    /** "Packs" the frame, setting its size to match the preferred layout sizes of its component */
    public void pack() {
        frame.pack();
        parent.repaint();
    }

    /** Makes the main window visible */
    public void show() {
        pack();
        frame.setVisible(true);
    }

    /**
     * Shows a simple message layered over a retro-looking starfield.
     * Terrible placeholder art.
     */
    public void showStarfieldMessage(String s) {
        clearAll();

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.add(new Starfield(networkClient.animator), JLayeredPane.DEFAULT_LAYER);
        layeredPane.setBackground(new Color(0,0,0,0));
        layeredPane.setPreferredSize(new Dimension(800, 600));

        JLabel label = new JLabel(s);
        label.setOpaque(true);
        label.setForeground(Color.CYAN);
        label.setBackground(Color.BLACK);
        label.setFont(new Font("Monospaced", Font.PLAIN, 36));
        Dimension labelSize = label.getPreferredSize();
        label.setSize(labelSize);
        label.setLocation((int)(400 - labelSize.getWidth() / 2), (int)(300 - labelSize.getHeight() / 2));
        layeredPane.add(label, JLayeredPane.MODAL_LAYER);

        center.add(layeredPane);
        pack();
    }

    /**
     * Shows a list of GameServers to pick from
     * 
     * TODO: Prettify!
     * @param servers
     */
    public void showGameServers(List<GameServerDetails> servers) {
        clearAll();

        JPanel panel = new JPanel();
        List<JPanel> serverPanels = servers.stream().map((gsd) -> {
            JPanel p = new JPanel();
            JLabel l = new JLabel(String.format("<html><h1>%s</h1><p>%s</p></html>", gsd.name(), gsd.description()));
            JButton newG = new JButton("Open games");

            newG.addActionListener((evt) -> {
                networkClient.getGameMetadata(gsd.name())
                  .onSuccess((list) -> showGames(gsd.name(), list));
            });

            p.add(l);
            p.add(newG);
            return p;
        }).toList();

        for (JPanel serverPanel : serverPanels) {
            panel.add(serverPanel);
        }

        center.add(panel);
        pack();
        parent.repaint();
    }

    /**
     * Shows a list of games to pick from
     * 
     * TODO: Prettify!
     * @param servers
     */
    public void showGames(String gameServer, List<GameMetadata> inProgress) {
        clearAll();

        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel("Your name");
        namePanel.add(nameLabel);
        namePanel.add(nameField);
        north.add(namePanel);


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        List<JPanel> gamePanels = inProgress.stream().map((g) -> {
            JPanel p = new JPanel();
            JLabel l = new JLabel(String.format("<html><h1>%s</h1><p>%s</p></html>", g.name(), String.join(",", g.players())));
            JButton join = new JButton("Join game");
            join.addActionListener((evt) -> {
                networkClient.joinGame(gameServer, g.name(), nameField.getText());
            });
            join.setEnabled(g.joinable());
            p.add(l);
            p.add(join);
            return p;
        }).toList();

        for (JPanel gamePanel : gamePanels) {
            panel.add(gamePanel);
        }

        JButton newG = new JButton("New game");
        newG.addActionListener((evt) -> {
            // FIXME: We've got a hardcoded player name here
            networkClient.newGame(gameServer, nameField.getText());
        });
        panel.add(newG);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        center.add(scrollPane);
        pack();
        parent.repaint();
    }


}
