package Zephyr.game.network;

import Zephyr.game.GameScreens.PVPScreen;
import Zephyr.game.Main;
import Zephyr.game.projectiles.Projectile;
import com.badlogic.gdx.ApplicationAdapter;
import java.io.*;
import java.net.Socket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

public class GameClient extends ApplicationAdapter {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;
    private boolean isRunning;
    private String serverIp;
    private int serverPort;
    private int playerId = -1;
    private GameStateCallback callback;
    private Json json;
    private Main game;

    public void sendProjectile(Projectile projectile) {
        if (writer != null && playerId != -1) {
            String projMessage = json.toJson(new ProjectileData(projectile.getX(), projectile.getY(),
                projectile.getVelocity().x, projectile.getVelocity().y));
            writer.println("PROJ " + playerId + " " + projMessage);
        }
    }

    public int getPlayerId() {
        return playerId;
    }


    public interface GameStateCallback {
        void onPlayerUpdate(int playerId, float x, float y);
        void onProjectileSpawn(int playerId, float x, float y, float directionX, float directionY);
        void onPlayerConnect(int id);
        void onPlayerDisconnect(int id);
        void onGameStart(int opponentId);
        void onPlayerDeath(int playerId);
    }

    // Add a method to send player death
    // Update GameClient.java to fix the sendPlayerDeath method
    public void sendPlayerDeath(String who) {
        if (writer != null && playerId != -1) {
            if (who.equals("self")) {
                writer.println("DEATH " + playerId);
                System.out.println("Sent DEATH signal for player " + playerId);
            } else if (who.equals("opp")) {
                // Find opponent player ID
                if (callback instanceof PVPScreen) {
                    PVPScreen screen = (PVPScreen) callback;
                    for (Integer remoteId : screen.getRemotePlayerIds()) {
                        writer.println("DEATH " + remoteId);
                        System.out.println("Sent DEATH signal for opponent " + remoteId);
                        break; // Only need first opponent in 1v1 game
                    }
                } else {
                    System.out.println("Cannot send opponent death - callback is not a PVPScreen");
                }
            }
        }
    }

    public void setGameStateCallback(GameStateCallback callback) {
        this.callback = callback;
    }

    public void setGame(Main game) {
        this.game = game;
    }

    public GameClient(String serverIp, int serverPort, GameStateCallback callback) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.callback = callback;
        this.json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
    }

    @Override
    public void create() {
        try {
            socket = new Socket(serverIp, serverPort);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isRunning = true;

            listenerThread = new Thread(this::listenToServer);
            listenerThread.start();

            String initMessage = reader.readLine();
            if (initMessage.startsWith("INIT")) {
                playerId = Integer.parseInt(initMessage.split(" ")[1]);
                System.out.println("Assigned player ID: " + playerId);
            }
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    public void sendPlayerPosition(float x, float y) {
        if (writer != null && playerId != -1) {
            String posMessage = json.toJson(new PlayerPosition(playerId, x, y));
            writer.println("POS " + posMessage);
        }
    }


    public void sendReadyToPlay() {
        if (writer != null && playerId != -1) {
            writer.println("READY " + playerId);
            System.out.println("Sent READY signal to server");
        }
    }

    private void listenToServer() {
        try {
            String message;
            while (isRunning && (message = reader.readLine()) != null) {
                final String msg = message;
                Gdx.app.postRunnable(() -> handleServerMessage(msg));
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("Connection to server lost: " + e.getMessage());
                disconnect();
            }
        }
    }
    private void disconnect() {
        try {
            isRunning = false;
            if (listenerThread != null) {
                listenerThread.join(1000);
            }
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }
    private void handleServerMessage(String message) {
        try {
            if (callback == null) return;

            String[] parts = message.split(" ", 2);
            switch (parts[0]) {
                case "POS":
                    PlayerPosition pos = json.fromJson(PlayerPosition.class, parts[1]);
                    callback.onPlayerUpdate(pos.playerId, pos.x, pos.y);
                    break;
                case "PROJ":
                    String[] projParts = parts[1].split(" ", 2);
                    int sourceId = Integer.parseInt(projParts[0]);
                    ProjectileData proj = json.fromJson(ProjectileData.class, projParts[1]);
                    callback.onProjectileSpawn(sourceId, proj.x, proj.y, proj.directionX, proj.directionY);
                    break;
                case "CONNECT":
                    callback.onPlayerConnect(Integer.parseInt(parts[1]));
                    break;
                case "START":
                    int opponentId = Integer.parseInt(parts[1]);
                    callback.onGameStart(opponentId);
                    break;
                case "DISCONNECT":
                    callback.onPlayerDisconnect(Integer.parseInt(parts[1]));
                    break;
                case "DEATH":
                    callback.onPlayerDeath(Integer.parseInt(parts[1]));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        try {
            isRunning = false;
            if (listenerThread != null) {
                listenerThread.join(1000);
            }
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }

    // Data classes for JSON serialization
    private static class PlayerPosition {
        public int playerId;
        public float x, y;

        public PlayerPosition() {}

        public PlayerPosition(int playerId, float x, float y) {
            this.playerId = playerId;
            this.x = x;
            this.y = y;
        }
    }

    private static class ProjectileData {
        public float x, y, directionX, directionY;

        public ProjectileData() {}

        public ProjectileData(float x, float y, float directionX, float directionY) {
            this.x = x;
            this.y = y;
            this.directionX = directionX;
            this.directionY = directionY;
        }
    }
}
