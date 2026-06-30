# Chat Application

A simple Java socket-based chat application with GUI clients and a server for real-time messaging.

## Features

- Java client/server architecture
- GUI-based chat interface
- Supports multiple clients connecting to a central server
- Real-time message exchange over sockets
- Useful for learning Java networking and Swing/AWT GUI development

## Project Structure

- `Server.java` — chat server that accepts client connections and broadcasts messages
- `ClientGUI.java` — GUI client for sending and receiving messages
- `ClientHandler.java` — handles communication with each connected client
- `MessageBubble.java` — chat bubble UI component for messages
- `User.java` — user data model

## Requirements

- Java JDK 8 or later

## How to Run

1. Compile the project:
   ```bash
   javac *.java

2. Start the server:
   `java Server`

3. Start one or more clients:
   `java ClientGUI`

4. Enter a username and connect to the server to begin chatting.

## Usage

- Type a message in the client window
- Press Enter or click Send
- Messages are broadcast to all connected clients

## Notes

- Make sure the server is running before starting clients
- Default port is defined in Server.java and ClientGUI.java

## Author 

  **Sumaira**
