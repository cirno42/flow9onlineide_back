package ru.nsu.flow9onlineide.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private HttpServer httpServer;

    public Server(String address, String contextRoot, int port) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(address, port), 0);
            httpServer.createContext(contextRoot, new ServerHttpHandler());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        httpServer.start();
    }
}
