/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

/**
 *
 * @author Bancho
 */
public class ClientHandler extends Thread {
    
    private Socket socket;
    private UUID id;
    private EchoServer server;
    private PrintWriter writer;
    private final String DISCONNECTED = "disconnected";
    
    public ClientHandler(Socket socket, UUID id, EchoServer server){
        this.socket = socket;
        this.id = id;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            handleClient(socket);
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    private void handleClient(Socket socket) throws IOException {
        Scanner input = new Scanner(socket.getInputStream());
        writer = new PrintWriter(socket.getOutputStream(), true);
        String message;
        try {
                message = input.nextLine(); //IMPORTANT blocking call
            } catch (NoSuchElementException e) {
                message = DISCONNECTED;
            }
        while (!message.equals(ProtocolStrings.STOP) && !message.equals(DISCONNECTED)) {
            //writer.println(message.toUpperCase());
            server.sendMsgToAll(message.toUpperCase());
            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, String.format("Received the message: %1$S ", message.toUpperCase()));
            try {
                message = input.nextLine(); //IMPORTANT blocking call
            } catch (NoSuchElementException e) {
                message = DISCONNECTED;
            }
        }
        if (!message.equals(DISCONNECTED)) {
            writer.println(ProtocolStrings.STOP);//Echo the stop message back to the client for a nice closedown
        }
        socket.close();
        server.removeHandler(id);
        if (message.equals(DISCONNECTED)) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Closed a Connection Abruptly");
        }else{
            Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Closed a Connection");
        }
    }
    
    
    public PrintWriter getWriter(){
        return writer;
    }
    
}
