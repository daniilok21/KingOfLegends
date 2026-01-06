package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

import io.github.some_example_name.GameResources;
import io.github.some_example_name.MyGdxGame;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.JoystickView;
import io.github.some_example_name.components.TopPanelView;
import io.github.some_example_name.game.AttackDirection;
import io.github.some_example_name.game.GameState;
import io.github.some_example_name.game.PlayerInput;
import io.github.some_example_name.net.Client;
import io.github.some_example_name.net.Server;
import io.github.some_example_name.objects.PlatformObject;
import io.github.some_example_name.objects.PlayerObject;

import java.util.ArrayList;

import static io.github.some_example_name.GameSettings.*;

public class GameScreen extends ScreenAdapter {

    private final MyGdxGame myGdxGame;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private Server server;
    private Client client;
    private GameState currentState;

    private PlayerObject serverPlayer;
    private PlayerObject clientPlayer;

    private ArrayList<PlatformObject> platforms;

    private JoystickView joystick;
    private ButtonView jumpButton;
    private ButtonView dodgeButton;
    private ButtonView attackButton;

    private TopPanelView topPanel;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpPressed = false;
    private boolean dodgePressed = false;
    private boolean attackPressed = false;
    private boolean attackUp = false;
    private boolean attackDown = false;

    private boolean jumpWasPressed = false;

    private boolean connected = false;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = myGdxGame.batch;
        this.currentState = new GameState();

