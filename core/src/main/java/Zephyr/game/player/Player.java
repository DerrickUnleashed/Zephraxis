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
    private boolean isDead;

    public Player(float x, float y, float speed, int screenWidth, int screenHeight , String side){
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
        this.isDead = false;
}

public void spawnProjectile(float startX, float startY, float directionX, float directionY) {
    Vector2 direction = new Vector2(directionX, directionY).nor();
    projectiles.add(new Projectile(startX, startY, 10, direction, 500, side));
}

public float getX() {
    return x;
}

public float getY() {
    return y;
}

public String getSide() {
    return side;
}

public void setPosition(float x, float y) {
    this.x = x;
    this.y = y;
    hitbox.setPosition(x, y);
}

public void update(Player opponent) {
    if (isDead) return;

    if (Gdx.input.isKeyPressed(Input.Keys.A) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.LEFT) && side.equals("up")) {
        x -= speed * Gdx.graphics.getDeltaTime();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.RIGHT) && side.equals("up")) {
        x += speed * Gdx.graphics.getDeltaTime();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.W) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.UP) && side.equals("up")) {
        y += speed * Gdx.graphics.getDeltaTime();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.DOWN) && side.equals("up")) {
        y -= speed * Gdx.graphics.getDeltaTime();
    }

    if (x < 0) x = 0;
    if (x > screenWidth - texture.getWidth()) x = screenWidth - texture.getWidth();
    if (y < 0) y = 0;
    if (y > screenHeight - texture.getHeight()) y = screenHeight - texture.getHeight();

    hitbox.setPosition(x, y);

    if ((Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER) && side.equals("up") || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && side.equals("down"))
        && projectileCooldown <= 0) {
        shoot();
        projectileCooldown = 0.3f;
    }

    projectileCooldown -= Gdx.graphics.getDeltaTime();

    for (int i = projectiles.size - 1; i >= 0; i--) {
        projectiles.get(i).update();

        if (opponent != null && projectiles.get(i).hit(opponent)) {
            projectiles.removeIndex(i);
        } else if (projectiles.get(i).isOffScreen(screenWidth, screenHeight)) {
            projectiles.removeIndex(i);
        }
    }
}

private void shoot() {
    Vector2 playerCenter = new Vector2(x + texture.getWidth() / 2f, y + texture.getHeight() / 2f);
    Vector2 direction = side.equals("up") ? new Vector2(0, -1) : new Vector2(0, 1);
    spawnProjectile(playerCenter.x, playerCenter.y, direction.x, direction.y);
}

public Rectangle getHitbox() {
    return hitbox;
}

public void takeDamage(int damage) {
    health -= damage;
    if (health <= 0) {
        health = 0;
        isDead = true;
    }
}

public boolean isDead() {
    return isDead;
}

public void render(SpriteBatch batch) {
    if (!isDead) {
        batch.draw(texture, x, y);
        for (Projectile projectile : projectiles) {
            projectile.render(batch);
        }
    }
}

public void dispose() {
    texture.dispose();
    for (Projectile projectile : projectiles) {
        projectile.dispose();
    }
}

}
