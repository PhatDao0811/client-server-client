package Week_10;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client_2 {
    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 12345);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println(reader.readLine()); // Nhập tên
            String name = scanner.nextLine();
            writer.println(name);

            String publicKeyLine = reader.readLine();
            PublicKey serverPublicKey = getPublicKeyFromBase64(publicKeyLine.replace("PublicKey:", ""));

            new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            while (true) {
                System.out.print("Nhập (receiver/message): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("bye")) {
                    writer.println("bye");
                    break;
                }

                String[] parts = input.split(":", 2);
                if (parts.length < 2) continue;

                // Tạo khóa AES ngẫu nhiên
                SecretKey aesKey = generateAESKey();
                // Mã hóa tin nhắn bằng AES
                byte[] encryptedMessage = encryptAES(parts[1], aesKey);
                // Mã hóa khóa AES bằng RSA
                byte[] encryptedAESKey = encryptRSA(aesKey.getEncoded(), serverPublicKey);

                // Chuyển thành Base64
                String base64Message = Base64.getEncoder().encodeToString(encryptedMessage);
                String base64AESKey = Base64.getEncoder().encodeToString(encryptedAESKey);

                // Gửi định dạng: receiver:encryptedMessage:encryptedAESKey
                writer.println(parts[0] + ":" + base64Message + ":" + base64AESKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PublicKey getPublicKeyFromBase64(String base64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64);
        return KeyFactory.getInstance("RSA")
                .generatePublic(new java.security.spec.X509EncodedKeySpec(keyBytes));
    }

    private static SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // Khóa AES 256-bit
        return keyGen.generateKey();
    }

    private static byte[] encryptAES(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(message.getBytes("UTF-8"));
    }

    private static byte[] encryptRSA(byte[] data, PublicKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
}