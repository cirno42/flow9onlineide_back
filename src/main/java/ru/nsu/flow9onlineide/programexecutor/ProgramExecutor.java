package ru.nsu.flow9onlineide.programexecutor;


import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ProgramExecutor implements Callable<Path> {
    private final String programCode;
    private final String programName;
    private  final static String WORK_DIR_PATH = "workdir/";
    private final static int COMPILATION_TIMEOUT_IN_MILLIS = 10000;
    private final String pathToCompiler;
    private final String pathToBin;
    private static final String PATH_TO_PROPERTIES = "paths.properties";

    @SneakyThrows
    public ProgramExecutor(String programName, String programCode) {
        this.programName = programName;
        this.programCode = programCode;
        Properties properties = new Properties();
        properties.load(Files.newInputStream(Path.of(PATH_TO_PROPERTIES)));
        pathToBin = properties.getProperty("PATH_TO_BIN");
        pathToCompiler = properties.getProperty("PATH_TO_COMPILER");
    }

    @Override
    public Path call() throws Exception {
        var sourceFile = writeSourceCodeToFile();
        var compiledFile = compileFileToJar(sourceFile);
        return compiledFile;
    }


    private Path compileFileToJar(Path sourceCodePath) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", "-jar", pathToCompiler, "bin-dir="+ pathToBin, "jar=1",
                sourceCodePath.toAbsolutePath().toString());
        //builder.inheritIO();
        try {
            System.err.println("Compiling " + sourceCodePath.toString());
            var compilerProcess = builder.start();
            var is = compilerProcess.getInputStream();
            compilerProcess.waitFor(COMPILATION_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
            System.err.println(new String(is.readAllBytes()));
            is.close();
            if (compilerProcess.isAlive()) {
                compilerProcess.destroy();
                return null;
            }
            var programPath = Path.of(sourceCodePath.toString().split("[.]")[0] + ".jar");
            if (Files.notExists(programPath)) {
                return null;
            }
            return programPath;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return null;
    }

    @SneakyThrows
    private Path writeSourceCodeToFile() {
        Path programFile = Files.createFile(Path.of(WORK_DIR_PATH + programName + ".flow"));
        var os = Files.newOutputStream(programFile);
        os.write(programCode.getBytes(StandardCharsets.UTF_8));
        os.close();
        return programFile;
    }
}
