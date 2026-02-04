package com.KingOfLegends.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.KingOfLegends.game.GameSettings;

public class MovingBackgroundView extends View {

    Texture texture;

    int texture1Y;
    int speed = 2;

    public MovingBackgroundView(String pathToTexture) {
        super(0, 0);
        texture1Y = 0;
        texture = new Texture(pathToTexture);
    }

    public void move() {
        texture1Y -= speed;

        if (texture1Y <= -GameSettings.SCREEN_HEIGHT) {
            texture1Y = GameSettings.SCREEN_HEIGHT;
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        batch.draw(texture, 0, texture1Y, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }

}
