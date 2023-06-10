package coordinator;

import worker.WorkerModel;

import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Coordinator {
    private static final Logger log = Logger.getLogger(Coordinator.class.getName());
    private static final int BUFFER_SIZE = 1024;
    private static final int INTERVALO = 15;
    private static final String TIME_REQUEST = "TIME_REQUEST";
    private static final String TIME_UPDATE = "TIME_UPDATE";
    private LocalTime currentTime;
    private DatagramSocket socket;
    private List<WorkerModel> workers;
    private long acceptedDeviance;

    public Coordinator(int port, LocalTime currentTime, int acceptedDeviance, List<WorkerModel> workers)
            throws SocketException {
        this.socket = new DatagramSocket(port);
        this.workers = workers;
        this.currentTime = currentTime;
        this.acceptedDeviance = acceptedDeviance * 1000000000;
    }

    public void run() {
        try {
            System.out.println(
                    "Servidor iniciado, mensagem de sincronizacao sera enviado em intervalos de: " + INTERVALO
                            + " segundos.");

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(requestTimeSender(), 1, INTERVALO, TimeUnit.SECONDS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable requestTimeSender() throws IOException, UnknownHostException {
        return () -> {
            try {
                byte[] resquestTimeBytes = TIME_REQUEST.getBytes();

                for (WorkerModel worker : this.workers) {
                    System.out.println("Enviando pedido de tempo para: " + worker.getIp() + ":" + worker.getPort());
                    InetAddress clientAddress = InetAddress.getByName(worker.getIp());
                    int clientPort = worker.getPort();
                    DatagramPacket requestTimePacket = new DatagramPacket(resquestTimeBytes, resquestTimeBytes.length,
                            clientAddress, clientPort);
                    LocalTime start = LocalTime.now();
                    socket.send(requestTimePacket);

                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(responsePacket);
                    LocalTime end = LocalTime.now();

                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    System.out.println("Pacote recebido de: " + responsePacket.getAddress() + ":"
                            + responsePacket.getPort() + ". Mensagem: " + response);
                    worker.setLastCurrentTime(LocalTime.parse(response));
                    long delay = end.toNanoOfDay() - start.toNanoOfDay();
                    worker.setDelay(delay);
                    System.out.println("Tempo entre envio do pedido e recebimento do pacote: "
                            + responsePacket.getAddress() + ": " + delay + " nanosegundos");
                }

                barkeleyAlgorithm(this.workers);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            }
        };
    }

    private void barkeleyAlgorithm(List<WorkerModel> workers) throws IOException {
        System.out.println("Calculando Berkeley");
        //LocalTime localTime = LocalTime.now();
        // Calcula a primeira média
        long workersAverage = workers.stream()
                .mapToLong(worker -> worker.getLastCurrentTime().toNanoOfDay())
                .sum();
        workersAverage += this.currentTime.toNanoOfDay();
        long average = workersAverage / (workers.size() + 1);
        // Calcula a nova média filtrando os valores acima do limite de desvio
        long newAverage = 0;
        int workerInCount = 0;
        for (WorkerModel worker : workers) {
            long diff = Math.abs(worker.getLastCurrentTime().toNanoOfDay() - average);
            if (diff <= acceptedDeviance) {
                newAverage += worker.getLastCurrentTime().toNanoOfDay();
                workerInCount++;
            }
        }
        // Verifica se o horário do servidor deve ser inserido na média
        if (Math.abs(this.currentTime.toNanoOfDay() - average) <= acceptedDeviance) {
            newAverage += this.currentTime.toNanoOfDay();
            newAverage = newAverage / (workerInCount + 1);
        } else {
            newAverage = newAverage / workerInCount;
        }

        for (WorkerModel worker : workers) {
            long oneWayDelay = worker.getDelay() / 2;
            long offset = oneWayDelay + (average - worker.getLastCurrentTime().toNanoOfDay());
            sendTimeToClients(offset, worker);
        }
        // Atualiza o tempo de servidor
        this.currentTime = this.currentTime.plusNanos(Math.round(newAverage));
        return;
    }

    private void sendTimeToClients(long offset, WorkerModel worker) throws IOException {
        System.out.println("Enviando offset para o worker: " + offset);
        InetAddress clientAddress = InetAddress.getByName(worker.getIp());
        int clientPort = worker.getPort();
        String sendTimeWorker = TIME_UPDATE + "|" + offset;
        byte[] sendTime = sendTimeWorker.getBytes();
        DatagramPacket requestTimePacket = new DatagramPacket(sendTime, sendTime.length,
                clientAddress, clientPort);
        socket.send(requestTimePacket);
        return;
    }
}

