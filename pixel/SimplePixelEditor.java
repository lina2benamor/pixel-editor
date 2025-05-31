import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class PixelArtEditor extends JFrame {
    private Color currentColor = Color.BLACK;
    private JPanel colorPalette;
    private DrawingPanel drawingPanel;
    private int brushSize = 1; // Default brush size (1x1 pixel)

    public static void main(String[] args) {
        new PixelArtEditor();
    }

    public PixelArtEditor() {
        setTitle("Pixel Art Editor with Brush Sizes");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create drawing panel
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        // Create control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Create color palette panel
        createColorPalette();
        controlPanel.add(colorPalette, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        // Add color picker button
        JButton colorPickerBtn = new JButton("Custom Color");
        colorPickerBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose a color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
            }
        });
        buttonPanel.add(colorPickerBtn);

        // Add brush size controls
        JButton increaseBrushBtn = new JButton("Bigger Brush [+]");
        increaseBrushBtn.addActionListener(e -> {
            brushSize = Math.min(brushSize + 1, 10); // Max 10x10 brush
            updateBrushStatus();
        });
        buttonPanel.add(increaseBrushBtn);

        JButton decreaseBrushBtn = new JButton("Smaller Brush [-]");
        decreaseBrushBtn.addActionListener(e -> {
            brushSize = Math.max(brushSize - 1, 1); // Min 1x1 brush
            updateBrushStatus();
        });
        buttonPanel.add(decreaseBrushBtn);

        // Add brush size status label
        JLabel brushStatus = new JLabel(" Brush: 1x1 ");
        buttonPanel.add(brushStatus);

        // Add save button
        JButton saveBtn = new JButton("Save Image");
        saveBtn.addActionListener(e -> saveImage());
        buttonPanel.add(saveBtn);

        // Add clear button
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> drawingPanel.clearImage());
        buttonPanel.add(clearBtn);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
        
        // Helper method to update brush status
        void updateBrushStatus() {
            brushStatus.setText(" Brush: " + brushSize + "x" + brushSize + " ");
        }
    }

    private void createColorPalette() {
        colorPalette = new JPanel(new FlowLayout());
        colorPalette.setBorder(BorderFactory.createTitledBorder("Colors"));

        Color[] colors = {
            Color.BLACK, Color.WHITE, Color.RED, 
            Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.ORANGE,
            Color.PINK, Color.GRAY, Color.DARK_GRAY
        };

        for (Color color : colors) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(color);
            colorBtn.setPreferredSize(new Dimension(30, 30));
            colorBtn.addActionListener(e -> currentColor = color);
            colorPalette.add(colorBtn);
        }
    }

    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Image");
        fileChooser.setSelectedFile(new File("pixel_art.png"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                fileToSave = new File(filePath + ".png");
            }
            
            try {
                ImageIO.write(drawingPanel.getImage(), "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Image saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class DrawingPanel extends JPanel {
        private BufferedImage image;
        private static final int PIXEL_SIZE = 10;
        private static final int GRID_SIZE = 32;

        public DrawingPanel() {
            image = new BufferedImage(GRID_SIZE, GRID_SIZE, BufferedImage.TYPE_INT_ARGB);
            clearImage();

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    drawBlock(e.getX(), e.getY());
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    drawBlock(e.getX(), e.getY());
                }
            });
        }

        private void drawBlock(int screenX, int screenY) {
            int pixelX = screenX / PIXEL_SIZE;
            int pixelY = screenY / PIXEL_SIZE;

            Graphics2D g = image.createGraphics();
            g.setColor(currentColor);
            
            // Draw a square block of the current brush size
            for (int x = pixelX; x < pixelX + brushSize && x < GRID_SIZE; x++) {
                for (int y = pixelY; y < pixelY + brushSize && y < GRID_SIZE; y++) {
                    if (x >= 0 && y >= 0) {
                        image.setRGB(x, y, currentColor.getRGB());
                    }
                }
            }
            g.dispose();
            repaint();
        }

        public void clearImage() {
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, GRID_SIZE, GRID_SIZE);
            g.dispose();
            repaint();
        }

        public BufferedImage getImage() {
            return image;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Draw the pixel art
            g.drawImage(image.getScaledInstance(
                GRID_SIZE * PIXEL_SIZE, 
                GRID_SIZE * PIXEL_SIZE, 
                Image.SCALE_FAST), 
                0, 0, null);
            
            // Draw grid
            g.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= GRID_SIZE; i++) {
                g.drawLine(i * PIXEL_SIZE, 0, i * PIXEL_SIZE, GRID_SIZE * PIXEL_SIZE);
                g.drawLine(0, i * PIXEL_SIZE, GRID_SIZE * PIXEL_SIZE, i * PIXEL_SIZE);
            }
            
            // Draw brush size preview
            if (brushSize > 1) {
                g.setColor(new Color(0, 0, 0, 50));
                Point mousePos = getMousePosition();
                if (mousePos != null) {
                    int pixelX = (mousePos.x / PIXEL_SIZE) * PIXEL_SIZE;
                    int pixelY = (mousePos.y / PIXEL_SIZE) * PIXEL_SIZE;
                    g.fillRect(pixelX, pixelY, brushSize * PIXEL_SIZE, brushSize * PIXEL_SIZE);
                }
            }
        }
    }
}