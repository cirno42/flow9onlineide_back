package ru.nsu.flow9onlineide.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.SneakyThrows;
import ru.nsu.flow9onlineide.programexecutor.ProgramExecutorsPool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ServerHttpHandler implements HttpHandler {
    private int respCode = 200;
    private byte[] response;
    @Override
    public void handle(HttpExchange exchange)  {
        if ("POST".equals(exchange.getRequestMethod())) {
            handlePostRequest(exchange);
        }
        else if ("GET".equals(exchange.getRequestMethod())){
            handleGetRequest(exchange);
        } else {
            respCode = 501;
        }

        sendResponse(exchange);
    }

    private void handlePostRequest(HttpExchange exchange) {

        try {
            ProgramExecutorsPool executorsPool = ProgramExecutorsPool.getProgramExecutorsPool();
            UUID clientId = UUID.randomUUID();
            InputStream in = exchange.getRequestBody();
            String body = new String(in.readAllBytes());

            if (body .length() == 0) {
                respCode = 400;
                return;
            }
            executorsPool.processNewClient(clientId, body);

            respCode = 200;
            response = clientId.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("content-type", "text/plain");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private void handleGetRequest(HttpExchange exchange) {
        String clientIdString = exchange.getRequestHeaders().get("clientID").get(0);
        UUID clientId = UUID.fromString(clientIdString);
        ProgramExecutorsPool executorsPool = ProgramExecutorsPool.getProgramExecutorsPool();
        switch (executorsPool.checkClientState(clientId)) {
            case NOT_FINISHED -> {
                response = ("NOT_FINISHED").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("content-type", "text/plain");
                respCode = 202;
            }
            case SUCCESSFUL -> {
                Path compiledFile = executorsPool.getCompiledFile(clientId);
                exchange.getResponseHeaders().add("content-type", "application/java-archive");
                var is = Files.newInputStream(compiledFile);
                response = is.readAllBytes(); //what if file too large?
                is.close();
                respCode = 200;
                executorsPool.finishProcessingClient(clientId);
            }
            case ERROR -> {
                respCode = 400;
                exchange.getResponseHeaders().add("content-type", "text/plain");
                response = ("ERROR").getBytes(StandardCharsets.UTF_8);
                executorsPool.finishProcessingClient(clientId);
            }
        }

    }

    private void sendResponse(HttpExchange exchange) {
        int responseLength;
        if (response != null) {
            responseLength = response.length;
        } else {
            responseLength = 0;
        }
        try {
            exchange.sendResponseHeaders(respCode, responseLength);
            exchange.getResponseBody().write(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        exchange.close();
    }


}