        setupPlatforms();
        setupPhysicsBodies();
        setupUI();
        setupTopPanel();
    }

    private void setupTopPanel() {
        topPanel = new TopPanelView(
            200, SCREEN_HEIGHT - TOP_PANEL_HEIGHT,
            SCREEN_WIDTH - 400, TOP_PANEL_HEIGHT,
            myGdxGame.font,
            GameResources.TOP_PANEL_BG,
            GameResources.HEART_FULL,
            GameResources.HEART_EMPTY
        );
    }

    private void setupPlatforms() {
        platforms = new ArrayList<>();

        platforms.add(new PlatformObject(
            0, 200,
            SCREEN_WIDTH,
            100,
            GameResources.PLATFORM,
            myGdxGame.world
        ));
    }

    private void setupPhysicsBodies() {
        serverPlayer = new PlayerObject(
            START_PLAYER_SERVER_X, START_PLAYER_SERVER_Y,
            PLAYER_WIDTH, PLAYER_HEIGHT,
            GameResources.RED_PLAYER_SPRITE_SHEET,
            myGdxGame.world
        );

        clientPlayer = new PlayerObject(
            START_PLAYER_CLIENT_X, START_PLAYER_CLIENT_Y,
            PLAYER_WIDTH, PLAYER_HEIGHT,
            GameResources.BLUE_PLAYER_SPRITE_SHEET,
            myGdxGame.world
        );

        currentState.serverBody = serverPlayer.getBody();
        currentState.clientBody = clientPlayer.getBody();
    }

    private void setupUI() {
        int offset = 50;

        joystick = new JoystickView(offset, offset, GameResources.JOYSTICK_BG, GameResources.JOYSTICK_HANDLE);

        jumpButton = new ButtonView(
            SCREEN_WIDTH - offset - BUTTON_WIDTH, offset, BUTTON_WIDTH, BUTTON_HEIGHT,
            GameResources.BUTTON_JUMP
        );
        dodgeButton = new ButtonView(
            SCREEN_WIDTH - offset - BUTTON_WIDTH * 2 - 20, offset, BUTTON_WIDTH, BUTTON_HEIGHT,
            GameResources.BUTTON_DODGE
        );
        attackButton = new ButtonView(
            SCREEN_WIDTH - offset - BUTTON_WIDTH * 3 - 2 * 20, offset, BUTTON_WIDTH, BUTTON_HEIGHT,
            GameResources.BUTTON_ATTACK
        );
    }

    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
            server.setPhysicsBodies(serverPlayer, clientPlayer);
            server.start(PORT);
            currentState = server.getLocalState();
            connected = true;
        }
        else {
            client = new Client();
            connected = client.connect(myGdxGame.hostIp, PORT);
            if (!connected) {
                System.out.println("Failed to connect to server");
                myGdxGame.showMenuScreen();
            }
        }
    }

    @Override
    public void show() {
        if (!connected) initializeNetwork();
    }

    @Override
    public void render(float delta) {
        checkTouchUp();
        handleInput();
        update(delta);
        draw();
        myGdxGame.stepWorld();
    }

    private void handleInput() {
        if (!connected) return;

        if (!myGdxGame.isHost && client != null) {
            GameState serverState = client.getState();
            if (serverState != null) {
                currentState.gameStatus = serverState.gameStatus;
                currentState.countdownTimer = serverState.countdownTimer;
            }
        }

//        if (currentState.gameStatus != GameState.GameStatus.PLAYING) {
//            leftPressed = false;
//            rightPressed = false;
//            jumpPressed = false;
//            dodgePressed = false;
//            attackPressed = false;
//            return;
//        }


        leftPressed = false;
        rightPressed = false;
        jumpPressed = false;
        dodgePressed = false;
        attackPressed = false;
        attackUp = false;
        attackDown = false;
        jumpButton.setPressed(false);
        dodgeButton.setPressed(false);
        attackButton.setPressed(false);

        if (joystick.isCaptured()) {
            int capturedPointer = joystick.getCapturedPointer();
            if (capturedPointer >= 0 && capturedPointer < 5) {
                if (!Gdx.input.isTouched(capturedPointer)) {
                    joystick.reset();
                }
            }
        }

        boolean[] fingerProcessed = new boolean[5];
        // джостик
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0));

                if (joystick.processTouch(touch.x, touch.y, true, i)) {
                    fingerProcessed[i] = true;
                }
            }
        }
        // кнопки
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0));

                if (jumpButton.isHit(touch.x, touch.y)) {
                    jumpPressed = true;
                    jumpButton.setPressed(true);
                }
                if (dodgeButton.isHit(touch.x, touch.y)) {
                    dodgePressed = true;
                    dodgeButton.setPressed(true);
                }
                if (attackButton.isHit(touch.x, touch.y)) {
                    attackPressed = true;
                    attackButton.setPressed(true);
                    if (joystick.isUp()) attackUp = true;
                    else if (joystick.isDown()) attackDown = true;
                }
            }
        }
        // отпускание пальца
        for (int i = 0; i < 5; i++) {
            if (!Gdx.input.isTouched(i) && fingerProcessed[i]) {
                joystick.processTouch(0, 0, false, i);
            }
        }

        if (joystick.isCaptured()) {
            if (joystick.isLeft()) {
                leftPressed = true;
            } else if (joystick.isRight()) {
                rightPressed = true;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) attackUp = true;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) attackDown = true;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) leftPressed = true;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) rightPressed = true;
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) dodgePressed = true;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) attackPressed = true;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) jumpPressed = true;

        if (attackPressed) {
            AttackDirection direction;

            if (attackUp) {
                direction = AttackDirection.UP;
            } else if (attackDown) {
                direction = AttackDirection.DOWN;
            } else {
                direction = AttackDirection.SIDE;
            }

            if (myGdxGame.isHost) serverPlayer.startAttack(direction);
        }
        boolean jumpJustPressed = jumpPressed && !jumpWasPressed;
        if (myGdxGame.isHost) {
            if (!serverPlayer.canReceiveInput()) {
                leftPressed = false;
                rightPressed = false;
                jumpPressed = false;
                dodgePressed = false;
                attackPressed = false;
            }
            Vector2 force = new Vector2(0, 0);
            if (leftPressed) force.x = -PLAYER_MOVE_FORCE;
            if (rightPressed) force.x = PLAYER_MOVE_FORCE;

            Body serverBody = serverPlayer.getBody();
            if (serverPlayer.canMove()) {
                serverBody.applyForceToCenter(force, true);
            }
            Vector2 vel = serverBody.getLinearVelocity();
            if (!serverPlayer.isInHitStun()) {
                vel.x = Math.max(-PLAYER_MAX_VELOCITY, Math.min(PLAYER_MAX_VELOCITY, vel.x));
            }
            serverBody.setLinearVelocity(vel);
            if (jumpJustPressed && serverPlayer.canJump()) {
                boolean jumpSuccessful = serverPlayer.jump(PLAYER_JUMP_FORCE);
                if (jumpSuccessful) {
                }
            }
            if (dodgePressed && serverPlayer.canDodge()) {
                float dodgeDirection = 0;
                if (leftPressed) dodgeDirection = -1;
                else if (rightPressed) dodgeDirection = 1;
                else {
                    float velX = serverBody.getLinearVelocity().x;
                    if (velX != 0) {
                        if (velX > 0) dodgeDirection = 1;
                        else dodgeDirection = -1;
                    }
                }

                boolean dodgeSuccessful = serverPlayer.dodge(dodgeDirection);
                if (dodgeSuccessful) {
                }
            }

        }
        else if (client != null) {
            PlayerInput input = new PlayerInput();
            input.moveLeft = leftPressed;
            input.moveRight = rightPressed;
            input.jump = jumpJustPressed;
            input.dodge = dodgePressed;
            input.attack = attackPressed;
            input.attackUp = attackUp;
            input.attackDown = attackDown;
            client.sendInput(input);

            GameState serverState = client.getState();
            if (serverState != null) {
                syncAttackState(serverState);

                serverState.serverBody = currentState.serverBody;
                serverState.clientBody = currentState.clientBody;

                currentState = serverState;
                currentState.applyToPhysics(0.3f);
            }
        }
        jumpWasPressed = jumpPressed;
    }

    private void syncAttackState(GameState serverState) {
        if (serverState.serverAttacking && !serverPlayer.isAttacking()) serverPlayer.startAttack(serverState.serverAttackDirection);
        else if (!serverState.serverAttacking && serverPlayer.isAttacking()) serverPlayer.stopAttacking();
        if (serverState.clientAttacking && !clientPlayer.isAttacking()) clientPlayer.startAttack(serverState.clientAttackDirection);
        else if (!serverState.clientAttacking && clientPlayer.isAttacking()) clientPlayer.stopAttacking();
    }

    private void checkTouchUp() {
        if (!Gdx.input.isTouched()) {
            jumpButton.setPressed(false);
            dodgeButton.setPressed(false);
            attackButton.setPressed(false);
            joystick.reset();
        }
    }

    private void update(float delta) {
        serverPlayer.update(delta);
        clientPlayer.update(delta);
        topPanel.update(delta);
        if (serverPlayer != null) {
            float serverX = serverPlayer.getX();
            float serverY = serverPlayer.getY();
            topPanel.checkOutOfBounds(serverX, serverY, true);

            if (topPanel.getNeedChange1Player() && topPanel.getPlayer1Lives() > 0) {
                serverPlayer.setX(START_PLAYER_SERVER_X);
                serverPlayer.setY(START_PLAYER_SERVER_Y);
                serverPlayer.getBody().setLinearVelocity(0, 0);
                serverPlayer.heal(100 - serverPlayer.getHealth());
                serverPlayer.setHitImmunityTimer(2.0f);
            }
        }

        if (clientPlayer != null) {
            float clientX = clientPlayer.getX();
            float clientY = clientPlayer.getY();
            topPanel.checkOutOfBounds(clientX, clientY, false);

            if (topPanel.getNeedChange2Player() && topPanel.getPlayer2Lives() > 0) {
                clientPlayer.setX(START_PLAYER_CLIENT_X);
                clientPlayer.setY(START_PLAYER_CLIENT_Y);
                clientPlayer.getBody().setLinearVelocity(0, 0);
                clientPlayer.heal(100 - clientPlayer.getHealth());
                clientPlayer.setHitImmunityTimer(2.0f);
            }
        }
        if (myGdxGame.isHost) {
            if (serverPlayer.isAttacking()) {
                if (serverPlayer.checkHit(clientPlayer)) {
                    System.out.println("Сервер попал по клиенту");
                }
            }

            updateGameState(delta);
            currentState.updateFromPhysics();
        }

    }
    private void updateGameState(float delta) {
        switch (currentState.gameStatus) {
            case WAITING:
                if (client != null && client.isConnected()) {
                    currentState.gameStatus = GameState.GameStatus.COUNTDOWN;
                    currentState.countdownTimer = 3.0f;
                }
                break;

            case COUNTDOWN:
                currentState.countdownTimer -= delta;
                if (currentState.countdownTimer <= 0) {
                    currentState.gameStatus = GameState.GameStatus.PLAYING;
                    System.out.println("Game started");
                }
                break;

            case PLAYING:
                break;
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.15f, 0.2f, 0.25f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        myGdxGame.camera.update();
        batch.setProjectionMatrix(myGdxGame.camera.combined);
        shapeRenderer.setProjectionMatrix(myGdxGame.camera.combined);

        batch.begin();

        for (PlatformObject platform : platforms) {
            platform.draw(batch);
        }
        if (myGdxGame.isHost) {
            clientPlayer.draw(batch);
            serverPlayer.draw(batch);
        }
        else {
            serverPlayer.draw(batch);
            clientPlayer.draw(batch);
        }

        drawAttackHitboxes(batch);

        topPanel.draw(batch);

        joystick.draw(batch);
        jumpButton.draw(batch);
        dodgeButton.draw(batch);
        attackButton.draw(batch);

        drawGameStateUI();

        batch.end();
    }

    private void drawAttackHitboxes(SpriteBatch batch) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.ORANGE);

        if (serverPlayer.isAttacking()) {
            Rectangle hitbox = serverPlayer.getAttackHitbox();
            shapeRenderer.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }

        if (clientPlayer.isAttacking()) {
            Rectangle hitbox = clientPlayer.getAttackHitbox();
            shapeRenderer.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }

        shapeRenderer.end();
        batch.begin();
    }

    private void drawPlayerHitboxes(SpriteBatch batch) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.GREEN);
        Rectangle serverHitbox = serverPlayer.getPlayerHitbox();
        shapeRenderer.rect(serverHitbox.x, serverHitbox.y, serverHitbox.width, serverHitbox.height);

        shapeRenderer.setColor(Color.CYAN);
        Rectangle clientHitbox = clientPlayer.getPlayerHitbox();
        shapeRenderer.rect(clientHitbox.x, clientHitbox.y, clientHitbox.width, clientHitbox.height);

        shapeRenderer.end();
        batch.begin();
    }

    private void drawGameStateUI() {
        myGdxGame.font.setColor(Color.WHITE);
        // myGdxGame.font.draw(batch, myGdxGame.isHost ? "HOST" : "CLIENT", 10, SCREEN_HEIGHT - 10);

        switch (currentState.gameStatus) {
            case WAITING:
                myGdxGame.font.setColor(Color.WHITE);
                myGdxGame.font.draw(batch, "WAITING FOR CLIENT...", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
                break;

            case COUNTDOWN:
                myGdxGame.font.setColor(Color.WHITE);
                int seconds = (int) Math.ceil(currentState.countdownTimer);
                myGdxGame.font.draw(batch, "STARTING IN: " + seconds, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
                break;

            case PLAYING:
                break;
        }
    }

    public void disconnect() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        connected = false;
    }

    @Override
    public void resize(int width, int height) {
        myGdxGame.camera.setToOrtho(false, width, height);
        myGdxGame.camera.update();
        batch.setProjectionMatrix(myGdxGame.camera.combined);
        shapeRenderer.setProjectionMatrix(myGdxGame.camera.combined);
    }

    @Override
    public void hide() {
        disconnect();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        disconnect();
        shapeRenderer.dispose();

        joystick.dispose();
        jumpButton.dispose();
        dodgeButton.dispose();
        attackButton.dispose();

        serverPlayer.dispose();
        clientPlayer.dispose();

        for (PlatformObject platform : platforms) {
            platform.dispose();
        }
    }
}
