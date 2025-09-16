package com.example.chat;

import java.io.IOException;
import java.net.*;

public class MulticastChatMain {
    private static final String GROUP_ADDRESS = "230.0.0.0"; // địa chỉ multicast
    private static final int PORT = 4446;                    // cổng multicast
    private MulticastSocket socket;
    private InetAddress group;

    public MulticastChatMain() {
        try {
            group = InetAddress.getByName(GROUP_ADDRESS);
            socket = new MulticastSocket(PORT);
            socket.setReuseAddress(true);
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            byte[] buf = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receiveMessage() {
        try {
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            return new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            // nếu socket đóng thì trả null
            return null;
        }
    }

    public void close() {
        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Entry point để chạy app
    public static void main(String[] args) {
        MulticastChatMain networkHandler = new MulticastChatMain();
        new MulticastChatApp(networkHandler);
    }
}
