package io.github.some_example_name.net;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;

import java.io.*;
import java.net.*;

public class Server {
    private GameState world = new GameState();
    private volatile boolean running = true;

    public void start(int port) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Server started on port " + port);
                Socket client = server.accept();
                System.out.println("Client connected: " + client.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());

                while (running) {
                    // Принять ввод
                    PlayerInput input = (PlayerInput) in.readObject();
                    if (input.moveRight) {
                        world.cubeX += 5;
                        if (world.cubeX > 800) world.cubeX = 0;
                    }

                    // Обновить логику
                    world.update();

                    // Отправить состояние
                    out.writeObject(world);
                    out.flush();

                    Thread.sleep(16); // ~60 FPS
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        running = false;
    }

    public GameState getLocalState() {
        return world;
    }
}
