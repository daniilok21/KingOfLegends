package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Application;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.GameResources;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.TextView;

import static io.github.some_example_name.GameSettings.SCREEN_WIDTH;
import static io.github.some_example_name.GameSettings.SCREEN_HEIGHT;

public class JoinScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private TextView titleView;
    private ButtonView connectButton;
    private ButtonView backButton;
    private Stage inputStage;
    private TextField ipField;
    private String errorMessage = "";

    public JoinScreen(MyGdxGame game) {
        this.game = game;

        float buttonY = SCREEN_HEIGHT / 2f - 100;
        connectButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY, 440, 70,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Connect"
        );
        backButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY - 90, 440, 70,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Back"
        );
        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 90, "JOIN GAME");
        titleView.setCenterX(SCREEN_WIDTH / 2f);
    }

    @Override
    public void show() {
        inputStage = new Stage(new ScreenViewport());
        Skin skin = new Skin();
        skin.add("default", game.defaultMenuFont);


        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.defaultFont;
        style.fontColor = Color.WHITE;
        skin.add("default", style);

        ipField = new TextField("", skin);
        ipField.setSize(400, 50);
        ipField.setPosition(Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f + 20);
        ipField.setMessageText("Enter IP...");
        ipField.setMaxLength(15);
        ipField.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
        ipField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n' || c == '\r') { // Enter / Готово
                    submitIp(textField.getText().trim());
                }
            }
        });

        inputStage.addActor(ipField);
        Gdx.input.setInputProcessor(inputStage);
        ipField.getOnscreenKeyboard().show(true);
    }

    @Override
    public void render(float delta) {
        handleInput();

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(new Color(0.2f, 0.2f, 0.3f, 1));


        game.batch.begin();

        titleView.draw(game.batch);
        connectButton.draw(game.batch);
        backButton.draw(game.batch);




        if (!errorMessage.isEmpty()) {
            game.defaultFont.setColor(Color.RED);
            game.defaultFont.draw(game.batch, errorMessage, SCREEN_WIDTH / 2f - 80, SCREEN_HEIGHT / 2f - 80);
            game.defaultFont.setColor(Color.WHITE); // сброс цвета
        }
        game.batch.end();


        inputStage.act(delta);
        inputStage.draw();


        // Доп. управление на ПК
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                submitIp(ipField.getText().trim());
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                game.setScreen(game.menuScreen);
            }
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (connectButton.isHit(touch.x, touch.y)) {
                submitIp(ipField.getText().trim());
            }
            if (backButton.isHit(touch.x, touch.y)) {
                game.setScreen(game.menuScreen);
            }
        }
    }

    private void submitIp(String ip) {
        if (isValidIP(ip)) {
            game.hostIp = ip;
            game.showGameScreen(false, game.hostIp);
        } else {
            errorMessage = "Invalid IP";
            Gdx.app.postRunnable(() -> {
                if (errorMessage.equals("Invalid IP")) {
                    errorMessage = "";
                }
            });
        }
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
        if (inputStage != null) inputStage.dispose();
    }
}
