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
    private TargetDataLine microphone;
    private SourceDataLine speakers;

    public void startCall(int myPort, String targetIp, int targetPort) {
        if (running) return;
        running = true;
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);

        new Thread(() -> runReceiver(myPort, format)).start();
        new Thread(() -> runSender(targetIp, targetPort, format)).start();
    }

    private void runSender(String ip, int port, AudioFormat format) {
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();

            InetAddress address = InetAddress.getByName(ip);
            byte[] buffer = new byte[1024];

            while (running) {
                int count = microphone.read(buffer, 0, buffer.length);
                // Фильтр шума: считаем среднюю амплитуду (RMS)
                if (count > 0 && calculateRMS(buffer) > 0.02) {
                    sendSocket.send(new DatagramPacket(buffer, count, address, port));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (microphone != null) { microphone.stop(); microphone.close(); }
        }
    }

    private void runReceiver(int port, AudioFormat format) {
        try {
            socket = new DatagramSocket(port);
            speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            byte[] buffer = new byte[1024];
            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                speakers.write(packet.getData(), 0, packet.getLength());
            }
        } catch (Exception e) {
            if (running) e.printStackTrace();
        } finally {
            if (speakers != null) { speakers.stop(); speakers.close(); }
            if (socket != null) socket.close();
        }
    }

    private double calculateRMS(byte[] buffer) {
        double sum = 0;
        for (int i = 0; i < buffer.length; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
            sum += sample * sample;
        }
        return Math.sqrt(sum / (buffer.length / 2.0)) / 32768.0;
    }

    public void stopCall() {
        running = false;
        if (socket != null) socket.close();
    }
}
