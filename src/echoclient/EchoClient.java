package echoclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class EchoClient extends Observable implements Runnable {

    Socket socket;
    private int port;
    private InetAddress serverAddress;
    private Scanner input;
    private PrintWriter output;

    public void connect(String address, int port) throws UnknownHostException, IOException {
        this.port = port;
        serverAddress = InetAddress.getByName(address);
        socket = new Socket(serverAddress, port);
        input = new Scanner(socket.getInputStream());
        output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
    }

    public void send(String msg) {
        output.println(msg);
    }

    public void stop() throws IOException {
        output.println(ProtocolStrings.STOP);
    }

    public String receive() {
        String msg;
        try {
            msg = input.nextLine();
        } catch (NoSuchElementException e) {
            msg = "Disconnected from the server";
        }
        if (msg.equals(ProtocolStrings.STOP)) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return msg;
    }

    public static void main(String[] args) {
        int port = 9090;
        String ip = "localhost";
        if (args.length == 2) {
            port = Integer.parseInt(args[0]);
            ip = args[1];
        }
        try {
            int CONNECTIONS = 1;
            EchoClient[] clients = new EchoClient[CONNECTIONS];
            for (int i = 0; i < CONNECTIONS; i++) {
                clients[i] = new EchoClient();
                clients[i].connect(ip, port);
                clients[i].send("ole");
            }
            Thread.sleep(5000);
//            EchoClient tester = new EchoClient();
//            tester.connect(ip, port);
//            System.out.println("Sending 'Hello world'");
//            tester.send("Hello World");
//            System.out.println("Waiting for a reply");
//            System.out.println("Received: " + tester.receive()); //Important Blocking call         
//            tester.stop();
            //System.in.read();      
        } catch (UnknownHostException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    @Override
    public void run() {
        while (true) {
            String msg = receive();
            setChanged();
            notifyObservers(msg);
        }
    }
}
