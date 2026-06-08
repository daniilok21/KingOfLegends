package com.KingOfLegends.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import com.KingOfLegends.game.GameResources;
import com.KingOfLegends.game.GameSettings;
import com.KingOfLegends.game.MyGdxGame;
import com.KingOfLegends.game.components.ButtonView;
import com.KingOfLegends.game.components.ImageView;
import com.KingOfLegends.game.components.MovingBackgroundView;
import com.KingOfLegends.game.components.SliderView;
import com.KingOfLegends.game.components.TextView;
import com.KingOfLegends.game.components.ToggleView;
import com.KingOfLegends.game.managers.MemoryManager;

import static com.KingOfLegends.game.GameSettings.SCREEN_HEIGHT;
import static com.KingOfLegends.game.GameSettings.SCREEN_WIDTH;

public class SettingsScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private MovingBackgroundView background;
    private TextView titleView;
    private SliderView musicSlider;
    private SliderView soundSlider;
    private ToggleView vibrationToggle;
    private ButtonView backButton;
    private ImageView board;

    private int activeTouchPointer = -1;

    public SettingsScreen(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        background = new MovingBackgroundView(GameResources.BACKGROUND_MENU);

        game.defaultMenuFont.setColor(Color.BROWN);

        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 90, "SETTINGS");
        titleView.setCenterX(SCREEN_WIDTH / 2f);

        board = new ImageView(SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 700f, 600, 600, GameResources.BOARD);
        board.setCenterX(SCREEN_WIDTH / 2f);

        float sliderWidth = 400f;
        float sliderX = SCREEN_WIDTH / 2f - sliderWidth / 2f;

        musicSlider = new SliderView(
            sliderX, SCREEN_HEIGHT / 2f + 80, sliderWidth,
            "Music Volume", GameSettings.MUSIC_VOLUME,
            game.defaultMenuFont,
            GameResources.SLIDER_FILL,
            GameResources.SLIDER_EMPTY,
            GameResources.CIRCLE_BUTTON
        );
        System.out.println(musicSlider.height);

        soundSlider = new SliderView(
            sliderX, SCREEN_HEIGHT / 2f - 30, sliderWidth,
            "Sound Volume", GameSettings.SOUND_VOLUME,
            game.defaultMenuFont,
            GameResources.SLIDER_FILL,
            GameResources.SLIDER_EMPTY,
            GameResources.CIRCLE_BUTTON
        );

        vibrationToggle = new ToggleView(
            sliderX + 50, SCREEN_HEIGHT / 2f - 160, 70, 70,
            "Vibration", GameSettings.VIBRATION_ENABLED,
            game.defaultMenuFont,
            game.defaultMenuFontWithBorder,
            GameResources.SWITCH_ONN,
            GameResources.SWITCH_OFF
        );

        backButton = new ButtonView(
            SCREEN_WIDTH / 2f - 220, SCREEN_HEIGHT / 2f - 270, 440, 70,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Back"
        );
    }

    @Override
    public void render(float delta) {
        handleInput();
        applySettings();

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);

        game.batch.begin();
        game.defaultMenuFont.setColor(Color.BROWN);

        background.draw(game.batch);
        titleView.draw(game.batch);
        board.draw(game.batch);
        musicSlider.draw(game.batch);
        soundSlider.draw(game.batch);
        vibrationToggle.draw(game.batch);
        backButton.draw(game.batch);
        game.batch.end();
    }

    private void handleInput() {
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 t = game.camera.unproject(
                    new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0)
                );
                if (activeTouchPointer == -1) {
                    if (musicSlider.touchDown(t.x, t.y)) activeTouchPointer = i;
                    else if (soundSlider.touchDown(t.x, t.y)) activeTouchPointer = i;
                } else if (activeTouchPointer == i) {
                    musicSlider.touchActive(t.x, t.y);
                    soundSlider.touchActive(t.x, t.y);
                }
            } else if (activeTouchPointer == i) {
                musicSlider.touchUp();
                soundSlider.touchUp();
                activeTouchPointer = -1;
            }
        }

        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
            );
            vibrationToggle.tap(touch.x, touch.y);
            if (backButton.isHit(touch.x, touch.y)) {
                saveAndGoBack();
            }
        }
        if (Gdx.input.isTouched()) {
            Vector3 touch = game.camera.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
            );
            if (vibrationToggle.isHit(touch.x, touch.y)) {
                vibrationToggle.setIsActive(true);
            }
        }
    }

    private void applySettings() {
        GameSettings.MUSIC_VOLUME = musicSlider.getValue();
        GameSettings.SOUND_VOLUME = soundSlider.getValue();
        GameSettings.VIBRATION_ENABLED = vibrationToggle.getValue();
        game.audioManager.applyVolumes();
    }

    private void saveAndGoBack() {
        MemoryManager.saveSettings(
            GameSettings.MUSIC_VOLUME,
            GameSettings.SOUND_VOLUME,
            GameSettings.VIBRATION_ENABLED
        );
        game.setScreen(game.menuScreen);
    }
    @Override
    public void dispose() {
        background.dispose();
        titleView = null;
        musicSlider = null;
        soundSlider = null;
        vibrationToggle = null;
        backButton = null;
        board = null;
    }
}
