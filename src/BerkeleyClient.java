import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.Properties;

public class BerkeleyClient {
	private static final int PORT = 8081;
	private static final int BUFFER_SIZE = 1024;

	public static void main(String[] args) {
		Properties config = new Properties();
		try {
			config.load(new FileReader("config.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String clientDateTime = config.getProperty("HORARIO_CLIENTE" + "1");

		try {
			DatagramSocket socket = new DatagramSocket(PORT);

			InetAddress serverAddress = InetAddress.getLocalHost();

			// Receive response from the server
			byte[] buffer = new byte[BUFFER_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
			while (true) {
				socket.receive(receivePacket);

				String receivedPacket = new String(receivePacket.getData(), 0, receivePacket.getLength());
				System.out.println("Received request from server: " + receivedPacket);

				// Send request to the server
				String response = "MEU TEMPO";
				byte[] responseBytes = response.getBytes();
				DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
						serverAddress, 8080);
				System.out.println("Enviando pacote");
				socket.send(responsePacket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
