package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import io.github.some_example_name.managers.AudioManager;
import io.github.some_example_name.managers.ContactManager;
import io.github.some_example_name.screens.GameScreen;
import io.github.some_example_name.screens.MenuScreen;

import static io.github.some_example_name.GameSettings.*;

public class MyGdxGame extends Game {
    public World world;
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public ContactManager contactManager;

    public AudioManager audioManager;

    public MenuScreen menuScreen;
    public GameScreen gameScreen;

    public BitmapFont defaultFont, largeFont, smallFont, titleFont, timerFont;

    public boolean isHost = false;
    public String hostIp = "";
    float accumulator = 0;

    @Override
    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, GRAVITY), true);
        contactManager = new ContactManager(world);
        audioManager = new AudioManager();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        defaultFont = FontBuilder.generate(24, Color.WHITE, GameResources.FONT_PATH);
        largeFont = FontBuilder.generate(48, Color.WHITE, GameResources.FONT_PATH);
        smallFont = FontBuilder.generate(16, Color.WHITE, GameResources.FONT_PATH);
        titleFont = FontBuilder.generate(64, Color.WHITE, GameResources.FONT_PATH);
        timerFont = FontBuilder.generate(24, Color.RED, GameResources.FONT_PATH);

        menuScreen = new MenuScreen(this);
        gameScreen = new GameScreen(this);

        setScreen(menuScreen);
    }

    @Override
    public void resize(int width, int height) {
        float screenRatio = width / (float) height;

        if (screenRatio > SCREEN_WIDTH / (float) SCREEN_HEIGHT) {
            float newWidth = SCREEN_HEIGHT * screenRatio;
            camera.viewportWidth = newWidth;
            camera.viewportHeight = SCREEN_HEIGHT;
        }
        else {
            float newHeight = SCREEN_WIDTH / screenRatio;
            camera.viewportWidth = SCREEN_WIDTH;
            camera.viewportHeight = newHeight;
        }

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        batch.dispose();

        defaultFont.dispose();
        largeFont.dispose();
        smallFont.dispose();
        titleFont.dispose();
        timerFont.dispose();

        if (menuScreen != null) {
            menuScreen.dispose();
        }

        if (gameScreen != null) {
            gameScreen.dispose();
        }
    }

    public void showGameScreen(boolean asHost, String ip) {
        this.isHost = asHost;
        if (ip != null && !ip.isEmpty()) {
            this.hostIp = ip;
        }
        setScreen(gameScreen);
    }

    public void showMenuScreen() {
        if (gameScreen != null) {
            gameScreen.disconnect();
        }
        setScreen(menuScreen);
    }
}
