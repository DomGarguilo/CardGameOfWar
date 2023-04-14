import java.util.Objects;
import java.util.Scanner;

class Client {

    public static final String GAME_OVER = "Game over.";
    public static final String PRESS_ENTER = "press enter";

    private static final String WELCOME_MSG = "--- Welcome to the card game 'war'\n";
    private static final String RULES_MSG = "\nRule set:\n - Each player flips one card at a time\n - Whoever had the highest card value takes both cards \n - "
            + "If there is a tie, the players 'go to war' meaning: each player puts 3 cards face down and 1 face up\n - The face up card is compared and handled as stated above\n - "
            + "Once you have no more cards in you hand, the second pile is reshuffled into your hand\n - The game is won once the other player runs out of cards\n\n";

    public static void main(String[] args) throws Exception {

        System.out.print(Client.WELCOME_MSG);
        System.out.print(Client.RULES_MSG);

        try (Scanner clientInputScanner = new Scanner(System.in)) {

            final String host = Client.getHost(clientInputScanner, "localhost");
            final int port = Utils.getPort(clientInputScanner, 1337);
            final boolean autoFlip = getAutoFlip(clientInputScanner);

            // This SocketConnection object is used to communicate with the server via a socket
            try (SocketConnection serverConnection = new SocketConnection(host, port)) {

                System.out.print("Ready to begin. Press enter to start game");
                var ignore = System.in.read(); // wait for user to press enter
                System.out.println("Waiting to receive start game msg from server");

                String input, response;

                // Constantly listening for messages from the server
                while ((response = serverConnection.receiveMsgFromServer()) != null) {

                    // Print response
                    System.out.print("\nServer: " + response);

                    // Check if game has ended
                    if (response.contains(GAME_OVER)) {
                        System.out.println("Game over msg received from Server");
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
            } finally {
                System.out.print("\n Server SocketConnection closed");
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
        System.out.print(result ? "Auto-flip enabled\n" : "Auto-flip disabled\n");
        return result;
    }

}
