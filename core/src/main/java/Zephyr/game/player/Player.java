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
    private String side;
    public Player(float x, float y, float speed, int screenWidth, int screenHeight , String side) {
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
        this.side = side;
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
        if (Gdx.input.isKeyPressed(Input.Keys.A) && side.equals("down")|| Gdx.input.isKeyPressed(Input.Keys.LEFT) && side.equals("up")) {
            x -= speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.RIGHT)&& side.equals("up") ) {
            x += speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) && side.equals("down")|| Gdx.input.isKeyPressed(Input.Keys.UP) &&side.equals("up")) {
            y += speed * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && side.equals("down")|| Gdx.input.isKeyPressed(Input.Keys.DOWN) &&side.equals("up")) {
            y -= speed * Gdx.graphics.getDeltaTime();
        }

        // Enforce screen boundaries
        if (x < 0) x = 0;
        if (x > screenWidth - texture.getWidth()) x = screenWidth - texture.getWidth();
        if (y < 0) y = 0;
        if (y > screenHeight - texture.getHeight()) y = screenHeight - texture.getHeight();

        hitbox.setPosition(x, y);

        // Shooting projectiles
        if ((Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && side.equals("up") || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && side.equals("down"))
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
        Vector2 playerCenter = new Vector2(x + texture.getWidth() / 2f, y + texture.getHeight() / 2f);
        Vector2 direction;

        if (side.equals("up")) {
            direction = new Vector2(0, -1); // Shoots downward
        } else {
            direction = new Vector2(0, 1); // Shoots upward
        }

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
