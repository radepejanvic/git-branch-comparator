package org.example;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class CommandUtils {

    /**
     * Executes the given command in the specified working directory.
     *
     * @param directory The working directory where the command should be executed.
     * @param command A list of strings representing the command and its arguments.
     * @return The {@link Process} representing the running command.
     * @throws IOException If an I/O error occurs while starting the process.
     */
    public Process executeCommand(File directory, List<String> command)  throws IOException{
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(directory);

        return processBuilder.start();
    }

    /**
     * Reads the output from an InputStream and returns it as a String.
     * @param inputStream The InputStream to read.
     * @return The output as a String.
     * @throws IOException IF an I/O error occurs.
     */
    public String readOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

}
