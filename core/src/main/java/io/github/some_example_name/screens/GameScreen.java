package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import io.github.some_example_name.*;
import io.github.some_example_name.components.*;
import io.github.some_example_name.game.*;
import io.github.some_example_name.net.*;
import io.github.some_example_name.objects.*;
import java.util.ArrayList;
import static io.github.some_example_name.GameSettings.*;

public class GameScreen extends ScreenAdapter {
    private final MyGdxGame myGdxGame;
    private SpriteBatch batch;
    private Server server;
    private Client client;
    private PlayerObject serverPlayer, clientPlayer;
    private ArrayList<PlatformObject> platforms = new ArrayList<>();
    private ArrayList<OneWayPlatformObject> oneWayPlatforms = new ArrayList<>();
    private JoystickView joystick;
    private ButtonView jumpButton, dodgeButton, attackButton;
    private TopPanelView topPanel;
    private PlayerInput localInput = new PlayerInput();
    private boolean jumpWasPressed = false, connected = false;
    private GameState.GameStatus gameStatus = GameState.GameStatus.WAITING;
    private float countdown = 3.0f, timeAccumulator = 0, resultDisplayTimer = 0f;
    private TextView waitingText, countdownText, resultText;

    public GameScreen(MyGdxGame game) {
        this.myGdxGame = game;
        this.batch = game.batch;
        waitingText = new TextView(game.titleFont, SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2, "WAITING...");
        countdownText = new TextView(game.titleFont, SCREEN_WIDTH / 2 - 80, SCREEN_HEIGHT / 2, "");
        resultText = new TextView(game.titleFont, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2 + 100, "");

        waitingText.setCenterX(SCREEN_WIDTH / 2f);
        countdownText.setCenterX(SCREEN_WIDTH / 2f);
        resultText.setCenterX(SCREEN_WIDTH / 2f);

        waitingText.setCenterY(SCREEN_HEIGHT / 2f - 10);
        countdownText.setCenterY(SCREEN_HEIGHT / 2f - 10);
        resultText.setCenterY(SCREEN_HEIGHT / 2f - 10);

        setupWorld();
    }

