package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.GameResources;
import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;
import io.github.some_example_name.objects.PlatformObject;
import io.github.some_example_name.objects.PlayerObject;

import java.util.ArrayList;

import static io.github.some_example_name.GameSettings.*;

public class GameScreen extends ScreenAdapter {

    private final MyGdxGame myGdxGame;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Server server;
    private Client client;
    private GameState currentState;

    private PlayerObject serverPlayer;
    private PlayerObject clientPlayer;

    private ArrayList<PlatformObject> platforms;

    private ButtonView leftButton;
    private ButtonView rightButton;
    private ButtonView jumpButton;
    private ButtonView dodgeButton;
    private ButtonView attackButton;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean dodgePressed = false;
    private boolean attackPressed = false;
    private boolean jumpWasPressed = false;
    private boolean dodgeWasPressed = false;

    private boolean connected = false;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = myGdxGame.batch;
        this.currentState = new GameState();

        setupPlatforms();
        setupPhysicsBodies();
        setupUI();
    }

    private void setupPlatforms() {
        platforms = new ArrayList<>();

        platforms.add(new PlatformObject(
            0, 200,
            1500,
            100,
            GameResources.PLATFORM,
            myGdxGame.world
        ));
    }

    private void setupPhysicsBodies() {
        serverPlayer = new PlayerObject(
            100, 400,
            PLAYER_WIDTH, PLAYER_HEIGHT,
            GameResources.RED_PLAYER_SPRITE_SHEET,
            myGdxGame.world
        );

        clientPlayer = new PlayerObject(
            SCREEN_WIDTH - 150, 400,
            PLAYER_WIDTH, PLAYER_HEIGHT,
            GameResources.BLUE_PLAYER_SPRITE_SHEET,
            myGdxGame.world
        );

        currentState.serverBody = serverPlayer.getBody();
        currentState.clientBody = clientPlayer.getBody();
    }

    private void setupUI() {
        int buttonSize = BUTTON_WIDTH;
        int offset = 50;

        leftButton = new ButtonView(
            offset, offset, buttonSize, buttonSize,
            GameResources.BUTTON_LEFT
        );

        rightButton = new ButtonView(
            offset + buttonSize + 20, offset, buttonSize, buttonSize,
            GameResources.BUTTON_RIGHT
        );

        jumpButton = new ButtonView(
            SCREEN_WIDTH - offset - buttonSize, offset, buttonSize, buttonSize,
            GameResources.BUTTON_JUMP
        );
        dodgeButton = new ButtonView(
            SCREEN_WIDTH - offset - buttonSize * 2 - 20, offset, buttonSize, buttonSize,
            GameResources.BUTTON_DODGE
        );
        attackButton = new ButtonView(
            SCREEN_WIDTH - offset - buttonSize * 3 - 2 * 20, offset, buttonSize, buttonSize,
            GameResources.BUTTON_ATTACK
        );
    }



    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
            server.setPhysicsBodies(serverPlayer.getBody(), clientPlayer.getBody());
            server.start(PORT);
            currentState = server.getLocalState();
            connected = true;
        }
        else {
            client = new Client();
            connected = client.connect(myGdxGame.hostIp, PORT);
            if (!connected) {
                System.out.println("Failed to connect to server");
                myGdxGame.showMenuScreen();
            }
        }
    }

    @Override
    public void show() {
        if (!connected) initializeNetwork();
    }

    @Override
    public void render(float delta) {
        handleInput();
        update(delta);
        draw();
        myGdxGame.stepWorld();
    }

    private void handleInput() {
        if (!connected) return;
        Vector3 touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

        if (!myGdxGame.isHost && client != null) {
            GameState serverState = client.getState();
            if (serverState != null) {
                currentState.gameStatus = serverState.gameStatus;
                currentState.countdownTimer = serverState.countdownTimer;
            }
        }

        if (currentState.gameStatus != GameState.GameStatus.PLAYING) {
            leftPressed = false;
            rightPressed = false;
            jumpPressed = false;
            dodgePressed = false;
            attackPressed = false;
            return;
        }

        if (!Gdx.input.isTouched()) {
            leftPressed = false;
            rightPressed = false;
            jumpPressed = false;
            dodgePressed = false;
            attackPressed = false;
            leftButton.setPressed(false);
            rightButton.setPressed(false);
            jumpButton.setPressed(false);
            dodgeButton.setPressed(false);
            attackButton.setPressed(false);
        }

        if (Gdx.input.isTouched()) {
            if (leftButton.isHit(touch.x, touch.y)) {
                leftPressed = true;
                leftButton.setPressed(true);
            }
            if (rightButton.isHit(touch.x, touch.y)) {
                rightPressed = true;
                rightButton.setPressed(true);
            }
            if (jumpButton.isHit(touch.x, touch.y)) {
                jumpPressed = true;
                jumpButton.setPressed(true);
            }
            if (dodgeButton.isHit(touch.x, touch.y)) {
                dodgePressed = true;
                dodgeButton.setPressed(true);
            }
            if (attackButton.isHit(touch.x, touch.y)) {
                attackPressed = true;
                attackButton.setPressed(true);
            }
        }

        if (myGdxGame.isHost) {
            Vector2 force = new Vector2(0, 0);
            if (leftPressed) force.x = -PLAYER_MOVE_FORCE;
            if (rightPressed) force.x = PLAYER_MOVE_FORCE;

            Body serverBody = serverPlayer.getBody();
            serverBody.applyForceToCenter(force, true);

            Vector2 vel = serverBody.getLinearVelocity();
            vel.x = Math.max(-PLAYER_MAX_VELOCITY, Math.min(PLAYER_MAX_VELOCITY, vel.x));
            serverBody.setLinearVelocity(vel);
            if (jumpPressed) {
                boolean jumpSuccessful = serverPlayer.jump(PLAYER_JUMP_FORCE);
                if (jumpSuccessful) {
                }
            }
            if (dodgePressed) {
                float dodgeDirection = 0;
                if (leftPressed) dodgeDirection = -1;
                else if (rightPressed) dodgeDirection = 1;
                else {
                    float velX = serverBody.getLinearVelocity().x;
                    if (velX != 0) {
                        if (velX > 0) dodgeDirection = 0;
                        else dodgeDirection = 1;
                    }
                }

                if (dodgeDirection != 0) {
                    boolean dodgeSuccessful = serverPlayer.dodge(dodgeDirection);
                    if (dodgeSuccessful) {
                    }
                }
            }

        } else if (client != null) {
            PlayerInput input = new PlayerInput();
            input.moveLeft = leftPressed;
            input.moveRight = rightPressed;
            input.jump = jumpPressed;
            input.dodge = dodgePressed;
            input.attack = attackPressed;
            client.sendInput(input);

            jumpPressed = false;
            dodgePressed = false;
            attackPressed = false;

            GameState serverState = client.getState();
            if (serverState != null) {
                serverState.serverBody = currentState.serverBody;
                serverState.clientBody = currentState.clientBody;

                currentState = serverState;
                currentState.applyToPhysics();
            }
        }
    }

    private void update(float delta) {
        serverPlayer.update(delta);
        clientPlayer.update(delta);
        if (myGdxGame.isHost) {
            updateGameState(delta);
            currentState.updateFromPhysics();
        }
    }
    private void updateGameState(float delta) {
        switch (currentState.gameStatus) {
            case WAITING:
                if (client != null && client.isConnected()) {
                    currentState.gameStatus = GameState.GameStatus.COUNTDOWN;
                    currentState.countdownTimer = 3.0f;
                }
                break;

            case COUNTDOWN:
                currentState.countdownTimer -= delta;
                if (currentState.countdownTimer <= 0) {
                    currentState.gameStatus = GameState.GameStatus.PLAYING;
                    System.out.println("Game started");
                }
                break;

            case PLAYING:
                break;
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.15f, 0.2f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        for (PlatformObject platform : platforms) {
            platform.draw(batch);
        }
        serverPlayer.draw(batch);
        clientPlayer.draw(batch);

        leftButton.draw(batch);
        rightButton.draw(batch);
        jumpButton.draw(batch);
        dodgeButton.draw(batch);
        attackButton.draw(batch);

        drawGameStateUI();

        batch.end();
    }

    private void drawGameStateUI() {
        myGdxGame.font.setColor(Color.WHITE);
        myGdxGame.font.draw(batch, myGdxGame.isHost ? "HOST" : "CLIENT", 10, SCREEN_HEIGHT - 10);

        switch (currentState.gameStatus) {
            case WAITING:
                myGdxGame.font.setColor(Color.WHITE);
                myGdxGame.font.draw(batch, "WAITING FOR CLIENT...", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
                break;

            case COUNTDOWN:
                myGdxGame.font.setColor(Color.WHITE);
                int seconds = (int) Math.ceil(currentState.countdownTimer);
                myGdxGame.font.draw(batch, "STARTING IN: " + seconds, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
                break;

            case PLAYING:
                break;
        }
    }

    public void disconnect() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        connected = false;
    }

    @Override
    public void resize(int width, int height) {
        myGdxGame.camera.setToOrtho(false, width, height);
        myGdxGame.camera.update();
        batch.setProjectionMatrix(myGdxGame.camera.combined);
        shapeRenderer.setProjectionMatrix(myGdxGame.camera.combined);
    }

    @Override
    public void hide() {
        disconnect();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        disconnect();
        shapeRenderer.dispose();

        leftButton.dispose();
        rightButton.dispose();
        jumpButton.dispose();
        dodgeButton.dispose();
        attackButton.dispose();

        serverPlayer.dispose();
        clientPlayer.dispose();

        for (PlatformObject platform : platforms) {
            platform.dispose();
        }
    }
}
