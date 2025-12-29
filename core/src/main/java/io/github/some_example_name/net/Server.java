package io.github.some_example_name.net;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.some_example_name.GameSettings.*;

public class Server {
    private GameState world = new GameState();
    private volatile boolean running = true;
    private AtomicReference<PlayerInput> latestInput = new AtomicReference<>(null);

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
                                latestInput.set(input);
                            }
                        }
                    } catch (EOFException | SocketException e) {
                        System.out.println("Client disconnected from input thread");
                    } catch (Exception e) {
                        System.out.println("Input thread error: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                inputThread.start();

                long lastTime = System.currentTimeMillis();

                while (running) {
                    long currentTime = System.currentTimeMillis();
                    float delta = (currentTime - lastTime) / 1000f;
                    lastTime = currentTime;

                    PlayerInput currentInput = latestInput.getAndSet(null);

                    if (currentInput != null && clientBody != null) {
                        applyInput(currentInput, delta);
                    }

                    world.updateFromPhysics();

                    out.writeObject(world.clone());
                    out.flush();
                    out.reset();

                    long elapsed = System.currentTimeMillis() - currentTime;
                    long sleepTime = FRAME_DELAY_MS - elapsed;
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }

                inputThread.interrupt();
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void applyInput(PlayerInput input, float delta) {
        Vector2 force = new Vector2(0, 0);
        if (input.moveRight) force.x = PLAYER_MOVE_FORCE * delta * FRAME_RATE;
        if (input.moveLeft) force.x = -PLAYER_MOVE_FORCE * delta * FRAME_RATE;

        clientBody.applyForceToCenter(force, true);

        Vector2 vel = clientBody.getLinearVelocity();
        vel.x = Math.max(-PLAYER_MAX_VELOCITY, Math.min(PLAYER_MAX_VELOCITY, vel.x));
        clientBody.setLinearVelocity(vel);

        if (input.jump) {
            if (Math.abs(clientBody.getLinearVelocity().y) < 0.1f) {
                clientBody.applyLinearImpulse(
                    new Vector2(0, PLAYER_JUMP_FORCE),
                    clientBody.getWorldCenter(),
                    true
                );
            }
        }
    }

    public void stop() {
        running = false;
    }

    public GameState getLocalState() {
        world.updateFromPhysics();
        return world;
    }
}
