package com.example.j_lab8;
import javax.sound.sampled.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.DoubleConsumer;

public class Phone {
    private volatile boolean running = false;
    private DatagramSocket socket;
    private TargetDataLine microphone;
    private SourceDataLine speakers;

    public void startCall(int myPort, String targetIp, int targetPort, DoubleConsumer volListener) {
        if (running) return;
        running = true;
        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
        new Thread(() -> runReceiver(myPort, format)).start();
        new Thread(() -> runSender(targetIp, targetPort, format, volListener)).start();
    }

    private void runSender(String ip, int port, AudioFormat format, DoubleConsumer volListener) {
        try (DatagramSocket sendSocket = new DatagramSocket()) {
            microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();
            InetAddress address = InetAddress.getByName(ip);
            byte[] buffer = new byte[1024];
            while (running) {
                int count = microphone.read(buffer, 0, buffer.length);
                if (count > 0) {
                    // Раньше здесь была проверка:
                    if (calculateLevel(buffer, count) > 0.05) { // 0.05 — порог шума
                        sendSocket.send(new DatagramPacket(buffer, count, address, port));
                    }
                    // Сейчас отправка идет без условий, а calculateLevel
                    // используется только для индикатора (volListener).
                    volListener.accept(calculateLevel(buffer, count));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally
        {
            closeMic();
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
        } catch (Exception e) { if (running) e.printStackTrace(); }
        finally { closeSpeakers(); if (socket != null) socket.close(); }
    }

    private double calculateLevel(byte[] buffer, int read) {
        long sum = 0;
        for (int i = 0; i < read; i += 2) {
            short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xff));
            sum += sample * sample;
        }
        double rms = Math.sqrt(sum / (read / 2.0));
        return Math.min(1.0, rms / 32768.0 * 10); // Коэффициент 10 для чувствительности
    }

    public void stopCall() {
        running = false;
        if (socket != null) socket.close();
    }

    private void closeMic() { if (microphone != null)
    {
        microphone.stop(); microphone.close();
    }
    }
    private void closeSpeakers()
    { if (speakers != null)
    {
        speakers.stop(); speakers.close();
    }
    }
}