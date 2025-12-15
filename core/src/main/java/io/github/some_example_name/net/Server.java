package io.github.some_example_name.net;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server {
    private GameState world = new GameState();
    private volatile boolean running = true;
    private BlockingQueue<PlayerInput> inputQueue = new ArrayBlockingQueue<>(10);

    public void start(int port) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Server started on port " + port);
                Socket client = server.accept();
                System.out.println("Client connected: " + client.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());

                // Поток для приема ввода от клиента
                Thread inputThread = new Thread(() -> {
                    try {
                        while (running) {
                            PlayerInput input = (PlayerInput) in.readObject();
                            if (input != null) {
                                inputQueue.put(input);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Input thread stopped: " + e.getMessage());
                    }
                });
                inputThread.start();

                // Основной игровой цикл
                while (running) {
                    // Обработать ввод от клиента (управление клиентским кубиком)
                    PlayerInput clientInput = inputQueue.poll();
                    if (clientInput != null) {
                        if (clientInput.moveRight) {
                            world.clientCubeX += 5;
                            if (world.clientCubeX > 800) world.clientCubeX = 0;
                        }
                        if (clientInput.moveLeft) {
                            world.clientCubeX -= 5;
                            if (world.clientCubeX < 0) world.clientCubeX = 800;
                        }
                    }

                    // Обновить логику игры
                    world.update();

                    // Отправить состояние клиенту
                    out.writeObject(world);
                    out.flush();
                    out.reset();

                    Thread.sleep(16); // ~60 FPS
                }

                inputThread.interrupt();
                client.close();
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

    // Метод для управления серверным кубиком с хоста
    public void moveServerCubeRight() {
        world.serverCubeX += 5;
        if (world.serverCubeX > 800) world.serverCubeX = 0;
    }

    public void moveServerCubeLeft() {
        world.serverCubeX -= 5;
        if (world.serverCubeX < 0) world.serverCubeX = 800;
    }
}
