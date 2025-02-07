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

    // Add new spawnProjectile method
    public void spawnProjectile(float startX, float startY, float directionX, float directionY) {
        Vector2 direction = new Vector2(directionX, directionY).nor();
        projectiles.add(new Projectile(startX, startY, direction, 300));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
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
        if (y > screenHeight - texture.getHeight()) y = screenHeight - texture.getHeight();

        hitbox.setPosition(x, y);

        // Shooting projectiles
        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            && projectileCooldown <= 0) {
            shoot();
            projectileCooldown = 0.25f;
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

    private void shoot() {
        Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        Vector2 playerCenter = new Vector2(x + texture.getWidth() / 2f, y + texture.getHeight() / 2f);
        Vector2 direction = mousePosition.sub(playerCenter).nor();
        spawnProjectile(playerCenter.x, playerCenter.y, direction.x, direction.y);
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

    public void dispose() {
        texture.dispose();
        for (Projectile projectile : projectiles) {
            projectile.dispose();
        }
    }
}
