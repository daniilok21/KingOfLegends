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
    boolean isHost = true;
    String hostIp = "192.168.0.15";

    Server server;
    Client client;
    GameState localState;


    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        if (isHost) {
            server = new Server();
            server.start(9090);
            localState = server.getLocalState();
        }
        else {
            client = new Client();
            if (!client.connect(hostIp, 9090)) {
                Gdx.app.exit();
            }
            localState = new GameState();
        }
    }

    @Override
    public void render() {
        // Обновление состояния
        if (!isHost) {
            localState = client.getState();
        } else {
            localState = server.getLocalState();
        }

        // Обработка ввода
        if (Gdx.input.isTouched()) {
            if (isHost) {
                server.moveServerCubeRight();
            }
            else {
                PlayerInput input = new PlayerInput();
                input.moveRight = true;
                client.sendInput(input);
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            if (isHost) {
                server.moveServerCubeLeft();
            }
            else {
                PlayerInput input = new PlayerInput();
                input.moveLeft = true;
                client.sendInput(input);
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // хост
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(localState.serverCubeX, localState.serverCubeY, 50, 50);

        // клиент
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(localState.clientCubeX, localState.clientCubeY, 50, 50);

        shapeRenderer.end();

    }

    @Override
    public void dispose() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        batch.dispose();
        shapeRenderer.dispose();
    }
}
