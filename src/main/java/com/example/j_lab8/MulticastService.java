package com.example.j_lab8;

import javafx.application.Platform;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MulticastService {

    private static final String GROUP_IP = "230.0.0.1";
    private static final int PORT = 8888;

    private final Map<String, Long> users = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public void start(String username, int myPort, Consumer<String> onUserUpdate) {
        running = true;

        new Thread(() -> sender(username, myPort)).start();
        new Thread(() -> receiver(onUserUpdate)).start();
        new Thread(() -> cleaner(onUserUpdate)).start();
    }

    private void sender(String username, int myPort) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(GROUP_IP);

            while (running) {
                String msg = "HELLO " + username + " " +
                        InetAddress.getLocalHost().getHostAddress() +
                        " " + myPort;

                byte[] buf = msg.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(buf, buf.length, group, PORT));

                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiver(Consumer<String> onUserUpdate) {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(GROUP_IP);
            socket.joinGroup(group);

            byte[] buf = new byte[256];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength());
                if (msg.startsWith("HELLO")) {
                    String[] parts = msg.split(" ");
                    String key = parts[1] + "@" + parts[2] + ":" + parts[3];

                    users.put(key, System.currentTimeMillis());

                    Platform.runLater(() ->
                            onUserUpdate.accept(String.join("\n", users.keySet()))
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleaner(Consumer<String> onUserUpdate) {
        while (running) {
            long now = System.currentTimeMillis();

            users.entrySet().removeIf(e -> now - e.getValue() > 15000);

            Platform.runLater(() ->
                    onUserUpdate.accept(String.join("\n", users.keySet()))
            );

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}
        }
    }

    public void stop() {
        running = false;
    }
}