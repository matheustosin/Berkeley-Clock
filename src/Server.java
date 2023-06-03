import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private static final String date = "03/06/2023 12:12:00";
    private static final int INTERVALO = 15;
    private static final String TIME_REQUEST = "TIME_REQUEST";
    private static final String TIME_UPDATE = "TIME_UPDATE";
    private DateTimeFormatter dateTimeFormatter;
    private DatagramSocket socket;
    private List<ClientModel> clients;

    public Server(int port, List<ClientModel> clients) throws SocketException {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.socket = new DatagramSocket(port);
        this.clients = clients;
    }

    public void run() {
        try {
            System.out.println(
                    "Servidor iniciado, mensagem de sincronizacao sera enviado em intervalos de: " + INTERVALO/1000 + " segundos.");

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(requestTimeSender(), 1, INTERVALO, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long dateTimeToUnix(String dateTime) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        ZoneId zoneId = ZoneId.systemDefault();
        return localDateTime.atZone(zoneId).toEpochSecond();
    }

    private Runnable requestTimeSender() throws IOException {
        return () -> {
            try {
                byte[] resquestTimeBytes = TIME_REQUEST.getBytes();

                for (ClientModel client : this.clients) {
                    InetAddress clientAddress = InetAddress.getByName(client.getIp());
                    int clientPort = client.getPort();
                    DatagramPacket requestTimePacket = new DatagramPacket(resquestTimeBytes, resquestTimeBytes.length,
                            clientAddress, clientPort);
                    long timer = System.currentTimeMillis();
                    socket.send(requestTimePacket);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    timer = (System.currentTimeMillis() - timer) / 1000;

                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    client.setLastCurrentTime(Long.parseLong(response));
                    client.setDelay(timer);
                }
                //TODO Criar algoritmo de berkeley
                //TODO for para devolver o tempo certo para cada cliente
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        };
    }

    private void sendTimeToClients() {

    }
}
