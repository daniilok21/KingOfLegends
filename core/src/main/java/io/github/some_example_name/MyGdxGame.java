package io.github.some_example_name;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    BitmapFont font;
    boolean isHost = true; // ← true на хосте, false на клиенте
    String hostIp = "192.168.0.15"; // ← ЗАМЕНИТЕ НА IP ХОСТА!

    Server server;
    Client client;
    GameState localState;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        if (isHost) {
            server = new Server();
            server.start(9090);
            localState = server.getLocalState();
        } else {
            client = new Client();
            if (!client.connect(hostIp, 9090)) {
                Gdx.app.exit(); // не удалось подключиться
            }
            localState = new GameState();
        }
    }

    @Override
    public void render() {
        // Обработка ввода
        if (Gdx.input.isTouched()) {
            if (isHost) {
                localState.cubeX += 5;
                if (localState.cubeX > 800) localState.cubeX = 0;
            } else {
                PlayerInput input = new PlayerInput();
                input.moveRight = true;
                client.sendInput(input);
            }
        }

        // Получение состояния (клиент)
        if (!isHost) {
            localState = client.getState();
        }

        // Рендеринг
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(localState.cubeX, localState.cubeY, 50, 50);
        shapeRenderer.end();

        batch.begin();
        batch.setColor(Color.WHITE);
        font.draw(batch, isHost ? "HOST" : "CLIENT", 10, Gdx.graphics.getHeight() - 10);
        batch.end();
    }

    @Override
    public void dispose() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        batch.dispose();
        shapeRenderer.dispose();
    }
}
