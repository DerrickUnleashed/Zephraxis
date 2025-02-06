package Zephyr.game.network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 12345;
    private static final int MAX_PLAYERS = 2;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_PLAYERS);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for players...");
            Socket[] players = new Socket[MAX_PLAYERS];
            int playerCount = 0;

            while (playerCount < MAX_PLAYERS) {
                Socket playerSocket = serverSocket.accept();
                players[playerCount] = playerSocket;
                System.out.println("Player " + (playerCount + 1) + " connected.");
                playerCount++;
            }

            System.out.println("Both players connected. Starting game...");

            // Launch a separate thread to handle communication for each player
            for (Socket playerSocket : players) {
                threadPool.execute(new PlayerHandler(playerSocket, players));
            }

            threadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class PlayerHandler implements Runnable {
    private Socket playerSocket;
    private Socket[] allPlayers;

    public PlayerHandler(Socket playerSocket, Socket[] allPlayers) {
        this.playerSocket = playerSocket;
        this.allPlayers = allPlayers;
    }

    @Override
    public void run() {
        try (InputStream input = playerSocket.getInputStream();
             OutputStream output = playerSocket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             PrintWriter writer = new PrintWriter(output, true)) {

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);

                // Broadcast the message to all other players
                for (Socket socket : allPlayers) {
                    if (socket != playerSocket) {
                        try {
                            PrintWriter otherPlayerWriter = new PrintWriter(socket.getOutputStream(), true);
                            otherPlayerWriter.println(message);
                        } catch (IOException e) {
                            System.err.println("Error sending message to a player: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling player: " + e.getMessage());
        } finally {
            try {
                playerSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing player socket: " + e.getMessage());
            }
        }
    }
}
