package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.GameResources;
import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;
import io.github.some_example_name.objects.PlatformObject;

import java.util.ArrayList;

import static io.github.some_example_name.GameSettings.*;

public class GameScreen extends ScreenAdapter {

    private final MyGdxGame myGdxGame;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Server server;
    private Client client;
    private GameState currentState;

    private Body serverBody;
    private Body clientBody;

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
        setupInput();
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
        serverBody = createPlayerBody(100, 400);
        clientBody = createPlayerBody(SCREEN_WIDTH - 150, 400);

        currentState.serverBody = serverBody;
        currentState.clientBody = clientBody;
    }

    private Body createPlayerBody(float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(x * SCALE, y * SCALE);

        Body body = myGdxGame.world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((CUBE_SIZE / 2) * SCALE, (CUBE_SIZE / 2) * SCALE);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.1f;

        fixtureDef.filter.categoryBits = PLAYER_BIT;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }

    private void setupUI() {
        int buttonSize = BUTTON_WIDTH;
        int margin = 50;

        // Используем твои ButtonView компоненты
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

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                int y = SCREEN_HEIGHT - screenY;

                if (leftButton.isHit(screenX, y)) {
                    leftPressed = true;
                    leftButton.setPressed(true);
                    return true;
                }
                if (rightButton.isHit(screenX, y)) {
                    rightPressed = true;
                    rightButton.setPressed(true);
                    return true;
                }
                if (jumpButton.isHit(screenX, y)) {
                    jumpPressed = true;
                    jumpButton.setPressed(true);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                int y = SCREEN_HEIGHT - screenY;

                if (leftButton.isHit(screenX, y)) {
                    leftPressed = false;
                    leftButton.setPressed(false);
                    return true;
                }
                if (rightButton.isHit(screenX, y)) {
                    rightPressed = false;
                    rightButton.setPressed(false);
                    return true;
                }
                if (jumpButton.isHit(screenX, y)) {
                    jumpPressed = false;
                    jumpButton.setPressed(false);
                    return true;
                }
                return false;
            }
        });
    }

    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
            server.setPhysicsBodies(serverBody, clientBody);
            server.start(PORT);
            currentState = server.getLocalState();
            connected = true;
        } else {
            client = new Client();
            connected = client.connect(myGdxGame.hostIp, PORT);
            if (!connected) {
                Gdx.app.error("GameScreen", "Failed to connect to server");
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

        if (myGdxGame.isHost) {
            Vector2 force = new Vector2(0, 0);
            if (leftPressed) force.x = -MOVE_FORCE;
            if (rightPressed) force.x = MOVE_FORCE;

            serverBody.applyForceToCenter(force, true);

            Vector2 vel = serverBody.getLinearVelocity();
            vel.x = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, vel.x));
            serverBody.setLinearVelocity(vel);

            if (jumpPressed && Math.abs(serverBody.getLinearVelocity().y) < 0.1f) {
                serverBody.applyLinearImpulse(new Vector2(0, JUMP_FORCE), serverBody.getWorldCenter(), true);
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
                currentState = serverState;
                currentState.applyToPhysics();
            }
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

        // Рисуем кнопки
        leftButton.draw(batch);
        rightButton.draw(batch);
        jumpButton.draw(batch);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Vector2 serverPos = serverBody.getPosition();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
            currentState.serverCubeX - CUBE_SIZE / 2,
            currentState.serverCubeY - CUBE_SIZE / 2,
            CUBE_SIZE, CUBE_SIZE
        );

        Vector2 clientPos = clientBody.getPosition();
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(
            currentState.clientCubeX - CUBE_SIZE / 2,
            currentState.clientCubeY - CUBE_SIZE / 2,
            CUBE_SIZE, CUBE_SIZE
        );

        shapeRenderer.end();

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

        // Удаляем кнопки
        leftButton.dispose();
        rightButton.dispose();
        jumpButton.dispose();

        for (PlatformObject platform : platforms) {
            platform.dispose();
        }
    }
}
