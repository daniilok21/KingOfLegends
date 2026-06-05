package com.KingOfLegends.game.components;

import com.KingOfLegends.game.MyGdxGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class ToggleView extends View {

    private Texture onTexture;
    private Texture offTexture;
    private BitmapFont font;
    private String label;
    private boolean value;
    private boolean is_active;

    public ToggleView(float x, float y, float width, float height, String label,
                      boolean initialValue, BitmapFont font,
                      String onTexturePath, String offTexturePath) {
        super(x, y, width, height);
        this.label = label;
        this.value = initialValue;
        this.font = font;
        onTexture = new Texture(onTexturePath);
        offTexture = new Texture(offTexturePath);
    }

    public boolean tap(float tx, float ty) {
        if (isHit(tx, ty)) {
            value = !value;
            return true;
        }
        return false;
    }

    public void setIsActive(boolean is_active) {
        this.is_active = is_active;
    }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }

    @Override
    public void draw(SpriteBatch batch) {
        font.setColor(Color.BROWN);
        font.draw(batch, label, x, y + height + 40);

        batch.draw(is_active ? offTexture : onTexture, x, y, width, height);

        font.setColor(value ? Color.GREEN : Color.RED);
        font.draw(batch, value ? "ON" : "OFF", x + width + 20, y + height / 2f + 10);
        font.setColor(Color.BROWN);

        is_active = false;
    }

    @Override
    public void dispose() {
        onTexture.dispose();
        offTexture.dispose();
    }
}
