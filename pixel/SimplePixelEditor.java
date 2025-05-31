import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class SimplePixelEditor extends JFrame {
    private Color currentColor = Color.BLACK;
    private JPanel colorPalette;

    public static void main(String[] args) {
        new SimplePixelEditor();
    }

    public SimplePixelEditor() {
        setTitle("Pixel Art Editor with Colors");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create drawing panel
        DrawingPanel drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        // Create color palette panel
        createColorPalette();
        add(colorPalette, BorderLayout.SOUTH);

        // Add color picker button
        JButton colorPickerBtn = new JButton("Custom Color");
        colorPickerBtn.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose a color", currentColor);
            if (newColor != null) {
                currentColor = newColor;
            }
        });
        add(colorPickerBtn, BorderLayout.NORTH);

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

        private void clearImage() {
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, GRID_SIZE, GRID_SIZE);
            g.dispose();
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