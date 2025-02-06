package Zephyr.game.projectiles;
//hehe

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private Texture texture;
    private float x, y;
    private Vector2 velocity;
    private float rotation;
    public Projectile(float x, float y, Vector2 direction, float speed) {
        this.texture = new Texture("Kunai.png");
        this.x = x;
        this.y = y;
        this.rotation = direction.angleDeg() - 90; // Adjust rotation for a vertical kunai
        // Set velocity based on direction and speed
        this.velocity = direction.scl(speed);
    }

    public void update() {
        x += velocity.x * Gdx.graphics.getDeltaTime();
        y += velocity.y * Gdx.graphics.getDeltaTime();
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
