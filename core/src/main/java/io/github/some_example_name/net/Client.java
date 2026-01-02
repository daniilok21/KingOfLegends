package io.github.some_example_name.net;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean running = true;
    private GameState latestState = new GameState();
    private volatile boolean connected = false;

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            connected = true;
            // приём состояния от сервера
            new Thread(() -> {
                try {
                    while (running) {
                        GameState state = (GameState) in.readObject();
                        if (state != null) {
                            synchronized (this) {
                                latestState = state;
                            }
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("Client disconnected: " + e.getMessage());
                }
            }).start();

            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendInput(PlayerInput input) {
        if (!connected || out == null) return;
        try {
            out.writeObject(input);
            out.flush();
            out.reset();
        }
        catch (IOException e) {
            System.out.println("Failed to send input: " + e.getMessage());
            connected = false;
        }
    }

    public GameState getState() {
        synchronized (this) {
            return latestState;
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void disconnect() {
        running = false;
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        }
        catch (IOException e) {}
    }
}
