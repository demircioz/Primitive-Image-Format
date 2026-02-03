package fr.iutfbleau.sae32_2025.views;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.List;

import fr.iutfbleau.sae32_2025.controllers.Converter;

/**
 * The <code>ConverterFrame</code> class represents the main graphical interface for the PIF Converter (Encoder).
 * <p>
 * This frame is designed to provide a comprehensive view of the image compression process.
 * It features a split-pane layout:
 * <ul>
 * <li><b>Top Section:</b> Displays the loaded image using a zoomable and pannable panel.</li>
 * <li><b>Bottom Section:</b> Displays statistical data (Huffman tables) for each color channel (Red, Green, Blue).</li>
 * </ul>
 * </p>
 *
 * @version 1.2
 * @author Canpolat DEMIRCI--ÖZMEN, Maxime ELIOT, Luka PLOUVIER
 */
public class ConverterFrame extends JFrame {

    /** Tabbed pane container for the frequency and code tables of each color channel. */
    private JTabbedPane tableTabs;
    
    /** Custom panel responsible for rendering the image with interactive controls. */
    private PanZoomImagePanel imagePanel;
    
    /** Menu item to initiate the PIF conversion process. It is disabled until an image is loaded. */
    private JMenuItem menuItemConvert;
    
    /** Container panel for the image view, used to apply borders and titles. */
    private JPanel imageContainer;
    
    /** Split pane allowing the user to resize the image preview relative to the data tables. */
    private JSplitPane splitPane;
    
    /** Checkbox menu item to toggle the visibility of the statistical tables. */
    private JCheckBoxMenuItem itemToggleTables;
    
    /** Status bar label located at the bottom of the frame to display application messages. */
    private JLabel statusBar;

    /**
     * Constructs the Converter Frame and initializes all GUI components.
     *
     * @param actionOpen    The ActionListener provided by the controller to handle the "Open File" event.
     * @param actionConvert The ActionListener provided by the controller to handle the "Convert" event.
     */
    public ConverterFrame(ActionListener actionOpen, ActionListener actionConvert) {
        this.setTitle("Primitive Image Format - PIF Converter");
        this.setSize(1280, 720);
        this.setMinimumSize(new Dimension(1280, 720)); 
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        
        // Sets the application icon using the centralized resource manager
        AppTheme.setAppIcon(this);
        
        // Attach the custom Drag & Drop handler (Defined as a named inner class below)
        this.setTransferHandler(new FileDropHandler());

        // --- Image Panel Setup ---
        this.imagePanel = new PanZoomImagePanel();
        this.imagePanel.setInteractiveMode(false); // Interactive mode is disabled when tables are visible

        this.imageContainer = new JPanel(new BorderLayout());
        updateImageTitle(null); 
        this.imageContainer.add(imagePanel, BorderLayout.CENTER);

        // --- Data Tables Setup ---
        this.tableTabs = new JTabbedPane();
        this.tableTabs.setPreferredSize(new Dimension(1280, 300)); 
        this.tableTabs.setFont(AppTheme.FONT_ROBOTO_BOLD);

        // --- Layout Setup (using Split Pane) ---
        this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imageContainer, tableTabs);
        this.splitPane.setResizeWeight(1.0); // Allocates extra space to the image panel upon resizing
        this.splitPane.setOneTouchExpandable(true);
        
        this.add(splitPane, BorderLayout.CENTER);

        // --- Status Bar Setup ---
        statusBar = new JLabel(" Ready");
        statusBar.setFont(AppTheme.FONT_ROBOTO); 
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        this.add(statusBar, BorderLayout.SOUTH);

        // --- Menu Bar Configuration ---
        configureMenuBar(actionOpen, actionConvert);

