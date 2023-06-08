package coordinator;

import worker.WorkerModel;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Properties;

public class BerkeleyCoordinator {
    public static void main(String[] args) throws InterruptedException {
        try {
            Properties config = new Properties();
            try {
                config.load(new FileReader("./config/config.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String coordinatorPort = config.getProperty("PORT_COORDINATOR");
            String coordinatorTime = config.getProperty("TIME_COORDINATOR");
            String acceptedDeviance = config.getProperty("ACCEPTED_DEVIANCE_SECONDS");
            LocalTime coordinatorLocalTime = LocalTime.parse(coordinatorTime);

            String worker1Port = config.getProperty("PORT_WORKER1");
            String worker1Host = config.getProperty("HOST_WORKER1");
            String worker2Port = config.getProperty("PORT_WORKER2");
            String worker2Host = config.getProperty("HOST_WORKER2");
            String worker3Port = config.getProperty("PORT_WORKER3");
            String worker3Host = config.getProperty("HOST_WORKER3");
            String worker4Port = config.getProperty("PORT_WORKER4");
            String worker4Host = config.getProperty("HOST_WORKER4");
            ArrayList<WorkerModel> workers = new ArrayList<>();

            workers.add(new WorkerModel(worker1Host, Integer.parseInt(worker1Port)));
            // workers.add(new worker.WorkerModel(worker2Host, Integer.parseInt(worker2Port)));
            // workers.add(new worker.WorkerModel(worker3Host, Integer.parseInt(worker3Port)));
            // workers.add(new worker.WorkerModel(worker4Host, Integer.parseInt(worker4Port)));

            Coordinator coordinator = new Coordinator(Integer.parseInt(coordinatorPort), coordinatorLocalTime,
                    Integer.parseInt(acceptedDeviance), workers);
            coordinator.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}