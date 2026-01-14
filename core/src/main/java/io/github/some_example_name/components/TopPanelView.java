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
    TextView player1NameText;
    TextView player2NameText;
    TextView timerText;
    TextView player1RespawnText;
    TextView player2RespawnText;
    private String player1Name;
    private String player2Name;
    private int player1Lives;
    private int player2Lives;
    private float matchTimer;
    private boolean isMatchActive;
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
                        BitmapFont font, BitmapFont timerFont, String panelTexturePath,
                        String heartTexturePath, String emptyHeartTexturePath) {
        super(x, y, width, height);


        player1NameText = new TextView(font, x, y + height - 40, "Player1");
        player1NameText.setCenterX(x + width / 4 - 10);
        player2NameText = new TextView(font, x + width - 100, y + height - 40, "Player2");
        player2NameText.setCenterX(x + width / 4 + 10);
        timerText = new TextView(font, x + width / 2, y + height / 2, "10:00");
        player1RespawnText = new TextView(timerFont, x + 20, y + 15, "");
        player2RespawnText = new TextView(timerFont, x + width - 50, y + 15, "");

        panelBackground = new Texture(panelTexturePath);
        heartTexture = new Texture(heartTexturePath);
        emptyHeartTexture = new Texture(emptyHeartTexturePath);

        reset();
    }

    private void updateTextPositions() {
        timerText.setCenterX(x + width / 2);
        timerText.setCenterY(y + height / 2);
        player1NameText.setCenterX(x + width / 4 - 10);
        player1RespawnText.setX(x + 20);
        player2NameText.setCenterX(x + width / 4 * 3 + 10);
        player2RespawnText.setX(x + width - player2RespawnText.getWidth() - 20);
    }

    public void reset() {
        player1Name = "Player1";
        player2Name = "Player2";
        player1Lives = GameSettings.PLAYER_MAX_LIVES;
        player2Lives = GameSettings.PLAYER_MAX_LIVES;
        matchTimer = GameSettings.MATCH_DURATION_SECONDS;
        isMatchActive = true;

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

            if (player1OutTimer >= GameSettings.OUT_OF_BOUNDS_RESPAWN_TIME) {
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
        }
        else {
            player1OutTimer = 0f;
        }

        if (player2IsOutOfBounds) {
            player2OutTimer += delta;

            if (player2OutTimer >= GameSettings.OUT_OF_BOUNDS_RESPAWN_TIME) {
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

        }
        else {
            player2OutTimer = 0f;
        }
        updateText();
        updateTextPositions();
    }
    private void updateText() {

        int minutes = (int) (matchTimer / 60);
        int seconds = (int) (matchTimer % 60);
        String timeText = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeText);

        if (player1IsOutOfBounds) {
            float timeLeft = 3.0f - player1OutTimer;
            player1RespawnText.setText(String.format("%.1f", timeLeft));
        } else {
            player1RespawnText.setText("");
        }

        if (player2IsOutOfBounds) {
            float timeLeft = 3.0f - player2OutTimer;
            player2RespawnText.setText(String.format("%.1f", timeLeft));
        } else {
            player2RespawnText.setText("");
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(panelBackground, x, y, width, height);

        float heartsStartX = x + width / 7f + 30;
        float heartsY = y + heartHeight / 2f;
        for (int i = 0; i < GameSettings.PLAYER_MAX_LIVES; i++) {
            if (i < player1Lives) {
                batch.draw(heartTexture, heartsStartX + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
            else {
                batch.draw(emptyHeartTexture, heartsStartX + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
        }

        float totalHeartsWidth = GameSettings.PLAYER_MAX_LIVES * heartWidth + (GameSettings.PLAYER_MAX_LIVES - 1) * heartSpacing;
        float heartsStartX2 = x + width - totalHeartsWidth - width / 7f - 30;

        for (int i = 0; i < GameSettings.PLAYER_MAX_LIVES; i++) {
            if (i < player2Lives) {
                batch.draw(heartTexture, heartsStartX2 + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
            else {
                batch.draw(emptyHeartTexture, heartsStartX2 + i * (heartWidth + heartSpacing), heartsY, heartWidth, heartHeight);
            }
        }

        player1NameText.draw(batch);
        player2NameText.draw(batch);
        timerText.draw(batch);
        player1RespawnText.draw(batch);
        player2RespawnText.draw(batch);
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
                System.out.println(player2Name + " вылетел. 3 секунды чтобы вернуться");
            }
            else if (!isOut && player2IsOutOfBounds) {
                player2IsOutOfBounds = false;
                player2OutTimer = 0f;
                System.out.println(player2Name + " вернулся в арену.");
            }
        }
        updateText();
        updateTextPositions();
    }
    public void setPlayer1Name(String name) {
        player1Name = name;
        updateText();
        updateTextPositions();
    }
    public void setPlayer2Name(String name) {
        player2Name = name;
        updateText();
        updateTextPositions();
    }

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

    public void setMatchTimer(float timer) {
        matchTimer = timer;
        updateText();
        updateTextPositions();
    }
    public float getMatchTimer() { return matchTimer; }

    public boolean isMatchActive() { return isMatchActive; }

    public boolean isPlayer1OutOfBounds() { return player1IsOutOfBounds; }
    public boolean isPlayer2OutOfBounds() { return player2IsOutOfBounds; }
    public String getPlayer1Name() { return player1Name; }
    public String getPlayer2Name() { return player2Name; }


    @Override
    public void dispose() {
        if (panelBackground != null) panelBackground.dispose();
        if (heartTexture != null) heartTexture.dispose();
        if (emptyHeartTexture != null) emptyHeartTexture.dispose();
        if (player1NameText != null) player1NameText.dispose();
        if (player2NameText != null) player2NameText.dispose();
        if (timerText != null) timerText.dispose();
        if (player1RespawnText != null) player1RespawnText.dispose();
        if (player2RespawnText != null) player2RespawnText.dispose();
    }
}
