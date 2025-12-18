package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;
import io.github.some_example_name.objects.PlatformObject;

import java.util.ArrayList;

import static io.github.some_example_name.GameSettings.*;

public class GameScreen extends ScreenAdapter {

    private final MyGdxGame myGdxGame;

    // Графические компоненты
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private Texture whitePixel; // Для отрисовки платформ

    // Сетевые компоненты
    private Server server;
    private Client client;
    private GameState currentState;

    // Box2D тела
    private Body serverBody;
    private Body clientBody;

    // Платформы
    private ArrayList<PlatformObject> platforms;

    // UI кнопки для Android
    private com.badlogic.gdx.math.Rectangle leftButton;
    private com.badlogic.gdx.math.Rectangle rightButton;
    private com.badlogic.gdx.math.Rectangle jumpButton;
    private com.badlogic.gdx.math.Rectangle backButton;

    // Состояние кнопок
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;

    // Состояние игры
    private boolean connected = false;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = myGdxGame.batch;
        this.font = myGdxGame.font;
        this.currentState = new GameState();

        // Создаем белую текстуру для платформ
        whitePixel = new Texture("white_pixel.png"); // Нужно добавить в assets

        setupPlatforms();
        setupPhysicsBodies();
        setupUI();
        setupInput();
    }

    private void setupPlatforms() {
        platforms = new ArrayList<>();

        // Основная платформа по центру
        platforms.add(new PlatformObject(
            SCREEN_WIDTH/2 - PLATFORM_WIDTH/2,
            200,
            PLATFORM_WIDTH,
            PLATFORM_HEIGHT,
            null, // Можно указать текстуру
            myGdxGame.world
        ));

        // Левая платформа
        platforms.add(new PlatformObject(
            300,
            350,
            PLATFORM_WIDTH,
            PLATFORM_HEIGHT,
            null,
            myGdxGame.world
        ));

        // Правая платформа
        platforms.add(new PlatformObject(
            SCREEN_WIDTH - 300 - PLATFORM_WIDTH,
            350,
            PLATFORM_WIDTH,
            PLATFORM_HEIGHT,
            null,
            myGdxGame.world
        ));

        // Верхняя платформа
        platforms.add(new PlatformObject(
            SCREEN_WIDTH/2 - PLATFORM_WIDTH/2,
            500,
            PLATFORM_WIDTH,
            PLATFORM_HEIGHT,
            null,
            myGdxGame.world
        ));
    }

    private void setupPhysicsBodies() {
        // Серверный кубик
        serverBody = createPlayerBody(100, 400);
        // Клиентский кубик
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
        shape.setAsBox((CUBE_SIZE/2) * SCALE, (CUBE_SIZE/2) * SCALE);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.3f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }

    private void setupUI() {
        // Кнопки для мобильного управления
        int buttonSize = BUTTON_WIDTH;
        int margin = 50;

        leftButton = new com.badlogic.gdx.math.Rectangle(
            margin, margin, buttonSize, buttonSize
        );

        rightButton = new com.badlogic.gdx.math.Rectangle(
            margin + buttonSize + 20, margin, buttonSize, buttonSize
        );

        jumpButton = new com.badlogic.gdx.math.Rectangle(
            SCREEN_WIDTH - margin - buttonSize, margin, buttonSize, buttonSize
        );

        backButton = new com.badlogic.gdx.math.Rectangle(
            SCREEN_WIDTH - margin - buttonSize,
            SCREEN_HEIGHT - margin - buttonSize,
            buttonSize, buttonSize
        );
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // Android: Y инвертирован
                int y = SCREEN_HEIGHT - screenY;

                if (leftButton.contains(screenX, y)) {
                    leftPressed = true;
                    return true;
                }
                if (rightButton.contains(screenX, y)) {
                    rightPressed = true;
                    return true;
                }
                if (jumpButton.contains(screenX, y)) {
                    jumpPressed = true;
                    return true;
                }
                if (backButton.contains(screenX, y)) {
                    myGdxGame.showMenuScreen();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                int y = SCREEN_HEIGHT - screenY;

                if (leftButton.contains(screenX, y)) {
                    leftPressed = false;
                    return true;
                }
                if (rightButton.contains(screenX, y)) {
                    rightPressed = false;
                    return true;
                }
                if (jumpButton.contains(screenX, y)) {
                    jumpPressed = false;
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
        if (!connected) {
            initializeNetwork();
        }
    }

    @Override
    public void render(float delta) {
        handleContinuousInput();
        update(delta);
        draw();
    }

    private void handleContinuousInput() {
        if (!connected) return;

        if (myGdxGame.isHost) {
            // Применяем ввод к серверному телу
            Vector2 force = new Vector2(0, 0);
            if (leftPressed) force.x = -MOVE_FORCE;
            if (rightPressed) force.x = MOVE_FORCE;

            serverBody.applyForceToCenter(force, true);

            // Ограничиваем скорость
            Vector2 vel = serverBody.getLinearVelocity();
            vel.x = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, vel.x));
            serverBody.setLinearVelocity(vel);

            // Прыжок
            if (jumpPressed && Math.abs(serverBody.getLinearVelocity().y) < 0.1f) {
                serverBody.applyLinearImpulse(
                    new Vector2(0, JUMP_FORCE),
                    serverBody.getWorldCenter(),
                    true
                );
                jumpPressed = false;
            }

        } else if (client != null) {
            // Отправляем ввод на сервер
            PlayerInput input = new PlayerInput();
            input.moveLeft = leftPressed;
            input.moveRight = rightPressed;
            input.jump = jumpPressed;
            client.sendInput(input);

            // Сбрасываем прыжок после отправки
            if (jumpPressed) jumpPressed = false;

            // Получаем состояние от сервера
            GameState serverState = client.getState();
            if (serverState != null) {
                currentState = serverState;
                currentState.applyToPhysics();
            }
        }
    }

    private void update(float delta) {
        // Физика обновляется в MyGdxGame.stepWorld()

        // Обновляем локальное состояние для хоста
        if (myGdxGame.isHost) {
            currentState.updateFromPhysics();
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.15f, 0.2f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Рисуем платформы
        batch.begin();
        for (PlatformObject platform : platforms) {
            batch.draw(whitePixel,
                platform.getX(), platform.getY(),
                platform.getWidth(), platform.getHeight());
        }
        batch.end();

        // Рисуем кубики
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Серверный кубик (красный)
        Vector2 serverPos = serverBody.getPosition();
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
            serverPos.x / SCALE - CUBE_SIZE/2,
            serverPos.y / SCALE - CUBE_SIZE/2,
            CUBE_SIZE, CUBE_SIZE
        );

        // Клиентский кубик (синий)
        Vector2 clientPos = clientBody.getPosition();
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(
            clientPos.x / SCALE - CUBE_SIZE/2,
            clientPos.y / SCALE - CUBE_SIZE/2,
            CUBE_SIZE, CUBE_SIZE
        );

        shapeRenderer.end();

        // Рисуем UI кнопки
        drawButtons();

        // Рисуем текст
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, myGdxGame.isHost ? "HOST" : "CLIENT", 10, SCREEN_HEIGHT - 10);
        font.draw(batch, "LEFT", (int)leftButton.x + 30, (int)leftButton.y + 90);
        font.draw(batch, "RIGHT", (int)rightButton.x + 25, (int)rightButton.y + 90);
        font.draw(batch, "JUMP", (int)jumpButton.x + 30, (int)jumpButton.y + 90);
        font.draw(batch, "BACK", (int)backButton.x + 30, (int)backButton.y + 90);
        batch.end();
    }

    private void drawButtons() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Левая кнопка
        shapeRenderer.setColor(leftPressed ?
            new Color(0, 1, 0, BUTTON_ALPHA) :
            new Color(0.5f, 0.5f, 0.5f, BUTTON_ALPHA));
        shapeRenderer.rect(leftButton.x, leftButton.y, leftButton.width, leftButton.height);

        // Правая кнопка
        shapeRenderer.setColor(rightPressed ?
            new Color(0, 1, 0, BUTTON_ALPHA) :
            new Color(0.5f, 0.5f, 0.5f, BUTTON_ALPHA));
        shapeRenderer.rect(rightButton.x, rightButton.y, rightButton.width, rightButton.height);

        // Кнопка прыжка
        shapeRenderer.setColor(jumpPressed ?
            new Color(1, 1, 0, BUTTON_ALPHA) :
            new Color(0.8f, 0.4f, 0, BUTTON_ALPHA));
        shapeRenderer.rect(jumpButton.x, jumpButton.y, jumpButton.width, jumpButton.height);

        // Кнопка назад
        shapeRenderer.setColor(new Color(0.8f, 0.2f, 0.2f, BUTTON_ALPHA));
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);

        shapeRenderer.end();
    }

    public void disconnect() {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (client != null) {
            client.disconnect();
            client = null;
        }
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
        whitePixel.dispose();
        for (PlatformObject platform : platforms) {
            platform.dispose();
        }
    }
}
