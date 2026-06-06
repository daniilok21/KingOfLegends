package com.KingOfLegends.game.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SkillMessage {
    public String text;
    public float x, y;
    public float lifeTime;
    public float maxLifeTime;
    public Color color;

    public SkillMessage(String text, float x, float y, Color color, float duration) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = new Color(color);
        this.maxLifeTime = duration;
        this.lifeTime = duration;
    }

    public void update(float delta) {
        lifeTime -= delta;
        y += 40f * delta; // Скорость плытия текста вверх
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        if (lifeTime <= 0) return;
        // Плавное исчезновение текста (альфа-канал)
        float alpha = lifeTime / maxLifeTime;
        font.setColor(color.r, color.g, color.b, alpha);
        font.draw(batch, text, x, y);
        font.setColor(Color.WHITE); // Возвращаем дефолтный цвет
    }
}
