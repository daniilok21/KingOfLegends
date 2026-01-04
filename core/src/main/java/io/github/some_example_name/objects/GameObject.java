package io.github.some_example_name.objects;

import static io.github.some_example_name.GameSettings.SCALE;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.some_example_name.GameSettings;

public class GameObject {

    public short cBits;
    public int width;
    public int height;
    public Body body;
    protected Texture texture;
    protected Rectangle bounds;
    protected BodyDef.BodyType bodyType;

    public GameObject(String texturePath, int x, int y, int width, int height, short cBits, World world) {
        this(texturePath, x, y, width, height, cBits, world, false, BodyDef.BodyType.DynamicBody);
    }

    protected GameObject(String texturePath, int x, int y, int width, int height,
                         short cBits, World world, boolean rectangular, BodyDef.BodyType bodyType) {
        this.width = width;
        this.height = height;
        this.cBits = cBits;
        this.bodyType = bodyType;

        this.bounds = new Rectangle(x - width/2, y - height/2, width, height);

        texture = new Texture(texturePath);

        if (rectangular) {
            body = createRectangleBody(x, y, world, bodyType);
        }
        else {
            body = createCircleBody(x, y, world, bodyType);
        }
    }

    private Body createCircleBody(float x, float y, World world, BodyDef.BodyType type) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.fixedRotation = true;
        Body body = world.createBody(def);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(Math.max(width, height) * SCALE / 2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = (type == BodyDef.BodyType.DynamicBody) ? 0.1f : 0f;
        fixtureDef.friction = 1f;
        fixtureDef.filter.categoryBits = cBits;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        circleShape.dispose();

        body.setTransform(x * SCALE, y * SCALE, 0);
        return body;
    }

    protected Body createRectangleBody(float x, float y, World world, BodyDef.BodyType type) {
        BodyDef def = new BodyDef();
        def.type = type;
        def.fixedRotation = true;
        Body body = world.createBody(def);

        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox((width/2f) * SCALE, (height/2f) * SCALE);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.density = (type == BodyDef.BodyType.DynamicBody) ? 0.1f : 0f;
        fixtureDef.friction = 1f;
        fixtureDef.filter.categoryBits = cBits;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        polygonShape.dispose();

        body.setTransform(x * SCALE, y * SCALE, 0);
        return body;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture,
            getX(),
            getY(),
            width,
            height);
    }

    public void hit() {
    }

    public int getX() {
        return (int)(body.getPosition().x / GameSettings.SCALE - width / 2);
    }

    public int getY() {
        return (int)(body.getPosition().y / GameSettings.SCALE - height / 2);
    }

    public void setX(int x) {
        body.setTransform(x * SCALE, body.getPosition().y, 0);
        updateBounds();
    }

    public void setY(int y) {
        body.setTransform(body.getPosition().x, y * SCALE, 0);
        updateBounds();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Rectangle getBounds() {
        updateBounds();
        return bounds;
    }

    protected void updateBounds() {
        bounds.setPosition(getX(), getY());
        bounds.setSize(width, height);
    }


    public boolean contains(float x, float y) {
        return getBounds().contains(x, y);
    }

    public boolean overlaps(GameObject other) {
        return getBounds().overlaps(other.getBounds());
    }

    public boolean overlaps(Rectangle rectangle) {
        return getBounds().overlaps(rectangle);
    }

    public Body getBody() {
        return body;
    }

    public BodyDef.BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyDef.BodyType newType) {
        this.bodyType = newType;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
