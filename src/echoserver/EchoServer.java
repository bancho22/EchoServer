package echoserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Utils;

public class EchoServer {

    private static boolean keepRunning = true;
    private static ServerSocket serverSocket;
    private static final Properties properties = Utils.initProperties("server.properties");
    private ConcurrentHashMap<UUID, ClientHandler> clients;

    public static void stopServer() {
        keepRunning = false;
    }

    private void runServer() {
        int port = Integer.parseInt(properties.getProperty("port"));
        String ip = properties.getProperty("serverIp");
        clients = new ConcurrentHashMap<UUID, ClientHandler>();

        Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Sever started. Listening on: " + port + ", bound to: " + ip);
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            do {
                Socket socket = serverSocket.accept(); //Important Blocking call
                Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Connected to a client");
                UUID id = UUID.randomUUID();
                ClientHandler client = new ClientHandler(socket, id, this);
                clients.put(id, client);
                Logger.getLogger(EchoServer.class.getName()).log(Level.INFO, "Connected clients: " + clients.size());
                new Thread(client).start();
            } while (keepRunning);
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMsgToAll(String msg){
        for (ClientHandler ch : clients.values()) {
            PrintWriter writer = ch.getWriter();
            writer.println(msg);
        }
    }
    
    public void removeHandler(UUID removeMe){
        clients.remove(removeMe);
    }

    public static void main(String[] args) {
        String logFile = properties.getProperty("logFile");
        Utils.setLogFile(logFile, EchoServer.class.getName());
        try {
            new EchoServer().runServer();
        } finally {
            Utils.closeLogger(EchoServer.class.getName());
        }
    }
}