        // --- Initial State ---
        // Post a Runnable to the Event Dispatch Thread to set the initial layout state
        new LayoutInitializer().run();
    }
    
    /**
     * Configures the menu bar with File and View menus.
     *
     * @param actionOpen    The external listener for opening files.
     * @param actionConvert The external listener for converting files.
     */
    private void configureMenuBar(ActionListener actionOpen, ActionListener actionConvert) {
        JMenuBar menuBar = new JMenuBar();
        
        // --- File Menu ---
        JMenu menuFile = new JMenu("File");
        menuFile.setFont(AppTheme.FONT_ROBOTO);

        JMenuItem itemOpen = new JMenuItem("Open file...");
        itemOpen.setFont(AppTheme.FONT_ROBOTO);
        itemOpen.addActionListener(actionOpen);
        
        menuItemConvert = new JMenuItem("Convert to .PIF");
        menuItemConvert.setFont(AppTheme.FONT_ROBOTO_BOLD); 
        menuItemConvert.addActionListener(actionConvert);
        menuItemConvert.setEnabled(false); // Disabled until an image is loaded

        menuFile.add(itemOpen);
        menuFile.addSeparator(); 
        menuFile.add(menuItemConvert);
        menuBar.add(menuFile);

        // --- View Menu ---
        JMenu menuView = new JMenu("View");
        menuView.setFont(AppTheme.FONT_ROBOTO);
        
        itemToggleTables = new JCheckBoxMenuItem("Show Tables", true);
        itemToggleTables.setFont(AppTheme.FONT_ROBOTO);
        
        // Attach the named listener for toggling tables
        itemToggleTables.addActionListener(new ToggleTableListener());
        
        menuView.add(itemToggleTables);
        menuBar.add(menuView);
        
        this.setJMenuBar(menuBar);
    }

    /**
     * Updates the text message displayed in the status bar.
     *
     * @param text The message string to display.
     */
    public void setStatus(String text) {
        statusBar.setText(" " + text);
    }

    /**
     * Toggles the visibility of the data tables panel.
     * <p>
     * When tables are hidden, the image panel expands to fill the window and interactive mode (pan/zoom) is enabled.
     * When tables are shown, the image panel is restricted and interactive mode is disabled to prevent conflicts.
     * </p>
     */
    private void toggleTablesVisibility() {
        boolean showTables = itemToggleTables.isSelected();
        
        if (showTables) {
            imagePanel.setInteractiveMode(false); 
            tableTabs.setVisible(true);
            splitPane.setDividerLocation(-1); // Resets divider to the component's preferred size
            splitPane.setEnabled(true);
        } else {
            imagePanel.setInteractiveMode(true); 
            tableTabs.setVisible(false);
            // Moves the divider to the very bottom to maximize the top component
            splitPane.setDividerLocation(getHeight() - splitPane.getInsets().bottom - splitPane.getDividerSize()); 
            splitPane.setEnabled(false);
        }
        
        this.revalidate();
        this.repaint();
    }

    /**
     * Updates the border title of the image container to display image resolution.
     *
     * @param img The loaded BufferedImage, or null if no image is loaded.
     */
    private void updateImageTitle(BufferedImage img) {
        String title = "Original Image Preview";
        if (img != null) {
            title += ". Resolution: [" + img.getWidth() + "x" + img.getHeight() + "]";
        }
        
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title, TitledBorder.CENTER, TitledBorder.TOP);
        border.setTitleFont(AppTheme.FONT_ROBOTO_BOLD); 
        imageContainer.setBorder(border);
    }

    /**
     * Updates the GUI with new image data and generated Huffman statistics.
     * This method is the main entry point for the Controller to update the View.
     *
     * @param image   The loaded image to display.
     * @param freqR   Frequency map for the Red channel.
     * @param codeR   Huffman codes for the Red channel.
     * @param canonR  Canonical codes for the Red channel.
     * @param freqG   Frequency map for the Green channel.
     * @param codeG   Huffman codes for the Green channel.
     * @param canonG  Canonical codes for the Green channel.
     * @param freqB   Frequency map for the Blue channel.
     * @param codeB   Huffman codes for the Blue channel.
     * @param canonB  Canonical codes for the Blue channel.
     */
    public void updateData(BufferedImage image,
                           Map<Integer, Integer> freqR, Map<Character, String> codeR, Map<Character, String> canonR,
                           Map<Integer, Integer> freqG, Map<Character, String> codeG, Map<Character, String> canonG,
                           Map<Integer, Integer> freqB, Map<Character, String> codeB, Map<Character, String> canonB) {
        
        imagePanel.setImage(image);
        updateImageTitle(image);
        menuItemConvert.setEnabled(true);

        // Clear existing tabs and rebuild them with new data
        tableTabs.removeAll();
        tableTabs.addTab("Red", createTablePanel(freqR, codeR, canonR));
        tableTabs.addTab("Green", createTablePanel(freqG, codeG, canonG));
        tableTabs.addTab("Blue", createTablePanel(freqB, codeB, canonB));

        // Assign colors to tabs for visual identification
        tableTabs.setForegroundAt(0, Color.RED);
        tableTabs.setForegroundAt(1, new Color(0, 128, 0)); // Dark Green
        tableTabs.setForegroundAt(2, Color.BLUE);
        
        // Refresh the view state
        toggleTablesVisibility();
        this.revalidate();
        this.repaint();
    }

    /**
     * Creates a scrollable panel containing a JTable for displaying channel statistics.
     *
     * @param freq   Map of pixel values to their frequency count.
     * @param codes  Map of pixel values to their initial Huffman codes.
     * @param canons Map of pixel values to their canonical Huffman codes.
     * @return A JScrollPane containing the populated JTable.
     */
    private JScrollPane createTablePanel(Map<Integer, Integer> freq, Map<Character, String> codes, Map<Character, String> canons) {
        String[] columns = {"Value (0-255)", "Frequency", "Initial Code", "Canonical Code", "Length"};
        
        // Use the named inner class for the table model (read-only)
        ReadOnlyTableModel model = new ReadOnlyTableModel(columns, 0);

        // Populate the model with data for all possible byte values (0-255)
        for (int i = 0; i < 256; i++) {
            model.addRow(new Object[]{
                i, 
                freq.getOrDefault(i, 0), 
                codes.getOrDefault((char) i, ""), 
                canons.getOrDefault((char) i, ""),
                canons.getOrDefault((char) i, "").length()
            });
        }
        
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true); // Enable column sorting
        table.setFont(AppTheme.FONT_ROBOTO);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_ROBOTO_BOLD);
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.setRowHeight(25); 
        table.setShowVerticalLines(false); 
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Apply the custom renderer for styling
        CustomTableRenderer renderer = new CustomTableRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setCellRenderer(renderer);
            
            // Set specific widths for columns
            if (i == 0) col.setMinWidth(100); 
            else if (i == 1) col.setMinWidth(100); 
            else if (i == 2) col.setMinWidth(200); 
            else if (i == 3) col.setMinWidth(200); 
            else if (i == 4) col.setMinWidth(80);  
        }

        return new JScrollPane(table);
    }

    // =================================================================================
    // NAMED INNER CLASSES
    // =================================================================================

    /**
     * Handles the Drag and Drop functionality for files.
     * It extends TransferHandler to process file imports when dropped onto the frame.
     */
    private class FileDropHandler extends TransferHandler {
        
        @Override
        public boolean canImport(TransferSupport support) {
            // Only accept file list data flavor
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            try {
                // Extract the list of files from the transferable data
                List<File> files = (List<File>) support.getTransferable()
                    .getTransferData(DataFlavor.javaFileListFlavor);
                
                if (!files.isEmpty()) {
                    // Pass the first file to the controller's logic
                    Converter.openImageDragDrop(files.get(0));
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * An ActionListener implementation for handling the "Toggle Tables" menu item.
     */
    private class ToggleTableListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            toggleTablesVisibility();
        }
    }

    /**
     * A Runnable implementation used to initialize the layout state on the Swing Event Dispatch Thread.
     */
    private class LayoutInitializer implements Runnable {
        @Override
        public void run() {
            toggleTablesVisibility();
        }
    }

    /**
     * A custom DefaultTableModel that makes all cells non-editable (read-only).
     */
    private static class ReadOnlyTableModel extends DefaultTableModel {
        public ReadOnlyTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Prevent user editing
        }
    }

    /**
     * A custom DefaultTableCellRenderer to apply specific styles to table cells.
     * It centers text, alternates row background colors, and uses a monospace font for binary data columns.
     */
    private static class CustomTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(SwingConstants.CENTER);
            
            // Apply Monospace font for "Initial Code" (col 2) and "Canonical Code" (col 3)
            if (column == 2 || column == 3) {
                c.setFont(AppTheme.FONT_MONOSPACE); 
                c.setForeground(new Color(0, 100, 0)); // Color for binary data ( like Matrix :) )
            } else {
                c.setFont(AppTheme.FONT_ROBOTO);
                c.setForeground(Color.BLACK);
            }

            // Alternating row background colors for readability
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
            }
            return c;
        }
    }
}