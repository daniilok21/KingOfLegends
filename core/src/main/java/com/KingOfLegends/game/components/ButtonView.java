package com.KingOfLegends.game.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ButtonView extends View {

    Texture texture;
    BitmapFont bitmapFont;

    String text;

    float textX;
    float textY;

    private boolean isEnabled;
    private boolean isPressed = false;

    public ButtonView(float x, float y, float width, float height, BitmapFont font, String texturePath, String text) {
        super(x, y, width, height);

        this.text = text;
        this.bitmapFont = font;

        isEnabled = true;

        texture = new Texture(texturePath);

        GlyphLayout glyphLayout = new GlyphLayout(bitmapFont, text);
        float textWidth = glyphLayout.width;
        float textHeight = glyphLayout.height;

        textX = x + (width - textWidth) / 2;
        textY = y + (height + textHeight) / 2;
    }

    public ButtonView(float x, float y, float width, float height, String texturePath) {
        super(x, y, width, height);
        isEnabled = true;
        texture  = new Texture(texturePath);
    }

    @Override
    public void draw(SpriteBatch batch) {
        Color originalColor = new Color(batch.getColor());
        if (!isEnabled) {
            batch.setColor(0.6f, 0.6f, 0.6f, 1f);
        }
        else if (isPressed) {
            batch.setColor(0.7f, 0.7f, 0.7f, 1f);
        }
        batch.draw(texture, x, y, width, height);
        batch.setColor(originalColor);

        if (bitmapFont != null) {
            Color originalFontColor = new Color(bitmapFont.getColor());
            if (!isEnabled) {
                bitmapFont.setColor(0.7f, 0.7f, 0.7f, 1f);
            }
            bitmapFont.draw(batch, text, textX, textY);
            bitmapFont.setColor(originalFontColor);
        }
    }
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    public void setPressed(boolean pressed) {this.isPressed = pressed;}
    public void setText(String newText) {
        this.text = newText;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
    public boolean isPressed() {
        return isPressed;
    }

    @Override
    public void dispose() {
        texture.dispose();
        if (bitmapFont != null) bitmapFont.dispose();
    }

}
