import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.Properties;

public class Worker {
    private static final int BUFFER_SIZE = 1024;
    private static final String TIME_REQUEST = "TIME_REQUEST";
    private static final String TIME_UPDATE = "TIME_UPDATE";
    private DatagramSocket socket;
    private LocalTime currentTime;
    private long delay;
    private String coordinatorHost;
    private int coordinatorPort;

    public Worker(int port, LocalTime startTime, long delay) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.currentTime = startTime;
        this.delay = delay;
        Properties config = new Properties();
        try {
            config.load(new FileReader("src/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.coordinatorHost = config.getProperty("HOST_COORDINATOR");
        this.coordinatorPort = Integer.parseInt(config.getProperty("PORT_COORDINATOR"));
    }

    public void run() {
        try {
            // Receive response from the server
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            while (true) {
                socket.receive(receivePacket);

                String receivedPacket = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (receivedPacket.equals(TIME_REQUEST)) {
                    try {
                        Thread.sleep(this.delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendCurrentTime();
                } else if (receivedPacket.contains(TIME_UPDATE)) {
                    updateCurrentTime(receivedPacket);
                }
                System.out.println("Received request from server: " + receivedPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendCurrentTime() throws IOException {
        InetAddress serverAddress = InetAddress.getByName(this.coordinatorHost);
        String response = this.currentTime.toString();
        byte[] responseBytes = response.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
                serverAddress, this.coordinatorPort);
        System.out.println("Enviando pacote com o tempo atual: " + response);
        socket.send(responsePacket);
    }

    // receber no formato TIME_UPDATE|TEMPO
    private void updateCurrentTime(String timeUpdateRequest) {
        System.out.println("Mensagem recebida: " + timeUpdateRequest);
        String[] messageSplitted = timeUpdateRequest.split("\\|");
        LocalTime timeReceived = LocalTime.parse(messageSplitted[1]);
        this.currentTime = timeReceived;
    }
}
