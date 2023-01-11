//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package com.example;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Example application that displays an extremely simple Swing user interface.
 * This is included in the plugin code so that the plugin can be tested from
 * a Gradle build.
 */
public class ExampleApp extends JPanel {

    private BufferedImage logo;

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setTitle("Example");
        window.setContentPane(new ExampleApp());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    public ExampleApp() {
        super();
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(235, 235, 235));

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("icon.png")) {
            logo = ImageIO.read(stream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load image", e);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(logo, getWidth() / 2 - 100, getHeight() / 2 - 100, 200, 200, null);
    }
}
