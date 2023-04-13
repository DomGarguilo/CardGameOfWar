import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

class Client {

    /**
     * Object to encapsulate the communication to and from the server.
     */
    private static class ServerConnection implements AutoCloseable {

        private final Socket clientSocket;
        private final DataOutputStream outToServer;
        private final InputStreamReader inputStreamReader;
        private final BufferedReader inFromServer;

        public ServerConnection(String host, int port) throws IOException {
            this.clientSocket = new Socket(host, port);
            this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
            this.inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            this.inFromServer = new BufferedReader(inputStreamReader);
        }

        /**
         * Sends a String to the server
         *
         * @param message String to send to the server
         */
        private void sendMsgToServer(String message) throws IOException {
            this.outToServer.writeBytes(message);
            this.outToServer.flush();
        }

        /**
         * Reads a line from the given BufferedReader
         *
         * @return A String of the line read from the reader
         */
        private String receiveMsgFromServer() throws IOException {
            return this.inFromServer.readLine();
        }

        @Override
        public void close() throws Exception {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (outToServer != null) {
                outToServer.close();
            }
        }
    }

    public static final String GAME_OVER = "Game over.";
    public static final String PRESS_ENTER = "press enter";

    private static final String WELCOME_MSG = "--- Welcome to the card game 'war'\n";
    private static final String RULES_MSG = "\nRule set:\n - Each player flips one card at a time\n - Whoever had the highest card value takes both cards \n - "
            + "If there is a tie, the players 'go to war' meaning: each player puts 3 cards face down and 1 face up\n - The face up card is compared and handled as stated above\n - "
            + "Once you have no more cards in you hand, the second pile is reshuffled into your hand\n - The game is won once the other player runs out of cards\n\n";

    public static void main(String[] args) throws Exception {

        String input, response;

        System.out.print(Client.WELCOME_MSG);
        System.out.print(Client.RULES_MSG);

        try (Scanner clientInputScanner = new Scanner(System.in)) {

            final String host = Client.getHost(clientInputScanner, "localhost");
            final int port = Utils.getPort(clientInputScanner, 1337);
            final boolean autoFlip = getAutoFlip(clientInputScanner);

            // initializing the client socket, output to server and input from server
            try (ServerConnection serverConnection = new ServerConnection(host, port)) {

                serverConnection.sendMsgToServer("Client to server test\n"); // output test
                System.out.print("\nClient to server test msg sent\n");
                response = serverConnection.receiveMsgFromServer(); // input test
                System.out.print("Test msg from server received: " + response + "\n");

                System.out.print("Ready to begin. Press enter to start game");
                var ignore = System.in.read(); // wait for user to press enter

                // Constantly listening for messages from the server
                while ((response = serverConnection.receiveMsgFromServer()) != null) {

                    // Print response
                    System.out.print("\nServer: " + response);

                    // Check if game has ended
                    if (response.contains(GAME_OVER)) {
                        break;
                    }
                    if (response.contains(PRESS_ENTER)) {
                        input = "send card\n";
                        if (!autoFlip) {
                            var ignore1 = System.in.read(); // wait for user to press enter
                        }
                        // Tell server to flip next card
                        serverConnection.sendMsgToServer(input);
                        System.out.print("\nYour card was successfully sent to the server."
                                + " Waiting on result from the server...");
                    }
                }
            }
        } finally {
            System.out.print("\nClient program ended\n");
        }
    }

    /**
     * Ask the user what host they want to connect to.
     *
     * @param inputScanner Scanner object to use to get user input
     * @param defaultHost  host to default to if user chooses to
     * @return the host that the user has chosen
     */
    public static String getHost(Scanner inputScanner, String defaultHost) {
        System.out.print("Please enter the servers ip address or\n");
        System.out.print("insert \"0\" to connect to server running on this machine (localhost): ");
        String input = inputScanner.next();

        return Objects.equals(input, "0") ? defaultHost : input;
    }

    /**
     * Ask the user if they want to enable automatic flipping of cards
     *
     * @param inputScanner the scanner to read user input from
     * @return true if auto flip is to be enabled, else false
     */
    public static Boolean getAutoFlip(Scanner inputScanner) {
        char input;
        Boolean result;
        do {
            System.out.print("\nDo you want to enable automatic card flipping? (y/n): ");
            input = Character.toUpperCase(inputScanner.next().charAt(0));
            result = (input == 'Y') ? Boolean.TRUE : (input == 'N') ? Boolean.FALSE : null;
        } while (result == null);
        System.out.print(result ? "Autoflip enabled\n" : "Autoflip disabled\n");
        return result;
    }

}
