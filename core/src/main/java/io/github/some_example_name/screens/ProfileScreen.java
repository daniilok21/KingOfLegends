package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.GameResources;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.ImageView;
import io.github.some_example_name.components.MovingBackgroundView;
import io.github.some_example_name.components.TextView;
import io.github.some_example_name.managers.MemoryManager;

import static io.github.some_example_name.GameSettings.SCREEN_WIDTH;
import static io.github.some_example_name.GameSettings.SCREEN_HEIGHT;

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

        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f - 150, SCREEN_HEIGHT - 90, "PROFILE");
        titleView.setCenterX(SCREEN_WIDTH / 2f);

        nameLabel = new TextView(game.defaultMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f + 100, "Name: " + game.playerName);
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

        nameEnterPlace = new ImageView(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 440, 80, GameResources.BUTTON_MENU);
        nameEnterPlace.setCenterX(SCREEN_WIDTH / 2f);
    }

    @Override
    public void render(float delta) {
        if (!isEditingName) {
            handleInput();
        }

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
        GlyphLayout layout = new GlyphLayout(nameLabel.font, nameLabel.text);
        nameLabel.width = layout.width;
        nameLabel.height = layout.height;
        nameLabel.setCenterX(SCREEN_WIDTH / 2f);
        nameLabel.draw(game.batch);

        if (!isEditingName) {
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
            float x = Gdx.graphics.getWidth() / 2f - nameField.getWidth() / 2f + 40;
            float y = Gdx.graphics.getHeight() / 2f + 35;
            nameField.setPosition(x, y);
        }
    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (changeNameButton.isHit(touch.x, touch.y)) {
                showNameInput();
            }
            if (backButton.isHit(touch.x, touch.y)) {
                game.setScreen(game.menuScreen);
            }
        }
    }

    private void showNameInput() {
        isEditingName = true;

        inputStage = new Stage(new ScreenViewport());
        Skin skin = new Skin();
        skin.add("default", game.defaultMenuFont);

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.defaultMenuFont;
        style.fontColor = Color.WHITE;
        style.background = null;
        skin.add("default", style);

        nameField = new TextField(game.playerName, skin);
        nameField.setSize(400, 50);
        nameField.setMaxLength(12);
        nameField.setMessageText("Enter Name...");

        nameField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n' || c == '\r') {
                    saveName(textField.getText().trim());
                }
            }
        });

        inputStage.addActor(nameField);
        Gdx.input.setInputProcessor(inputStage);

        inputStage.setKeyboardFocus(nameField);
    }

    private void saveName(String name) {
        if (name != null && !name.isEmpty()) {
            game.playerName = name;
            MemoryManager.saveProfileName(game.playerName);
            nameLabel.setText("Name: " + game.playerName);
            nameLabel.setCenterX(SCREEN_WIDTH / 2f);
        }
        isEditingName = false;
        if (inputStage != null) {
            inputStage.dispose();
            inputStage = null;
        }
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (inputStage != null) {
            inputStage.dispose();
        }
    }
}
