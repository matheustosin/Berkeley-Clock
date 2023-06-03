public class ClientModel {
    private String ip;
    private int port;
    private long lastCurrentTime;
    private long delay;

    public ClientModel(String ip, int port, long lastCurrentTime, long delay) {
        this.ip = ip;
        this.port = port;
        this.lastCurrentTime = lastCurrentTime;
        this.delay = delay;
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

    public long getLastCurrentTime() {
        return lastCurrentTime;
    }

    public void setLastCurrentTime(long currentTime) {
        this.lastCurrentTime = currentTime;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}

