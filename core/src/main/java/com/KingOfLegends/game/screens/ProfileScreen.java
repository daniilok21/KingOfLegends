package com.KingOfLegends.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Application;

import com.KingOfLegends.game.MyGdxGame;
import com.KingOfLegends.game.GameResources;
import com.KingOfLegends.game.components.ButtonView;
import com.KingOfLegends.game.components.ImageView;
import com.KingOfLegends.game.components.MovingBackgroundView;
import com.KingOfLegends.game.components.TextView;
import com.KingOfLegends.game.managers.MemoryManager;

import static com.KingOfLegends.game.GameSettings.SCREEN_WIDTH;
import static com.KingOfLegends.game.GameSettings.SCREEN_HEIGHT;

public class ProfileScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private MovingBackgroundView background;
    private TextView titleView;
    private TextView nameLabel;
    private ButtonView changeNameButton;
    private ButtonView backButton;
    private ImageView board;

    private Stage inputStage;
    private TextField nameField;
    private ImageView nameEnterPlace;
    private boolean isEditingName = false;

    public ProfileScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        background = new MovingBackgroundView(GameResources.BACKGROUND_PROFILE);

        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 90, "PROFILE");
        titleView.setCenterX(SCREEN_WIDTH / 2f);

        nameLabel = new TextView(game.defaultMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f + 50, "Name: " + game.playerName);
        nameLabel.setCenterX(SCREEN_WIDTH / 2f);

        changeNameButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, SCREEN_HEIGHT / 2f - 90f, 440, 70,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Change Name"
        );

        backButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, SCREEN_HEIGHT / 2f - 180f, 440, 70,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Back"
        );

        board = new ImageView(SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 650f, 600, 500, GameResources.BOARD);
        board.setCenterX(SCREEN_WIDTH / 2f);

        nameEnterPlace = new ImageView(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f + 15, 440, 80, GameResources.BUTTON_MENU);
        nameEnterPlace.setCenterX(SCREEN_WIDTH / 2f);

        inputStage = new Stage(new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, game.camera));

        inputStage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!(event.getTarget() instanceof TextField)) {
                    isEditingName = false;
                    Gdx.input.setInputProcessor(null);
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
                return false;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        inputStage.getViewport().update(width, height, false);
        game.camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        game.camera.update();
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

        if (isEditingName) {
            nameEnterPlace.draw(game.batch);
        }

        if (!isEditingName) {
            nameLabel.setCenterX(SCREEN_WIDTH / 2f);
            nameLabel.draw(game.batch);
            changeNameButton.draw(game.batch);
            backButton.draw(game.batch);
        }

        game.batch.end();

        if (isEditingName) {
            updateNameFieldPosition();
            inputStage.act(delta);
            inputStage.draw();
        }
    }

    private void updateNameFieldPosition() {
        if (nameField != null) {
            float x = nameEnterPlace.getX() + nameEnterPlace.getWidth() / 2f - nameField.getWidth() / 2f + 5;
            float y = nameEnterPlace.getY() + nameEnterPlace.getHeight() / 2f - nameField.getHeight() / 2f;
            nameField.setPosition(x, y);
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (!isEditingName) {
                if (changeNameButton.isHit(touch.x, touch.y)) {
                    showNameInput();
                }
                if (backButton.isHit(touch.x, touch.y)) {
                    game.setScreen(game.menuScreen);
                }
            } else {
                if (!nameEnterPlace.isHit(touch.x, touch.y)) {
                    isEditingName = false;
                    Gdx.input.setInputProcessor(null);
                }
            }
        }
    }

    private void showNameInput() {
        if (Gdx.app.getType() == Application.ApplicationType.Android || Gdx.app.getType() == Application.ApplicationType.iOS) {
            Gdx.input.getTextInput(new Input.TextInputListener() {
                @Override
                public void input(String text) {
                    if (!text.trim().isEmpty()) {
                        saveName(text.trim());
                    }
                }
                @Override
                public void canceled() {}
            }, "Change Nickname", game.playerName, "Enter name");
            return;
        }

        isEditingName = true;
        Skin skin = new Skin();
        skin.add("default", game.textFieldFont);

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.textFieldFont;
        style.fontColor = Color.BROWN.cpy();
        style.background = null;
        skin.add("default", style);

        nameField = new TextField(game.playerName, skin);
        nameField.setSize(400, 50);
        nameField.setMaxLength(12);

        nameField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n' || c == '\r') {
                    saveName(textField.getText().trim());
                }
            }
        });

        inputStage.clear();
        inputStage.addActor(nameField);
        Gdx.input.setInputProcessor(inputStage);
        inputStage.setKeyboardFocus(nameField);
    }

    private void saveName(String name) {
        if (name != null && !name.isEmpty()) {
            game.playerName = name;
            MemoryManager.saveProfileName(game.playerName);
            nameLabel.setText("Name: " + game.playerName);
        }
        isEditingName = false;
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (inputStage != null) inputStage.dispose();
    }
}
