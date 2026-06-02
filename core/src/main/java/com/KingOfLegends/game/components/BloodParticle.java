package com.KingOfLegends.game.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class BloodParticle {

    private static class Particle {
        float x, y;
        float vx, vy;
        float life;
        float maxLife;
        float size;
        float rotation;
        float rotationSpeed;
    }

    private final ArrayList<Particle> particles = new ArrayList<>();
    private final Texture texture;
    private final Random random = new Random();

    public BloodParticle() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(8, 8, 7);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void spawn(float x, float y, int count) {
        for (int i = 0; i < count; i++) {
            Particle particle = new Particle();
            particle.x = x;
            particle.y = y;

            float angle = random.nextFloat() * 360f;
            float speed = 80f + random.nextFloat() * 200f;
            particle.vx = (float)(Math.cos(Math.toRadians(angle))) * speed;
            particle.vy = (float)(Math.sin(Math.toRadians(angle))) * speed;

            particle.maxLife = 0.3f + random.nextFloat() * 0.3f;
            particle.life = particle.maxLife;
            particle.size = 4f + random.nextFloat() * 8f;
            particle.rotation = random.nextFloat() * 360f;
            particle.rotationSpeed = (random.nextFloat() - 0.5f) * 400f;
            particles.add(particle);
        }
    }

    public void update(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = it.next();
            particle.life -= delta;
            if (particle.life <= 0) {
                it.remove();
                continue;
            }
            particle.x += particle.vx * delta;
            particle.y += particle.vy * delta;
            particle.vy -= 300f * delta;
            particle.rotation += particle.rotationSpeed * delta;
        }
    }

    public void draw(SpriteBatch batch) {
        for (Particle particle : particles) {
            float alpha = particle.life / particle.maxLife;
            batch.setColor(0.8f, 0f, 0f, alpha);
            batch.draw(texture,
                particle.x - particle.size / 2f,
                particle.y - particle.size / 2f,
                particle.size / 2f, particle.size / 2f,
                particle.size, particle.size,
                1f, 1f,
                particle.rotation,
                0, 0,
                texture.getWidth(), texture.getHeight(),
                false, false);
        }
        batch.setColor(Color.WHITE);
    }

    public void dispose() {
        texture.dispose();
    }
}
