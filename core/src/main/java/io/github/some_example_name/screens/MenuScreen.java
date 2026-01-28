package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.GameResources;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.MovingBackgroundView;
import io.github.some_example_name.components.TextView;

import static io.github.some_example_name.GameSettings.SCREEN_WIDTH;
import static io.github.some_example_name.GameSettings.SCREEN_HEIGHT;

public class MenuScreen extends ScreenAdapter {

    private final MyGdxGame game;
    private MovingBackgroundView backgroundView;
    private TextView titleView;
    private ButtonView hostButton;
    private ButtonView joinButton;
    private ButtonView settingsButton;

    private ButtonView exitButton;
    float buttonY = SCREEN_HEIGHT / 2f + 80;

    public MenuScreen(MyGdxGame game) {
        this.game = game;

        titleView = new TextView(game.titleMenuFont, SCREEN_WIDTH / 2f, SCREEN_HEIGHT - 90, "KING OF LEGENDS");
        titleView.setCenterX(SCREEN_WIDTH / 2f);
        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_MENU);

        hostButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY, 325, 110,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Host Game"
        );
        joinButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY - 125, 325, 110,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Join Game"
        );
        settingsButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY - 250, 325, 110,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Settings"
        );
        exitButton = new ButtonView(
            SCREEN_WIDTH / 2f - 162.5f, buttonY - 375, 325, 110,
            game.defaultMenuFont, GameResources.BUTTON_MENU, "Exit"
        );
    }

    @Override
    public void render(float delta) {

        handleInput();

        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(new Color(0.2f, 0.2f, 0.3f, 1));

        game.batch.begin();
        backgroundView.draw(game.batch);
        titleView.draw(game.batch);
        hostButton.draw(game.batch);
        joinButton.draw(game.batch);
        settingsButton.draw(game.batch);
        exitButton.draw(game.batch);
        game.batch.end();

    }

    private void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touch = game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (hostButton.isHit(touch.x, touch.y)) {
                game.showGameScreen(true, null);
            }  if (joinButton.isHit(touch.x, touch.y)) {
                game.setScreen(game.joinScreen);
            }  if (settingsButton.isHit(touch.x, touch.y)){
                game.setScreen(game.profileScreen);
            } if (exitButton.isHit(touch.x, touch.y)) {
                Gdx.app.exit();
            }
        }
    }
}
