import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
import java.util.Scanner;

public class Server {

    private static Deck gameDeck;
    private static Deck p1hand;
    private static Deck p2hand;
    private static Deck p1discard;
    private static Deck p2discard;

    private static ServerSocket serverSocket;

    private static Socket socket1, socket2;

    private static DataOutputStream outClient_1, outClient_2;
    private static BufferedReader inClient_1, inClient_2;

    private static final String WELCOME_MSG = "Welcome to the card game 'War'.\n";

    public static void main(String[] args) throws IOException {

        // Printing welcome message
        System.out.println(Server.WELCOME_MSG);

        // initialize the decks
        p1hand = new Deck();
        p2hand = new Deck();

        p1discard = new Deck();
        p2discard = new Deck();

        gameDeck = new Deck();
        gameDeck.fill();
        gameDeck.shuffle();

        // Dealing half of the cards to each player
        for (int j = 0; j < 52; j++) {
            if (j % 2 == 0) {
                Server.p1hand.push(Server.gameDeck.pop());
            } else {
                Server.p2hand.push(Server.gameDeck.pop());
            }
        }
        Utils.assertTrue(p1hand.length() == p2hand.length(), "P1 and P2 had unequal hands after dealing");

        printServerIP();

        // Setting up server socket on specified port
        int port;
        try (Scanner scanner = new Scanner(System.in)) {
            port = Utils.getPort(scanner, 1337);
        }
        Server.serverSocket = new ServerSocket(port);
        System.out.println("\nServer is running on port: " + Server.serverSocket.getLocalPort() + " ...");

        // Player one socket, data in, and data out set up
        Server.socket1 = Server.serverSocket.accept();
        if (Server.socket1.isConnected()) {
            System.out.println("\nPlayer one (" + (Server.socket1.getRemoteSocketAddress().toString()).substring(1)
                    + ":" + Server.socket1.getLocalPort() + ") has joined ... waiting for player two ...");
        }
        Server.outClient_1 = new DataOutputStream(Server.socket1.getOutputStream());
        System.out.println("Successfully set up client 1 output stream");
        Server.inClient_1 = new BufferedReader(new InputStreamReader(Server.socket1.getInputStream()));
        System.out.println("Successfully set up client 1 input stream");

        testClientServerCommunication(1);

        // Player two socket, data in, and data out set up
        Server.socket2 = Server.serverSocket.accept();
        if (Server.socket2.isConnected()) {
            System.out.println("Player two (" + (Server.socket2.getRemoteSocketAddress().toString()).substring(1)
                    + ") has joined ... lets start ...");
        }
        Server.outClient_2 = new DataOutputStream(Server.socket2.getOutputStream());
        System.out.println("Successfully set up client 2 output stream");
        Server.inClient_2 = new BufferedReader(new InputStreamReader(Server.socket2.getInputStream()));
        System.out.println("Successfully set up client 2 input stream");

        testClientServerCommunication(2);

        final String startingGame = "Game starting\n";
        System.out.print(startingGame);
        Server.send(startingGame, 3);

        boolean game = true; // flag to stop the game loop
        int count = 0; // round counter
        String round; // round message
        String p1Send; // outgoing string to player 1
        String p2Send; // outgoing string to player 2

        // Main game loop
        while (game) {
            // print round # to server and both clients
            round = "ROUND #" + count + "\n";
            Server.send(round, 3);
            System.out.print(round);
            count++;

            endGameCheck(1); // check if end game conditions are met

            p1Send = "P1, press enter to flip card. You have " + Server.p1hand.length() + " cards in hand, "
                    + Server.p1discard.length() + " cards in discard pile.\n";
            p2Send = "P2, press enter to flip card. You have " + Server.p2hand.length() + " cards in hand, "
                    + Server.p2discard.length() + " cards in discard pile.\n";

            // send message to client
            Server.send(p1Send, 1);
            Server.send(p2Send, 2);

            // print message to server
            System.out.print(p1Send + p2Send);

            // wait for response
            Server.receive(1);
            Server.receive(2);

            // flip top card on deck
            String p1in = Server.p1hand.pop();
            String p2in = Server.p2hand.pop();

            // compare the cards
            whoWins(p1in, p2in);
        }

        // close sockets
        Server.socket1.close();
        Server.socket2.close();
        Server.serverSocket.close();

    }

    /**
     * Ensures the server can send and receive messages from the client
     *
     * @param clientNum which client to test
     */
    private static void testClientServerCommunication(int clientNum) throws IOException {
        Utils.assertTrue(clientNum == 1 || clientNum == 2);
        System.out.println("Waiting for test message from client " + clientNum);
        System.out.println("Message from client: " + Server.receive(clientNum)); // input test
        Server.send("Server to client " + clientNum + "\n", clientNum); // output test
        System.out.print("Server to client " + clientNum + " success. Player " + clientNum + " ready.\n");
    }

