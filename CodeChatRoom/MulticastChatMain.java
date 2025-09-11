package com.example.chat;

import javax.swing.*;
import java.io.IOException;
import java.net.*;

public class MulticastChatMain {
    private static String MULTICAST_ADDRESS = "224.0.0.69";
    private static int PORT = 3000;
    private static final int BUFFER_SIZE = 64;

    private MulticastSocket socket;
    private boolean running = true;

    public MulticastChatMain(String[] args) {
        parseArgs(args);
        initNetwork();
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a") && i + 1 < args.length) {
                MULTICAST_ADDRESS = args[++i];
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                PORT = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--reuse")) {
                // Bật reuse address (xử lý trong initNetwork)
            }
        }
    }

    private void initNetwork() {
        try {
            socket = new MulticastSocket(PORT);
            socket.setReuseAddress(true);
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);
            System.out.println("Đã tham gia nhóm multicast: " + MULTICAST_ADDRESS + ":" + PORT);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Lỗi khởi tạo mạng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public void sendMessage(String message) {
        try {
            byte[] sendData = message.getBytes();
            if (sendData.length > BUFFER_SIZE) {
                sendData = java.util.Arrays.copyOf(sendData, BUFFER_SIZE);
            }
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                    InetAddress.getByName(MULTICAST_ADDRESS), PORT);
            socket.send(sendPacket);
        } catch (IOException e) {
            System.err.println("Lỗi gửi tin: " + e.getMessage());
        }
    }

    public String receiveMessage() {
        if (!running) return null;
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
            String message = "[" + packet.getAddress().getHostAddress() + "]: " +
                           new String(packet.getData(), 0, packet.getLength()).trim();
            System.out.println("Nhận tin nhắn: " + message); // Debug
            return message;
        } catch (IOException e) {
            if (!e.getMessage().contains("Socket closed")) {
                System.err.println("Lỗi nhận tin: " + e.getMessage());
            }
            return null;
        }
    }

    public void close() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            try {
                socket.leaveGroup(InetAddress.getByName(MULTICAST_ADDRESS));
                socket.close();
            } catch (IOException e) {
                System.err.println("Lỗi đóng socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MulticastChatMain main = new MulticastChatMain(args);
            new MulticastChatApp(main);
        });
    }
}
