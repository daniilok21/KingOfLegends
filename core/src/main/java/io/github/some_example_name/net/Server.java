package io.github.some_example_name.net;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.game.AttackDirection;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.objects.PlayerObject;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.some_example_name.GameSettings.*;

public class Server {
    private GameState world = new GameState();
    private volatile boolean running = true;
    private AtomicReference<PlayerInput> latestInput = new AtomicReference<>(null);

    private int clientJumpsRemaining = 2;
    private boolean clientIsOnGround = false;

    private boolean clientIsDodging = false;
    private float clientDodgeTimer = 0f;
    private float clientDodgeCooldown = 0f;

    private Body serverBody;
    private Body clientBody;
    private PlayerObject serverPlayer;
    private PlayerObject clientPlayer;

    private Socket clientSocket;

    public void setPhysicsBodies(PlayerObject serverPlayer, PlayerObject clientPlayer) {
        this.serverPlayer = serverPlayer;
        this.clientPlayer = clientPlayer;
        serverBody = serverPlayer.getBody();
        clientBody = clientPlayer.getBody();
        world.serverBody = serverBody;
        world.clientBody = clientBody;
    }

    public void start(int port) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(port)) {
                System.out.println("Server started");
                clientSocket = server.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

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

                    updateGameState(delta);
                    updateDodgeState(delta);
                    updateClientGroundStatus();

                    PlayerInput currentInput = latestInput.getAndSet(null);

                    if (currentInput != null && clientBody != null) {
                        applyInput(currentInput, delta);
                    }

                    if (serverPlayer != null) {
                        world.serverAttacking = serverPlayer.isAttacking();
                        world.serverAttackDirection = serverPlayer.getCurrentAttackDirection();
                    }
                    if (clientPlayer != null) {
                        world.clientAttacking = clientPlayer.isAttacking();
                        world.clientAttackDirection = clientPlayer.getCurrentAttackDirection();
                    }

                    if (serverPlayer != null) world.serverHealth = serverPlayer.getHealth();
                    if (clientPlayer != null) world.clientHealth = clientPlayer.getHealth();

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
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateGameState(float delta) {
        switch (world.gameStatus) {
            case WAITING:
                if (clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed()) {
                    world.gameStatus = GameState.GameStatus.COUNTDOWN;
                    world.countdownTimer = 3.0f;
                }
                break;
            case COUNTDOWN:
                world.countdownTimer -= delta;
                if (world.countdownTimer <= 0) {
                    world.gameStatus = GameState.GameStatus.PLAYING;
                    System.out.println("Game started");
                }
                break;
            case PLAYING:
                break;
        }
    }

    private void updateDodgeState(float delta) {
        if (clientIsDodging) {
            clientDodgeTimer += delta;
            if (clientDodgeTimer >= DODGE_DURATION) {
                clientIsDodging = false;
                clientDodgeTimer = 0f;
                if (clientBody != null) {
                    clientBody.setGravityScale(1.0f);
                }
            }
        }

        if (clientDodgeCooldown > 0) {
            clientDodgeCooldown -= delta;
        }
    }

    private void updateClientGroundStatus() {
        if (clientBody == null) return;

        float velocityY = clientBody.getLinearVelocity().y;
        if (Math.abs(velocityY) <= 0.1f) {
            if (!clientIsOnGround) {
                clientIsOnGround = true;
                clientJumpsRemaining = 2;
            }
        }
        else {
            clientIsOnGround = false;
        }
    }

    private void applyInput(PlayerInput input, float delta) {
        if (world.gameStatus != GameState.GameStatus.PLAYING) return;
        if (clientPlayer != null && !clientPlayer.canReceiveInput()) return;
        Vector2 force = new Vector2(0, 0);
        if (input.moveRight && clientPlayer.canMove()) force.x = PLAYER_MOVE_FORCE * delta * FRAME_RATE;
        if (input.moveLeft && clientPlayer.canMove()) force.x = -PLAYER_MOVE_FORCE * delta * FRAME_RATE;

        if (clientPlayer.canMove()) {
            clientBody.applyForceToCenter(force, true);
        }

        Vector2 vel = clientBody.getLinearVelocity();
        if (!clientPlayer.isInHitStun()) {
            vel.x = Math.max(-PLAYER_MAX_VELOCITY, Math.min(PLAYER_MAX_VELOCITY, vel.x));
        }
        clientBody.setLinearVelocity(vel);

        if (input.jump && clientPlayer.canJump()) {
            if (clientJumpsRemaining > 0 ) {
                if (clientIsDodging) {
                    clientIsDodging = false;
                    clientDodgeTimer = 0f;
                    clientBody.setGravityScale(1.0f);
                }
                Vector2 currentVelocity = clientBody.getLinearVelocity();
                clientBody.setLinearVelocity(currentVelocity.x, 0);
                clientBody.applyLinearImpulse(new Vector2(0, PLAYER_JUMP_FORCE), clientBody.getWorldCenter(), true);
                clientJumpsRemaining--;
            }
        }

        if (input.dodge && clientDodgeCooldown <= 0 && !clientIsDodging && clientPlayer.canDodge()) {
            float dodgeDirection = 0;
            if (input.moveLeft) dodgeDirection = -1;
            else if (input.moveRight) dodgeDirection = 1;
            else {
                float currentVelX = clientBody.getLinearVelocity().x;
                if (currentVelX != 0) {
                    if (currentVelX > 0) dodgeDirection = 0;
                    else dodgeDirection = 1;
                }
            }

            if (dodgeDirection != 0) {
                clientIsDodging = true;
                clientDodgeTimer = 0f;
                clientDodgeCooldown = DODGE_COOLDOWN;

                Vector2 currentPos = clientBody.getPosition();
                Vector2 s = new Vector2(dodgeDirection, 0).nor().scl(DODGE_DISTANCE * SCALE);
                Vector2 newPos = new Vector2(currentPos).add(s);
                clientBody.setTransform(newPos, clientBody.getAngle());
            }
            else {
                clientBody.setLinearVelocity(0, 0);
            }
        }
        if (input.attack && clientPlayer.canAttack()) {
            AttackDirection direction;
            if (input.attackUp) direction = AttackDirection.UP;
            else if (input.attackDown) direction = AttackDirection.DOWN;
            else direction = AttackDirection.SIDE;

            clientPlayer.startAttack(direction);
            System.out.println("Server: Client attack " + direction);

            if (serverPlayer != null) {
                if (clientPlayer.isAttacking()) {
                    boolean hit = clientPlayer.checkHit(serverPlayer);
                    if (hit) {
                        System.out.println("Клиент попал по серверу на сервере!");
                    }
                }
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
