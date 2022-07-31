package minigames.client.backgrounds;

import minigames.client.*;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.logging.log4j.*;

/**
 * A retro rectangle of twinkling dots.
 * Includes demo code for requesting an animation tick.
 */
public class Starfield extends Canvas implements Tickable {

    /** A logger for logging output */
    private static final Logger logger = LogManager.getLogger(Starfield.class);

    static Random rand = new Random();

    record Star(int x, int y, float hue, float brightness) {
        Color color() {
            return Color.getHSBColor(hue, 0.5f, brightness);
        }

        Star mutate() {
            return new Star(x, y, hue, Math.min(1, Math.max(0, brightness - 0.1f + 0.2f * rand.nextFloat())));
        }
    };

    Star randomStar(int w, int h) {
        return new Star(rand.nextInt(w), rand.nextInt(h), rand.nextFloat(), rand.nextFloat());
    }

    List<Star> stars = Stream.generate(() -> randomStar(800, 600)).limit(100).toList();

    Animator animator;

    public Starfield(Animator animationList) {
        this.animator = animationList;
        animator.requestTick(this);

        setPreferredSize(new Dimension(800, 600));
        setSize(new Dimension(800, 600));
    }

    @Override 
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;

        g2d.setPaint(Color.BLACK);
        g2d.fillRect(0, 0, 800, 600);

        for (Star s : stars) {
            g2d.setPaint(s.color());
            g2d.fillRect(s.x, s.y, 2, 2);
        }
    }

    @Override
    public void tick(Animator al, long now, long delta) {
        logger.trace("Tick received, now={}ns, delta={}ns, h={}", now, delta, getHeight());

        if (this.isVisible()) {
            stars = stars.stream().map((s) -> s.mutate()).toList();
            repaint();
        }
        al.requestTick(this);
    }
    
}
