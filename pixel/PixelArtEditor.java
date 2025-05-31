import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Stack;

public class PixelArtEditor extends JFrame {
    private DrawArea drawArea;
    private Color currentColor = Color.BLACK;
    private int brushSize = 1;
    private int zoomLevel = 10;
    private int gridSize = 16;
    private boolean showGrid = true;
    private Stack<BufferedImage> undoStack = new Stack<>();
    private Stack<BufferedImage> redoStack = new Stack<>();

    public PixelArtEditor() {
        super("🎮 Pixel Art Editor");
        setupUI();
    }

    private void setupUI() {
        drawArea = new DrawArea();
        setupMenuBar();
        setupToolPanel();
        setupMainLayout();
        
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("New", KeyEvent.VK_N, e -> newCanvas()));
        fileMenu.add(createMenuItem("Open", KeyEvent.VK_O, e -> openImage()));
        fileMenu.add(createMenuItem("Save", KeyEvent.VK_S, e -> saveImage()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Exit", KeyEvent.VK_Q, e -> System.exit(0)));
        
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(createMenuItem("Undo", KeyEvent.VK_Z, e -> undo()));
        editMenu.add(createMenuItem("Redo", KeyEvent.VK_Y, e -> redo()));
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String text, int key, ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.setAccelerator(KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(action);
        return item;
    }

    private void setupToolPanel() {
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        // Color Picker
        JButton colorBtn = new JButton("Color");
        colorBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Color", currentColor);
            if (newColor != null) currentColor = newColor;
        });
        
        // Brush Size
        JComboBox<Integer> sizeCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        sizeCombo.setSelectedItem(brushSize);
        sizeCombo.addActionListener(e -> brushSize = (Integer)sizeCombo.getSelectedItem());
        
        // Grid Controls
        JComboBox<Integer> gridCombo = new JComboBox<>(new Integer[]{8, 16, 32, 64});
        gridCombo.setSelectedItem(gridSize);
        gridCombo.addActionListener(e -> {
            gridSize = (Integer)gridCombo.getSelectedItem();
            drawArea.setGridSize(gridSize);
        });
        
        JCheckBox gridToggle = new JCheckBox("Grid", showGrid);
        gridToggle.addActionListener(e -> {
            showGrid = gridToggle.isSelected();
            drawArea.setShowGrid(showGrid);
        });
        
        // Zoom Controls
        JComboBox<Integer> zoomCombo = new JComboBox<>(new Integer[]{5, 10, 15, 20});
        zoomCombo.setSelectedItem(zoomLevel);
        zoomCombo.addActionListener(e -> {
            zoomLevel = (Integer)zoomCombo.getSelectedItem();
            drawArea.setZoomLevel(zoomLevel);
        });
        
        toolPanel.add(colorBtn);
        toolPanel.add(new JLabel("Brush:"));
        toolPanel.add(sizeCombo);
        toolPanel.add(new JLabel("Grid:"));
        toolPanel.add(gridCombo);
        toolPanel.add(gridToggle);
        toolPanel.add(new JLabel("Zoom:"));
        toolPanel.add(zoomCombo);
        
        add(toolPanel, BorderLayout.NORTH);
    }

    private void setupMainLayout() {
        add(drawArea, BorderLayout.CENTER);
        
        JPanel colorPalette = new JPanel(new GridLayout(1, 10, 2, 2));
        Color[] defaultColors = {
            Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK
        };
        
        for (Color color : defaultColors) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(color);
            colorBtn.setPreferredSize(new Dimension(30, 30));
            colorBtn.addActionListener(e -> currentColor = color);
            colorPalette.add(colorBtn);
        }
        
