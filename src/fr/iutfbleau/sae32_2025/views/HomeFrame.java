package fr.iutfbleau.sae32_2025.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import fr.iutfbleau.sae32_2025.controllers.Converter;
import fr.iutfbleau.sae32_2025.controllers.Decoder;

/**
 * The <code>HomeFrame</code> class represents the main welcome window (Dashboard) of the application.
 * <p>
 * It serves as the central hub, allowing the user to navigate between the two main modules:
 * <ul>
 * <li><b>The Converter:</b> To compress images into the PIF format.</li>
 * <li><b>The Viewer:</b> To decode and view PIF files.</li>
 * </ul>
 * </p>
 * <p>
 * The interface is designed with a modern aesthetic, featuring a responsive background and flat-design buttons.
 * </p>
 *
 * @version 1.4
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class HomeFrame extends JFrame {

    /**
     * The image used as the window background. Can be null if the resource is missing.
     */
    private Image backgroundImage;

    /**
     * Constructs the main application window and initializes the graphical components.
     * <p>
     * It configures the window properties (size, icon, location), loads the background resources,
     * and organizes the layout with the logo and navigation buttons.
     * </p>
     */
    public HomeFrame() {
        super("PIF - Primitive Image Format");

        // Set the minimum window dimensions to ensure usability
        Dimension minSize = new Dimension(720, 480);
        this.setSize(minSize);
        this.setMinimumSize(minSize);
        this.setPreferredSize(minSize);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null); // Center on screen
        AppTheme.setAppIcon(this);
        
        // Attach a listener to enforce size constraints dynamically
        this.addComponentListener(new MinSizeEnforcer(minSize));

        // Attempt to load the background image from resources
        // this.backgroundImage = AppTheme.loadAssetImage("background.png");
        // if (this.backgroundImage == null) {
        //     this.backgroundImage = AppTheme.loadAssetImage("icons/background.png");
        // }

        // Initialize the custom background panel
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout()); // Centers content
        this.setContentPane(mainPanel);

        // Container for vertical organization of UI elements (Logo -> Title -> Buttons)
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false); // Transparent to show background

        // --- Logo Section ---
        Image iconImg = AppTheme.loadAssetImage("icons/icon.png");
        JLabel iconLabel;
        if (iconImg != null) {
            Image scaledIcon = iconImg.getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            iconLabel = new JLabel(new ImageIcon(scaledIcon));
        } else {
            // Fallback text if icon is missing
            iconLabel = new JLabel("PIF Project");
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
            iconLabel.setForeground(Color.WHITE);
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Buttons Section ---
        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0)); 
        buttonRow.setOpaque(false);

        JButton btnConverter = createModernButton("Converter (Encoder)");
        JButton btnDecoder = createModernButton("Viewer (Decoder)");

        // Attach navigation logic
        btnConverter.addActionListener(new ConverterLauncher());
        btnDecoder.addActionListener(new DecoderLauncher());

        buttonRow.add(btnConverter);
        buttonRow.add(btnDecoder);

        // --- Assembly with spacing ---
        contentContainer.add(iconLabel);
        contentContainer.add(Box.createVerticalStrut(20));
        
        JLabel titleLabel = new JLabel("PIF Multimedia Tool");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentContainer.add(titleLabel);
        
        contentContainer.add(Box.createVerticalStrut(50)); 
        contentContainer.add(buttonRow);

        mainPanel.add(contentContainer);
    }

    /**
     * Helper method to create a stylized button conforming to the "Modern Flat" design.
     *
     * @param text The label of the button.
     * @return A configured JButton instance.
     */
    private JButton createModernButton(String text) {
        JButton btn = new JButton(text);
        
        // MODERN FLAT COLORS (Azure Blue)
        Color azureNormal = new Color(0, 120, 215); 
        Color azureHover = new Color(0, 90, 180);   
        
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(azureNormal);
        
        // Remove standard 3D borders and focus rings for a cleaner look
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effect interaction
        btn.addMouseListener(new ButtonHoverListener(btn, azureNormal, azureHover));

        return btn;
    }

    // =============================================================================================
    // NAMED INNER CLASSES
    // =============================================================================================

    /**
     * Custom panel responsible for rendering the background.
     * It draws the image if available, or a gradient otherwise, covering the entire area.
     */
    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // if (backgroundImage != null) {
            //     // Draw the image and darken it slightly for better text readability
            //     g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            //     g.setColor(new Color(0, 0, 0, 100)); // Semi-transparent black overlay
            //     g.fillRect(0, 0, getWidth(), getHeight());
            // } else {
                // Modern Gradient Background fallback
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 40, 60), 0, getHeight(), new Color(20, 20, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            // }
        }
    }

    /**
     * Action listener to switch to the Converter module.
     * It closes the current window and starts the Converter controller.
     */
    private class ConverterLauncher implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose(); // Close HomeFrame
            Converter.main(new String[]{});
        }
    }

    /**
     * Action listener to switch to the Decoder (Viewer) module.
     * It closes the current window and starts the Decoder controller.
     */
    private class DecoderLauncher implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose(); // Close HomeFrame
            Decoder.main(new String[]{});
        }
    }

    /**
     * Mouse listener to handle visual feedback (hover effects) on buttons.
     * Changes the background color when the mouse enters or exits the component.
     */
    private static class ButtonHoverListener extends MouseAdapter {
        private final JButton button;
        private final Color normalColor;
        private final Color hoverColor;

        public ButtonHoverListener(JButton btn, Color normalColor, Color hoverColor) {
            this.button = btn;
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(hoverColor);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(normalColor);
        }
    }

    /**
     * Component listener to enforce a strict minimum window size.
     * If the user tries to resize the window smaller than allowed, it snaps back to the minimum.
     */
    private class MinSizeEnforcer extends ComponentAdapter {
        private final Dimension minSize;
        
        public MinSizeEnforcer(Dimension minSize) { 
            this.minSize = minSize; 
        }
        
        @Override
        public void componentResized(ComponentEvent e) {
            int width = getWidth();
            int height = getHeight();
            boolean resizeNeeded = false;
            
            if (width < minSize.width) { 
                width = minSize.width; 
                resizeNeeded = true; 
            }
            if (height < minSize.height) { 
                height = minSize.height; 
                resizeNeeded = true; 
            }
            
            if (resizeNeeded) { 
                setSize(width, height); 
            }
        }
    }
}