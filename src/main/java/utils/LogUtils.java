package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.server.ServerNotActiveException;
import java.time.LocalTime;

public class LogUtils {

    public LogUtils(String name) throws IOException {
        createLogFile(name);
    }

    private void createLogFile(String name) throws IOException {
        try {
            File file = new File(name);
            if (!file.exists()) {
                newFile(name);
            }
        } catch (Exception ignored) {}
    }

    public void saveLog(String message, String workerIp, String workerPort, String fileName) throws IOException, ServerNotActiveException {
        BufferedWriter outStream = new BufferedWriter(new FileWriter(fileName, true));
        var localTime = LocalTime.now();

        outStream.write(localTime + " - Worker: " + workerIp +
                ":" + workerPort + " - " + message + "\n");
        outStream.flush();
        outStream.close();
    }

    public void saveLog(String message, String fileName) throws IOException, ServerNotActiveException {
        BufferedWriter outStream = new BufferedWriter(new FileWriter(fileName, true));
        var localTime = LocalTime.now();

        outStream.write(localTime + " - " +  message + "\n");
        outStream.flush();
        outStream.close();
    }

    private void newFile(String name) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(name, true));
        writer.write("Log de execução");
        writer.newLine();
        writer.flush();
        writer.close();
    }

}

