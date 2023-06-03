import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.Properties;

public class BerkeleyClient {
    private static final int PORT = 8081;
    private static final int BUFFER_SIZE = 1024;
    private static final String timeRequest = "TIME_REQUEST";
    private static final String timeUpdate = "TIME_UPDATE";

    public static void main(String[] args) throws SocketException {
        Properties config = new Properties();
        try {
            config.load(new FileReader("config.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String clientDateTime = config.getProperty("HORARIO_CLIENTE" + "1");
        Client client = new Client(8081, 1685826724, 50);
        client.run();
    }

}
