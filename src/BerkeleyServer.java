import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BerkeleyServer {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 1024;
    private static final String date = "03/06/2023 12:12:00";
    private static final int INTERVALO = 5000;

    public static void main(String[] args) throws InterruptedException {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(date, dateTimeFormatter);
            ZoneId zoneId = ZoneId.systemDefault();
            System.out.println(localDateTime.atZone(zoneId).toEpochSecond());

            DatagramSocket socket = new DatagramSocket(PORT);

            System.out.println(
                    "Servidor iniciado, mensagem de sincronizacao sera enviado em intervalos de: " + INTERVALO);

            while (true) {
                Thread.sleep(INTERVALO);

                String resquestTime = "TIME_REQUEST";
                byte[] resquestTimeBytes = resquestTime.getBytes();

                // Get the client's address and port
                InetAddress clientAddress = InetAddress.getLocalHost();
                int clientPort = 8081;

                DatagramPacket requestTimePacket = new DatagramPacket(resquestTimeBytes, resquestTimeBytes.length,
                        clientAddress, clientPort);
                System.out.println("Enviando");
                socket.send(requestTimePacket);

                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);

                // Receive request from a client
                System.out.println("Esperando resposta");
                socket.receive(requestPacket);
                System.out.println("Resposta recebida");
                String request = new String(requestPacket.getData(), 0, requestPacket.getLength());
                System.out.println("Received request from client: " + request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}