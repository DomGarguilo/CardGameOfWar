import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Object to encapsulate the communication between sockets.
 */
public class SocketConnection implements AutoCloseable {

    private final Socket socket;
    private final DataOutputStream output;
    private final InputStreamReader inputStreamReader;
    private final BufferedReader input;

    public SocketConnection(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public SocketConnection(ServerSocket serverSocket) throws IOException {
        this(serverSocket.accept());
    }

    private SocketConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new DataOutputStream(socket.getOutputStream());
        this.inputStreamReader = new InputStreamReader(socket.getInputStream());
        this.input = new BufferedReader(inputStreamReader);
    }

    /**
     * Sends a String over the Socket
     *
     * @param message String to send
     */
    public void sendMsgToServer(String message) throws IOException {
        this.output.writeBytes(message);
        this.output.flush();
    }

    /**
     * Reads a line from the BufferedReader
     *
     * @return A String of the line read from the reader
     */
    public String receiveMsgFromServer() throws IOException {
        return this.input.readLine();
    }

    public boolean socketIsConnected() {
        return this.socket.isConnected();
    }

    public String getAddress() {
        return socket.getRemoteSocketAddress().toString();
    }

    @Override
    public void close() throws Exception {
        if (socket != null) {
            socket.close();
        }
        if (input != null) {
            input.close();
        }
        if (inputStreamReader != null) {
            inputStreamReader.close();
        }
        if (output != null) {
            output.close();
        }
    }

}