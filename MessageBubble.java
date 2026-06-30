import javax.swing.*;
import java.awt.*;

public class MessageBubble extends JPanel {

    public MessageBubble(String message, String timestamp, boolean isSender,
            Color senderColor, Color receiverColor, Color textColor, Color timestampColor) {
        setLayout(new FlowLayout(isSender ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Bubble panel with rounded corners effect
        JPanel bubblePanel = new RoundedPanel(isSender ? senderColor : receiverColor, 15);
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setPreferredSize(new Dimension(350, 80));

        // Message label
        JLabel messageLabel = new JLabel("<html><p style='width: 300px; margin: 0;'>" + message + "</p></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(textColor);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 5, 12));

        // Timestamp label
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        timeLabel.setForeground(timestampColor);
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));

        bubblePanel.add(messageLabel, BorderLayout.CENTER);
        bubblePanel.add(timeLabel, BorderLayout.SOUTH);

        add(bubblePanel);
    }

    // Custom panel for rounded corners
    private static class RoundedPanel extends JPanel {
        private int radius;
        private Color backgroundColor;

        public RoundedPanel(Color bg, int radius) {
            this.backgroundColor = bg;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(backgroundColor);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }
}
