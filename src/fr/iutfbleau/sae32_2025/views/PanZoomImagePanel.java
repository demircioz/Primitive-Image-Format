package fr.iutfbleau.sae32_2025.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * The <code>PanZoomImagePanel</code> class is a specialized Swing component that renders an image
 * with support for interactive navigation.
 * <p>
 * It provides two main features:
 * 1. Panning: The user can click and drag to move the image around.
 * 2. Zooming: The user can scroll the mouse wheel to zoom in or out, focused on the cursor position.
 * </p>
 * <p>
 * The rendering logic relies on Java 2D's AffineTransform to apply translation and scaling efficiently
 * without modifying the original image data.
 * </p>
 *
 * @version 1.3
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class PanZoomImagePanel extends JPanel {
    
    /** The source image to display. */
    private BufferedImage image;
    
    /** The tiled background pattern (checkerboard or similar). */
    private BufferedImage backgroundImage;
    
    /** The current zoom scale (1.0 = 100%). */
    private double zoomFactor = 1.0;
    
    /** The horizontal translation offset. */
    private double xOffset = 0;
    
    /** The vertical translation offset. */
    private double yOffset = 0;
    
    /** Tracks the last recorded mouse position for dragging calculations. */
    private Point lastMousePos;
    
    /** Flag to enable or disable user interaction (used to freeze the view in analysis mode). */
    private boolean interactiveMode = false;

    /**
     * Constructs the panel and initializes the event listeners using named inner classes.
     */
    public PanZoomImagePanel() {
        // Load the background asset securely through the AppTheme utility
        // Image bgImg = AppTheme.loadAssetImage("icons/background.png");
        // if (bgImg != null) {
        //     this.backgroundImage = toBufferedImage(bgImg);
        // }
        
        // Set a fallback background color if the image fails to load
        // if (this.backgroundImage == null) {
        this.setBackground(Color.DARK_GRAY);
        // }

        // Attach the mouse handler for panning and zooming
        MouseInputHandler mouseHandler = new MouseInputHandler();
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
        this.addMouseWheelListener(mouseHandler);

        // Attach the component listener to handle window resizing
        this.addComponentListener(new ResizeHandler());
    }

    /**
     * Utility method to convert a standard Image to a BufferedImage.
     * This ensures compatibility with Graphics2D drawing operations.
     *
     * @param img the source image object
     * @return a BufferedImage copy
     */
    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }

    /**
     * Sets the image to be displayed and resets the view to fit the panel.
     *
     * @param image the new image to render
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        resetView();
    }

    /**
     * Enables or disables mouse interaction (pan/zoom).
     *
     * @param active true to enable interaction, false to disable
     */
    public void setInteractiveMode(boolean active) {
        this.interactiveMode = active;
        resetView();
        repaint();
    }

    /**
     * Resets the zoom factor and offsets to fit the image entirely within the panel.
     * It calculates the best fit ratio to ensure the image is fully visible without distortion.
     */
    private void resetView() {
        if (image != null && getWidth() > 0 && getHeight() > 0) {
            double ratioX = (double) getWidth() / image.getWidth();
            double ratioY = (double) getHeight() / image.getHeight();
            
            // Choose the smaller ratio to ensure the whole image fits
            this.zoomFactor = Math.min(ratioX, ratioY);
            
            // Do not upscale small images by default beyond 100%
            if (this.zoomFactor > 1.0) {
                this.zoomFactor = 1.0; 
            }

            // Center the image in the panel
            double scaledW = image.getWidth() * zoomFactor;
            double scaledH = image.getHeight() * zoomFactor;
            this.xOffset = (getWidth() - scaledW) / 2.0;
            this.yOffset = (getHeight() - scaledH) / 2.0;
        }
        repaint();
    }

    /**
     * Paints the component content.
     * First, it tiles the background image. Then, it applies the geometric transformations
     * (translation and scaling) to draw the main image.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw the tiled background pattern
        // if (backgroundImage != null) {
        //     int bgW = backgroundImage.getWidth();
        //     int bgH = backgroundImage.getHeight();
        //     if (bgW > 0 && bgH > 0) {
        //         for (int x = 0; x < getWidth(); x += bgW) {
        //             for (int y = 0; y < getHeight(); y += bgH) {
        //                 g.drawImage(backgroundImage, x, y, this);
        //             }
        //         }
        //     }
        // } else {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        // }

        // Draw the main image if available
        if (image != null) {
            // Enable high-quality interpolation for smooth rendering
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Use AffineTransform to combine translation and scaling
            AffineTransform at = new AffineTransform();
            at.translate(xOffset, yOffset);
            at.scale(zoomFactor, zoomFactor);
            
            g2.drawRenderedImage(image, at);
        }
    }

    // =============================================================================================
    // NAMED INNER CLASSES
    // =============================================================================================

    /**
     * Handles all mouse interactions: clicks, drags, and wheel scrolling.
     * This class encapsulates the logic for panning and zooming.
     */
    private class MouseInputHandler extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (!interactiveMode) return;
            lastMousePos = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!interactiveMode || image == null || lastMousePos == null) return;
            
            // Calculate movement delta
            int dx = e.getX() - lastMousePos.x;
            int dy = e.getY() - lastMousePos.y;
            
            // Apply delta to offsets
            xOffset += dx;
            yOffset += dy;
            
            lastMousePos = e.getPoint();
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!interactiveMode || image == null) return;
            
            double oldZoom = zoomFactor;
            double zoomMultiplier = 1.1; // 10% zoom step
            
            // Adjust zoom factor based on scroll direction
            if (e.getWheelRotation() < 0) {
                zoomFactor *= zoomMultiplier; // Zoom In
            } else {
                zoomFactor /= zoomMultiplier; // Zoom Out
            }

            // Enforce zoom limits (1% to 5000%)
            if (zoomFactor < 0.01) zoomFactor = 0.01;
            if (zoomFactor > 50.0) zoomFactor = 50.0;

            // Calculate the new offset to keep the zoom centered on the mouse cursor.
            // The logic shifts the image origin so that the point under the mouse
            // remains stationary relative to the mouse pointer after scaling.
            Point mousePoint = e.getPoint();
            double zoomChange = zoomFactor / oldZoom;
            xOffset = mousePoint.x - (mousePoint.x - xOffset) * zoomChange;
            yOffset = mousePoint.y - (mousePoint.y - yOffset) * zoomChange;
            
            repaint();
        }
    }

    /**
     * Handles component resize events.
     * When the window is resized, we reset the view if not in interactive mode
     * to keep the image centered and fitted.
     */
    private class ResizeHandler extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            if (!interactiveMode) {
                resetView();
            }
        }
    }
}