        add(colorPalette, BorderLayout.SOUTH);
    }

    private void newCanvas() {
        String input = JOptionPane.showInputDialog(this, "Canvas Size (width x height):", "32x32");
        if (input != null) {
            try {
                String[] dimensions = input.split("x");
                int width = Integer.parseInt(dimensions[0].trim());
                int height = Integer.parseInt(dimensions[1].trim());
                drawArea.createNewCanvas(width, height);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid format! Use 'width x height'");
            }
        }
    }

    private void openImage() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage img = ImageIO.read(fc.getSelectedFile());
                drawArea.setImage(img);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading image");
            }
        }
    }

    private void saveImage() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("pixel_art.png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fc.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getParentFile(), file.getName() + ".png");
                }
                ImageIO.write(drawArea.getImage(), "png", file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image");
            }
        }
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(drawArea.getImage());
            drawArea.setImage(undoStack.pop());
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(drawArea.getImage());
            drawArea.setImage(redoStack.pop());
        }
    }

    private void saveState() {
        undoStack.push(copyImage(drawArea.getImage()));
        redoStack.clear();
    }

    private BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    class DrawArea extends JPanel {
        private BufferedImage image;
        private BufferedImage displayImage;
        private int width = 32;
        private int height = 32;

        public DrawArea() {
            setBackground(Color.DARK_GRAY);
            createNewCanvas(width, height);
            
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    saveState();
                    drawPixel(e.getX(), e.getY());
                }
            });
            
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    drawPixel(e.getX(), e.getY());
                }
            });
        }
        
        public void createNewCanvas(int w, int h) {
            width = w;
            height = h;
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.dispose();
            updateDisplayImage();
            repaint();
        }
        
        private void updateDisplayImage() {
            displayImage = new BufferedImage(
                width * zoomLevel, 
                height * zoomLevel, 
                BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g = displayImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, displayImage.getWidth(), displayImage.getHeight());
            
            // Draw pixels
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(image.getScaledInstance(
                width * zoomLevel, 
                height * zoomLevel, 
                Image.SCALE_REPLICATE), 0, 0, null);
            
            // Draw grid
            if (showGrid) {
                g.setColor(new Color(200, 200, 200, 100));
                for (int x = 0; x <= width; x++) {
                    g.drawLine(x * zoomLevel, 0, x * zoomLevel, height * zoomLevel);
                }
                for (int y = 0; y <= height; y++) {
                    g.drawLine(0, y * zoomLevel, width * zoomLevel, y * zoomLevel);
                }
            }
            
            g.dispose();
        }
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (displayImage != null) {
                g.drawImage(displayImage, 
                    (getWidth() - displayImage.getWidth()) / 2,
                    (getHeight() - displayImage.getHeight()) / 2, 
                    null);
            }
        }
        
        private void drawPixel(int screenX, int screenY) {
            // Convert screen coordinates to image coordinates
            int imgX = (screenX - (getWidth() - displayImage.getWidth()) / 2) / zoomLevel;
            int imgY = (screenY - (getHeight() - displayImage.getHeight()) / 2) / zoomLevel;
            
            if (imgX >= 0 && imgX < width && imgY >= 0 && imgY < height) {
                Graphics2D g = image.createGraphics();
                g.setColor(currentColor);
                
                if (brushSize == 1) {
                    image.setRGB(imgX, imgY, currentColor.getRGB());
                } else {
                    g.fillRect(
                        imgX - brushSize/2, 
                        imgY - brushSize/2, 
                        brushSize, brushSize);
                }
                
                g.dispose();
                updateDisplayImage();
                repaint();
            }
        }
        
        public void setImage(BufferedImage img) {
            this.image = img;
            this.width = img.getWidth();
            this.height = img.getHeight();
            updateDisplayImage();
            repaint();
        }
        
        public BufferedImage getImage() {
            return image;
        }
        
        public void setGridSize(int size) {
            this.gridSize = size;
            updateDisplayImage();
            repaint();
        }
        
        public void setShowGrid(boolean show) {
            this.showGrid = show;
            updateDisplayImage();
            repaint();
        }
        
        public void setZoomLevel(int zoom) {
            this.zoomLevel = zoom;
            updateDisplayImage();
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PixelArtEditor());
    }
}