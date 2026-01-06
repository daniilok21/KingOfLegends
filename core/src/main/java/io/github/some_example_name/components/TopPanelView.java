package io.github.some_example_name.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

import io.github.some_example_name.GameSettings;

public class TopPanelView extends View {
    private Texture panelBackground;
    private Texture heartTexture;
    private Texture emptyHeartTexture;

    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private String player1Name;
    private String player2Name;
    private int player1Lives;
    private int player2Lives;
    private float matchTimer;
    private boolean isMatchActive;
    private float player1RespawnTimer;
    private float player2RespawnTimer;
    private boolean player1IsOutOfBounds;
    private boolean player2IsOutOfBounds;
    private float player1OutTimer = 0f;
    private float player2OutTimer = 0f;
    private boolean needChange1Player;
    private boolean needChange2Player;

    private int heartWidth = 30;
    private int heartHeight = 30;
    private int heartSpacing = 5;

    public TopPanelView(float x, float y, float width, float height,
                        BitmapFont font, String panelTexturePath,
                        String heartTexturePath, String emptyHeartTexturePath) {
        super(x, y, width, height);

        this.font = font;
        this.glyphLayout = new GlyphLayout();

        panelBackground = new Texture(panelTexturePath);
        heartTexture = new Texture(heartTexturePath);
        emptyHeartTexture = new Texture(emptyHeartTexturePath);

        reset();
    }

    public void reset() {
        player1Name = "Player1";
        player2Name = "Player2";
        player1Lives = GameSettings.PLAYER_MAX_LIVES;
        player2Lives = GameSettings.PLAYER_MAX_LIVES;
        matchTimer = GameSettings.MATCH_DURATION_SECONDS;
        isMatchActive = true;

        player1RespawnTimer = 0;
        player2RespawnTimer = 0;
        player1IsOutOfBounds = false;
        player2IsOutOfBounds = false;
        player1OutTimer = 0f;
        player2OutTimer = 0f;

        needChange1Player = false;
        needChange2Player = false;
    }

