package io.github.some_example_name.net;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static io.github.some_example_name.GameSettings.*;

public class Server {
    private GameState world = new GameState();
    private volatile boolean running = true;
    private BlockingQueue<PlayerInput> inputQueue = new ArrayBlockingQueue<>(10);

    private Body serverBody;
    private Body clientBody;

    public void setPhysicsBodies(Body serverBody, Body clientBody) {
        this.serverBody = serverBody;
        this.clientBody = clientBody;
        world.serverBody = serverBody;
        world.clientBody = clientBody;
    }

    public void start(int port) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Server started");
                Socket client = server.accept();
                System.out.println("Client connected: " + client.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());

                Thread inputThread = new Thread(() -> {
                    try {
                        while (running) {
                            PlayerInput input = (PlayerInput) in.readObject();
                            if (input != null) {
                                inputQueue.put(input);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Input stopped: " + e.getMessage());
                    }
                });
                inputThread.start();

                while (running) {
                    PlayerInput clientInput = inputQueue.poll();
                    if (clientInput != null && clientBody != null) {
                        Vector2 force = new Vector2(0, 0);
                        if (clientInput.moveRight) force.x = MOVE_FORCE;
                        if (clientInput.moveLeft) force.x = -MOVE_FORCE;
                        if (clientInput.jump) {
                            if (Math.abs(clientBody.getLinearVelocity().y) < 0.1f) {
                                clientBody.applyLinearImpulse(
                                    new Vector2(0, JUMP_FORCE),
                                    clientBody.getWorldCenter(),
                                    true
                                );
                            }
                        }
                        clientBody.applyForceToCenter(force, true);

                        Vector2 vel = clientBody.getLinearVelocity();
                        vel.x = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, vel.x));
                        clientBody.setLinearVelocity(vel);
                    }

                    world.updateFromPhysics();

                    out.writeObject(world.clone());
                    out.flush();
                    out.reset();

                    Thread.sleep(FRAME_DELAY_MS);
                }

                inputThread.interrupt();
                client.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        running = false;
    }

    public GameState getLocalState() {
        world.updateFromPhysics();
        return world;
    }

    public void moveServerCubeRight() {
        if (serverBody != null) {
            serverBody.applyForceToCenter(new Vector2(MOVE_FORCE, 0), true);
        }
    }

    public void moveServerCubeLeft() {
        if (serverBody != null) {
            serverBody.applyForceToCenter(new Vector2(-MOVE_FORCE, 0), true);
        }
    }

    public void serverJump() {
        if (serverBody != null && Math.abs(serverBody.getLinearVelocity().y) < 0.1f) {
            serverBody.applyLinearImpulse(
                new Vector2(0, JUMP_FORCE),
                serverBody.getWorldCenter(),
                true
            );
        }
    }
}
