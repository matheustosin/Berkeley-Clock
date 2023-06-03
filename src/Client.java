import java.io.IOException;
import java.net.*;

public class Client {
    private static final int BUFFER_SIZE = 1024;
    private static final String TIME_REQUEST = "TIME_REQUEST";
    private static final String TIME_UPDATE = "TIME_UPDATE";
    private DatagramSocket socket;
    private long currentTimeUnix;
    private int delay;

    public Client(int port, long startTimeUnix, int delay) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.currentTimeUnix = startTimeUnix;
        this.delay = delay;
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
                    sendCurrentTime();
                } else if (receivedPacket.equals(TIME_UPDATE)) {
                    updateCurrentTime();
                }
                System.out.println("Received request from server: " + receivedPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   private void sendCurrentTime() throws IOException {
       // Send request to the server
       InetAddress serverAddress = InetAddress.getLocalHost();
       String response = "MEU TEMPO";
       byte[] responseBytes = response.getBytes();
       DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
               serverAddress, 8080);
       System.out.println("Enviando pacote");
       socket.send(responsePacket);
    }

    private void updateCurrentTime(){

    }
}
