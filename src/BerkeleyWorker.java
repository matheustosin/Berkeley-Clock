import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.Properties;

public class BerkeleyWorker {
    public static void main(String[] args) throws SocketException {
        if (args.length != 1) {
            System.out.println("java BerkeleyWorker <workerNr> 1 to 4");
            return;
        }
        int id = Integer.parseInt(args[0]);

        Properties config = new Properties();
        try {
            config.load(new FileReader("src/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String workerTime = config.getProperty("TIME_WORKER" + id);
        String workerDelay = config.getProperty("DELAY_MILI_WORKER" + id);
        String workerPort = config.getProperty("PORT_WORKER" + id);
        LocalTime workerLocalTime = LocalTime.parse(workerTime);
        long delayLocalTime = Long.parseLong(workerDelay);
        int workerPortInt = Integer.parseInt(workerPort);
        Worker client = new Worker(workerPortInt, workerLocalTime, delayLocalTime);
        client.run();
    }

}