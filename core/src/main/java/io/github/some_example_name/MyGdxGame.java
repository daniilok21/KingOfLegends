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

    // Экран
    public MenuScreen menuScreen;
    public GameScreen gameScreen;

    public BitmapFont defaultFont;
    public BitmapFont largeFont;
    public BitmapFont smallFont;
    public BitmapFont titleFont;
    public BitmapFont timerFont;


    public boolean isHost = false;
    public String hostIp = "192.168.0.14";
    float accumulator = 0;

    @Override
    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, GRAVITY), true);
        contactManager = new ContactManager(world);
        audioManager = new AudioManager();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

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
    public void render() {
        super.render();
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

        gameScreen.initializeNetwork();
        setScreen(gameScreen);
    }

    public void showMenuScreen() {
        if (gameScreen != null) {
            gameScreen.disconnect();
        }
        setScreen(menuScreen);
    }
    public void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;
            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }
}
