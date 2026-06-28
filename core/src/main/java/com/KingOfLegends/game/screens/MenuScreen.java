package com.KingOfLegends.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import com.KingOfLegends.game.MyGdxGame;
import com.KingOfLegends.game.GameResources;
import com.KingOfLegends.game.components.ButtonView;
import com.KingOfLegends.game.components.ImageView;
import com.KingOfLegends.game.components.MovingBackgroundView;
import com.KingOfLegends.game.components.TextView;

import static com.KingOfLegends.game.GameSettings.SCREEN_WIDTH;
import static com.KingOfLegends.game.GameSettings.SCREEN_HEIGHT;

public class MenuScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private MovingBackgroundView backgroundView;
    private ButtonView hostButton;
    private ButtonView joinButton;
    private ButtonView skillsButton;
    private ButtonView settingsButton;
    private ButtonView profileButton;
    private ImageView titleView;
    private ButtonView exitButton;
    float buttonY = SCREEN_HEIGHT / 2f + 80;

    public MenuScreen(MyGdxGame game) {
        this.game = game;

    }

    @Override
    public void show() {
        game.audioManager.playMenuMusic();
        game.audioManager.applyVolumes();

        game.defaultMenuFont.setColor(Color.BROWN);

        titleView = new ImageView(SCREEN_WIDTH / 2f - 500, SCREEN_HEIGHT - 193,1000,220,GameResources.TITLE_MENU);

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_MENU);

        hostButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY, 325, 100,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Host Game"
        );
        joinButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY - 115, 325, 100,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Join Game"
        );
        skillsButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY - 240, 325, 100,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Skills"
        );
        settingsButton = new ButtonView(
            20, 20, 80, 80,
            GameResources.BUTTON_SETTINGS
        );
        profileButton = new ButtonView(
            settingsButton.getX() + settingsButton.getHeight() + 20, settingsButton.getY(), settingsButton.getWidth(), settingsButton.getHeight(), GameResources.BUTTON_PROFILE
        );
        exitButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY - 365, 325, 100,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Exit"
        );
    }

    @Override
    public void render(float delta) {

        handleInput();

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(new Color(0, 0, 0, 1));

        game.batch.begin();
        game.defaultMenuFont.setColor(Color.BROWN);

        backgroundView.draw(game.batch);
        titleView.draw(game.batch);
        hostButton.draw(game.batch);
        joinButton.draw(game.batch);
        settingsButton.draw(game.batch);
        skillsButton.draw(game.batch);
        profileButton.draw(game.batch);
        exitButton.draw(game.batch);
        game.batch.end();

    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (hostButton.isHit(touch.x, touch.y)) {
                game.showGameScreen(true, null);
                game.audioManager.playClickSound();
            }  if (joinButton.isHit(touch.x, touch.y)) {
                game.setScreen(game.joinScreen);
                game.audioManager.playClickSound();
            }  if (skillsButton.isHit(touch.x, touch.y)){
                game.setScreen(game.upgradeScreen);
                game.audioManager.playClickSound();
            } if (profileButton.isHit(touch.x, touch.y)){
                game.setScreen(game.profileScreen);
                game.audioManager.playClickSound();
            } if (exitButton.isHit(touch.x, touch.y)) {
                game.audioManager.playClickSound();
                Gdx.app.exit();
            } if (settingsButton.isHit(touch.x, touch.y)) {
                game.setScreen(game.settingsScreen);
                game.audioManager.playClickSound();
            }
        }
    }
}
