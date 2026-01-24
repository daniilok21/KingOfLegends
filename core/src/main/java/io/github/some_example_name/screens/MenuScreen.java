package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(game.camera.combined);
        batch.begin();

        game.titleFont.draw(batch, "KING OF LEGENDS",
            SCREEN_WIDTH / 2 - 200, SCREEN_HEIGHT - 100);

        // Элементы меню
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedItem) {
                game.titleFont.setColor(Color.GREEN);
                game.titleFont.draw(batch, "> " + menuItems[i],
                    SCREEN_WIDTH / 2 - 120, SCREEN_HEIGHT / 2 - i * 60);
                game.titleFont.setColor(Color.WHITE);
            } else {
                game.titleFont.draw(batch, menuItems[i],
                    SCREEN_WIDTH / 2 - 120, SCREEN_HEIGHT / 2 - i * 60);
            }
        }

        game.smallFont.draw(batch, "Use UP/DOWN to navigate, ENTER to select", 50, 50);
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedItem = (selectedItem + 1) % menuItems.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedItem = (selectedItem - 1 + menuItems.length) % menuItems.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (enteringIp) {
                game.showGameScreen(false, ipAddress);
                enteringIp = false;
            } else {
                switch (selectedItem) {
                    case 0: game.showGameScreen(true, null); break;
                    case 1: game.showGameScreen(false, ipAddress); break;
                    case 2: Gdx.app.exit(); break;
                }
            }
        }

        // Поддержка тача для меню
        if (Gdx.input.justTouched()) {
            // надо это убрать и заменить на норм кнопки
            if (selectedItem == 0) game.showGameScreen(true, null);
            else if (selectedItem == 1) game.showGameScreen(false, ipAddress);
            else if (selectedItem == 2) Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        float screenRatio = width / (float) height;
        float targetRatio = SCREEN_WIDTH / (float) SCREEN_HEIGHT;

        if (screenRatio > targetRatio) {
            float newWidth = SCREEN_HEIGHT * screenRatio;
            game.camera.viewportWidth = newWidth;
            game.camera.viewportHeight = SCREEN_HEIGHT;
        }
        else {
            float newHeight = SCREEN_WIDTH / screenRatio;
            game.camera.viewportWidth = SCREEN_WIDTH;
            game.camera.viewportHeight = newHeight;
        }

        game.camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        game.camera.update();
    }

    @Override public void dispose() { shapeRenderer.dispose(); }
}
