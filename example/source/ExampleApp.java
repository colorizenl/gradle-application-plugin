//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package com.example;

import nl.colorize.multimedialib.renderer.Canvas;
import nl.colorize.multimedialib.renderer.DisplayMode;
import nl.colorize.multimedialib.renderer.ErrorHandler;
import nl.colorize.multimedialib.renderer.FilePointer;
import nl.colorize.multimedialib.renderer.GraphicsMode;
import nl.colorize.multimedialib.renderer.Renderer;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.WindowOptions;
import nl.colorize.multimedialib.renderer.java2d.Java2DRenderer;
import nl.colorize.multimedialib.renderer.libgdx.GDXRenderer;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.scene.Scene;
import nl.colorize.multimedialib.scene.SceneContext;
import nl.colorize.util.swing.ApplicationMenuListener;

/**
 * Example application that displays an extremely simple MultimediaLib scene.
 * This acts as a "real" application that is included in the plugin code,
 * both for testing purposes and as an example on how to use the plugin.
 */
public class ExampleApp implements Scene, ApplicationMenuListener {

    public static void main(String[] args) {
        ExampleApp app = new ExampleApp();

        Canvas canvas = new Canvas(800, 600, ScaleStrategy.flexible());
        DisplayMode displayMode = new DisplayMode(canvas, 60);

        WindowOptions windowOptions = new WindowOptions("Example");
        windowOptions.setAppMenuListener(app);

        if (args.length > 0 && args[0].contains("java2d")) {
            windowOptions.setTitle(windowOptions.getTitle() + " (Java2D renderer)");
            Renderer renderer = new Java2DRenderer(displayMode, windowOptions);
            renderer.start(app, ErrorHandler.DEFAULT);
        } else {
            Renderer renderer = new GDXRenderer(GraphicsMode.MODE_2D, displayMode, windowOptions);
            renderer.start(app, ErrorHandler.DEFAULT);
        }
    }

    @Override
    public void start(SceneContext context) {
        context.getStage().setBackgroundColor(new ColorRGB(235, 235, 235));

        Image icon = context.getMediaLoader().loadImage(new FilePointer("icon.png"));
        Sprite sprite = new Sprite(icon);
        sprite.setPosition(context.getCanvas().getWidth() / 2f, context.getCanvas().getHeight() / 2f);
        sprite.getTransform().setScale(25);
        context.getStage().getRoot().addChild(sprite);
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
    }

    @Override
    public void onQuit() {
    }

    @Override
    public void onAbout() {
    }
}
