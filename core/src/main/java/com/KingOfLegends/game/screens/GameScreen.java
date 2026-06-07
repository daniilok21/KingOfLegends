package com.KingOfLegends.game.screens;

import com.KingOfLegends.game.managers.MemoryManager;
import com.KingOfLegends.game.managers.SkillMessageManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.KingOfLegends.game.*;
import com.KingOfLegends.game.components.*;
import com.KingOfLegends.game.game.*;
import com.KingOfLegends.game.managers.ContactManager;
import com.KingOfLegends.game.net.*;
import com.KingOfLegends.game.objects.*;
import java.util.ArrayList;
import java.util.Random;

import static com.KingOfLegends.game.GameSettings.*;

public class GameScreen extends ScreenAdapter {
    private final MyGdxGame myGdxGame;
    MovingBackgroundView backgroundView;
    Random random;
    private ShapeRenderer shapeRenderer;
    private BloodParticle bloodParticles;
    private SpriteBatch batch;
    private Server server;
    private Client client;
    private PlayerObject serverPlayer, clientPlayer;
    private ArrayList<PlatformObject> platforms = new ArrayList<>();
    private ArrayList<OneWayPlatformObject> oneWayPlatforms = new ArrayList<>();
    private JoystickView joystick;
    private ButtonView jumpButton, dodgeButton, attackButton, homeButton;
    private TopPanelView topPanel;
    private PlayerInput localInput = new PlayerInput();
    private boolean jumpWasPressed = false, connected = false;
    private boolean musicStarted = false, musicWainigStarted = false;
    private GameState.GameStatus gameStatus = GameState.GameStatus.WAITING;
    private float countdown = 3.0f, timeAccumulator = 0, resultDisplayTimer = 0f;
    private TextView waitingText, countdownText, resultText, ipAddressText;
    private int selectedMusicIndex = 0;
    private int cRespawnConfirmFrames = 0;
    private int sRespawnConfirmFrames = 0;
    private static final int RESPAWN_FRAMES = 5;
    SkillMessageManager skillMessageManager;

    private int[] localSkills;
    private int[] clientSkills;
    private int[] serverSkills;
    private int localMaxHealth;
    public boolean sLuckProc;
    public boolean sProtectProc;
    public boolean sProtectShown;
    public boolean sCritProc;
    private boolean sCritShown;

    public boolean cLuckProc;
    public boolean cProtectProc;
    public boolean cProtectShown;
    public boolean cCritProc;
    private boolean cCritShown;

    boolean sStartAtack;
    boolean cStartAtack;

    private Rectangle scissorRect = new Rectangle();
    private float serverRespawnIgnoreTimer = 0;
    private float clientRespawnIgnoreTimer = 0;

