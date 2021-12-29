package ru.nsu.flow9onlineide;
import ru.nsu.flow9onlineide.server.Server;


public class Main {
    public  static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: address, context root, port");
            System.out.println("Example: localhost /flow 8080");
            return;
        }
        String address = args[0];
        String contextRoot = args[1];
        int port = Integer.parseInt(args[2]);
        if (port <= 0 || port >= 65535) {
            System.out.println("Invalid port");
            return;
        }
        Server server = new Server(address, contextRoot, port);
        server.start();
    }

}
