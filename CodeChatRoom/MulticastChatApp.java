package com.example.chat;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class MulticastChatApp extends JFrame {
    private static final long serialVersionUID = 1L;

    private final MulticastChatMain networkHandler;
    private final JTextPane chatArea = new JTextPane();
    private final JTextField inputField = new JTextField();

    private final DefaultTableModel clientTableModel = new DefaultTableModel(
            new Object[]{"Tên", "Trạng thái", "Vai trò"}, 0
    );
    private final JTable clientTable = new JTable(clientTableModel);

    private final JLabel topChatLabel = new JLabel("Top chat: None");
    private final JLabel onlineStatusLabel = new JLabel("Online: 0");
    private final JTextPane historyLog = new JTextPane();

    private final ConcurrentMap<String, Integer> chatFrequency = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> onlineClients = new ConcurrentHashMap<>();
    private final Set<String> importantUsers = Collections.synchronizedSet(new HashSet<>());
    private final List<String> chatHistory = Collections.synchronizedList(new ArrayList<>());

    private volatile boolean running = true;
    private String currentUser = "";
    private boolean isAdmin = false;

    private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    private final Map<String, Color> userColors = new HashMap<>();

    public MulticastChatApp(MulticastChatMain networkHandler) {
        this.networkHandler = networkHandler;
        authenticateUser();
        initUserColors();
        initUI();
        startReceiverThread();
        startHeartbeatThread();
        startOnlineCheckThread();
    }

    private void initUserColors() {
        userColors.put("Admin", new Color(128, 0, 128));
        userColors.put("anh", new Color(30, 144, 255));
        userColors.put("binh", new Color(34, 139, 34));
        userColors.put("chi", new Color(220, 20, 60));
        userColors.put("quynh", new Color(255, 140, 0));
        userColors.put("hue", new Color(255, 20, 147));
        userColors.put("SYSTEM", Color.GRAY);
    }

    private void authenticateUser() {
        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put("Admin", "admin123");
        userCredentials.put("anh", "1");
        userCredentials.put("binh", "2");
        userCredentials.put("chi", "3");
        userCredentials.put("quynh", "4");
        userCredentials.put("hue", "5");

        while (true) {
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            Object[] fields = {"Username:", usernameField, "Password:", passwordField};
            int option = JOptionPane.showConfirmDialog(this, fields, "Login", JOptionPane.OK_CANCEL_OPTION);

            if (option != JOptionPane.OK_OPTION) {
                System.exit(0);
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
                currentUser = username;
                if (username.equals("Admin")) isAdmin = true;
                JOptionPane.showMessageDialog(this, "Welcome " + username + "!");
                break;
            } else {
                JOptionPane.showMessageDialog(this, "Sai username hoặc password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initUI() {
        setTitle("Chat Room - User: " + currentUser);
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Font font = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Label.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("Button.font", font);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("💬 Chat", createChatPanel());
        tabs.addTab("👥 Clients", createClientsPanel());
        tabs.addTab("📜 History", createHistoryPanel());
        if (isAdmin) tabs.addTab("⚙️ Quản trị", createAdminPanel());

        add(tabs, BorderLayout.CENTER);
        add(topChatLabel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                networkHandler.sendMessage("[LOGOUT] " + currentUser);
                running = false;
                dispose();
            }
        });

        setVisible(true);
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        chatArea.setEditable(false);
        chatArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(chatArea);

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(inputField, BorderLayout.CENTER);
        JButton sendBtn = new JButton("Send");
        bottom.add(sendBtn, BorderLayout.EAST);

        inputField.addActionListener(e -> sendMessage());
        sendBtn.addActionListener(e -> sendMessage());

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createClientsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        clientTable.setRowHeight(28);
        clientTable.setFillsViewportHeight(true);
        clientTable.setDefaultEditor(Object.class, null);
        clientTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String name = (String) table.getValueAt(row, 0);

                if (userColors.containsKey(name)) {
                    lbl.setForeground(userColors.get(name));
                } else if (importantUsers.contains(name)) {
                    lbl.setForeground(Color.RED);
                } else {
                    lbl.setForeground(Color.BLUE);
                }
                return lbl;
            }
        });

        panel.add(new JScrollPane(clientTable), BorderLayout.CENTER);
        panel.add(onlineStatusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        historyLog.setEditable(false);
        historyLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        panel.add(new JScrollPane(historyLog), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JButton kickBtn = new JButton("Kick");
        JButton markBtn = new JButton("Mark Important");

        kickBtn.addActionListener(e -> {
            int row = clientTable.getSelectedRow();
            if (row >= 0) {
                String sel = (String) clientTableModel.getValueAt(row, 0);
                if (!"Admin".equals(sel)) {
                    networkHandler.sendMessage("[KICK] " + sel);
                }
            }
        });

        markBtn.addActionListener(e -> {
            int row = clientTable.getSelectedRow();
            if (row >= 0) {
                String sel = (String) clientTableModel.getValueAt(row, 0);
                networkHandler.sendMessage("[IMPORTANT] " + sel);
            }
        });

        JPanel btns = new JPanel();
        btns.add(kickBtn);
        btns.add(markBtn);

        panel.add(new JScrollPane(clientTable), BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            String fullMsg = currentUser + ": " + msg;
            networkHandler.sendMessage(fullMsg);
            inputField.setText("");
        }
    }

    private void startReceiverThread() {
        new Thread(() -> {
            while (running) {
                String msg = networkHandler.receiveMessage();
                if (msg != null) processReceivedMessage(msg);
            }
        }).start();
    }

    private void processReceivedMessage(String msg) {
        if (msg.startsWith("[HEARTBEAT]")) {
            String user = msg.substring(11).trim();
            if (!IP_PATTERN.matcher(user).matches()) {
                boolean isNew = !onlineClients.containsKey(user);
                onlineClients.put(user, System.currentTimeMillis());
                if (isNew) {
                    SwingUtilities.invokeLater(() -> appendColoredMessage("SYSTEM", user + " đã vào phòng"));
                }
                SwingUtilities.invokeLater(this::updateOnlineStatusLabelAndTable);
            }
        } else if (msg.startsWith("[KICK]")) {
            String target = msg.substring(7).trim();
            if (target.equals(currentUser)) {
                JOptionPane.showMessageDialog(this, "Bạn đã bị kick!");
                running = false;
                dispose();
            }
            if (onlineClients.containsKey(target)) {
                SwingUtilities.invokeLater(() -> appendColoredMessage("SYSTEM", target + " đã thoát"));
            }
            onlineClients.remove(target);
            importantUsers.remove(target);
            SwingUtilities.invokeLater(this::updateOnlineStatusLabelAndTable);
        } else if (msg.startsWith("[IMPORTANT]")) {
            String target = msg.substring(11).trim();
            importantUsers.add(target);
            SwingUtilities.invokeLater(this::updateOnlineStatusLabelAndTable);
        } else if (msg.startsWith("[LOGOUT]")) {
            String target = msg.substring(8).trim();
            if (onlineClients.containsKey(target)) {
                SwingUtilities.invokeLater(() -> appendColoredMessage("SYSTEM", target + " đã thoát"));
            }
            onlineClients.remove(target);
            importantUsers.remove(target);
            SwingUtilities.invokeLater(this::updateOnlineStatusLabelAndTable);
        } else {
            chatHistory.add(msg);
            String[] parts = msg.split(":", 2);
            if (parts.length == 2) {
                String user = parts[0].trim();
                String text = parts[1].trim();
                SwingUtilities.invokeLater(() -> {
                    appendColoredMessage(user, text);
                    updateTopChat();
                    updateHistoryLog();
                });
                chatFrequency.merge(user, 1, Integer::sum);
            }
        }
    }

    private void startHeartbeatThread() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (running) networkHandler.sendMessage("[HEARTBEAT] " + currentUser);
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void startOnlineCheckThread() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::checkOnlineStatus, 0, 15, TimeUnit.SECONDS);
    }

    private void checkOnlineStatus() {
        long now = System.currentTimeMillis();
        Set<String> removeList = new HashSet<>();
        for (Map.Entry<String, Long> e : onlineClients.entrySet()) {
            if (now - e.getValue() > 30000) {
                removeList.add(e.getKey());
            }
        }
        for (String u : removeList) {
            if (onlineClients.containsKey(u)) {
                SwingUtilities.invokeLater(() -> appendColoredMessage("SYSTEM", u + " đã thoát (timeout)"));
            }
            onlineClients.remove(u);
            importantUsers.remove(u);
        }
        SwingUtilities.invokeLater(this::updateOnlineStatusLabelAndTable);
    }

    private void updateOnlineStatusLabelAndTable() {
        clientTableModel.setRowCount(0);
        for (String user : onlineClients.keySet()) {
            String role = user.equals("Admin") ? "Admin" : "User";
            clientTableModel.addRow(new Object[]{user, "Online", role});
        }
        onlineStatusLabel.setText("Online: " + onlineClients.size());
        clientTable.repaint();
    }

    private void updateTopChat() {
        String top = "None";
        int max = 0;
        for (Map.Entry<String, Integer> e : chatFrequency.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                top = e.getKey();
            }
        }
        final String disp = top + " (" + max + " msgs)";
        SwingUtilities.invokeLater(() -> topChatLabel.setText("Top chat: " + disp));
    }

    private void updateHistoryLog() {
        StringBuilder sb = new StringBuilder();
        for (String line : chatHistory) sb.append(line).append("\n");
        historyLog.setText(sb.toString());
    }

    private void appendColoredMessage(String user, String text) {
        StyledDocument doc = chatArea.getStyledDocument();
        Style style = chatArea.addStyle("UserStyle", null);

        Color color = userColors.getOrDefault(user, Color.BLACK);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, true);

        try {
            doc.insertString(doc.getLength(), user + ": ", style);
            StyleConstants.setForeground(style, Color.DARK_GRAY);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
