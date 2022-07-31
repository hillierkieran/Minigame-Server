package minigames.client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import minigames.client.backgrounds.Starfield;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

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
    }

    /** Clears all sections of the UI  */
    public void clearAll() {
        for (JPanel p : new JPanel[] { north, south, east, west, center }) {
            p.removeAll();
        }
    }

    public void showGameChooser() {
        clearAll();
    }

    /** "Packs" the frame, setting its size to match the preferred layout sizes of its component */
    public void pack() {
        frame.pack();
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
     * A simple UI for getting the list of games
     */
    class GameChooser {

        JPanel panel = new JPanel();



    }
}
