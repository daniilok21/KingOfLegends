package io.github.some_example_name.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static io.github.some_example_name.GameSettings.SCALE;

public class PlatformObject {

    private Body body;
    private Texture texture;
    private float width, height;
    private Rectangle bounds;

    public PlatformObject(float x, float y, float width, float height, String texturePath, World world) {
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
        texture = new Texture(texturePath);
        createBody(x + width/2, y + height/2, world);
    }

    private void createBody(float x, float y, World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x * SCALE, y * SCALE);

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox((width/2) * SCALE, (height/2) * SCALE);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef);
        shape.dispose();
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, body.getPosition().x / SCALE - width/2, body.getPosition().y / SCALE - height/2, width, height);
    }

    public Body getBody() {
        return body;
    }

    public float getX() {
        return body.getPosition().x / SCALE - width/2;
    }

    public float getY() {
        return body.getPosition().y / SCALE - height/2;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getBounds() {
        bounds.setPosition(getX(), getY());
        return bounds;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
