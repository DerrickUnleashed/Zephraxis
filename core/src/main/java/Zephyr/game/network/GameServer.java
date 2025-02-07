package Zephyr.game.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 6000;
    private static final int MAX_PLAYERS = 2;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);
    private static final ConcurrentHashMap<Integer, PlayerConnection> players = new ConcurrentHashMap<>();
    private static int nextPlayerId = 1;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                try {
                    Socket playerSocket = serverSocket.accept();
                    if (players.size() >= MAX_PLAYERS) {
                        System.out.println("Server full, rejecting connection");
                        playerSocket.close();
                        continue;
                    }

                    int playerId = nextPlayerId++;
                    PlayerConnection playerConn = new PlayerConnection(playerId, playerSocket, players);
                    players.put(playerId, playerConn);
                    threadPool.execute(playerConn);

                    System.out.println("Player " + playerId + " connected. Total players: " + players.size());
                    broadcastToAll("CONNECT " + playerId);

                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            System.exit(1);
        }
    }

    private static void broadcastToAll(String message) {
        for (PlayerConnection player : players.values()) {
            player.sendMessage(message);
        }
    }
}

class PlayerConnection implements Runnable {
    private final int playerId;
    private final Socket socket;
    private final ConcurrentHashMap<Integer, PlayerConnection> allPlayers;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean isRunning = true;

    public PlayerConnection(int playerId, Socket socket, ConcurrentHashMap<Integer, PlayerConnection> allPlayers) {
        this.playerId = playerId;
        this.socket = socket;
        this.allPlayers = allPlayers;
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send initial player ID
            sendMessage("INIT " + playerId);

            // Main message loop
            String message;
            while (isRunning && (message = reader.readLine()) != null) {
                String[] parts = message.split(" ", 2);
                switch (parts[0]) {
                    case "POS":
                        broadcastToOthers("POS " + parts[1]);
                        break;
                    case "PROJ":
                        broadcastToOthers("PROJ " + parts[1]);
                        break;
                    default:
                        System.out.println("Unknown message type: " + parts[0]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling player " + playerId + ": " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }

    private void broadcastToOthers(String message) {
        for (PlayerConnection player : allPlayers.values()) {
            if (player.playerId != this.playerId) {
                player.sendMessage(message);
            }
        }
    }

    private void disconnect() {
        try {
            isRunning = false;
            allPlayers.remove(playerId);
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();

            // Notify other players
            for (PlayerConnection player : allPlayers.values()) {
                player.sendMessage("DISCONNECT " + playerId);
            }

            System.out.println("Player " + playerId + " disconnected. Remaining players: " + allPlayers.size());
        } catch (IOException e) {
            System.err.println("Error disconnecting player " + playerId + ": " + e.getMessage());
        }
    }
}
