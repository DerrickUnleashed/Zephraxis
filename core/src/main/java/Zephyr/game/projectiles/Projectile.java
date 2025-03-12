package Zephyr.game.projectiles;

import Zephyr.game.player.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class Projectile implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient Texture texture;
    private float x, y;
    private Vector2 velocity;
    private int damage;
    private float rotation;
    private Rectangle projbox;
    private int sourceID; // Now stores player ID from GameClient
    private boolean active;

    public Projectile(float x, float y, int damage, Vector2 direction, float speed, int sourceID) {
        this.texture = new Texture("Kunai.png");
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.rotation = direction.angleDeg() - 90;
        this.velocity = new Vector2(direction).nor().scl(speed);
        this.projbox = new Rectangle(x, y, 16, 16); // Fixed projectile size
        this.sourceID = sourceID;
        this.active = true;
    }

    public void update(float deltaTime) {
        if (!active) return;
        x += velocity.x * deltaTime;
        y += velocity.y * deltaTime;
        projbox.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        if (texture == null) texture = new Texture("Kunai.png"); // Ensure texture reloads on client
        if (!active) return;
        TextureRegion region = new TextureRegion(texture);
        batch.draw(region, x, y, region.getRegionWidth() / 2f, region.getRegionHeight() / 2f,
            region.getRegionWidth(), region.getRegionHeight(), 1, 1, rotation);
    }

    public boolean hit(Player player) {
        if (player != null && projbox.overlaps(player.getHitbox())) {
            player.takeDamage(damage);
            active = false;
            return true;
        }
        return false;
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < 0 || x > screenWidth || y < 0 || y > screenHeight;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isActive() { return active; }
    public int getSourceID() { return sourceID; }
    public Vector2 getVelocity() { return velocity; }
}
