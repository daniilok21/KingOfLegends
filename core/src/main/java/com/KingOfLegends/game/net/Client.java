package com.KingOfLegends.game.net;

import com.KingOfLegends.game.game.PlayerInput;
import com.KingOfLegends.game.game.GameState;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private volatile boolean running = true;
    private volatile boolean readerStarted = false;

    public volatile NetworkPacket latestPacket;
    public GameState.GameStatus serverStatus = GameState.GameStatus.WAITING;
    public float serverCountdown = 3f;

    public boolean connect(String host, int port) {
        return connectWithTimeout(host, port, 0);
    }

    public boolean connectWithTimeout(String host, int port, int timeoutMs) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setTcpNoDelay(true);
            out = new ObjectOutputStream(socket.getOutputStream());
            return true;
        } catch (Exception e) {
            running = false;
            return false;
        }
    }

    public synchronized void startReading() {
        if (readerStarted || socket == null) return;
        readerStarted = true;
        running = true;

        new Thread(() -> {
            try {
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                while (running) {
                    Object obj = in.readObject();
                    if (obj instanceof NetworkPacket) {
                        latestPacket = (NetworkPacket) obj;
                        serverStatus = latestPacket.status;
                        serverCountdown = latestPacket.countdown;
                    }
                }
            } catch (Exception e) {
                running = false;
            }
        }, "client-receive").start();
    }

    public void sendInput(PlayerInput input) {
        if (out == null || !running) return;
        try {
            out.writeObject(input);
            out.flush();
            out.reset();
        } catch (IOException e) {
            running = false;
        }
    }

    public void disconnect() {
        running = false;
        try { if (socket != null) socket.close(); } catch (Exception e) {}
    }

    public boolean isConnected() { return running && socket != null && socket.isConnected(); }
}
