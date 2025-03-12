package Zephyr.game.player;

import Zephyr.game.network.GameClient;
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
    private int playerID;
    private GameClient client;
    public Player(float x, float y, float speed, int screenWidth, int screenHeight, String side, GameClient client) {
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
        this.client = client;
        this.playerID = client.getPlayerId();
    }

    public int getID() {
        return playerID;
    }

    // Fix the shoot method to ensure correct direction
    public void shoot() {
        Vector2 playerCenter = new Vector2(x + texture.getWidth() / 2f, y + texture.getHeight() / 2f);

        // Set correct direction based on player side
        Vector2 direction = side.equals("up") ? new Vector2(0, -1) : new Vector2(0, 1);

        System.out.println("Shooting projectile from " + side + " with direction " + direction);

        // Spawn projectile locally
        Projectile projectile = spawnProjectile(playerCenter.x, playerCenter.y, direction.x, direction.y, true);

        // If client is set, notify network about projectile
        if (client != null) {
            client.sendProjectile(projectile);
        }
    }

    public Projectile spawnProjectile(float startX, float startY, float directionX, float directionY, boolean isLocallyManaged) {
        if (client == null) {
            System.err.println("Warning: Client is null in spawnProjectile!");
        }

        Vector2 direction = new Vector2(directionX, directionY).nor();
        int sourceId = (client != null) ? client.getPlayerId() : -1; // Handle null client
        Projectile proj = new Projectile(startX, startY, 10, direction, 500, sourceId);
        projectiles.add(proj);
        return proj;
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

    public void update(Player opponent, float deltaTime) {
        if (isDead) return;

        // Handle player movement based on input (only for local player)
        if (client != null && client.getPlayerId() == this.playerID) {
            if (Gdx.input.isKeyPressed(Input.Keys.A) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.LEFT) && side.equals("up")) {
                x -= speed * deltaTime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.RIGHT) && side.equals("up")) {
                x += speed * deltaTime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.UP) && side.equals("up")) {
                y += speed * deltaTime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) && side.equals("down") || Gdx.input.isKeyPressed(Input.Keys.DOWN) && side.equals("up")) {
                y -= speed * deltaTime;
            }

            // Clamp player position within screen bounds
            if (x < 0) x = 0;
            if (x > screenWidth - texture.getWidth()) x = screenWidth - texture.getWidth();
            if (y < 0) y = 0;
            if (y > screenHeight - texture.getHeight()) y = screenHeight - texture.getHeight();

            hitbox.setPosition(x, y);

            // Handle shooting (only for local player)
            if ((Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ENTER) && side.equals("up") ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && side.equals("down"))
                && projectileCooldown <= 0) {
                shoot();
                projectileCooldown = 0.3f;
            }

            projectileCooldown -= deltaTime;
        }

        // Update all projectiles and check for collisions
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);

            // Update projectile position with deltaTime
            projectile.update(deltaTime);

            // Check for hits against opponent
            if (opponent != null && !opponent.isDead() && projectile.hit(opponent)) {
                projectiles.removeIndex(i);

                // If the opponent's health is now 0, notify server about death
                if (opponent.getHealth() <= 0 && client != null) {
                    client.sendPlayerDeath("opp");
                    System.out.println("Opponent died from our projectile, sent death notification");
                }
            }
            // Remove if off-screen
            else if (projectile.isOffScreen(screenWidth, screenHeight)) {
                projectiles.removeIndex(i);
            }
        }
    }

    // Add setter for client
    public void setClient(GameClient client) {
        this.client = client;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            health = 0;
            isDead = true;

            // If we're damaged to death and have a client, notify server
            if (client != null) {
                client.sendPlayerDeath("self");
                System.out.println("Local player died from damage, sent death notification");
            }
        }
    }

    // Make isDead public (it looks like it already is, but just to confirm)
    public boolean isDead() {
        return isDead;
    }

    // Add a method to explicitly set death state (for network sync)
    public void setDead(boolean dead) {
        this.isDead = dead;
    }

    public void reset() {
        isDead = false;
        health = 100;
    }
    public void render(SpriteBatch batch) {
        if (!isDead) {
            batch.draw(texture, x, y);

            // Render all projectiles
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

    public int getHealth() {
        return this.health;
    }
}
