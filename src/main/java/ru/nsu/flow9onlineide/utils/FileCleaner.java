package ru.nsu.flow9onlineide.utils;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

public class FileCleaner implements Runnable{
    private final static String WORKDIR = "workdir/";
    private final static int TIME_BEFORE_DELETE_IN_MILLIS = 30*1000;
    private final static int SLEEPING_TIME_IN_MILLIS = 10*1000;
    public FileCleaner() {

    }
    @SneakyThrows
    @Override
    public void run() {
        while(true) {
            Instant now = Instant.now();
            var filesInWorkdir = Files.list(Path.of(WORKDIR));
            filesInWorkdir.forEach(path -> {
                try {
                    FileTime creationTime = (FileTime) Files.getAttribute(path, "creationTime");
                    Duration duration = Duration.between(creationTime.toInstant(), now);
                    if (duration.abs().toMillis() >= TIME_BEFORE_DELETE_IN_MILLIS) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Thread.sleep(SLEEPING_TIME_IN_MILLIS);
        }
    }
}
