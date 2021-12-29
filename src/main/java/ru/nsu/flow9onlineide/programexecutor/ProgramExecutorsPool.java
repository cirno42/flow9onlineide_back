package ru.nsu.flow9onlineide.programexecutor;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProgramExecutorsPool {
    private ExecutorService executorService = null;
    private final HashMap<UUID, Future<Path>> clientsPool; //contains client id and path to his program
    private static ProgramExecutorsPool executorInstance = null;

    private ProgramExecutorsPool() {
        executorService = Executors.newCachedThreadPool();
        clientsPool = new HashMap<>();
    }

    public void processNewClient(UUID clientID, String program) {

        Future<Path> futureProgramPath = executorService.submit(new ProgramExecutor(UUIDToProgramName(clientID), program));
        clientsPool.put(clientID, futureProgramPath);
    }


    @SneakyThrows
    public CompilationResult checkClientState(UUID clientID) {
        if (!clientsPool.containsKey(clientID)) {
            return CompilationResult.ERROR;
        }

        var futurePath = clientsPool.get(clientID);

        if (futurePath.isDone()) {
            var path = futurePath.get();
            if (path == null) {
                return CompilationResult.ERROR;
            }
            return CompilationResult.SUCCESSFUL;
        } else {
            return CompilationResult.NOT_FINISHED;
        }
    }

    @SneakyThrows
    public Path getCompiledFile(UUID clientId) {
        //clientsPool.remove(clientId);
        return clientsPool.get(clientId).get();
    }


    public void finishProcessingClient(UUID clientID) {
        Path source = Path.of("workdir/" + UUIDToProgramName(clientID) + ".flow");
        Path program = Path.of("workdir/" + UUIDToProgramName(clientID) + ".jar");

        try {
            if (Files.exists(source)) {
                Files.delete(source);
            }
            if (Files.exists(program)) {
                Files.delete(program);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientsPool.remove(clientID);
    }

    public static ProgramExecutorsPool getProgramExecutorsPool() {
        if (executorInstance == null) {
            synchronized (ProgramExecutor.class) {
                if (executorInstance == null) {
                    executorInstance = new ProgramExecutorsPool();
                }
            }
        }
        return executorInstance;
    }

    private String UUIDToProgramName(UUID uuid) {
        return "client_" + uuid.toString().replace('-', '_');
    }
}
