package io.github.some_example_name.net;

import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.game.GameState;
import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private volatile boolean running = true;

    public volatile NetworkPacket latestPacket;
    public GameState.GameStatus serverStatus = GameState.GameStatus.WAITING;
    public float serverCountdown = 3f;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            socket.setTcpNoDelay(true);
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
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
            }).start();
            return true;
        } catch (Exception e) {
            return false;
        }
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
