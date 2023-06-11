package coordinator;

import utils.LogUtils;
import worker.WorkerModel;

import java.io.IOException;
import java.net.*;
import java.rmi.server.ServerNotActiveException;
import java.time.LocalTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
    private LogUtils logUtils;

    public Coordinator(int port, LocalTime currentTime, int acceptedDeviance, int timeIncrement, List<WorkerModel> workers) throws IOException {
        this.socket = new DatagramSocket(port);
        this.workers = workers;
        this.currentTime = currentTime;
        this.acceptedDeviance = acceptedDeviance * 1000000000L;
        Timer timer = new Timer();
        timer.schedule(timerTask(timeIncrement), 0, 5000);
        this.logUtils = new LogUtils("LogCoordinator");
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

    private TimerTask timerTask(int timeIncrement) {
        return new TimerTask() {
            @Override
            public void run() {
                currentTime = currentTime.plusNanos(timeIncrement * 1000000L);
                System.out.println("COORDINATOR TIME: " + currentTime);
            }
        };
    }

    private Runnable requestTimeSender() throws IOException, UnknownHostException {
        return () -> {
            try {
                byte[] resquestTimeBytes = TIME_REQUEST.getBytes();
                logUtils.saveLog("Solicitando tempo dos workers");

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
                    logUtils.saveLog("Novo tempo recebido: " + worker.getLastCurrentTime().toString().substring(0, worker.getLastCurrentTime().toString().indexOf(".")+4), worker.getIp(), String.valueOf(worker.getPort()));
                    long delay = end.toNanoOfDay() - start.toNanoOfDay();
                    worker.setDelay(delay);
                    System.out.println("Tempo entre envio do pedido e recebimento do pacote: "
                            + responsePacket.getAddress() + ": " + delay + " nanosegundos");
                }

                barkeleyAlgorithm(this.workers);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getMessage(), e);
            } catch (ServerNotActiveException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void barkeleyAlgorithm(List<WorkerModel> workers) throws IOException, ServerNotActiveException {
        System.out.println("Calculando Berkeley");
        // Calcula a primeira média
        long workersAverage = workers.stream()
                .mapToLong(worker -> worker.getLastCurrentTime().toNanoOfDay())
                .sum();
        workersAverage += this.currentTime.toNanoOfDay();
        long average = workersAverage / (workers.size() + 1);
        logUtils.saveLog("Media antes dos descartes: " + LocalTime.ofNanoOfDay(average));
        // Calcula a nova média filtrando os valores acima do limite de desvio
        long newAverage = 0;
        int workerInCount = 0;
        for (WorkerModel worker : workers) {
            long diff = Math.abs(worker.getLastCurrentTime().toNanoOfDay() - average);
            if (diff <= acceptedDeviance) {
                logUtils.saveLog("considerado dentro do desvio com desvio de: "+ diff/1000000000L +" segundos da media ", worker.getIp(), String.valueOf(worker.getPort()));
                newAverage += worker.getLastCurrentTime().toNanoOfDay();
                workerInCount++;
            } else {
                logUtils.saveLog("descartado com desvio de: "+ diff/1000000000L +" segundos da media ", worker.getIp(), String.valueOf(worker.getPort()));
            }
        }
        // Verifica se o horário do servidor deve ser inserido na média
        var diffCoordinator = Math.abs(this.currentTime.toNanoOfDay() - average);
        if (diffCoordinator <= acceptedDeviance) {
            logUtils.saveLog("Coordinator sera considerado na media com desvio de: " + diffCoordinator/1000000000L +" segundos");
            newAverage += this.currentTime.toNanoOfDay();
            newAverage = newAverage / (workerInCount + 1);
        } else {
            logUtils.saveLog("Coordinator sera descartado com desvio de: " + diffCoordinator/1000000000L +" segundos");
            newAverage = newAverage / workerInCount;

        }
        for (WorkerModel worker : workers) {
            long oneWayDelay = worker.getDelay() / 2;
            long offset = oneWayDelay + (average - worker.getLastCurrentTime().toNanoOfDay());
//            logUtils.saveLog("descartado com a diferença de: "+ diff/1000000000L +" segundos da media ", worker.getIp(), String.valueOf(worker.getPort()));
            sendTimeToClients(offset, worker);
        }
        // Atualiza o tempo de servidor
        this.currentTime = this.currentTime.plusNanos(Math.round(newAverage));
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
    }
}

