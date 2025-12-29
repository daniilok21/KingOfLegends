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

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;

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
        int margin = 50;

        leftButton = new ButtonView(
            margin, margin, buttonSize, buttonSize,
            GameResources.BUTTON_LEFT
        );

        rightButton = new ButtonView(
            margin + buttonSize + 20, margin, buttonSize, buttonSize,
            GameResources.BUTTON_RIGHT
        );

        jumpButton = new ButtonView(
            SCREEN_WIDTH - margin - buttonSize, margin, buttonSize, buttonSize,
            GameResources.BUTTON_JUMP
        );
    }



    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
            server.setPhysicsBodies(serverPlayer.getBody(), clientPlayer.getBody());
            server.start(PORT);
            currentState = server.getLocalState();
            connected = true;
        } else {
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

            if (jumpPressed && Math.abs(serverBody.getLinearVelocity().y) < 0.1f) {
                serverBody.applyLinearImpulse(new Vector2(0, PLAYER_JUMP_FORCE),
                    serverBody.getWorldCenter(), true);
                jumpPressed = false;
            }

        } else if (client != null) {
            PlayerInput input = new PlayerInput();
            input.moveLeft = leftPressed;
            input.moveRight = rightPressed;
            input.jump = jumpPressed;
            client.sendInput(input);

            if (jumpPressed) jumpPressed = false;

            GameState serverState = client.getState();
            if (serverState != null) {
                serverState.serverBody = currentState.serverBody;
                serverState.clientBody = currentState.clientBody;

                currentState = serverState;
                currentState.applyToPhysics();
            }
        }
        if (!Gdx.input.isTouched()) {
            leftPressed = false;
            rightPressed = false;
            jumpPressed = false;
            leftButton.setPressed(false);
            rightButton.setPressed(false);
            jumpButton.setPressed(false);
        }
    }

    private void update(float delta) {
        if (myGdxGame.isHost) {
            currentState.updateFromPhysics();
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
        batch.end();
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);


        batch.begin();
        myGdxGame.font.setColor(Color.WHITE);
        myGdxGame.font.draw(batch, myGdxGame.isHost ? "HOST" : "CLIENT", 10, SCREEN_HEIGHT - 10);
        batch.end();
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

        serverPlayer.dispose();
        clientPlayer.dispose();

        for (PlatformObject platform : platforms) {
            platform.dispose();
        }
    }
}
