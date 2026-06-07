package com.KingOfLegends.game.managers;

import com.KingOfLegends.game.components.SkillMessage;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.Iterator;

public class SkillMessageManager {

    private final ArrayList<SkillMessage> messages = new ArrayList<>();
    private final BitmapFont font;

    public SkillMessageManager(BitmapFont font) {
        this.font = font;
    }

    public void show(String text, float x, float y, Color color) {
        messages.add(new SkillMessage(text, x, y, color, 1.5f));
    }

    public void update(float delta) {
        Iterator<SkillMessage> it = messages.iterator();
        while (it.hasNext()) {
            SkillMessage msg = it.next();
            msg.update(delta);
            if (msg.lifeTime <= 0) it.remove();
        }
    }

    public void draw(SpriteBatch batch) {
        for (SkillMessage msg : messages) {
            msg.draw(batch, font);
        }
    }
}
