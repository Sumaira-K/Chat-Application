import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server Started on port " + PORT + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);

                clients.add(client);
                System.out.println("Client connected. Total clients: " + clients.size());

                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    //  Broadcast message to all EXCEPT sender
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    //  Broadcast to ALL (including sender)
    public static void broadcastToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    //  Send updated user list to all clients
    public static void sendUserList() {
        StringBuilder userList = new StringBuilder();

        for (ClientHandler client : clients) {
            String name = client.getUsername();

            // Skip users who haven't set username yet
            if (!name.equals("Unknown")) {
                if (userList.length() > 0) {
                    userList.append(", ");
                }
                userList.append(name);
            }
        }

        String userListMsg = "[USERS]" + userList.toString();

        // Send to all clients
        for (ClientHandler client : clients) {
            client.sendMessage(userListMsg);
        }
    }

    //  Remove client on disconnect
    public static void removeClient(ClientHandler client) {
        clients.remove(client);

        System.out.println("Client disconnected: " + client.getUsername() +
                ". Total clients: " + clients.size());

        // Update user list after disconnect
        sendUserList();
    }
}