    public void update(float delta) {
        if (!isMatchActive) return;

        matchTimer -= delta;
        if (matchTimer <= 0) {
            matchTimer = 0;
            isMatchActive = false;
        }

        if (player1IsOutOfBounds) {
            player1OutTimer += delta;
            player1RespawnTimer -= delta;

            if (player1OutTimer >= 3.0f) {
                player1Lives--;
                player1IsOutOfBounds = false;
                player1OutTimer = 0f;
                System.out.println(player1Name + " потерял жизнь. Осталось: " + player1Lives);
                needChange1Player = true;
                if (player1Lives <= 0 || player2Lives <= 0) {
                    isMatchActive = false;
                    System.out.println("Матч завершен!");
                }
            }

            if (player1RespawnTimer <= 0) {
                player1IsOutOfBounds = false;
                player1OutTimer = 0f;
            }
        } else {
            player1OutTimer = 0f;
        }

        if (player2IsOutOfBounds) {
            player2OutTimer += delta;
            player2RespawnTimer -= delta;

            if (player2OutTimer >= 3.0f) {
                player2Lives--;
                player2IsOutOfBounds = false;
                player2OutTimer = 0f;
                System.out.println(player2Name + " потерял жизнь. Осталось: " + player2Lives);
                needChange2Player = true;
                if (player1Lives <= 0 || player2Lives <= 0) {
                    isMatchActive = false;
                    System.out.println("Матч завершен!");
                }
            }

            if (player2RespawnTimer <= 0) {
                player2IsOutOfBounds = false;
                player2OutTimer = 0f;
            }
        } else {
            player2OutTimer = 0f;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(panelBackground, x, y, width, height);

        Color originalColor = batch.getColor();

        font.setColor(Color.WHITE);
        glyphLayout.setText(font, player1Name);
        font.draw(batch, player1Name, x + 20, y + height - 20);

        float heartsStartX = x + 20;
        float heartsY = y + heartHeight / 2f;

        for (int i = 0; i < GameSettings.PLAYER_MAX_LIVES; i++) {
            if (i < player1Lives) {
                batch.draw(heartTexture, heartsStartX + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
            else {
                batch.draw(emptyHeartTexture, heartsStartX + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
        }

        int minutes = (int) (matchTimer / 60);
        int seconds = (int) (matchTimer % 60);
        String timeText = String.format("%02d:%02d", minutes, seconds);


        glyphLayout.setText(font, timeText);
        font.draw(batch, timeText, x + width / 2 - glyphLayout.width / 2, y + height - 20);

        glyphLayout.setText(font, player2Name);
        font.draw(batch, player2Name, x + width - glyphLayout.width - 20, y + height - 20);

        float totalHeartsWidth = GameSettings.PLAYER_MAX_LIVES * heartWidth + (GameSettings.PLAYER_MAX_LIVES - 1) * heartSpacing;
        float heartsStartX2 = x + width - totalHeartsWidth - 20;

        for (int i = 0; i < GameSettings.PLAYER_MAX_LIVES; i++) {
            if (i < player2Lives) {
                batch.draw(heartTexture, heartsStartX2 + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
            else {
                batch.draw(emptyHeartTexture, heartsStartX2 + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
        }

        if (player1IsOutOfBounds) {
            font.setColor(Color.RED);
            float timeLeft = 3.0f - player1OutTimer;
            String respawnText = String.format("%.1f", timeLeft);
            glyphLayout.setText(font, respawnText);
            font.draw(batch, respawnText, x + 20, y + 15);
        }

        if (player2IsOutOfBounds) {
            font.setColor(Color.RED);
            float timeLeft = 3.0f - player2OutTimer;
            String respawnText = String.format("%.1f", timeLeft);
            glyphLayout.setText(font, respawnText);
            font.draw(batch, respawnText, x + width - glyphLayout.width - 20, y + 15);
        }

        font.setColor(Color.WHITE);
        batch.setColor(originalColor);
    }

    public void checkOutOfBounds(float playerX, float playerY, boolean isPlayer1) {
        if (!isMatchActive) return;

        boolean isOut = playerY < GameSettings.ARENA_BOTTOM_BOUND ||
            playerX < GameSettings.ARENA_LEFT_BOUND ||
            playerX > GameSettings.ARENA_RIGHT_BOUND;

        if (isPlayer1) {
            if (isOut && !player1IsOutOfBounds) {
                player1IsOutOfBounds = true;
                player1OutTimer = 0f;
                player1RespawnTimer = GameSettings.OUT_OF_BOUNDS_RESPAWN_TIME;
                System.out.println(player1Name + " вылетел. 3 секунды чтобы вернуться");
            }
            else if (!isOut && player1IsOutOfBounds) {
                player1IsOutOfBounds = false;
                player1OutTimer = 0f;
                System.out.println(player1Name + " вернулся в арену.");
            }
        }
        else {
            if (isOut && !player2IsOutOfBounds) {
                player2IsOutOfBounds = true;
                player2OutTimer = 0f;
                player2RespawnTimer = GameSettings.OUT_OF_BOUNDS_RESPAWN_TIME;
                System.out.println(player2Name + " вылетел. 3 секунды чтобы вернуться");
            }
            else if (!isOut && player2IsOutOfBounds) {
                player2IsOutOfBounds = false;
                player2OutTimer = 0f;
                System.out.println(player2Name + " вернулся в арену.");
            }
        }
    }
    public void setPlayer1Name(String name) { player1Name = name; }
    public void setPlayer2Name(String name) { player2Name = name; }

    public void setPlayer1Lives(int lives) { player1Lives = lives; }
    public void setPlayer2Lives(int lives) { player2Lives = lives; }
    public boolean getNeedChange1Player() {
        if (needChange1Player) {
            needChange1Player = false;
            return true;
        }
        return false;
    }
    public boolean getNeedChange2Player() {
        if (needChange2Player) {
            needChange2Player = false;
            return true;
        }
        return false;
    }
    public int getPlayer1Lives() { return player1Lives; }
    public int getPlayer2Lives() { return player2Lives; }

    public void setMatchTimer(float timer) { matchTimer = timer; }
    public float getMatchTimer() { return matchTimer; }

    public boolean isMatchActive() { return isMatchActive; }

    public boolean isPlayer1OutOfBounds() { return player1IsOutOfBounds; }
    public boolean isPlayer2OutOfBounds() { return player2IsOutOfBounds; }

    @Override
    public void dispose() {
        if (panelBackground != null) panelBackground.dispose();
        if (heartTexture != null) heartTexture.dispose();
        if (emptyHeartTexture != null) emptyHeartTexture.dispose();
    }
}
