package io.github.some_example_name.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.some_example_name.MyGdxGame;

import static io.github.some_example_name.GameSettings.*;

public class MenuScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private String[] menuItems = {"Host Game", "Join Game", "Exit"};
    private int selectedItem = 0;

    // Режим ручного ввода (только для ПК)
    private boolean enteringIpManual = false;
    private String currentInput = "";

    // Режим TextField (для телефона и ПК — универсальный)
    private boolean usingTextField = false;
    private Stage ipStage;
    private TextField ipField;

    public MenuScreen(MyGdxGame game) {
        this.game = game;
        this.batch = game.batch;
        this.shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void show() {
        selectedItem = 0;
        enteringIpManual = false;
        usingTextField = false;
        currentInput = "";
    }

    @Override
    public void render(float delta) {
        if (usingTextField) {
            handleTextFieldInput();
            Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();
            game.titleFont.setColor(Color.YELLOW);
            game.titleFont.draw(batch, "Enter server IP:", SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2 + 60);
            batch.end();

            ipStage.act(delta);
            ipStage.draw();
        } else {
            handleManualInput(); // только для ПК

            Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            batch.begin();

            game.titleFont.draw(batch, "NETWORK CUBE GAME",
                SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT - 50);

            if (!enteringIpManual) {
                // Обычное меню
                for (int i = 0; i < menuItems.length; i++) {
                    float y = SCREEN_HEIGHT / 2 - i * 40;
                    if (i == selectedItem) {
                        game.titleFont.setColor(Color.GREEN);
                        game.titleFont.draw(batch, "> " + menuItems[i],
                            SCREEN_WIDTH / 2 - 100, y);
                        game.titleFont.setColor(Color.WHITE);
                    } else {
                        game.titleFont.draw(batch, menuItems[i],
                            SCREEN_WIDTH / 2 - 100, y);
                    }
                }
                game.titleFont.setColor(Color.LIGHT_GRAY);
                game.titleFont.draw(batch, "Use UP/DOWN to navigate, ENTER to select", 10, 40);
            } else {
                // Ручной ввод (только ПК)
                game.titleFont.setColor(Color.YELLOW);
                game.titleFont.draw(batch, "Enter server IP:", SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2 + 60);
                game.titleFont.setColor(Color.WHITE);
                game.titleFont.draw(batch, currentInput + "_", SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2 + 20);
                game.titleFont.setColor(Color.LIGHT_GRAY);
                game.titleFont.draw(batch, "Press ENTER to connect, ESC to cancel", 10, 40);
            }

            batch.end();
        }
    }

    private void handleManualInput() {
        if (enteringIpManual) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (isValidIP(currentInput)) {
                    game.hostIp = currentInput;
                    game.showGameScreen(false, game.hostIp);
                }
                enteringIpManual = false;
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
                enteringIpManual = false;
                return;
            }

            // Backspace
            if (Gdx.input.isKeyJustPressed(Input.Keys.DEL) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                if (!currentInput.isEmpty()) {
                    currentInput = currentInput.substring(0, currentInput.length() - 1);
                }
            }

            // --- ЦИФРЫ ---
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
                if (currentInput.length() < 15) currentInput += "0";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                if (currentInput.length() < 15) currentInput += "1";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                if (currentInput.length() < 15) currentInput += "2";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                if (currentInput.length() < 15) currentInput += "3";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
                if (currentInput.length() < 15) currentInput += "4";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
                if (currentInput.length() < 15) currentInput += "5";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
                if (currentInput.length() < 15) currentInput += "6";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) {
                if (currentInput.length() < 15) currentInput += "7";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_8)) {
                if (currentInput.length() < 15) currentInput += "8";
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
                if (currentInput.length() < 15) currentInput += "9";
            }

// --- ТОЧКА ---
            else if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
                if (currentInput.length() < 15 && !currentInput.isEmpty() && !currentInput.endsWith(".")) {
                    currentInput += ".";
                }
            }
            // Точка
            else if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)) {
                if (currentInput.length() < 15 && !currentInput.isEmpty() && !currentInput.endsWith(".")) {
                    currentInput += ".";
                }
            }

        } else {
            // Навигация по меню
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedItem = (selectedItem + 1) % menuItems.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedItem = (selectedItem - 1 + menuItems.length) % menuItems.length;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isTouched()) {
                if (Gdx.input.isTouched()) {
                    selectedItem = 0;
                }
                switch (selectedItem) {
                    case 0:
                        game.showGameScreen(true, null);
                        break;
                    case 1:
                        // Определяем платформу
                        if (Gdx.app.getType() == Application.ApplicationType.Android ||
                            Gdx.app.getType() == Application.ApplicationType.iOS) {
                            // На телефоне — используем TextField
                            setupTextFieldMode();
                        } else {
                            // На ПК — ручной ввод
                            enteringIpManual = true;
                            currentInput = "";
                        }
                        break;
                    case 2:
                        Gdx.app.exit();
                        break;
                }
            }
        }
    }

    private void setupTextFieldMode() {
        usingTextField = true;
        enteringIpManual = false;

        ipStage = new Stage(new ScreenViewport());

        Skin skin = new Skin();
        skin.add("default", game.defaultFont);

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.defaultFont;
        style.fontColor = Color.WHITE;
        style.cursor = (com.badlogic.gdx.scenes.scene2d.utils.Drawable) Color.YELLOW;
        skin.add("default", style);

        ipField = new TextField("", skin);
        ipField.setSize(400, 50);
        ipField.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f + 20);
        ipField.setMessageText("e.g. 192.168.1.100");

        // ✅ Используем анонимный класс вместо лямбды
        ipField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isDigit(c) || c == '.';
            }
        });

        ipStage.addActor(ipField);
        Gdx.input.setInputProcessor(ipStage);

        // 👇 Добавь это, чтобы клавиатура появилась на Android/iOS
        ipField.getOnscreenKeyboard().show(true);
    }

    private void handleTextFieldInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String ip = ipField.getText().trim();
            if (isValidIP(ip)) {
                game.hostIp = ip;
                game.showGameScreen(false, game.hostIp);
            }
            cleanupTextField();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            cleanupTextField();
        }
    }

    private void cleanupTextField() {
        usingTextField = false;
        if (ipStage != null) {
            ipStage.dispose();
            ipStage = null;
        }
        Gdx.input.setInputProcessor(null); // вернуть управление (если нужно)
    }

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void resize(int width, int height) {
        game.camera.setToOrtho(false, width, height);
        game.camera.update();
        batch.setProjectionMatrix(game.camera.combined);
        if (ipStage != null) {
            ipStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (ipStage != null) {
            ipStage.dispose();
        }
    }
}
