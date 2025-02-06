package Zephyr.game.player;

import Zephyr.game.projectiles.Projectile;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player {
    private Texture texture;
    private float x, y;
    private float speed;
    private int screenWidth, screenHeight;

    private int health, defense, strength;

    private Array<Projectile> projectiles;
    private float projectileCooldown;
    private Rectangle hitbox;

    public Player(float x, float y, float speed, int screenWidth, int screenHeight) {
        this.texture = new Texture("Player.png");
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.hitbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
        this.health = 100;
        this.defense = 10;
        this.strength = 15;

        this.projectiles = new Array<>();
        this.projectileCooldown = 0;
    }
    public float getX() {
        return x;
    }

    // Getter for y-coordinate
    public float getY() {
        return y;
    }

    // Setter for position
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;

        // Update hitbox position
        hitbox.setPosition(x, y);
    }
    public void update() {
        // Movement controls
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            x -= speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            x += speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            y += speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            y -= speed * Gdx.graphics.getDeltaTime();
        }

        // Enforce screen boundaries
        if (x < 0) x = 0;
        if (x > screenWidth - texture.getWidth()) x = screenWidth - texture.getWidth();
        if (y < 0) y = 0;
        if (y > screenHeight / 2 - texture.getHeight()) y = screenHeight / 2 - texture.getHeight(); // Restrict to half the screen height

        hitbox.setPosition(x, y);

        // Shooting projectiles
        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            && projectileCooldown <= 0) {
            shoot();
            projectileCooldown = 0.25f; // 0.5 seconds cooldown
        }

        projectileCooldown -= Gdx.graphics.getDeltaTime();

        // Update projectiles
        for (Projectile projectile : projectiles) {
            projectile.update();
        }

        // Remove off-screen projectiles
        for (int i = projectiles.size - 1; i >= 0; i--) {
            if (projectiles.get(i).isOffScreen(screenWidth, screenHeight)) {
                projectiles.removeIndex(i);
            }
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
        for (Projectile projectile : projectiles) {
            projectile.render(batch);
        }
    }

    private void shoot() {
        // Get the mouse position in world coordinates
        Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        Vector2 playerCenter = new Vector2(x + texture.getWidth() / 2f, y + texture.getHeight() / 2f);

        // Calculate direction vector
        Vector2 direction = mousePosition.sub(playerCenter).nor();

        // Add new projectile
        projectiles.add(new Projectile(playerCenter.x, playerCenter.y, direction, 300));
    }

    public void takeDamage(int amount) {
        health -= Math.max(0, amount - defense);
        if (health <= 0) {
            // Handle player death
        }
    }

    public void heal(int amount) {
        health += amount;
        if (health > 100) health = 100; // Cap health at max
    }

    public void dispose() {
        texture.dispose();
        for (Projectile projectile : projectiles) {
            projectile.dispose();
        }
    }
}
