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
import io.github.some_example_name.screens.GameScreen;
import io.github.some_example_name.screens.MenuScreen;

import static io.github.some_example_name.GameSettings.*;

public class MyGdxGame extends Game {
    World world;
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public BitmapFont font;

    public AudioManager audioManager;

    // Экран
    public MenuScreen menuScreen;
    public GameScreen gameScreen;


    public boolean isHost = true;
    public String hostIp = "192.168.1.49";
    public int port = 9090;

    float accumulator = 0;

    @Override
    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, -10), true);
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        audioManager = new AudioManager();

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
        font.dispose();

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
