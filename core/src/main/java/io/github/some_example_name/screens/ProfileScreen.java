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

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.GameResources;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.TextView;
import io.github.some_example_name.managers.MemoryManager;

import static io.github.some_example_name.GameSettings.SCREEN_WIDTH;
import static io.github.some_example_name.GameSettings.SCREEN_HEIGHT;

public class ProfileScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private TextView titleView;
    private TextView nameLabel;
    private ButtonView changeNameButton;
    private ButtonView backButton;

    // Для ввода имени
    private Stage inputStage;
    private TextField nameField;
    private boolean isEditingName = false;

    public ProfileScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        float buttonY = SCREEN_HEIGHT / 2f + 60;

        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f - 150, SCREEN_HEIGHT - 50, "PROFILE");
        titleView.setCenterX(SCREEN_WIDTH / 2f);

        // Загружаем текущее имя из game.playerName (оно уже загружено из MemoryManager в MyGdxGame)
        nameLabel = new TextView(game.defaultFont, SCREEN_WIDTH / 2f - 150, buttonY, "Name: " + game.playerName);
        nameLabel.setCenterX(SCREEN_WIDTH / 2f);

        changeNameButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY - 90, 440, 70,
            game.defaultFont, GameResources.BUTTON_LONG_MENU, "Change Name"
        );

        backButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, buttonY - 180, 440, 70,
            game.defaultFont, GameResources.BUTTON_LONG_MENU, "Back"
        );
    }

    @Override
    public void render(float delta) {
        if (!isEditingName) {
            handleInput();
        }

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(new Color(0.2f, 0.2f, 0.3f, 1));

        game.batch.begin();
        titleView.draw(game.batch);
        nameLabel.draw(game.batch);
        if (!isEditingName) {
            changeNameButton.draw(game.batch);
            backButton.draw(game.batch);
        }
        game.batch.end();

        if (isEditingName) {
            inputStage.act(delta);
            inputStage.draw();
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
        skin.add("default", game.defaultFont);

        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.defaultFont;
        style.fontColor = Color.WHITE;
        style.background = null; // прозрачный фон
        skin.add("default", style);

        // Текущее имя из игры
        nameField = new TextField(game.playerName, skin);
        nameField.setSize(400, 50);
        float x = Gdx.graphics.getWidth() / 2f - 200;
        nameField.setPosition(x, Gdx.graphics.getHeight() / 2f + 20);
        nameField.setMaxLength(16); // максимум 16 символов

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
        nameField.getOnscreenKeyboard().show(true);
    }

    private void saveName(String name) {
        if (name != null && !name.isEmpty()) {
            // Обновляем имя в игре
            game.playerName = name;
            // Сохраняем в файл
            MemoryManager.saveProfileName(game.playerName);
            // Обновляем надпись
            nameLabel.setText("Name: " + game.playerName);
        }
        // Возвращаемся в настройки
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
