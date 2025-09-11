package com.example.chat;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MulticastChatApp extends JFrame {
    private JTextArea chatLog;
    private JTextField inputField;
    private JButton sendButton;
    private ConcurrentLinkedQueue<String> messageQueue;
    private MulticastChatMain networkHandler;
    private BufferedWriter logWriter;

    public MulticastChatApp(MulticastChatMain networkHandler) {
        this.networkHandler = networkHandler;
        this.messageQueue = new ConcurrentLinkedQueue<>();

        // Cấu hình giao diện
        setTitle("Chat Room - Giao Tiếp Nhóm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);

        // Tạo panel chính với border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(240, 248, 255));

        // Khởi tạo chat log
        chatLog = new JTextArea();
        chatLog.setEditable(false);
        chatLog.setFont(new Font("Arial", Font.PLAIN, 14));
        chatLog.setBackground(new Color(255, 255, 255));
        chatLog.setForeground(new Color(0, 100, 0));
        JScrollPane scrollPane = new JScrollPane(chatLog);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Khởi tạo input và button
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBackground(new Color(240, 248, 255));

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField.setMargin(new Insets(5, 5, 5, 5));

        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(60, 179, 113));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(e -> sendMessage());

        // Thêm input và button vào panel
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Thêm các thành phần vào main panel
        mainPanel.add(new JLabel("Chat Room - Nhập tin nhắn để bắt đầu!", JLabel.CENTER), BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Thêm main panel vào frame
        add(mainPanel);

        // Làm mới layout
        revalidate();
        repaint();
        pack();

        // Khởi tạo file log
        try {
            logWriter = new BufferedWriter(new FileWriter("chat_log.txt", true)); // true để append
            logWriter.write("--- Bắt đầu ghi log chat - " + new java.util.Date() + " ---\n");
            logWriter.flush();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khởi tạo file log: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        // Bắt đầu thread nhận tin nhắn
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String message = networkHandler.receiveMessage();
                if (message != null) {
                    messageQueue.add(message);
                    SwingUtilities.invokeLater(() -> {
                        updateChatLog();
                        saveToLog(message); // Lưu tin nhắn nhận được
                    });
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();

        setVisible(true);

        // Thêm tin nhắn mặc định
        String welcomeMessage = "Chat Room đã sẵn sàng. Nhập và gửi tin nhắn để bắt đầu!\n";
        chatLog.append(welcomeMessage);
        saveToLog(welcomeMessage); // Lưu tin nhắn mặc định
    }

    private void updateChatLog() {
        while (!messageQueue.isEmpty()) {
            String message = messageQueue.poll();
            chatLog.append(message + "\n");
            chatLog.setCaretPosition(chatLog.getDocument().getLength());
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            if (message.length() > 64) {
                message = message.substring(0, 64);
            }
            networkHandler.sendMessage(message);
            String sentMessage = "[" + getLocalAddress() + "]: " + message; // Thêm địa chỉ gửi
            messageQueue.add(sentMessage); // Hiển thị tin nhắn đã gửi
            SwingUtilities.invokeLater(() -> {
                updateChatLog();
                saveToLog(sentMessage); // Lưu tin nhắn gửi đi
            });
            inputField.setText("");
        }
    }

    private String getLocalAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (IOException e) {
            return "Unknown";
        }
    }

    private void saveToLog(String message) {
        if (logWriter != null) {
            try {
                logWriter.write(message + "\n");
                logWriter.flush();
            } catch (IOException e) {
                System.err.println("Lỗi ghi log: " + e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        if (logWriter != null) {
            try {
                logWriter.write("--- Kết thúc ghi log - " + new java.util.Date() + " ---\n");
                logWriter.close();
            } catch (IOException e) {
                System.err.println("Lỗi đóng file log: " + e.getMessage());
            }
        }
        if (networkHandler != null) {
            networkHandler.close();
        }
        super.dispose();
    }
}
