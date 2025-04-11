package Week_10;

import java.io.PrintWriter;
import java.net.Socket;

public class ClientInfo {
    private String name;
    private Socket socket;
    private PrintWriter writer;

    public ClientInfo(String name, Socket socket, PrintWriter writer) {
        this.name = name;
        this.socket = socket;
        this.writer = writer;
    }

    public PrintWriter getWriter() {
        return writer;
    }
}