    /**
     * Prints the current machines local ip address to System out
     */
    private static void printServerIP() throws SocketException {
        System.out.print("Server network information:\n");
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isSiteLocalAddress()) {
                    System.out.println(address.getHostAddress());
                }
            }
        }
    }

    /**
     * Sends strings to client sockets
     *
     * @param message        the message to send
     * @param playerToSendTo which client to send to. 1 for P1, 2 for P2, 3 for both
     */
    public static void send(String message, int playerToSendTo) throws IOException {
        if (playerToSendTo == 1) {
            Server.outClient_1.writeBytes(message);
            Server.outClient_1.flush();
        } else if (playerToSendTo == 2) {
            Server.outClient_2.writeBytes(message);
            Server.outClient_2.flush();
        } else if (playerToSendTo == 3) {
            Server.send(message, 1);
            Server.send(message, 2);
        } else {
            System.out.print("Error in Server.send");
        }
    }

    /**
     * Attempts to read the next line from given client
     *
     * @param clientToRead the client to read from. 1 for P1, 2 for P2
     * @return A String containing the message received from the client
     */
    public static String receive(int clientToRead) throws IOException {
        if (clientToRead == 1) {
            return Server.inClient_1.readLine();
        } else if (clientToRead == 2) {
            return Server.inClient_2.readLine();
        } else {
            return "Error in Server.receive";
        }
    }

    /**
     * Determine which player wins a round
     *
     * @param player1card first player to assess
     * @param player2card second player to assess
     * @return true if player 2 wins, else false
     */
    public static boolean whoWins(String player1card, String player2card) throws IOException {

        // print result to server and both clients
        String message = "P1 flipped " + Deck.getString(player1card) + " and P2 flipped " + Deck.getString(player2card) + "\n";
        System.out.print(message);
        Server.send(message, 3);

        // compare the cards to see which wins
        int result = Integer.compare(Deck.getValue(player1card), Deck.getValue(player2card));

        if (result == -1) { // if P2's card is higher value
            message = "P2 wins\n\n";
            System.out.print(message);
            Server.send(message, 3);
            Server.p2discard.push(player1card);
            Server.p2discard.push(player2card);
            return true;
        } else if (result == 1) { // if P2's card is higher value
            message = "P1 wins\n\n";
            System.out.print(message);
            Server.send(message, 3);
            Server.p1discard.push(player1card);
            Server.p1discard.push(player2card);
            return false;
        } else { // if the cards tie then the players enter war
            // print to server and both clients
            message = "Tie! Time for war!\n";
            System.out.print(message);
            Server.send(message, 3);

            endGameCheck(4); // check if both players have enough cards for war

            // prompt players to flip their cards
            message = "P1, press enter to start war. You have " + Server.p1hand.length() + " cards in hand, "
                    + Server.p1discard.length() + " cards in discard pile.\n";
            System.out.print(message);
            Server.send(message, 1);
            message = Server.receive(1);
            // flip 4 cards from P1
            String[] temp1 = new String[4];
            temp1[0] = Server.p1hand.pop();
            temp1[1] = Server.p1hand.pop();
            temp1[2] = Server.p1hand.pop();
            temp1[3] = Server.p1hand.pop();
            message = "P2, press enter to start war. You have " + Server.p2hand.length() + " cards in hand, "
                    + Server.p2discard.length() + " cards in discard pile.\n";
            System.out.print(message);
            Server.send(message, 2);
            message = Server.receive(2);
            // flip 4 cards from P1
            String[] temp2 = new String[4];
            temp2[0] = Server.p2hand.pop();
            temp2[1] = Server.p2hand.pop();
            temp2[2] = Server.p2hand.pop();
            temp2[3] = Server.p2hand.pop();
            // compare the top card from each player then distribute cards accordingly
            boolean temp = whoWins(temp1[3], temp2[3]);
            if (!temp) {
                for (int i = 0; i < 4; i++) {
                    Server.p1discard.push(temp1[i]);
                    Server.p1discard.push(temp2[i]);
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    Server.p2discard.push(temp1[i]);
                    Server.p2discard.push(temp2[i]);
                }
            }
            return temp;
        }
    }

    /**
     * checks to see whether both players have enough cards to continue play
     *
     * @param cardsNeeded the number of cards needed to continue play
     */
    public static void endGameCheck(int cardsNeeded) throws IOException {
        String response;
        if (Server.p1hand.length() < cardsNeeded) {
            if ((Server.p1hand.length() + Server.p1discard.length()) < cardsNeeded) {
                // game over
                response = "Game over. P1 ran out of cards and P2 wins!\n";
                System.out.print(response);
                try {
                    Server.send(response, 3);
                } catch (Exception IOException) {
                    System.out.print("\nTerminating program");
                }
                System.exit(0);
            } else {
                response = "Not enough cards. Reshuffling P1's discard pile into their hand\n";
                Server.send(response, 1);
                Server.p1hand.combine(Server.p1discard);
                Server.p1hand.shuffle();
            }

        }
        if (Server.p2hand.length() < cardsNeeded) {
            if ((Server.p2hand.length() + Server.p2discard.length()) < cardsNeeded) {
                // game over
                response = "Game over. P2 ran out of cards and P1 wins!\n";
                System.out.print(response);
                try {
                    Server.send(response, 3);
                } catch (Exception IOException) {
                    System.out.print("\nTerminating program");
                }
                System.exit(0);
            } else {
                response = "Not enough cards. Reshuffling P1's discard into their hand\n";
                Server.send(response, 2);
                Server.p2hand.combine(Server.p2discard);
                Server.p2hand.shuffle();
            }
        }
    }
}
