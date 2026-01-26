package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.GameResources;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.TextView;

import static io.github.some_example_name.GameSettings.SCREEN_WIDTH;
import static io.github.some_example_name.GameSettings.SCREEN_HEIGHT;

public class MenuScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private TextView titleView;
    private ButtonView hostButton;
    private ButtonView joinButton;
    private ButtonView exitButton;

    // Режим TextField (для всех платформ)
    private boolean usingTextField = false;
    private Stage ipStage;
    private TextField ipField;

    public MenuScreen(MyGdxGame game) {
        this.game = game;

        titleView = new TextView(game.titleFont, SCREEN_WIDTH / 2f - 150, SCREEN_HEIGHT - 50, "NETWORK CUBE GAME");

        float buttonY = SCREEN_HEIGHT / 2f + 60;
        hostButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY, 440, 70,
            game.defaultFont, GameResources.BUTTON_MENU, "Host Game"
        );
        joinButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY - 90, 440, 70,
            game.defaultFont, GameResources.BUTTON_MENU, "Join Game"
        );
        exitButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY - 180, 440, 70,
            game.defaultFont, GameResources.BUTTON_MENU, "Exit"
        );
    }

    @Override
    public void render(float delta) {
        // Обработка ввода только в основном меню
        if (!usingTextField) {
            handleMenuInput();
        }

        if (usingTextField) {
            handleTextFieldInput(); // обработка Back/Esc на ПК
            ScreenUtils.clear(new Color(0.2f, 0.2f, 0.3f, 1));

            game.batch.begin();
            game.titleFont.setColor(Color.YELLOW);
            game.titleFont.draw(game.batch, "Enter server IP:", SCREEN_WIDTH / 2f - 200, SCREEN_HEIGHT / 2f + 90);
            game.batch.end();

            ipStage.act(delta);
            ipStage.draw();
        } else {
            game.camera.update();
            game.batch.setProjectionMatrix(game.camera.combined);
            ScreenUtils.clear(new Color(0.2f, 0.2f, 0.3f, 1));

            game.batch.begin();
            titleView.draw(game.batch);
            hostButton.draw(game.batch);
            joinButton.draw(game.batch);
            exitButton.draw(game.batch);
            game.batch.end();
        }
    }

    private void handleMenuInput() {
        // Управление мышью/тачем (все платформы)
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (hostButton.isHit(touch.x, touch.y)) {
                game.showGameScreen(true, null);
            } else if (joinButton.isHit(touch.x, touch.y)) {
                showIpInputScreen();
            } else if (exitButton.isHit(touch.x, touch.y)) {
                Gdx.app.exit();
            }
        }

        // Доп. управление клавиатурой (только ПК)
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.showGameScreen(true, null); // Host by default on PC
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
            }
        }
    }

    private void showIpInputScreen() {
        usingTextField = true;

        ipStage = new Stage(new ScreenViewport());

        Skin skin = new Skin();
        skin.add("default", game.defaultFont);

        // Стиль TextField
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = game.defaultFont;
        textFieldStyle.fontColor = Color.WHITE;
        skin.add("default", textFieldStyle);

        // Стиль кнопок
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.defaultFont;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        ipField = new TextField("", skin);
        ipField.setSize(400, 50);
        ipField.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f + 20);
        ipField.setMessageText("");
        ipField.setMaxLength(15);

        ipField.setTextFieldFilter(new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.isDigit(c) || c == '.';
            }
        });

        // Подтверждение через Enter (на ПК) или "Готово" (на телефоне)
        ipField.setTextFieldListener(new TextField.TextFieldListener() {

            @Override
            public void keyTyped(TextField textField, char c) {

            }

            public void enterPressed(TextField textField) {
                submitIp(textField.getText().trim());
            }
        });

        // Кнопка Connect
        TextButton connectBtn = new TextButton("Connect", skin);
        connectBtn.setSize(150, 50);
        connectBtn.setPosition(Gdx.graphics.getWidth() / 2f - 75, Gdx.graphics.getHeight() / 2f - 40);
        connectBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                submitIp(ipField.getText().trim());
            }
        });

        // Кнопка Back
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.setSize(150, 50);
        backBtn.setPosition(Gdx.graphics.getWidth() / 2f - 225, Gdx.graphics.getHeight() / 2f - 40);
        backBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                closeIpScreen();
            }
        });

        ipStage.addActor(ipField);
        ipStage.addActor(connectBtn);
        ipStage.addActor(backBtn);

        Gdx.input.setInputProcessor(ipStage);
        ipField.getOnscreenKeyboard().show(true);
    }

    private void submitIp(String ip) {
        if (isValidIP(ip)) {
            game.hostIp = ip;
            game.showGameScreen(false, game.hostIp);
        }
        closeIpScreen();
    }

    private void handleTextFieldInput() {
        // Доп. управление клавиатурой на ПК
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                submitIp(ipField.getText().trim());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                closeIpScreen();
            }
        }
    }

    private void closeIpScreen() {
        usingTextField = false;
        if (ipStage != null) {
            ipStage.dispose();
            ipStage = null;
        }
        Gdx.input.setInputProcessor(null);
    }

    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return false;
        for (String p : parts) {
            try {
                int n = Integer.parseInt(p);
                if (n < 0 || n > 255) return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        if (ipStage != null) {
            ipStage.dispose();
        }
    }
}
