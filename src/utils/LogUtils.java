package utils;

import java.time.LocalTime;

public class LogUtils {

    public LogUtils() throws IOException {
        createLogFile();
    }

    private void createLogFile() throws IOException {
        try {
            File file = new File("log.txt");
            if (!file.exists()) {
                newFile();
            }
        } catch (Exception ignored) {}
    }

    private void saveLog(String message, String workerIp, String workerPort) throws IOException, ServerNotActiveException {
        BufferedWriter outStream = new BufferedWriter(new FileWriter("log.txt", true));
        var localTime = LocalTime.now();

        outStream.write(localTime + " - Worker: " + workerIp +
                ":" + workerPort + " - " + message + "\n");
        outStream.flush();
        outStream.close();
    }

    public void saveLog(String message) throws IOException, ServerNotActiveException {
        BufferedWriter outStream = new BufferedWriter(new FileWriter("log.txt", true));
        var localTime = LocalTime.now();

        outStream.write(localTime + " - " +  message + "\n");
        outStream.flush();
        outStream.close();
    }

}
