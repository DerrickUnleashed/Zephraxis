package Zephyr.game.projectiles;

import Zephyr.game.player.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Texture texture;
    private float x, y;
    private Vector2 velocity;
    private int damage;
    private float rotation;
    private Rectangle projbox;
    private String sourceSide; // Tracks which player shot it

    public Projectile(float x, float y, int damage, Vector2 direction, float speed, String sourceSide) {
        this.texture = new Texture("Kunai.png");
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.rotation = direction.angleDeg() - 90;
        this.velocity = direction.scl(speed);
        this.projbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
        this.sourceSide = sourceSide;
    }

    public boolean hit(Player player) {
        if (player != null && !player.getSide().equals(sourceSide) && projbox.overlaps(player.getHitbox())) {
            player.takeDamage(damage);
            return true; // Indicates projectile should be removed
        }
        return false;
    }

    public void update() {
        x += velocity.x * Gdx.graphics.getDeltaTime();
        y += velocity.y * Gdx.graphics.getDeltaTime();
        projbox.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        // Create a TextureRegion from the texture
        TextureRegion region = new TextureRegion(texture);

        // Draw the region with rotation
        batch.draw(region, x, y, region.getRegionWidth() / 2f, region.getRegionHeight() / 2f,
            region.getRegionWidth(), region.getRegionHeight(), 1, 1, rotation);
    }


    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < 0 || x > screenWidth || y < 0 || y > screenHeight;
    }

    public void dispose() {
        texture.dispose();
    }
}
