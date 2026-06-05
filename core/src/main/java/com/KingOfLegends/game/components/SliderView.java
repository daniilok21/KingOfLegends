package com.KingOfLegends.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SliderView extends View {

    private Texture fillTexture;
    private Texture emptyTexture;
    private Texture handleTexture;
    private BitmapFont font;
    private String text;

    private float value;
    private boolean isActive = false;

    private static final float TRACK_HEIGHT = 40f;

    public SliderView(float x, float y, float width, String text, float value,
                      BitmapFont font, String fillTexturePath, String emptyTexturePath, String circleButtonTexturePath) {
        super(x, y, width, TRACK_HEIGHT);
        this.text = text;
        this.value = value;
        this.font = font;
        fillTexture = new Texture(fillTexturePath);
        emptyTexture = new Texture(emptyTexturePath);
        handleTexture = new Texture(circleButtonTexturePath);
    }

    public boolean touchDown(float tx, float ty) {
        if (tx >= x && tx <= x + width && ty >= y - 20 && ty <= y + height + 20) {
            isActive = true;
            updateValue(tx);
            return true;
        }
        return false;
    }

    public boolean touchActive(float tx, float ty) {
        if (isActive) {
            updateValue(tx);
            return true;
        }
        return false;
    }

    public void touchUp() {
        isActive = false;
    }

    private void updateValue(float tx) {
        value = Math.max(0f, Math.min(1f, (tx - x) / width));
    }

    public float getValue() { return value; }

    public void setValue(float value) {
        this.value = Math.max(0f, Math.min(1f, value));
    }

    @Override
    public void draw(SpriteBatch batch) {
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width / 2f) - (layout.width / 2f);
        font.draw(batch, text, textX, y + height + 35);

        float filledWidth = value * width;
        float emptyWidth = width - filledWidth;

        if (filledWidth > 0) {
            batch.draw(fillTexture, x, y, filledWidth, TRACK_HEIGHT, 0, 0, value, 1f);
        }
        if (emptyWidth > 0) {
            batch.draw(emptyTexture, x + filledWidth, y, emptyWidth, TRACK_HEIGHT, value, 0f, 1f, 1f);
        }

        float circleBtnSize = TRACK_HEIGHT + 20f;
        batch.draw(handleTexture,
            x + value * width - circleBtnSize / 2f,
            y + TRACK_HEIGHT / 2f - circleBtnSize / 2f,
            circleBtnSize, circleBtnSize);
    }

    @Override
    public void dispose() {
        fillTexture.dispose();
        emptyTexture.dispose();
    }
}