    private void setupWorld() {
        int offset_buttons = 50;
        platforms.add(new PlatformObject(100, 100, SCREEN_WIDTH - 200, 100, GameResources.PLATFORM, myGdxGame.world));
        platforms.add(new PlatformObject(100, 350, 100, 220, GameResources.PLATFORM, myGdxGame.world));
        platforms.add(new PlatformObject(SCREEN_WIDTH - 200, 350, 100, 220, GameResources.PLATFORM, myGdxGame.world));
        oneWayPlatforms.add(new OneWayPlatformObject(SCREEN_WIDTH / 2 - 250, 325, 500, 50, GameResources.PLATFORM, myGdxGame.world));
        serverPlayer = new PlayerObject(START_PLAYER_SERVER_X, START_PLAYER_SERVER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, GameResources.RED_PLAYER_SPRITE_SHEET, myGdxGame.world);
        clientPlayer = new PlayerObject(START_PLAYER_CLIENT_X, START_PLAYER_CLIENT_Y, PLAYER_WIDTH, PLAYER_HEIGHT, GameResources.BLUE_PLAYER_SPRITE_SHEET, myGdxGame.world);
        joystick = new JoystickView(50, 30, GameResources.JOYSTICK_BG, GameResources.JOYSTICK_HANDLE);
        jumpButton = new ButtonView(SCREEN_WIDTH - 130 - offset_buttons, offset_buttons, BUTTON_WIDTH, BUTTON_HEIGHT, GameResources.BUTTON_JUMP);
        dodgeButton = new ButtonView(SCREEN_WIDTH - 130 - (BUTTON_WIDTH + 20) - offset_buttons, offset_buttons, BUTTON_WIDTH, BUTTON_HEIGHT, GameResources.BUTTON_DODGE);
        attackButton = new ButtonView(SCREEN_WIDTH - 130 - 2 * (BUTTON_WIDTH + 20) - offset_buttons, offset_buttons, BUTTON_WIDTH, BUTTON_HEIGHT, GameResources.BUTTON_ATTACK);
        topPanel = new TopPanelView(200, SCREEN_HEIGHT-TOP_PANEL_HEIGHT, SCREEN_WIDTH - 400, TOP_PANEL_HEIGHT, myGdxGame.defaultFont, myGdxGame.timerFont, GameResources.TOP_PANEL_BG, GameResources.HEART_FULL, GameResources.HEART_EMPTY);
    }

    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
            server.start(PORT);
            connected = true;
        } else {
            client = new Client();
            connected = client.connect(myGdxGame.hostIp, PORT);
        }
    }

    @Override
    public void render(float delta) {
        if (!connected) initializeNetwork();
        timeAccumulator += delta;
        while (timeAccumulator >= STEP_TIME) {
            updateLogic(STEP_TIME);
            timeAccumulator -= STEP_TIME;
            myGdxGame.world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
        draw();
    }

    private void updateLogic(float delta) {
        handleInput();

        checkMatchEndConditions();

        if (gameStatus == GameState.GameStatus.FINISHED) {
            resultDisplayTimer -= delta;
            if (resultDisplayTimer <= 0) {
                myGdxGame.showMenuScreen();
            }
            return;
        }

        if (myGdxGame.isHost) {
            PlayerInput remoteInput = server.getClientInput();
            updateServerState(delta);

            applyPlayerInput(serverPlayer, localInput);
            serverPlayer.update(delta);

            if (remoteInput != null && (remoteInput.x != 0 || remoteInput.y != 0)) {
                if (!clientPlayer.isInHitStun() && !clientPlayer.isDodging()) {
                    clientPlayer.getBody().setTransform(remoteInput.x, remoteInput.y, 0);
                    clientPlayer.getBody().setLinearVelocity(remoteInput.vx, remoteInput.vy);
                }
                clientPlayer.setHealth(remoteInput.health);
                clientPlayer.setFacingRight(remoteInput.facingRight);
                if (remoteInput.isAttacking && !clientPlayer.isAttacking()) {
                    clientPlayer.startAttack(remoteInput.attackDir);
                }
            }
            clientPlayer.update(delta);

            if (serverPlayer.isAttacking()) serverPlayer.checkHit(clientPlayer);
            if (clientPlayer.isAttacking()) clientPlayer.checkHit(serverPlayer);

            if (gameStatus == GameState.GameStatus.PLAYING) {
                topPanel.checkOutOfBounds(serverPlayer.getX(), serverPlayer.getY(), true);
                topPanel.checkOutOfBounds(clientPlayer.getX(), clientPlayer.getY(), false);
                topPanel.update(delta);
            }

            boolean sRespawn = false;
            boolean cRespawn = false;

            if (topPanel.getNeedChange1Player()) {
                serverPlayer.getBody().setTransform(START_PLAYER_SERVER_X * SCALE, START_PLAYER_SERVER_Y * SCALE, 0);
                serverPlayer.getBody().setLinearVelocity(0, 0);
                serverPlayer.setHealth(100);
                serverPlayer.setHitImmunityTimer(2.0f);
                sRespawn = true;
            }
            if (topPanel.getNeedChange2Player()) {
                clientPlayer.getBody().setTransform(START_PLAYER_CLIENT_X * SCALE, START_PLAYER_CLIENT_Y * SCALE, 0);
                clientPlayer.getBody().setLinearVelocity(0, 0);
                clientPlayer.setHealth(100);
                clientPlayer.setHitImmunityTimer(2.0f);
                cRespawn = true;
            }

            NetworkPacket packet = new NetworkPacket();
            packet.status = gameStatus;
            packet.countdown = countdown;
            packet.matchTimer = topPanel.getMatchTimer();
            packet.sNeedRespawn = sRespawn;
            packet.cNeedRespawn = cRespawn;

            packet.sX = serverPlayer.getBody().getPosition().x;
            packet.sY = serverPlayer.getBody().getPosition().y;
            packet.sVX = serverPlayer.getBody().getLinearVelocity().x;
            packet.sVY = serverPlayer.getBody().getLinearVelocity().y;
            packet.sHealth = serverPlayer.getHealth();
            packet.sLives = topPanel.getPlayer1Lives();
            packet.sFacingRight = serverPlayer.getFacingRight();
            packet.sIsAttacking = serverPlayer.isAttacking();
            packet.sAttackDir = serverPlayer.getCurrentAttackDirection();
            packet.sIsOut = topPanel.isPlayer1OutOfBounds();

            packet.cX = clientPlayer.getBody().getPosition().x;
            packet.cY = clientPlayer.getBody().getPosition().y;
            packet.cVX = clientPlayer.getBody().getLinearVelocity().x;
            packet.cVY = clientPlayer.getBody().getLinearVelocity().y;
            packet.cHealth = clientPlayer.getHealth();
            packet.cLives = topPanel.getPlayer2Lives();
            packet.cIsDodging = clientPlayer.isDodging();
            packet.cInHitStun = clientPlayer.isInHitStun();
            packet.cIsOut = topPanel.isPlayer2OutOfBounds();

            server.sendState(packet);

            if (gameStatus != GameState.GameStatus.FINISHED && gameStatus != GameState.GameStatus.WAITING && gameStatus != GameState.GameStatus.COUNTDOWN) {
                if (topPanel.getPlayer1Lives() <= 0 || topPanel.getPlayer2Lives() <= 0) {
                    endMatch();
                }
            }

        } else {
            NetworkPacket packet = client.latestPacket;

            applyPlayerInput(clientPlayer, localInput);
            clientPlayer.update(delta);

            if (packet != null) {
                gameStatus = packet.status;
                countdown = packet.countdown;
                topPanel.setMatchTimer(packet.matchTimer);
                topPanel.setPlayer1Lives(packet.sLives);
                topPanel.setPlayer2Lives(packet.cLives);
                topPanel.setPlayer1Out(packet.sIsOut);
                topPanel.setPlayer2Out(packet.cIsOut);

                if (packet.sNeedRespawn) {
                    serverPlayer.getBody().setTransform(packet.sX, packet.sY, 0);
                    serverPlayer.getBody().setLinearVelocity(0, 0);
                } else {
                    serverPlayer.getBody().setTransform(packet.sX, packet.sY, 0);
                    serverPlayer.getBody().setLinearVelocity(packet.sVX, packet.sVY);
                }

                serverPlayer.setHealth(packet.sHealth);
                serverPlayer.setFacingRight(packet.sFacingRight);
                if (packet.sIsAttacking && !serverPlayer.isAttacking()) {
                    serverPlayer.startAttack(packet.sAttackDir);
                }

                clientPlayer.setHealth(packet.cHealth);

                if (packet.cNeedRespawn) {
                    clientPlayer.getBody().setTransform(packet.cX, packet.cY, 0);
                    clientPlayer.getBody().setLinearVelocity(0, 0);
                } else if (packet.cInHitStun || packet.cIsDodging) {
                    clientPlayer.getBody().setTransform(packet.cX, packet.cY, 0);
                    clientPlayer.getBody().setLinearVelocity(packet.cVX, packet.cVY);
                }
            }

            if (gameStatus == GameState.GameStatus.PLAYING) {
                topPanel.update(delta);
            }

            localInput.x = clientPlayer.getBody().getPosition().x;
            localInput.y = clientPlayer.getBody().getPosition().y;
            localInput.vx = clientPlayer.getBody().getLinearVelocity().x;
            localInput.vy = clientPlayer.getBody().getLinearVelocity().y;
            localInput.health = clientPlayer.getHealth();
            localInput.facingRight = clientPlayer.getFacingRight();
            localInput.isAttacking = clientPlayer.isAttacking();
            localInput.attackDir = clientPlayer.getCurrentAttackDirection();

            client.sendInput(localInput);
            serverPlayer.update(delta);
        }
    }

    private void checkMatchEndConditions() {
        if (gameStatus != GameState.GameStatus.PLAYING) return;

        if (topPanel.getMatchTimer() <= 0) {
            if (topPanel.getPlayer1Lives() > 0 && topPanel.getPlayer2Lives() > 0) endMatchWithDraw();
            else endMatch();
        }
        if (topPanel.getPlayer1Lives() <= 0 || topPanel.getPlayer2Lives() <= 0) {
            endMatch();
        }
    }

    private void endMatch() {
        gameStatus = GameState.GameStatus.FINISHED;
        String winner;
        if (topPanel.getPlayer1Lives() <= 0 && topPanel.getPlayer2Lives() > 0) {
            winner = topPanel.getPlayer1Name() + " - WIN!";
        } else if (topPanel.getPlayer2Lives() <= 0 && topPanel.getPlayer1Lives() > 0) {
            winner = topPanel.getPlayer2Name() + " - WIN!";
        } else {
            winner = "DRAW!";
        }

        resultText.setText(winner);
        resultDisplayTimer = 3.0f;

        serverPlayer.setCanControl(false);
        clientPlayer.setCanControl(false);

        serverPlayer.getBody().setLinearVelocity(0, 0);
        clientPlayer.getBody().setLinearVelocity(0, 0);
    }

    private void endMatchWithDraw() {
        gameStatus = GameState.GameStatus.FINISHED;
        resultText.setText("DRAW!");
        resultDisplayTimer = 3.0f;

        serverPlayer.setCanControl(false);
        clientPlayer.setCanControl(false);

        serverPlayer.getBody().setLinearVelocity(0, 0);
        clientPlayer.getBody().setLinearVelocity(0, 0);
    }

    private void handleInput() {
        localInput.moveLeft = false;
        localInput.moveRight = false;
        localInput.wantToGoDown = false;
        localInput.attack = false;
        localInput.dodge = false;
        localInput.jump = false;

        updateUI();

        localInput.moveLeft = Gdx.input.isKeyPressed(Input.Keys.A) || (joystick.isCaptured() && joystick.isLeft());
        localInput.moveRight = Gdx.input.isKeyPressed(Input.Keys.D) || (joystick.isCaptured() && joystick.isRight());
        localInput.wantToGoDown = Gdx.input.isKeyPressed(Input.Keys.S) || (joystick.isCaptured() && joystick.isDown());

        boolean isJumpButtonDown = Gdx.input.isKeyPressed(Input.Keys.SPACE) || jumpButton.isPressed();
        localInput.jump = isJumpButtonDown && !jumpWasPressed;
        jumpWasPressed = isJumpButtonDown;

        localInput.dodge = Gdx.input.isKeyPressed(Input.Keys.Q) || dodgeButton.isPressed();
        localInput.attack = Gdx.input.isKeyPressed(Input.Keys.E) || attackButton.isPressed();
        localInput.attackUp = joystick.isUp() || Gdx.input.isKeyPressed(Input.Keys.W);
        localInput.attackDown = joystick.isDown() || Gdx.input.isKeyPressed(Input.Keys.S);

        localInput.x = clientPlayer.getBody().getPosition().x;
        localInput.y = clientPlayer.getBody().getPosition().y;
        localInput.vx = clientPlayer.getBody().getLinearVelocity().x;
        localInput.vy = clientPlayer.getBody().getLinearVelocity().y;
        localInput.health = clientPlayer.getHealth();
        localInput.facingRight = clientPlayer.getFacingRight();
        localInput.isAttacking = clientPlayer.isAttacking();
        localInput.attackDir = clientPlayer.getCurrentAttackDirection();
    }


    private void applyPlayerInput(PlayerObject p, PlayerInput in) {
        if (gameStatus != GameState.GameStatus.PLAYING || !p.canReceiveInput()) return;

        Vector2 vel = p.getBody().getLinearVelocity();
        float targetX;

        if (in.moveLeft && p.canMove()) {
            targetX = -PLAYER_MAX_VELOCITY;
        }
        else if (in.moveRight && p.canMove()) {
            targetX = PLAYER_MAX_VELOCITY;
        }
        else {
            targetX = vel.x * 0.8f;
            if (Math.abs(targetX) < 0.1f) targetX = 0;
        }

        if (!p.isInHitStun() && !p.isDodging()) {
            p.getBody().setLinearVelocity(targetX, vel.y);
        }

        if (in.jump && p.canJump()) {
            p.jump(PLAYER_JUMP_FORCE);
        }

        if (in.dodge && p.canDodge()) {
            float dir = 0;
            if (in.moveLeft) dir = -1;
            else if (in.moveRight) dir = 1;
            p.dodge(dir);
        }

        p.setWantsToGoDown(in.wantToGoDown);
        if (in.attack && p.canAttack()) {
            p.startAttack(in.attackUp ? AttackDirection.UP : (in.attackDown ? AttackDirection.DOWN : AttackDirection.SIDE));
        }
    }

    private void updateServerState(float delta) {
        if (gameStatus == GameState.GameStatus.WAITING && server.isConnected()) {
            gameStatus = GameState.GameStatus.COUNTDOWN;
            countdown = 3f;
        } else if (gameStatus == GameState.GameStatus.COUNTDOWN) {
            countdown -= delta;
            if (countdown <= 0) gameStatus = GameState.GameStatus.PLAYING;
        }
    }

    private void updateUI() {
        boolean anyTouch = false;
        jumpButton.setPressed(false);
        dodgeButton.setPressed(false);
        attackButton.setPressed(false);

        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                anyTouch = true;
                Vector3 t = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0));
                if (jumpButton.isHit(t.x, t.y)) jumpButton.setPressed(true);
                if (dodgeButton.isHit(t.x, t.y)) dodgeButton.setPressed(true);
                if (attackButton.isHit(t.x, t.y)) attackButton.setPressed(true);
                joystick.processTouch(t.x, t.y, true, i);
            } else {
                joystick.processTouch(0, 0, false, i);
            }
        }
        if (!anyTouch) joystick.reset();
    }

    private void draw() {
        Gdx.gl.glClearColor(0.15f, 0.2f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(myGdxGame.camera.combined);
        batch.begin();

        for (PlatformObject p : platforms) p.draw(batch);
        for (OneWayPlatformObject o : oneWayPlatforms) o.draw(batch);

        if (myGdxGame.isHost) {
            clientPlayer.draw(batch);
            serverPlayer.draw(batch);
        }
        else {
            serverPlayer.draw(batch);
            clientPlayer.draw(batch);
        }
        topPanel.draw(batch);
        joystick.draw(batch);
        jumpButton.draw(batch);
        dodgeButton.draw(batch);
        attackButton.draw(batch);

        if (gameStatus == GameState.GameStatus.WAITING) {
            waitingText.draw(batch);
        } else if (gameStatus == GameState.GameStatus.COUNTDOWN) {
            countdownText.setText("START IN: " + (int)Math.ceil(countdown));
            countdownText.setCenterX(SCREEN_WIDTH / 2f);
            countdownText.draw(batch);
        } else if (gameStatus == GameState.GameStatus.FINISHED) {
            resultText.setCenterX(SCREEN_WIDTH / 2f);
            resultText.draw(batch);
        }

        batch.end();
    }

    public void disconnect() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        connected = false;
    }
}
