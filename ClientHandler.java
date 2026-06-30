import java.net.*;
import java.io.*;

public class ClientHandler implements Runnable {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String username;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        this.username = null;
    }

    public void run() {
        try {
            String msg;

            while ((msg = in.readLine()) != null) {

                // 🔥 HANDLE USERNAME
                if (msg.startsWith("[USERNAME]")) {
                    username = msg.substring(10);
                    System.out.println(username + " connected");

                    // ✅ IMPORTANT: update user list for all clients
                    Server.sendUserList();
                }

                // 🔥 HANDLE TYPING
                else if (msg.startsWith("[TYPING]")) {
                    Server.broadcast(msg, this);
                }

                // 🔥 HANDLE NORMAL MESSAGE
                else if (msg.startsWith("[MSG]")) {
                    Server.broadcast(msg, this);
                }
            }

        } catch (IOException e) {
            System.out.println(getUsername() + " disconnected");
        } finally {
            closeConnection();
            Server.removeClient(this);
        }
    }

    // 🔥 Cleanly close resources
    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username != null ? username : "Unknown";
    }
}