    public GameScreen(MyGdxGame game) {
        this.myGdxGame = game;
        this.batch = game.batch;
        shapeRenderer = new ShapeRenderer();
        bloodParticles = new BloodParticle();
        waitingText = new TextView(game.titleFontWithBorder, SCREEN_WIDTH / 2f - 100, SCREEN_HEIGHT / 4f * 3f - 30f, "WAITING...");
        ipAddressText = new TextView(game.titleFontWithBorder, SCREEN_WIDTH / 2f - 100, SCREEN_HEIGHT / 4f * 3f + 20f, "");
        countdownText = new TextView(game.titleFontWithBorder, SCREEN_WIDTH / 2f - 80, SCREEN_HEIGHT, "");
        resultText = new TextView(game.titleFontWithBorder, SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f + 100, "");
        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_GAME);
        skillMessageManager = new SkillMessageManager(myGdxGame.defaultFontWithBorder);
        random = new Random();
    }

    @Override
    public void show() {
        resetGame();
    }

    private void resetGame() {
        disconnect();
        gameStatus = GameState.GameStatus.WAITING;
        countdown = 3.0f;
        timeAccumulator = 0;
        resultDisplayTimer = 0f;
        connected = false;

        musicStarted = false;
        musicWainigStarted = false;
        sLuckProc = false;
        sProtectProc = false;
        sCritProc = false;
        cLuckProc = false;
        cProtectProc = false;
        cCritProc = false;
        sCritShown = false;
        cCritShown = false;

        sStartAtack = false;
        cStartAtack = false;

        platforms.clear();
        oneWayPlatforms.clear();

        if (myGdxGame.world != null) myGdxGame.world.dispose();
        myGdxGame.world = new World(new Vector2(0, GRAVITY), true);
        myGdxGame.contactManager = new ContactManager(myGdxGame.world);

        localSkills = MemoryManager.loadAllSkills();

        setupWorld();

        ipAddressText.setText("YOUR IP: " + getIP());
        waitingText.setCenterX(SCREEN_WIDTH / 2f);
        ipAddressText.setCenterX(SCREEN_WIDTH / 2f);

        initializeNetwork();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void setupWorld() {
        int offset_buttons = 50;
        platforms.add(new PlatformObject(398, 200, SCREEN_WIDTH - 788, 180, GameResources.PLATFORM, myGdxGame.world));
        oneWayPlatforms.add(new OneWayPlatformObject(90, 275, 250, 30, GameResources.PLATFORM, myGdxGame.world));
        oneWayPlatforms.add(new OneWayPlatformObject(503, 506, 288, 19, GameResources.PLATFORM, myGdxGame.world));
        oneWayPlatforms.add(new OneWayPlatformObject(945, 198, 250, 19, GameResources.PLATFORM, myGdxGame.world));
        serverPlayer = new PlayerObject(START_PLAYER_SERVER_X, START_PLAYER_SERVER_Y, PLAYER_WIDTH, PLAYER_HEIGHT,
            new String[]{
                GameResources.BLUE_PLAYER_IDLE_SHEET,
                GameResources.BLUE_PLAYER_RUN_SHEET,
                GameResources.BLUE_PLAYER_JUMP_SHEET,
                GameResources.BLUE_PLAYER_ATTACK_SHEET,
                GameResources.BLUE_PLAYER_DODGE_SHEET,
                GameResources.BLUE_PLAYER_HIT_SHEET,
                GameResources.BLUE_PLAYER_INVOCATION_SHEET,
                GameResources.BLUE_PLAYER_CLIMB_SHEET
            }, new int[] {4, 7, 6, 5, 5, 2, 5, 1}, new float[] {48f, 48f, 42f, 32f, 43f, 39.5f, 6f, 453.5f}, new float[] {51f, 51f, 51f, 51f, 51f, 51f, 51f, 5f}, myGdxGame.world);
        clientPlayer = new PlayerObject(START_PLAYER_CLIENT_X, START_PLAYER_CLIENT_Y, PLAYER_WIDTH, PLAYER_HEIGHT,new String[]{
                GameResources.RED_PLAYER_IDLE_SHEET,
                GameResources.RED_PLAYER_RUN_SHEET,
                GameResources.RED_PLAYER_JUMP_SHEET,
                GameResources.RED_PLAYER_ATTACK_SHEET,
                GameResources.RED_PLAYER_DODGE_SHEET,
                GameResources.RED_PLAYER_HIT_SHEET,
                GameResources.RED_PLAYER_INVOCATION_SHEET,
                GameResources.RED_PLAYER_CLIMB_SHEET
            }, new int[] {4, 7, 6, 5, 5, 2, 5, 1}, new float[] {48f, 48f, 42f, 32f, 43f, 39.5f, 6f, 453.5f}, new float[] {51f, 51f, 51f, 51f, 51f, 51f, 51f, 5f}, myGdxGame.world);
        clientPlayer.setFacingRight(false);
        joystick = new JoystickView(50, 30, GameResources.JOYSTICK_BG, GameResources.JOYSTICK_HANDLE);
        jumpButton = new ButtonView(SCREEN_WIDTH - 130 - offset_buttons, offset_buttons, BUTTON_WIDTH, BUTTON_HEIGHT, GameResources.BUTTON_JUMP);
        dodgeButton = new ButtonView(SCREEN_WIDTH - 130 - (BUTTON_WIDTH + 20) - offset_buttons, offset_buttons, BUTTON_WIDTH, BUTTON_HEIGHT, GameResources.BUTTON_DODGE);
        attackButton = new ButtonView(SCREEN_WIDTH - 130 - 2 * (BUTTON_WIDTH + 20) - offset_buttons, offset_buttons, BUTTON_WIDTH, BUTTON_HEIGHT, GameResources.BUTTON_ATTACK);
        homeButton = new ButtonView(20, SCREEN_HEIGHT - 80, 60, 60, GameResources.BUTTON_HOME);
        if (topPanel != null) topPanel.dispose();
        topPanel = new TopPanelView(200, SCREEN_HEIGHT-TOP_PANEL_HEIGHT, SCREEN_WIDTH - 400, TOP_PANEL_HEIGHT, myGdxGame.defaultFontWithBorder, myGdxGame.timerFont, GameResources.TOP_PANEL_BG, GameResources.HEART_FULL, GameResources.HEART_EMPTY);
        topPanel.setHost(myGdxGame.isHost);
        localMaxHealth = 100 + GameSettings.HEALTH_BUFF[localSkills[0]];
        if (myGdxGame.isHost) {
            serverPlayer.setMaxHealth(localMaxHealth);
            serverPlayer.setHealth(localMaxHealth);
        } else {
            clientPlayer.setMaxHealth(localMaxHealth);
            clientPlayer.setHealth(localMaxHealth);
        }
    }

    public void initializeNetwork() {
        if (myGdxGame.isHost) {
            server = new Server();
            server.start(PORT);
            connected = true;
            topPanel.setPlayer1Name(myGdxGame.playerName);
        } else {
            client = new Client();
            connected = client.connect(myGdxGame.hostIp, PORT);
            topPanel.setPlayer2Name(myGdxGame.playerName);
        }
    }

    @Override
    public void resize(int width, int height) {
        float screenRatio = width / (float) height;
        if (screenRatio > SCREEN_WIDTH / (float) SCREEN_HEIGHT) {
            myGdxGame.camera.viewportWidth = SCREEN_HEIGHT * screenRatio;
            myGdxGame.camera.viewportHeight = SCREEN_HEIGHT;
        } else {

            myGdxGame.camera.viewportWidth = SCREEN_WIDTH;
            myGdxGame.camera.viewportHeight = SCREEN_WIDTH / screenRatio;
        }

        myGdxGame.camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        myGdxGame.camera.update();
        batch.setProjectionMatrix(myGdxGame.camera.combined);

        Vector3 min = myGdxGame.camera.project(new Vector3(0, 0, 0));
        Vector3 max = myGdxGame.camera.project(new Vector3(SCREEN_WIDTH, SCREEN_HEIGHT, 0));
        scissorRect.set(min.x, min.y, max.x - min.x, max.y - min.y);
    }

    @Override
    public void render(float delta) {
        if (!connected) return;
        timeAccumulator += delta;
        while (timeAccumulator >= STEP_TIME) {
            updateLogic(STEP_TIME);
            timeAccumulator -= STEP_TIME;
            myGdxGame.world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
        draw();
    }

    private void updateLogic(float delta) {
        handleInput();

        if (serverRespawnIgnoreTimer > 0) serverRespawnIgnoreTimer -= delta;
        if (clientRespawnIgnoreTimer > 0) clientRespawnIgnoreTimer -= delta;

        if (gameStatus == GameState.GameStatus.PLAYING || gameStatus == GameState.GameStatus.COUNTDOWN) {
            if (myGdxGame.isHost && (server == null || !server.isConnected())) {
                endMatchWithWinner(topPanel.getPlayer1Name() + " WIN (Opponent left)");
                return;
            } else if (!myGdxGame.isHost && (client == null || !client.isConnected())) {
                endMatchWithWinner(topPanel.getPlayer2Name() + " WIN (Host left)");
                return;
            }
        }
        if (gameStatus == GameState.GameStatus.COUNTDOWN) {
            if (musicWainigStarted) {
                myGdxGame.audioManager.stopWaitingMusic();
                musicWainigStarted = false;
            }
        }
        else if (gameStatus == GameState.GameStatus.WAITING) {
            if (!musicWainigStarted) {
                myGdxGame.audioManager.playWaitingMusic();
                musicWainigStarted = true;
            }
        }
        else if (gameStatus == GameState.GameStatus.PLAYING) {
            if (!musicStarted) {
                myGdxGame.audioManager.playGameMusic(selectedMusicIndex);
                musicStarted = true;
            }
            if (!topPanel.isMatchActive()) {
                topPanel.setMatchActive(true);
            }
        }
        if (gameStatus == GameState.GameStatus.WAITING) {
            if (!musicWainigStarted) {
                myGdxGame.audioManager.playWaitingMusic();
                musicWainigStarted = true;
            }
        }
        checkMatchEndConditions();

        if (gameStatus == GameState.GameStatus.FINISHED) {
            resultDisplayTimer -= delta;
            if (resultDisplayTimer <= 0) {
                myGdxGame.showMenuScreen();
            }
            return;
        }

        if (myGdxGame.isHost) {
            PlayerInput remoteInput = (server != null) ? server.getClientInput() : null;
            updateServerState(delta);
            if (remoteInput != null) {
                if (remoteInput.playerName != null) {
                    topPanel.setPlayer2Name(remoteInput.playerName);
                }

                topPanel.setLuckLevel(localSkills[2], remoteInput.skills != null ? remoteInput.skills[2] : 0);

                if ((remoteInput.x != 0 || remoteInput.y != 0) && clientRespawnIgnoreTimer <= 0) {
                    if (!clientPlayer.isInHitStun() && !clientPlayer.isDodging()) {
                        clientPlayer.getBody().setTransform(remoteInput.x, remoteInput.y, 0);
                        clientPlayer.getBody().setLinearVelocity(remoteInput.vx, remoteInput.vy);
                    }
                    clientPlayer.setFacingRight(remoteInput.facingRight);
                    clientPlayer.setIsDodging(remoteInput.isDodging);
                    clientPlayer.setIsOnGround(remoteInput.isOnGround);
                    clientPlayer.setJumpsRemaining(remoteInput.jumpsRemaining);
                    clientPlayer.setIsClimbing(remoteInput.isClimbing);
                    if (remoteInput.isAttacking && !clientPlayer.isAttacking()) {
                        clientPlayer.startAttack(remoteInput.attackDir);
                    }
                    if (remoteInput.skills != null) {
                        clientSkills = remoteInput.skills;
                        int clientMaxHealth = 100 + GameSettings.HEALTH_BUFF[clientSkills[0]];
                        if (clientPlayer.getMaxHealth() != clientMaxHealth) {
                            clientPlayer.setMaxHealth(clientMaxHealth);
                            if (gameStatus != GameState.GameStatus.PLAYING) {
                                clientPlayer.setHealth(clientMaxHealth);
                            }
                        }
                    }
                }
            }
            clientPlayer.update(delta);
            sCritProc = false;
            cCritProc = false;
            boolean sIsCrit;
            boolean cIsCrit;
            sProtectProc = false;
            cProtectProc = false;
            boolean sIsProtect;
            boolean cIsProtect;
            boolean sIsHit = false;
            boolean cIsHit = false;

            if (serverPlayer.isAttacking() && gameStatus == GameState.GameStatus.PLAYING && !sStartAtack) {
                sIsCrit = GameSettings.CRITICAL_BUFF[localSkills[4]] >= random.nextFloat();
                sIsProtect = GameSettings.PROTECT_BUFF[clientSkills != null ? clientSkills[3] : 0] >= random.nextFloat();
                if (sIsProtect) {
                    sIsCrit = false;
                }
                if (serverPlayer.checkHit(clientPlayer, GameSettings.KNOCKBACK_BUFF[localSkills[1]], sIsCrit, sIsProtect)) {
                    sStartAtack = true;
                    sCritProc = sIsCrit;
                    sProtectProc = sIsProtect;

                    myGdxGame.audioManager.playHitSound();
                    if (!sProtectProc) {
                        bloodParticles.spawn(clientPlayer.getX() + clientPlayer.getWidth() / 2f, clientPlayer.getY() + clientPlayer.getHeight() / 2f, 12);
                        cIsHit = true;
                    }
                    if (sProtectProc && !sProtectShown) {
                        sProtectShown = true;
                        skillMessageManager.show("BLOCK!", clientPlayer.getX(), clientPlayer.getY() + clientPlayer.getHeight() + 20, Color.CYAN);
                    }
                    else if (sCritProc && !sCritShown && !sProtectShown) {
                        sCritShown = true;
                        skillMessageManager.show("CRIT!", clientPlayer.getX(), clientPlayer.getY() + clientPlayer.getHeight() + 20, Color.YELLOW);
                    }
                }
            }
            if (!serverPlayer.isAttacking()) { sStartAtack = false; }

            if (clientPlayer.isAttacking() && gameStatus == GameState.GameStatus.PLAYING && !cStartAtack) {
                cIsCrit = GameSettings.CRITICAL_BUFF[clientSkills != null ? clientSkills[4] : 0] >= random.nextFloat();
                cIsProtect = GameSettings.PROTECT_BUFF[localSkills[3]] >= random.nextFloat();
                if (cIsProtect) {
                    cIsCrit = false;
                }
                if (clientPlayer.checkHit(serverPlayer, GameSettings.KNOCKBACK_BUFF[clientSkills != null ? clientSkills[1] : 0], cIsCrit, cIsProtect)) {
                    cStartAtack = true;
                    cCritProc = cIsCrit;
                    cProtectProc = cIsProtect;
                    myGdxGame.audioManager.playHitSound();
                    myGdxGame.vibrate();
                    if (!cProtectProc) {
                        sIsHit = true;
                        bloodParticles.spawn(serverPlayer.getX() + serverPlayer.getWidth() / 2f, serverPlayer.getY() + serverPlayer.getHeight() / 2f, 12);
                    }
                    if (cProtectProc && !cProtectShown) {
                        cProtectShown = true;
                        skillMessageManager.show("BLOCK!", serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() + 20, Color.CYAN);
                    }
                    else if (cCritProc && !cCritShown && !cProtectShown) {
                        cProtectShown = true;
                        skillMessageManager.show("CRIT!", serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() + 20, Color.YELLOW);
                    }
                }
            }

            if (!clientPlayer.isAttacking()) { cStartAtack = false; }

            if (!sProtectProc) { sProtectShown = false; }
            if (!cProtectProc) { cProtectShown = false; }
            if (!sCritProc) { sCritShown = false; }
            if (!cCritProc) { cCritShown = false; }

            sLuckProc = false;
            cLuckProc = false;
            if (topPanel.getSLuckProc()) {
                sLuckProc = true;
                topPanel.setSLuckProc(false);
                skillMessageManager.show("LUCKY!", serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() + 20, Color.GREEN);
            }
            if (topPanel.getCLuckProc()) {
                cLuckProc = true;
                topPanel.setCLuckProc(false);
                skillMessageManager.show("LUCKY!", clientPlayer.getX(), clientPlayer.getY() + clientPlayer.getHeight() + 20, Color.GREEN);
            }

            bloodParticles.update(delta);

            if (gameStatus == GameState.GameStatus.PLAYING) {
                topPanel.checkOutOfBounds(serverPlayer.getX(), serverPlayer.getY(), true);
                topPanel.checkOutOfBounds(clientPlayer.getX(), clientPlayer.getY(), false);
            }
            topPanel.update(delta);

            if (serverPlayer.isInvoking() && serverPlayer.isInvocationFinished()) serverPlayer.setIsInvoking(false);
            if (clientPlayer.isInvoking() && clientPlayer.isInvocationFinished()) clientPlayer.setIsInvoking(false);

            if (topPanel.getNeedChange2Player()) {
                clientPlayer.getBody().setTransform(START_PLAYER_CLIENT_X * SCALE, START_PLAYER_CLIENT_Y * SCALE, 0);
                clientPlayer.getBody().setLinearVelocity(0, 0);
                clientPlayer.setHealth(clientPlayer.getMaxHealth());
                topPanel.setPlayer2Out(false);
                clientPlayer.setHitImmunityTimer(2.0f);
                clientRespawnIgnoreTimer = 0.5f;
                cRespawnConfirmFrames = RESPAWN_FRAMES;
            }
            if (topPanel.getNeedChange1Player()) {
                serverPlayer.getBody().setTransform(START_PLAYER_SERVER_X * SCALE, START_PLAYER_SERVER_Y * SCALE, 0);
                serverPlayer.getBody().setLinearVelocity(0, 0);
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
                topPanel.setPlayer1Out(false);
                serverPlayer.setHitImmunityTimer(2.0f);
                serverRespawnIgnoreTimer = 0.5f;
                sRespawnConfirmFrames = RESPAWN_FRAMES;
            }

            applyPlayerInput(serverPlayer, localInput);
            serverPlayer.update(delta);
            boolean cRespawn = cRespawnConfirmFrames > 0;
            boolean sRespawn = sRespawnConfirmFrames > 0;
            if (cRespawn) cRespawnConfirmFrames--;
            if (sRespawn) sRespawnConfirmFrames--;

            NetworkPacket packet = new NetworkPacket();
            packet.status = gameStatus;
            packet.countdown = countdown;
            packet.matchTimer = topPanel.getMatchTimer();
            packet.sNeedRespawn = sRespawn;
            packet.cNeedRespawn = cRespawn;
            packet.musicIndex = selectedMusicIndex;
            packet.sName = topPanel.getPlayer1Name();
            packet.cName = topPanel.getPlayer2Name();

            packet.sX = serverPlayer.getBody().getPosition().x;
            packet.sY = serverPlayer.getBody().getPosition().y;
            packet.sVX = serverPlayer.getBody().getLinearVelocity().x;
            packet.sVY = serverPlayer.getBody().getLinearVelocity().y;
            packet.sHealth = serverPlayer.getHealth();
            packet.sLives = topPanel.getPlayer1Lives();
            packet.sFacingRight = serverPlayer.getFacingRight();
            packet.sIsAttacking = serverPlayer.isAttacking();
            packet.sAttackDir = serverPlayer.getCurrentAttackDirection();
            packet.sInHitStun = serverPlayer.isInHitStun();
            packet.sIsDodging = serverPlayer.isDodging();
            packet.sOnGround = serverPlayer.isOnGround();
            packet.sJumps = serverPlayer.getJumpsRemaining();
            packet.sIsOut = topPanel.isPlayer1OutOfBounds();
            packet.sIsInvoking = serverPlayer.isInvoking();
            packet.sInvocationDuration = serverPlayer.getInvocationDuration();
            packet.sIsClimbing = serverPlayer.isClimbing();
            packet.sLuckProc = sLuckProc;
            packet.sCritProc = sCritProc;
            packet.sProtectProc = sProtectProc;
            packet.sHit = sIsHit;

            packet.cX = clientPlayer.getBody().getPosition().x;
            packet.cY = clientPlayer.getBody().getPosition().y;
            packet.cVX = clientPlayer.getBody().getLinearVelocity().x;
            packet.cVY = clientPlayer.getBody().getLinearVelocity().y;
            packet.cHealth = clientPlayer.getHealth();
            packet.cLives = topPanel.getPlayer2Lives();
            packet.cIsDodging = clientPlayer.isDodging();
            packet.cInHitStun = clientPlayer.isInHitStun();
            packet.cOnGround = clientPlayer.isOnGround();
            packet.cJumps = clientPlayer.getJumpsRemaining();
            packet.cIsOut = topPanel.isPlayer2OutOfBounds();
            packet.cIsInvoking = clientPlayer.isInvoking();
            packet.cInvocationDuration = clientPlayer.getInvocationDuration();
            packet.cIsClimbing = clientPlayer.isClimbing();
            packet.cLuckProc = cLuckProc;
            packet.cCritProc = cCritProc;
            packet.cProtectProc = cProtectProc;
            packet.cHit = cIsHit;

            if (server != null) server.sendState(packet);
        }
        else {
            NetworkPacket packet = (client != null) ? client.latestPacket : null;

            if (packet != null) {
                gameStatus = packet.status;
                countdown = packet.countdown;
                topPanel.setMatchTimer(packet.matchTimer);
                topPanel.setPlayer1Lives(packet.sLives);
                topPanel.setPlayer2Lives(packet.cLives);
                topPanel.setPlayer1Out(packet.sIsOut);
                topPanel.setPlayer2Out(packet.cIsOut);
                selectedMusicIndex = packet.musicIndex;
                if (packet.sName != null) topPanel.setPlayer1Name(packet.sName);
                if (packet.sName != null) topPanel.setPlayer2Name(packet.cName);
                if (packet.sSkills != null) {
                    serverSkills = packet.sSkills;
                    int serverMaxHealth = 100 + GameSettings.HEALTH_BUFF[serverSkills[0]];
                    if (serverPlayer.getMaxHealth() != serverMaxHealth) {
                        serverPlayer.setMaxHealth(serverMaxHealth);
                    }
                }


                if (serverRespawnIgnoreTimer <= 0) {
                    serverPlayer.getBody().setTransform(packet.sX, packet.sY, 0);
                    serverPlayer.getBody().setLinearVelocity(packet.sVX, packet.sVY);
                }
                serverPlayer.setHealth(packet.sHealth);
                serverPlayer.setFacingRight(packet.sFacingRight);
                serverPlayer.setInHitStun(packet.sInHitStun);
                serverPlayer.setIsDodging(packet.sIsDodging);
                serverPlayer.setIsOnGround(packet.sOnGround);
                serverPlayer.setJumpsRemaining(packet.sJumps);
                serverPlayer.setIsClimbing(packet.sIsClimbing);

                if (packet.sLuckProc) { skillMessageManager.show("LUCKY!", serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() + 20, Color.GREEN); }
                if (packet.cLuckProc) { skillMessageManager.show("LUCKY!", clientPlayer.getX(), clientPlayer.getY() + clientPlayer.getHeight() + 20, Color.GREEN); }

                if (packet.sProtectProc && !sProtectShown) {
                    sProtectShown = true;
                    skillMessageManager.show("BLOCK!", clientPlayer.getX(), clientPlayer.getY() + clientPlayer.getHeight() + 20, Color.CYAN);
                } else if (packet.sCritProc && !sCritShown && !sProtectShown) {
                    sCritShown = true;
                    skillMessageManager.show("CRIT!", clientPlayer.getX(), clientPlayer.getY() + clientPlayer.getHeight() + 20, Color.YELLOW);
                }
                if (packet.cProtectProc && !cProtectShown) {
                    cProtectShown = true;
                    skillMessageManager.show("BLOCK!", serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() + 20, Color.CYAN);
                } else if (packet.cCritProc && !cCritShown && !cProtectShown) {
                    cCritShown = true;
                    skillMessageManager.show("CRIT!", serverPlayer.getX(), serverPlayer.getY() + serverPlayer.getHeight() + 20, Color.YELLOW);
                }

                if (!packet.sCritProc) sCritShown = false;
                if (!packet.cCritProc) cCritShown = false;
                if (!packet.sProtectProc) sProtectShown = false;
                if (!packet.cProtectProc) cProtectShown = false;

                if (!serverPlayer.isInvoking() && packet.sIsInvoking) {
                    serverPlayer.startInvocation(packet.sInvocationDuration);
                }
                serverPlayer.setIsInvoking(packet.sIsInvoking, packet.sInvocationDuration);
                if (gameStatus == GameState.GameStatus.COUNTDOWN && packet.sIsInvoking) {
                    serverPlayer.setStateTime(3.0f - packet.countdown);
                }

                if (packet.sIsAttacking && !serverPlayer.isAttacking()) {
                    serverPlayer.startAttack(packet.sAttackDir);
                }

                clientPlayer.setHealth(packet.cHealth);
                clientPlayer.setInHitStun(packet.cInHitStun);
                clientPlayer.setIsClimbing(packet.cIsClimbing);
                if (packet.cHit) {
                    myGdxGame.audioManager.playHitSound();
                    myGdxGame.vibrate();
                    bloodParticles.spawn(clientPlayer.getX() + clientPlayer.getWidth() / 2f,
                        clientPlayer.getY() + clientPlayer.getHeight() / 2f, 12);
                }
                if (packet.sHit) {
                    myGdxGame.audioManager.playHitSound();
                    bloodParticles.spawn(serverPlayer.getX() + serverPlayer.getWidth() / 2f,
                        serverPlayer.getY() + serverPlayer.getHeight() / 2f, 12);
                }
                bloodParticles.update(delta);
                if (!clientPlayer.isInvoking() && packet.cIsInvoking) {
                    clientPlayer.startInvocation(packet.cInvocationDuration);
                }
                clientPlayer.setIsInvoking(packet.cIsInvoking, packet.cInvocationDuration);
                if (gameStatus == GameState.GameStatus.COUNTDOWN && packet.cIsInvoking) {
                    clientPlayer.setStateTime(3.0f - packet.countdown);
                }

                if (packet.cNeedRespawn) {
                    clientPlayer.getBody().setTransform(packet.cX, packet.cY, 0);
                    clientPlayer.getBody().setLinearVelocity(0, 0);
                    clientPlayer.setHealth(clientPlayer.getMaxHealth());
                    clientRespawnIgnoreTimer = 0.5f;
                } else if ((packet.cInHitStun || packet.cIsDodging) && clientRespawnIgnoreTimer <= 0) {
                    clientPlayer.getBody().setTransform(packet.cX, packet.cY, 0);
                    clientPlayer.getBody().setLinearVelocity(packet.cVX, packet.cVY);
                }
            }

            applyPlayerInput(clientPlayer, localInput);
            clientPlayer.update(delta);

            topPanel.update(delta);

            localInput.playerName = myGdxGame.playerName;
            localInput.x = clientPlayer.getBody().getPosition().x;
            localInput.y = clientPlayer.getBody().getPosition().y;
            localInput.vx = clientPlayer.getBody().getLinearVelocity().x;
            localInput.vy = clientPlayer.getBody().getLinearVelocity().y;
            localInput.health = clientPlayer.getHealth();
            localInput.facingRight = clientPlayer.getFacingRight();
            localInput.isAttacking = clientPlayer.isAttacking();
            localInput.attackDir = clientPlayer.getCurrentAttackDirection();
            localInput.isDodging = clientPlayer.isDodging();
            localInput.isOnGround = clientPlayer.isOnGround();
            localInput.jumpsRemaining = clientPlayer.getJumpsRemaining();
            localInput.isClimbing = clientPlayer.isClimbing();
            localInput.skills = localSkills;

            if (client != null) client.sendInput(localInput);
            serverPlayer.update(delta);
        }

        skillMessageManager.update(delta);
    }

    private void checkMatchEndConditions() {
        if (gameStatus != GameState.GameStatus.PLAYING) return;
        if (topPanel.getMatchTimer() <= 0 || topPanel.getPlayer1Lives() <= 0 || topPanel.getPlayer2Lives() <= 0) {
            endMatch();
        }
    }

    private void endMatch() {
        myGdxGame.audioManager.playVictorySound();
        myGdxGame.audioManager.stopGameMusic();
        String winner = "DRAW!";
        boolean localWon = false;

        if (topPanel.getPlayer1Lives() <= 0) {
            winner = topPanel.getPlayer2Name() + " - WIN!";
            localWon = !myGdxGame.isHost;
        }
        else if (topPanel.getPlayer2Lives() <= 0) {
            winner = topPanel.getPlayer1Name() + " - WIN!";
            localWon = myGdxGame.isHost;
        }
        awardExp(localWon);
        endMatchWithWinner(winner);
    }
    private void awardExp(boolean won) {
        int[] skills = MemoryManager.loadAllSkills();

        int baseExp = won ? GameSettings.EXP_WIN : GameSettings.EXP_LOSE;
        int finalExp = (int)(baseExp * GameSettings.MORE_EXP_BUFF[skills[5]]);

        MemoryManager.addExp(finalExp);
        System.out.println("Exp awarded: " + finalExp + " (won=" + won + ", multiplier=" + GameSettings.MORE_EXP_BUFF[skills[5]] + ")");
    }
    private void endMatchWithWinner(String message) {
        gameStatus = GameState.GameStatus.FINISHED;
        resultText.setText(message);
        resultDisplayTimer = 7.0f;
    }

    private void setPivotOffset() {
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
            serverPlayer.setPivotOffsetX(serverPlayer.getPivotOffsetX() - 0.5f);
            clientPlayer.setPivotOffsetX(clientPlayer.getPivotOffsetX() - 0.5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
            serverPlayer.setPivotOffsetX(serverPlayer.getPivotOffsetX() + 0.5f);
            clientPlayer.setPivotOffsetX(clientPlayer.getPivotOffsetX() + 0.5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
            serverPlayer.setPivotOffsetY(serverPlayer.getPivotOffsetY() - 0.5f);
            clientPlayer.setPivotOffsetY(clientPlayer.getPivotOffsetY() - 0.5f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
            serverPlayer.setPivotOffsetY(serverPlayer.getPivotOffsetY() + 0.5f);
            clientPlayer.setPivotOffsetY(clientPlayer.getPivotOffsetY() + 0.5f);
        }
        System.out.println("Pivot X: " + serverPlayer.getPivotOffsetX() + "; Pivot Y: " + serverPlayer.getPivotOffsetY());
    }

    private void handleInput() {
        localInput.moveLeft = false;
        localInput.moveRight = false;
        localInput.wantToGoDown = false;
        localInput.attack = false;
        localInput.dodge = false;
        localInput.jump = false;

        updateUI();

        localInput.moveLeft = Gdx.input.isKeyPressed(Input.Keys.A) || (joystick.isCaptured() && joystick.isLeft());
        localInput.moveRight = Gdx.input.isKeyPressed(Input.Keys.D) || (joystick.isCaptured() && joystick.isRight());
        localInput.wantToGoDown = Gdx.input.isKeyPressed(Input.Keys.S) || (joystick.isCaptured() && joystick.isDown());

        boolean isJumpButtonDown = Gdx.input.isKeyPressed(Input.Keys.SPACE) || jumpButton.isPressed();
        localInput.jump = isJumpButtonDown && !jumpWasPressed;
        jumpWasPressed = isJumpButtonDown;

        localInput.dodge = Gdx.input.isKeyPressed(Input.Keys.Q) || dodgeButton.isPressed();
        localInput.attack = Gdx.input.isKeyPressed(Input.Keys.E) || attackButton.isPressed();
        localInput.attackUp = joystick.isUp() || Gdx.input.isKeyPressed(Input.Keys.W);
        localInput.attackDown = joystick.isDown() || Gdx.input.isKeyPressed(Input.Keys.S);
    }

    private void applyPlayerInput(PlayerObject p, PlayerInput in) {
        if (gameStatus != GameState.GameStatus.PLAYING || !p.canReceiveInput()) return;
        Vector2 vel = p.getBody().getLinearVelocity();
        float targetX;
        if (in.moveLeft && p.canMove()) targetX = -PLAYER_MAX_VELOCITY;
        else if (in.moveRight && p.canMove()) targetX = PLAYER_MAX_VELOCITY;
        else targetX = vel.x * 0.8f;

        if (!p.isInHitStun() && !p.isDodging()) p.getBody().setLinearVelocity(targetX, vel.y);
        p.setIsClimbing(p.canMove() && p.isOnGround() && (in.moveLeft || in.moveRight) && Math.abs(vel.x) < 0.1f && Math.abs(vel.y) < 0.1f);
        if (in.jump && p.canJump()) p.jump(PLAYER_JUMP_FORCE);
        if (in.dodge && p.canDodge()) p.dodge(in.moveLeft ? -1 : (in.moveRight ? 1 : 0));
        p.setWantsToGoDown(in.wantToGoDown);
        if (in.attack && p.canAttack()) p.startAttack(in.attackUp ? AttackDirection.UP : (in.attackDown ? AttackDirection.DOWN : AttackDirection.SIDE));
    }

    private void updateServerState(float delta) {
        if (gameStatus == GameState.GameStatus.WAITING && server != null && server.isConnected()) {
            gameStatus = GameState.GameStatus.COUNTDOWN;
            countdown = 3f;
            serverPlayer.startInvocation(3.0f);
            clientPlayer.startInvocation(3.0f);
            musicStarted = false;
            selectedMusicIndex = myGdxGame.audioManager.getRandomMusicIndex();
        } else if (gameStatus == GameState.GameStatus.COUNTDOWN) {
            countdown -= delta;
            musicStarted = false;
            if (countdown <= 0) {
                gameStatus = GameState.GameStatus.PLAYING;
                serverPlayer.setIsInvoking(false);
                clientPlayer.setIsInvoking(false);
            }
        }
    }

    private void updateUI() {
        boolean anyTouch = false;
        jumpButton.setPressed(false);
        dodgeButton.setPressed(false);
        attackButton.setPressed(false);
        homeButton.setPressed(false);
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                anyTouch = true;
                Vector3 t = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0));
                if (jumpButton.isHit(t.x, t.y)) jumpButton.setPressed(true);
                if (dodgeButton.isHit(t.x, t.y)) dodgeButton.setPressed(true);
                if (attackButton.isHit(t.x, t.y)) attackButton.setPressed(true);
                if (gameStatus == GameState.GameStatus.WAITING && homeButton.isHit(t.x, t.y)) {
                    homeButton.setPressed(true);
                    System.out.println(1221);
                    myGdxGame.showMenuScreen();
                    return;
                }
                joystick.processTouch(t.x, t.y, true, i);
            } else {
                joystick.processTouch(0, 0, false, i);
            }
        }
        if (!anyTouch) joystick.reset();
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(myGdxGame.camera.combined);

        HdpiUtils.glScissor((int)scissorRect.x, (int)scissorRect.y, (int)scissorRect.width, (int)scissorRect.height);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

        batch.begin();

        backgroundView.draw(batch);

        if (myGdxGame.isHost) {
            clientPlayer.draw(batch);
            serverPlayer.draw(batch);
        }
        else {
            serverPlayer.draw(batch);
            clientPlayer.draw(batch);
        }
        bloodParticles.draw(batch);

        batch.end();
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);

        batch.begin();
        topPanel.draw(batch);
        joystick.draw(batch);
        jumpButton.draw(batch);
        dodgeButton.draw(batch);
        attackButton.draw(batch);
        skillMessageManager.draw(batch);

        if (gameStatus == GameState.GameStatus.WAITING) {
            homeButton.draw(batch);
            waitingText.draw(batch);
            ipAddressText.setText("YOUR IP: " + getIP());
            ipAddressText.setCenterX(SCREEN_WIDTH / 2f);
            ipAddressText.draw(batch);
        } else if (gameStatus == GameState.GameStatus.COUNTDOWN) {
            countdownText.setText("START IN: " + (int)Math.ceil(countdown));
            countdownText.setCenterX(SCREEN_WIDTH / 2f);
            countdownText.draw(batch);
        } else if (gameStatus == GameState.GameStatus.FINISHED) {
            resultText.setCenterX(SCREEN_WIDTH / 2f);
            resultText.draw(batch);
        }

        batch.end();
    }

    private void drawPlayerHitboxes() {
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

    private void drawAttackHitboxes() {
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

    private String getIP() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface network = interfaces.nextElement();
                if (network.isUp() && !network.isLoopback()) {
                    java.util.Enumeration<java.net.InetAddress> addresses = network.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        if (addr.getAddress().length == 4 && !addr.getHostAddress().startsWith("127.")) return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    @Override
    public void hide() {
        disconnect();
    }
    public void disconnect() {
        if (server != null) server.stop();
        if (client != null) client.disconnect();
        connected = false;
    }
}
