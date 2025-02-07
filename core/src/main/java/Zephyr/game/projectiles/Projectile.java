package Zephyr.game.projectiles;

import com.badlogic.gdx.Gdx;
import Zephyr.game.player.Player;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Texture texture;
    private float x, y;
    private Vector2 velocity;
    private int damage = 10;
    private float rotation;
    private Rectangle projbox;

    public Projectile(float x, float y, Vector2 direction, float speed) {
        this.texture = new Texture("Kunai.png");
        this.x = x;
        this.y = y;
        this.rotation = direction.angleDeg() - 90; // Adjust rotation for a vertical kunai
        this.velocity = direction.scl(speed);
        // Initialize the projectile hitbox
        this.projbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    public void hit(Player player) {
        if (player.getHitbox().overlaps(projbox)) {
            player.takeDamage(damage); 
            texture.dispose();
        }
    }

    public void update() {
        // Update position
        x += velocity.x * Gdx.graphics.getDeltaTime();
        y += velocity.y * Gdx.graphics.getDeltaTime();

        // Update hitbox position
        projbox.setPosition(x, y);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, texture.getWidth() / 2f, texture.getHeight() / 2f,
                texture.getWidth(), texture.getHeight(), 1, 1, rotation, 0, 0,
                texture.getWidth(), texture.getHeight(), false, false);
    }

    public boolean isOffScreen(int screenWidth, int screenHeight) {
        return x < 0 || x > screenWidth || y < 0 || y > screenHeight;
    }

    public void dispose() {
        texture.dispose();
    }
}
