package Week_10;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.concurrent.*;

public class server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        KeyPair keyPair = null;

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            System.err.println("Không thể tạo key pair");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang lắng nghe tại cổng " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Kết nối từ " + clientSocket.getInetAddress().getHostAddress());
                Ex1_ClientHandler handler = new Ex1_ClientHandler(clientSocket, keyPair);
                pool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
