import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class SimplePixelEditor extends JFrame {
    private Color currentColor = Color.BLACK;
    private JPanel colorPalette;
    private DrawingPanel drawingPanel;

    public static void main(String[] args) {
        new SimplePixelEditor();
    }

    public SimplePixelEditor() {
        setTitle("Pixel Art Editor with Save");
        setSize(700, 550);
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
        
        // Set default filename
        fileChooser.setSelectedFile(new File("pixel_art.png"));
        
        // Show save dialog
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .png extension
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                fileToSave = new File(filePath + ".png");
            }
            
            try {
                // Save the image
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
                    drawPixel(e.getX(), e.getY());
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    drawPixel(e.getX(), e.getY());
                }
            });
        }

        private void drawPixel(int screenX, int screenY) {
            int pixelX = screenX / PIXEL_SIZE;
            int pixelY = screenY / PIXEL_SIZE;

            if (pixelX >= 0 && pixelX < GRID_SIZE && pixelY >= 0 && pixelY < GRID_SIZE) {
                image.setRGB(pixelX, pixelY, currentColor.getRGB());
                repaint();
            }
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
        }
    }
}