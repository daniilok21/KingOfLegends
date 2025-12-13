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

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Поток приёма состояния
            new Thread(() -> {
                try {
                    while (running) {
                        GameState state = (GameState) in.readObject();
                        if (state != null) {
                            latestState = state;
                        }
                    }
                } catch (Exception e) {
                    // disconnected
                }
            }).start();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendInput(PlayerInput input) {
        try {
            out.writeObject(input);
            out.flush();
        } catch (IOException e) {
            // ignore
        }
    }

    public GameState getState() {
        return latestState;
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}
