package com.example.j_lab8;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Phone {
    private volatile boolean running = false;
    private DatagramSocket socket;

    public void startCall(int myPort, String targetIp, int targetPort) {
        running = true;
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);

        new Thread(() -> runReceiver(myPort, format)).start();
        new Thread(() -> runSender(targetIp, targetPort, format)).start();
    }

    private void runSender(String ip, int port, AudioFormat format) {
        try (DatagramSocket sendSocket = new DatagramSocket();
             TargetDataLine line = AudioSystem.getTargetDataLine(format)) {
            line.open(format);
            line.start();
            InetAddress address = InetAddress.getByName(ip);
            byte[] buffer = new byte[1024];

            while (running) {
                int count = line.read(buffer, 0, buffer.length);
                if (count > 0) {
                    DatagramPacket packet = new DatagramPacket(buffer, count, address, port);
                    sendSocket.send(packet);
                }
            }
            line.stop();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void runReceiver(int port, AudioFormat format) {
        try {
            socket = new DatagramSocket(port);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                line.write(packet.getData(), 0, packet.getLength());
            }
            line.stop();
            line.close();
            socket.close();
        } catch (Exception e) { if (running) e.printStackTrace(); }
    }

    public void stopCall() {
        running = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
