package com.KingOfLegends.game.screens;

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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import com.KingOfLegends.game.MyGdxGame;
import com.KingOfLegends.game.GameResources;
import com.KingOfLegends.game.components.ButtonView;
import com.KingOfLegends.game.components.ImageView;
import com.KingOfLegends.game.components.MovingBackgroundView;
import com.KingOfLegends.game.components.TextView;

import static com.KingOfLegends.game.GameSettings.SCREEN_WIDTH;
import static com.KingOfLegends.game.GameSettings.SCREEN_HEIGHT;

public class JoinScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private MovingBackgroundView background;
    private TextView titleView;
    private ButtonView connectButton;
    private ButtonView backButton;
    private Stage inputStage;
    private TextField ipField;
    private ImageView board;
    private ImageView ipEnterPlace;
    private String errorMessage = "";

    public JoinScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        float buttonY = SCREEN_HEIGHT / 2f - 100;

        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 90, "JOIN GAME");
        titleView.setCenterX(SCREEN_WIDTH / 2f);

        background = new MovingBackgroundView(GameResources.BACKGROUND_JOIN);

        connectButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY, 440, 80,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Connect"
        );

        backButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY - 90, 440, 80,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Back"
        );

        board = new ImageView(SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 650f, 600, 500, GameResources.BOARD);
        board.setCenterX(SCREEN_WIDTH / 2f);

        ipEnterPlace = new ImageView(0, 0, 440, 80, GameResources.BUTTON_MENU);

        inputStage = new Stage(new ScreenViewport());
        Skin skin = new Skin();
        skin.add("default", game.defaultMenuFont);

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.defaultMenuFont;
        style.fontColor = Color.WHITE;
        style.background = null;
        skin.add("default", style);

        ipField = new TextField("", skin);
        ipField.setSize(400, 50);
        ipField.setMaxLength(15);
        ipField.setMessageText("Enter IP...");
        ipField.setTextFieldFilter((textField, c) -> Character.isDigit(c) || c == '.');
        ipField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n' || c == '\r') {
                    submitIp(textField.getText().trim());
                }
            }
        });

        inputStage.addActor(ipField);
        Gdx.input.setInputProcessor(inputStage);
    }

    @Override
    public void render(float delta) {
        handleInput();

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(new Color(0, 0, 0, 1));

        game.batch.begin();

        background.draw(game.batch);
        titleView.draw(game.batch);
        board.draw(game.batch);

        ipEnterPlace.setCenterX(SCREEN_WIDTH / 2f);
        ipEnterPlace.setY(SCREEN_HEIGHT / 2f + 15);
        ipEnterPlace.draw(game.batch);

        connectButton.draw(game.batch);
        backButton.draw(game.batch);

        if (!errorMessage.isEmpty()) {
            game.defaultMenuFont.setColor(Color.RED);
            game.defaultMenuFont.draw(game.batch, errorMessage, SCREEN_WIDTH / 2f - 80, SCREEN_HEIGHT / 2f - 80);
            game.defaultMenuFont.setColor(Color.WHITE);
        }

        game.batch.end();

        updateIpFieldPosition();

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

    private void updateIpFieldPosition() {
        if (ipField != null) {
            float x = Gdx.graphics.getWidth() / 2f - ipField.getWidth() / 2f + 90;
            float y = Gdx.graphics.getHeight() / 2f + 55;
            ipField.setPosition(x, y);
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
            errorMessage = "";
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
