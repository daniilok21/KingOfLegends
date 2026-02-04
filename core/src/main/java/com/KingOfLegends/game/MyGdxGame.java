package com.KingOfLegends.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.World;
import com.KingOfLegends.game.managers.AudioManager;
import com.KingOfLegends.game.managers.ContactManager;
import com.KingOfLegends.game.managers.MemoryManager;
import com.KingOfLegends.game.screens.GameScreen;
import com.KingOfLegends.game.screens.JoinScreen;
import com.KingOfLegends.game.screens.MenuScreen;
import com.KingOfLegends.game.screens.ProfileScreen;

import static com.KingOfLegends.game.GameSettings.*;

public class MyGdxGame extends Game {
    public World world;
    public SpriteBatch batch;
    public OrthographicCamera camera;
    public ContactManager contactManager;

    public AudioManager audioManager;

    public MenuScreen menuScreen;
    public GameScreen gameScreen;
    public JoinScreen joinScreen;
    public ProfileScreen profileScreen;

    public BitmapFont defaultFont, largeFont, smallFont, titleFont, timerFont, defaultMenuFont, titleMenuFont, textFieldFont;

    public boolean isHost = false;
    public String hostIp = "";
    public String playerName;

    float accumulator = 0;

    @Override
    public void create() {
        Box2D.init();
        world = new World(new Vector2(0, GRAVITY), true);
        contactManager = new ContactManager(world);
        audioManager = new AudioManager();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();

        playerName = MemoryManager.loadProfileName();
        defaultFont = FontBuilder.generate(24, Color.WHITE, GameResources.FONT_PATH);
        largeFont = FontBuilder.generate(48, Color.WHITE, GameResources.FONT_PATH);
        smallFont = FontBuilder.generate(16, Color.WHITE, GameResources.FONT_PATH);
        titleFont = FontBuilder.generate(64, Color.WHITE, GameResources.FONT_PATH);
        timerFont = FontBuilder.generate(24, Color.RED, GameResources.FONT_PATH);

        defaultMenuFont = FontBuilder.generate(30, Color.WHITE, GameResources.DRAK_FONT_PATH);
        titleMenuFont = FontBuilder.generate(110, Color.WHITE, GameResources.MENU_FONT_PATH);
        textFieldFont = FontBuilder.generate(30, Color.WHITE, GameResources.DRAK_FONT_PATH);

        menuScreen = new MenuScreen(this);
        gameScreen = new GameScreen(this);
        joinScreen = new JoinScreen(this);
        profileScreen = new ProfileScreen(this);

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
        defaultMenuFont.dispose();
        titleMenuFont.dispose();
        textFieldFont.dispose();

        audioManager.dispose();

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
        audioManager.stopMenuMusic();
        setScreen(gameScreen);
    }

    public void showMenuScreen() {
        if (gameScreen != null) {
            gameScreen.disconnect();
        }
        audioManager.stopGameMusic();
        setScreen(menuScreen);
    }
}
