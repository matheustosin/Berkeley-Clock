package worker;

import utils.LogUtils;

import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.server.ServerNotActiveException;
import java.time.LocalTime;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class Worker {
    private static final int BUFFER_SIZE = 1024;
    private static final String TIME_REQUEST = "TIME_REQUEST";
    private static final String TIME_UPDATE = "TIME_UPDATE";
    private String FILE_NAME;
    private DatagramSocket socket;
    private LocalTime currentTime;
    private long delay;
    private String coordinatorHost;
    private int coordinatorPort;
    private LogUtils logUtils;

    public Worker(int port, LocalTime startTime, long delay, int timeIncrement) throws IOException {
        this.socket = new DatagramSocket(port);
        this.currentTime = startTime;
        this.delay = delay;
        this.FILE_NAME = "LogWorker" + port;
        this.logUtils = new LogUtils(FILE_NAME);
        Properties config = new Properties();
        try {
            config.load(new FileReader("src/main/java/config/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.coordinatorHost = config.getProperty("HOST_COORDINATOR");
        this.coordinatorPort = Integer.parseInt(config.getProperty("PORT_COORDINATOR"));

        Timer timer = new Timer();
        timer.schedule(timerTask(timeIncrement), 0, 5000);
    }

    private TimerTask timerTask(int timeIncrement) {
        return new TimerTask() {
            @Override
            public void run() {
                currentTime = currentTime.plusNanos(timeIncrement * 1000000L);
                System.out.println("WORKER TIME: " + currentTime);
            }
        };
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
                        this.logUtils.saveLog("Nova solicitacao de tempo. Tempo atual: " + this.currentTime.toString()
                                .substring(0, this.currentTime.toString().indexOf(".") + 4), FILE_NAME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ServerNotActiveException e) {
                        throw new RuntimeException(e);
                    }
                    sendCurrentTime();
                } else if (receivedPacket.contains(TIME_UPDATE)) {
                    updateCurrentTime(receivedPacket);
                }
                System.out.println("Received request from server: " + receivedPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServerNotActiveException e) {
            throw new RuntimeException(e);
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
    private void updateCurrentTime(String timeUpdateRequest) throws ServerNotActiveException, IOException {
        this.logUtils.saveLog(
                "Tempo antes do ajuste: "
                        + this.currentTime.toString().substring(0, this.currentTime.toString().indexOf(".") + 4),
                FILE_NAME);
        // System.out.println("Mensagem recebida: " + timeUpdateRequest);
        String[] messageSplitted = timeUpdateRequest.split("\\|");
        long offset = Long.parseLong(messageSplitted[1]);
        this.logUtils.saveLog("Offset recebido: " + offset / 1000000000L + " segundos", FILE_NAME);
        // System.out.println("Offset recebido: " + offset);
        LocalTime timeReceived = this.currentTime.plusNanos(offset);
        // System.out.println("Novo tempo do worker: " + timeReceived.toString());
        this.currentTime = timeReceived;
        this.logUtils.saveLog(
                "Tempo depois do ajuste: "
                        + this.currentTime.toString().substring(0, this.currentTime.toString().indexOf(".") + 4),
                FILE_NAME);
    }
}
