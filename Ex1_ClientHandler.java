package Week_10;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Ex1_ClientHandler implements Runnable {
    private static ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();
    private Socket socket;
    private String clientName;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    public Ex1_ClientHandler(Socket socket, KeyPair keyPair) {
        this.socket = socket;
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    public void run() {
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            writer.println("Nhập tên của bạn:");
            clientName = reader.readLine();

            clients.put(clientName, new ClientInfo(clientName, socket, writer));
            System.out.println("[SERVER] " + clientName + " đã kết nối!");

            // Gửi public key cho client
            writer.println("PublicKey:" + Base64.getEncoder().encodeToString(publicKey.getEncoded()));

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.equalsIgnoreCase("bye")) {
                    writer.println("Tạm biệt " + clientName);
                    break;
                }

                // Cú pháp nhận: <receiver>:<encrypted_message>:<encrypted_AES_key>
                if (inputLine.contains("/")) {
                    String[] parts = inputLine.split("/", 3);
                    if (parts.length < 3) {
                        writer.println("Sai cú pháp! Đúng: <TênNgườiNhận>/<TinNhắnMãHóa>/<KhóaAESMãHóa>");
                        continue;
                    }

                    String recipient = parts[0];
                    String encryptedMessage = parts[1];
                    String encryptedAESKey = parts[2];

                    // Giải mã khóa AES bằng RSA
                    byte[] aesKeyBytes = Base64.getDecoder().decode(encryptedAESKey);
                    byte[] decryptedAESKey = decryptRSA(aesKeyBytes);
                    SecretKey aesKey = new SecretKeySpec(decryptedAESKey, "AES");

                    // Giải mã tin nhắn bằng AES
                    byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
                    String decryptedMessage = decryptAES(encryptedBytes, aesKey);

                    // In ra thông tin
                    System.out.println("[SERVER] " + clientName + " gửi đến " + recipient + ":");
                    System.out.println("  ➤ Mã hóa tin nhắn: " + encryptedMessage);
                    System.out.println("  ➤ Mã hóa khóa AES: " + encryptedAESKey);
                    System.out.println("  ➤ Giải mã: " + decryptedMessage);

                    // Gửi cho client đích
                    sendToClient(recipient, "[Đã nhận từ " + clientName + "]: " + decryptedMessage);
                } else {
                    writer.println("Sai cú pháp! Đúng: <TênNgườiNhận>/<TinNhắnMãHóa>/<KhóaAESMãHóa>");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý client " + clientName + ": " + e.getMessage());
        } finally {
            if (clientName != null) {
                clients.remove(clientName);
                System.out.println("[SERVER] " + clientName + " đã ngắt kết nối.");
            }
        }
    }

    private static void sendToClient(String receiver, String message) {
        ClientInfo recipient = clients.get(receiver);
        if (recipient != null) {
            recipient.getWriter().println(message);
        } else {
            System.out.println("[SERVER] Không tìm thấy client: " + receiver);
        }
    }

    private static byte[] decryptRSA(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    private static String decryptAES(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(data), "UTF-8");
    }
}