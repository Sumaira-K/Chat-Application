import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientGUI {
    private JFrame frame;
    private JPanel chatPanel;
    private JTextField messageField;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String username;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 🔥 Typing label
    private JLabel typingLabel;

    // 🔥 Debounce timer
    private Timer typingTimer;

    // 🔥 Timer to clear typing indicator after inactivity
    private Timer clearTypingTimer;

    // Color Palette from WhatsApp-like UI
    private static final Color DARK_BG = new Color(26, 27, 46); // Dark background
    private static final Color SENDER_COLOR = new Color(255, 107, 107); // Coral/Salmon (user messages)
    private static final Color RECEIVER_COLOR = new Color(156, 39, 176); // Purple (others messages)
    private static final Color TEXT_COLOR = new Color(255, 255, 255); // White text
    private static final Color TIMESTAMP_COLOR = new Color(200, 200, 200); // Light gray timestamps
    private static final Color INPUT_BG = new Color(40, 45, 70); // Input background

    // ✅ UPDATED CONSTRUCTOR (no popup)
    public ClientGUI(String usernameInput) {
        this.username = usernameInput;

        frame = new JFrame("Chat App - " + username);
        frame.setLayout(new BorderLayout());
        frame.setBackground(DARK_BG);

        // LEFT PANEL - Online Users
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(180, 0));
        leftPanel.setBackground(new Color(35, 36, 60));
        leftPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 100), 1));

        JLabel usersLabel = new JLabel("👥 Online Users");
        usersLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        usersLabel.setForeground(TEXT_COLOR);
        usersLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        usersLabel.setBackground(new Color(50, 52, 80));
        usersLabel.setOpaque(true);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(35, 36, 60));
        userList.setForeground(TEXT_COLOR);
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userList.setSelectionBackground(RECEIVER_COLOR);
        userList.setCellRenderer(new UserListCellRenderer());

        leftPanel.add(usersLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(DARK_BG);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);

        // HEADER PANEL
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(50, 52, 80));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 65, 100)));
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel chatTitle = new JLabel("💬 " + username);
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chatTitle.setForeground(TEXT_COLOR);
        chatTitle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Typing label
        typingLabel = new JLabel("");
        typingLabel.setForeground(TIMESTAMP_COLOR);
        typingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        typingLabel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        headerPanel.add(chatTitle, BorderLayout.NORTH);
        headerPanel.add(typingLabel, BorderLayout.SOUTH);

        rightPanel.add(headerPanel, BorderLayout.NORTH);

        // CHAT PANEL
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(DARK_BG);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBackground(DARK_BG);
        scrollPane.getViewport().setBackground(DARK_BG);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setBackground(new Color(50, 52, 80));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = RECEIVER_COLOR;
            }
        });

        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // INPUT PANEL
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(INPUT_BG);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        messageField = new JTextField("Type a message...");
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageField.setBackground(new Color(60, 65, 100));
        messageField.setForeground(TEXT_COLOR);
        messageField.setCaretColor(TEXT_COLOR);
        messageField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Remove placeholder text on focus
        messageField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (messageField.getText().equals("Type a message...")) {
                    messageField.setText("");
                    messageField.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (messageField.getText().isEmpty()) {
                    messageField.setForeground(TIMESTAMP_COLOR);
                    messageField.setText("Type a message...");
                }
            }
        });

        JButton sendButton = new JButton("📤");
        sendButton.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton.setBackground(SENDER_COLOR);
        sendButton.setForeground(TEXT_COLOR);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        rightPanel.add(inputPanel, BorderLayout.SOUTH);

        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectToServer();

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        // 🔥 Typing detection with debounce
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (typingTimer != null) {
                    typingTimer.stop();
                }

                typingTimer = new Timer(300, ev -> {
                    String text = messageField.getText();
                    if (!text.isEmpty() && !text.equals("Type a message...")) {
                        out.println("[TYPING]" + username + ":" + text);
                    }
                });

                typingTimer.setRepeats(false);
                typingTimer.start();
            }
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 1234);

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("[USERNAME]" + username);

                String msg;
                while ((msg = in.readLine()) != null) {
                    final String message = msg;

                    SwingUtilities.invokeLater(() -> {

                        if (message.startsWith("[USERS]")) {
                            updateUserList(message.substring(7));
                        }

                        // HANDLE TYPING
                        else if (message.startsWith("[TYPING]")) {
                            String data = message.substring(8);
                            String[] parts = data.split(":", 2);

                            if (parts.length == 2) {
                                String sender = parts[0];
                                String typingText = parts[1];

                                if (!sender.equals(username)) {
                                    // Display live typing preview with actual text
                                    if (!typingText.isEmpty()) {
                                        typingLabel.setText("✍️ " + sender + " typing: " + typingText);
                                    } else {
                                        typingLabel.setText("✍️ " + sender + " is typing...");
                                    }

                                    // Reset clear typing timer
                                    if (clearTypingTimer != null) {
                                        clearTypingTimer.stop();
                                    }

                                    // Set timer to clear typing indicator if no new typing message within 3 seconds
                                    clearTypingTimer = new Timer(3000, ev -> {
                                        typingLabel.setText("");
                                    });
                                    clearTypingTimer.setRepeats(false);
                                    clearTypingTimer.start();
                                }
                            }
                        }

                        // HANDLE MESSAGE
                        else if (message.startsWith("[MSG]")) {
                            String actualMsg = message.substring(5);
                            addMessage(actualMsg, false);
                            typingLabel.setText("");
                        }

                        else {
                            addMessage(message, false);
                        }
                    });
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Connection failed: " + e.getMessage());
            }
        }).start();
    }

    private void sendMessage() {
        String msg = messageField.getText();
        if (!msg.isEmpty() && !msg.equals("Type a message...")) {

            out.println("[MSG]" + username + ": " + msg);

            addMessage(username + ": " + msg, true);
            messageField.setText("");

            typingLabel.setText("");

            // Send empty typing message to clear typing indicator on other clients
            out.println("[TYPING]" + username + ":");
        }
    }

    private void addMessage(String msg, boolean isSender) {
        String timestamp = LocalTime.now().format(timeFormatter);
        MessageBubble bubble = new MessageBubble(msg, timestamp, isSender, SENDER_COLOR, RECEIVER_COLOR, TEXT_COLOR,
                TIMESTAMP_COLOR);
        chatPanel.add(bubble);
        chatPanel.add(Box.createVerticalStrut(5));
        chatPanel.revalidate();
        chatPanel.repaint();

        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, chatPanel);
            if (scrollPane != null) {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            }
        });
    }

    private void updateUserList(String userListStr) {
        userListModel.clear();
        if (userListStr != null && !userListStr.isEmpty()) {
            String[] users = userListStr.split(",");
            for (String user : users) {
                userListModel.addElement(user.trim());
            }
        }
    }

    // Custom cell renderer for user list
    private static class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText("🟢 " + value);
            label.setBackground(isSelected ? RECEIVER_COLOR : new Color(35, 36, 60));
            label.setForeground(TEXT_COLOR);
            label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            return label;
        }
    }

    // ✅ UPDATED MAIN METHOD
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientGUI("Jennifer Liatly");
            new ClientGUI("You");
        });
    }
}
