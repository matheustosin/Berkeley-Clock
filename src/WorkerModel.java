import java.time.LocalTime;

public class WorkerModel {
    private String ip;
    private int port;
    private LocalTime lastCurrentTime;
    private long delay;

    public WorkerModel(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public LocalTime getLastCurrentTime() {
        return lastCurrentTime;
    }

    public void setLastCurrentTime(LocalTime currentTime) {
        this.lastCurrentTime = currentTime;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
