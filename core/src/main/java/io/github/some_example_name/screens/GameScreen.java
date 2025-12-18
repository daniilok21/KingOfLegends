package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;

import static io.github.some_example_name.GameSettings.*;

public class GameScreen extends ScreenAdapter {

    private final MyGdxGame myGdxGame;

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

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = myGdxGame.batch;
        this.font = myGdxGame.font;
        this.currentState = new GameState();
    }

    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
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
        update(delta);

        draw();
    }

    private void update(float delta) {
        inputCooldown -= delta;

        if (myGdxGame.isHost) {
            currentState = server.getLocalState();
        } else if (client != null) {
            currentState = client.getState();
        }

        handleInput(delta);
    }

    private void handleInput(float delta) {
        if (Gdx.input.isTouched()) {
            Vector3 touch = myGdxGame.camera.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
            );
        }
//        boolean leftPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
//        boolean rightPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
//
//        if (inputCooldown <= 0 && (isTouched || leftPressed || rightPressed)) {
//            PlayerInput input = new PlayerInput();
//            input.moveRight = isTouched || rightPressed;
//            input.moveLeft = leftPressed;
//
//            if (game.isHost) {
//                // Обработка на сервере
//                if (input.moveRight) {
//                    server.moveServerCubeRight();
//                }
//                if (input.moveLeft) {
//                    server.moveServerCubeLeft();
//                }
//            } else {
//                // Отправка на сервер
//                client.sendInput(input);
//            }
//
//            inputCooldown = 0f;
//        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
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
        myGdxGame.camera.setToOrtho(false, width, height);
        myGdxGame.camera.update();
        batch.setProjectionMatrix(myGdxGame.camera.combined);
        shapeRenderer.setProjectionMatrix(myGdxGame.camera.combined);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        disconnect();
    }

    @Override
    public void dispose() {
        disconnect();
        shapeRenderer.dispose();
    }
}
