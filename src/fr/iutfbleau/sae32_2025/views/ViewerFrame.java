package fr.iutfbleau.sae32_2025.views;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

import fr.iutfbleau.sae32_2025.controllers.Decoder;

/**
 * The <code>ViewerFrame</code> class represents the main window for the PIF Viewer (Decoder).
 * It provides a graphical interface to open, view, and navigate (pan/zoom) through images.
 * This class strictly adheres to Swing architecture without using lambda expressions or anonymous classes.
 *
 * @version 1.1
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class ViewerFrame extends JFrame {

    /**
     * Menu item to trigger the file selection dialog.
     */
    private JMenuItem selectionItem;

    /**
     * Custom panel responsible for rendering the image and handling mouse interactions.
     */
    private ImagePanel contentPanel;
    
    /**
     * Constructs the main viewer window.
     * It initializes the layout, menu bar, and the image display panel.
     */
    public ViewerFrame() {
        super("PIF - Primitive Image Format - Viewer (Decoder)");

        // Enforce a minimum size to ensure usability
        this.setMinimumSize(new Dimension(500, 500));
        this.setSize(500, 500); 
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); // Centers the window on screen
        
        // Apply the application icon using the centralized theme manager
        AppTheme.setAppIcon(this);

        // Menu Construction
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(AppTheme.FONT_ROBOTO);
        
        this.selectionItem = new JMenuItem("Open file...");
        this.selectionItem.setFont(AppTheme.FONT_ROBOTO);
        
        // REFACTORING: Replaced lambda with a named inner class instance
        this.selectionItem.addActionListener(new FileOpenListener());

        fileMenu.add(this.selectionItem);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

        // Main content area
        this.contentPanel = new ImagePanel();
        this.getContentPane().add(this.contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Helper method to handle the file choosing process.
     * It configures filters for PIF and standard images, then delegates loading to the controller or ImageIO.
     */
    private void openFileChooser() {
        JFileChooser chooser = new JFileChooser(new File(".")); 
        
        FileNameExtensionFilter pifFilter = new FileNameExtensionFilter("PIF Files (.pif)", "pif");
        FileNameExtensionFilter imgFilter = new FileNameExtensionFilter("Images (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif");
        
        chooser.addChoosableFileFilter(pifFilter);
        chooser.addChoosableFileFilter(imgFilter);
        chooser.setFileFilter(pifFilter); // Default to PIF

        int returnValue = chooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                BufferedImage img = null;
                String path = selectedFile.getAbsolutePath().toLowerCase();

                // Dispatch loading logic based on file extension
                if (path.endsWith(".pif")) {
                    img = Decoder.openPifImage(selectedFile);
                } else {
                    img = ImageIO.read(selectedFile);
                }

                if (img != null) {
                    this.displayImage(img);
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to read file (Unknown format or empty).");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
            }
        }
    }

    /**
     * Updates the view to display the provided image.
     * It also resizes the window intelligently to fit the image within the screen boundaries.
     *
     * @param image the image to display
     */
    public void displayImage(BufferedImage image) {
        this.contentPanel.setImage(image);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int marginW = 40; 
        int marginH = 80;
        int targetW = image.getWidth() + marginW;
        int targetH = image.getHeight() + marginH;

        // Window size is between 500x500 and the screen size
        int finalW = Math.max(500, Math.min(targetW, screenSize.width));
        int finalH = Math.max(500, Math.min(targetH, screenSize.height - 50));

        this.setSize(finalW, finalH);
        this.setLocationRelativeTo(null);
        
        this.setVisible(true);
        this.contentPanel.repaint();
    }

    /**
     * Named inner class to handle the "Open file" menu action.
     * Replaces the previous lambda expression.
     */
    private class FileOpenListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            openFileChooser();
        }
    }

    /**
     * Inner class responsible for rendering the image and managing zoom/pan operations.
     * It extends JPanel to perform custom painting via paintComponent.
     */
    private class ImagePanel extends JPanel {
        
        private BufferedImage image;
        private double zoomFactor = 1.0;
        private double xOffset = 0;
        private double yOffset = 0;
        private Point lastMousePos;

        /**
         * Initializes the panel, sets the background color, and attaches input listeners.
         */
        public ImagePanel() {
            // Set background color to Dark Gray (same as Converter)
            this.setBackground(Color.DARK_GRAY);

            // MouseAdapter 
            MouseNavigationHandler navigationHandler = new MouseNavigationHandler();
            this.addMouseListener(navigationHandler);
            this.addMouseMotionListener(navigationHandler);
            this.addMouseWheelListener(navigationHandler);
        }

        /**
         * Sets a new image to display and resets the zoom/pan state.
         *
         * @param image the new image to render
         */
        public void setImage(BufferedImage image) {
            this.image = image;
            this.zoomFactor = 1.0; 
            this.xOffset = 0;
            this.yOffset = 0;
            repaint();
        }

        /**
         * Custom painting method.
         * It draws the background first, then the image with the applied transformations (zoom, pan).
         *
         * @param g the Graphics context
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Draw solid background
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw the main image with transformations
            if (image != null) {
                // Enable "bilinear interpolation" for smoother cause we had some annoying issues with pixelated zooming
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                double scaledW = image.getWidth() * zoomFactor;
                double scaledH = image.getHeight() * zoomFactor;
                
                double drawX = xOffset;
                double drawY = yOffset;

                // Center the image if it is smaller than the panel
                if (scaledW < getWidth()) {
                    drawX = (getWidth() - scaledW) / 2.0;
                    xOffset = drawX; 
                }
                if (scaledH < getHeight()) {
                    drawY = (getHeight() - scaledH) / 2.0;
                    yOffset = drawY;
                }
                
                // Use AffineTransform to apply scaling and translation efficiently
                AffineTransform at = new AffineTransform();
                at.translate(drawX, drawY);
                at.scale(zoomFactor, zoomFactor);
                g2.drawRenderedImage(image, at);
            }
        }

        /**
         * Named inner class to handle mouse input for Panning and Zooming.
         * Replaces the previous anonymous MouseAdapter.
         */
        private class MouseNavigationHandler extends MouseAdapter {
            
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (image != null && lastMousePos != null) {
                    // Calculate the distance moved
                    int dx = e.getX() - lastMousePos.x;
                    int dy = e.getY() - lastMousePos.y;
                    
                    // Update offset
                    xOffset += dx;
                    yOffset += dy;
                    
                    lastMousePos = e.getPoint();
                    repaint(); // Trigger redraw with new position
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (image != null) {
                    double oldZoom = zoomFactor;
                    double zoomMultiplier = 1.1;
                    
                    // Determine zoom direction
                    if (e.getWheelRotation() < 0) {
                        zoomFactor *= zoomMultiplier; // Zoom In
                    } else {
                        zoomFactor /= zoomMultiplier; // Zoom Out
                    }

                    // Clamp zoom values to prevent extreme scaling
                    if (zoomFactor < 0.01) zoomFactor = 0.01;
                    if (zoomFactor > 50.0) zoomFactor = 50.0;

                    // Adjust offset to zoom towards the mouse pointer
                    Point mousePoint = e.getPoint();
                    double zoomChange = zoomFactor / oldZoom;
                    xOffset = mousePoint.x - (mousePoint.x - xOffset) * zoomChange;
                    yOffset = mousePoint.y - (mousePoint.y - yOffset) * zoomChange;
                    
                    repaint(); // Trigger redraw with new scale
                }
            }
        }
    }
}