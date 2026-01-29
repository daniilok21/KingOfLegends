package io.github.some_example_name.net;

import io.github.some_example_name.game.PlayerInput;
import java.io.*;
import java.net.*;

public class Server {
    private volatile boolean running = true;
    private ObjectOutputStream out;
    private PlayerInput latestClientInput = new PlayerInput();
    private volatile boolean isClientConnected = false;
    private ServerSocket serverSocket;

    public void start(int port) {
        running = true;
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("SERVER: Started on port " + port);
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                isClientConnected = true;
                System.out.println("Client connected: " + socket.getInetAddress());


                while (running) {
                    Object obj = in.readObject();
                    if (obj instanceof PlayerInput) {
                        latestClientInput = (PlayerInput) obj;
                    }
                }
            } catch (Exception e) {
                if (running) {
                    e.printStackTrace();
                }
                isClientConnected = false;
            } finally {
                stop();
            }
        }).start();
    }

    public void sendState(NetworkPacket packet) {
        if (out == null || !isClientConnected) return;
        try {
            out.writeObject(packet);
            out.flush();
            out.reset();
        } catch (IOException e) {
            isClientConnected = false;
        }
    }

    public PlayerInput getClientInput() { return latestClientInput; }
    public boolean isConnected() { return isClientConnected; }

    public void stop() {
        running = false;
        isClientConnected = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
