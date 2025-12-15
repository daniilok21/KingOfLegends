package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;

import static io.github.some_example_name.GameSettings.*;

public class GameScreen implements Screen {

    private final MyGdxGame game;

    // Графические компоненты
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // Сетевые компоненты
    private Server server;
    private Client client;
    private GameState currentState;

    // Состояние игры
    private boolean connected = false;
    private float inputCooldown = 0;

    public GameScreen(MyGdxGame game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = game.batch;
        this.font = game.font;
        this.currentState = new GameState();
    }

    public void initializeNetwork() {
        if (game.isHost) {
            // Запускаем сервер
            server = new Server();
            server.start(PORT);
            currentState = server.getLocalState();
            connected = true;
        } else {
            // Подключаемся как клиент
            client = new Client();
            connected = client.connect(game.hostIp, PORT);
            if (!connected) {
                Gdx.app.error("GameScreen", "Failed to connect to server");
                game.showMenuScreen();
            }
        }
    }

    @Override
    public void show() {
        // Инициализация при показе экрана
        if (!connected) {
            initializeNetwork();
        }
    }

    @Override
    public void render(float delta) {
        // Обновление
        update(delta);

        // Отрисовка
        draw();
    }

    private void update(float delta) {
        inputCooldown -= delta;

        // Получение актуального состояния
        if (game.isHost) {
            currentState = server.getLocalState();
        } else if (client != null) {
            currentState = client.getState();
        }

        // Обработка ввода
        handleInput(delta);
    }

    private void handleInput(float delta) {
        boolean isTouched = Gdx.input.isTouched();
        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (inputCooldown <= 0 && (isTouched || leftPressed || rightPressed)) {
            PlayerInput input = new PlayerInput();
            input.moveRight = isTouched || rightPressed;
            input.moveLeft = leftPressed;

            if (game.isHost) {
                // Обработка на сервере
                if (input.moveRight) {
                    server.moveServerCubeRight();
                }
                if (input.moveLeft) {
                    server.moveServerCubeLeft();
                }
            } else {
                // Отправка на сервер
                client.sendInput(input);
            }

            inputCooldown = 0.1f;
        }

        // Выход в меню по кнопке BACK (Android) или ESC (Desktop)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMenuScreen();
        }
    }

    private void draw() {
        // Очистка экрана
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Отрисовка кубиков
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Серверный кубик (красный)
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(currentState.serverCubeX, currentState.serverCubeY,
            CUBE_SIZE, CUBE_SIZE);

        // Клиентский кубик (синий)
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(currentState.clientCubeX, currentState.clientCubeY,
            CUBE_SIZE, CUBE_SIZE);

        shapeRenderer.end();

        // Отрисовка текста
        batch.begin();
        String status = game.isHost ? "HOST (Red Cube)" : "CLIENT (Blue Cube)";
        font.draw(batch, status, 10, SCREEN_HEIGHT - 10);
        font.draw(batch, "Touch screen or press RIGHT to move right", 10, SCREEN_HEIGHT - 40);
        font.draw(batch, "Press LEFT to move left", 10, SCREEN_HEIGHT - 70);
        font.draw(batch, "Press BACK/ESC to return to menu", 10, SCREEN_HEIGHT - 100);
        batch.end();
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
        game.camera.setToOrtho(false, width, height);
        game.camera.update();
        batch.setProjectionMatrix(game.camera.combined);
        shapeRenderer.setProjectionMatrix(game.camera.combined);
    }

    @Override
    public void pause() {
        // Пауза при сворачивании приложения
    }

    @Override
    public void resume() {
        // Возобновление
    }

    @Override
    public void hide() {
        // При скрытии экрана отключаем сеть
        disconnect();
    }

    @Override
    public void dispose() {
        disconnect();
        shapeRenderer.dispose();
    }
}
