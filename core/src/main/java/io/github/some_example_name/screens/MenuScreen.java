package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.MyGdxGame;

import static io.github.some_example_name.GameSettings.*;

public class MenuScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private String[] menuItems = {"Host Game", "Join Game", "Exit"};
    private int selectedItem = 0;

    private boolean enteringIp = false;
    private String ipAddress;

    public MenuScreen(MyGdxGame game) {
        this.game = game;
        this.batch = game.batch;
        this.shapeRenderer = new ShapeRenderer();
        this.ipAddress = game.hostIp;
    }

    @Override
    public void show() {
        selectedItem = 0;
        enteringIp = false;
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Заголовок
        game.titleFont.draw(batch, "NETWORK CUBE GAME",
            SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT - 50);
        game.titleFont.getData().setScale(1.0f);

        // Элементы меню
        game.titleFont.setColor(Color.WHITE);
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedItem) {
                game.titleFont.setColor(Color.GREEN);
                game.titleFont.draw(batch, "> " + menuItems[i],
                    SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - i * 40);
                game.titleFont.setColor(Color.WHITE);
            } else {
                game.titleFont.draw(batch, menuItems[i],
                    SCREEN_WIDTH / 2 - 100, SCREEN_HEIGHT / 2 - i * 40);
            }
        }

        // Инструкции
        game.titleFont.setColor(Color.LIGHT_GRAY);
        game.titleFont.draw(batch, "Use UP/DOWN to navigate, ENTER to select",
            10, 40);

        batch.end();
    }

    private void handleInput() {
        // Навигация по меню
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedItem = (selectedItem + 1) % menuItems.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedItem = (selectedItem - 1 + menuItems.length) % menuItems.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isTouched()) {
            if (enteringIp) {
                // Подтверждение IP
                game.showGameScreen(false, ipAddress.toString());
                enteringIp = false;
            } else {
                if (Gdx.input.isTouched()) {
                    selectedItem = 0;
                }
                // Выбор пункта меню
                switch (selectedItem) {
                    case 0: // Host Game
                        game.showGameScreen(true, null);
                        break;
                    case 1: // Join Game
                        enteringIp = true;
                        break;
                    case 2: // Exit
                        Gdx.app.exit();
                        break;
                }
            }
        }

        // Выход по кнопке BACK
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) && enteringIp) {
            enteringIp = false;
        }
    }


    @Override
    public void resize(int width, int height) {
        game.camera.setToOrtho(false, width, height);
        game.camera.update();
        batch.setProjectionMatrix(game.camera.combined);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
