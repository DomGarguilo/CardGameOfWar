import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

public class Server {

    private static Deck p1hand;
    private static Deck p2hand;
    private static Deck p1discard;
    private static Deck p2discard;

    private static final String WELCOME_MSG = "Welcome to the card game 'War'.\n";

    public static void main(String[] args) throws Exception {

        // Printing welcome message
        System.out.println(WELCOME_MSG);

        // initialize the decks
        p1hand = new Deck();
        p2hand = new Deck();

        p1discard = new Deck();
        p2discard = new Deck();

        final Deck gameDeck = new Deck();
        gameDeck.fill();
        gameDeck.shuffle();

        // Dealing half of the cards to each player
        for (int j = 0; j < 52; j++) {
            if (j % 2 == 0) {
                p1hand.push(gameDeck.pop());
            } else {
                p2hand.push(gameDeck.pop());
            }
        }
        Utils.assertTrue(p1hand.length() == p2hand.length(), "P1 and P2 had unequal hands after dealing");

        printServerIP();

        // Setting up server socket on specified port
        int port;
        try (Scanner scanner = new Scanner(System.in)) {
            port = Utils.getPort(scanner, 1337);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("\nServer is running on port: " + serverSocket.getLocalPort() + ". Waiting for clients to connect");
            try (SocketConnection clientSocket1 = new SocketConnection(serverSocket)) {
                if (clientSocket1.socketIsConnected()) {
                    System.out.println("\nPlayer one (" + (clientSocket1.getAddress()).substring(1)
                            + ") has joined, waiting for player two.");
                }

                try (SocketConnection clientSocket2 = new SocketConnection(serverSocket)) {
                    if (clientSocket2.socketIsConnected()) {
                        System.out.println("Player two (" + (clientSocket2.getAddress()).substring(1)
                                + ") has joined. Starting game.");
                    }

                    final String startingGame = "Game starting\n";
                    System.out.print(startingGame);
                    clientSocket1.sendMsgToServer(startingGame);
                    clientSocket2.sendMsgToServer(startingGame);

                    int roundNumber = 0; // round counter

                    // Main game loop
                    while (true) {
                        // print round # to server and both clients
                        String roundMsg = "ROUND #" + roundNumber + "\n";
                        clientSocket1.sendMsgToServer(roundMsg);
                        clientSocket2.sendMsgToServer(roundMsg);
                        System.out.print(roundMsg);
                        roundNumber++;

                        endGameCheck(1, clientSocket1, clientSocket2); // check if end game conditions are met

                        String p1Send = "P1, press enter to flip card. You have " + p1hand.length() + " cards in hand, "
                                + p1discard.length() + " cards in discard pile.\n";
                        String p2Send = "P2, press enter to flip card. You have " + p2hand.length() + " cards in hand, "
                                + p2discard.length() + " cards in discard pile.\n";

                        // send message to client
                        clientSocket1.sendMsgToServer(p1Send);
                        clientSocket2.sendMsgToServer(p2Send);

                        // print message to server
                        System.out.print(p1Send + p2Send);

                        // wait for response
                        clientSocket1.receiveMsgFromServer();
                        clientSocket2.receiveMsgFromServer();

                        // flip top card on deck
                        String p1in = p1hand.pop();
                        String p2in = p2hand.pop();

                        // compare the cards
                        whoWins(p1in, p2in, clientSocket1, clientSocket2);
                    }
                } finally {
                    System.out.println("Client2 socket connection closed");
                }
            } finally {
                System.out.println("Client1 socket connection closed");
            }
        } finally {
            System.out.println("Server socket closed");
        }

    }

    /**
     * Ensures the server can send and receive messages from the client
     *
     * @param clientNum which client to test
     */
//    private static void testClientServerCommunication(int clientNum) throws IOException {
//        Utils.assertTrue(clientNum == 1 || clientNum == 2);
//        System.out.println("Waiting for test message from client " + clientNum);
//        System.out.println("Message from client: " + receive(clientNum)); // input test
//        send("Server to client " + clientNum + "\n", clientNum); // output test
//        System.out.print("Server to client " + clientNum + " success. Player " + clientNum + " ready.\n");
//    }

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
     * Determine which player wins a round
     *
     * @param player1card first player to assess
     * @param player2card second player to assess
     * @return true if player 2 wins, else false
     */
    public static boolean whoWins(String player1card, String player2card, SocketConnection socket1, SocketConnection socket2) throws IOException {

        // print result to server and both clients
        String message = "P1 flipped " + Deck.getString(player1card) + " and P2 flipped " + Deck.getString(player2card) + "\n";
        System.out.print(message);
        socket1.sendMsgToServer(message);
        socket2.sendMsgToServer(message);

        // compare the cards to see which wins
        int result = Integer.compare(Deck.getValue(player1card), Deck.getValue(player2card));

        if (result < 0) { // if P2's card is higher value
            message = "P2 wins\n\n";
            System.out.print(message);
            socket1.sendMsgToServer(message);
            socket2.sendMsgToServer(message);
            p2discard.push(player1card);
            p2discard.push(player2card);
            return true;
        } else if (result > 0) { // if P1's card is higher value
            message = "P1 wins\n\n";
            System.out.print(message);
            socket1.sendMsgToServer(message);
            socket2.sendMsgToServer(message);
            p1discard.push(player1card);
            p1discard.push(player2card);
            return false;
        } else { // if the cards tie then the players enter war
            // print to server and both clients
            message = "Tie! Time for war!\n";
            System.out.print(message);
            socket1.sendMsgToServer(message);
            socket2.sendMsgToServer(message);

            endGameCheck(4, socket1, socket2); // check if both players have enough cards for war

            // prompt players to flip their cards
            message = "P1, press enter to start war. You have " + p1hand.length() + " cards in hand, "
                    + p1discard.length() + " cards in discard pile.\n";
            System.out.print(message);
            socket1.sendMsgToServer(message);
            socket1.receiveMsgFromServer();
            // flip 4 cards from P1
            String[] temp1 = new String[4];
            temp1[0] = p1hand.pop();
            temp1[1] = p1hand.pop();
            temp1[2] = p1hand.pop();
            temp1[3] = p1hand.pop();
            message = "P2, press enter to start war. You have " + p2hand.length() + " cards in hand, "
                    + p2discard.length() + " cards in discard pile.\n";
            System.out.print(message);
            socket2.sendMsgToServer(message);
            socket2.receiveMsgFromServer();
            // flip 4 cards from P1
            String[] cards = new String[4];
            cards[0] = p2hand.pop();
            cards[1] = p2hand.pop();
            cards[2] = p2hand.pop();
            cards[3] = p2hand.pop();
            // compare the top card from each player then distribute cards accordingly
            boolean temp = whoWins(temp1[3], cards[3], socket1, socket2);
            if (!temp) {
                for (int i = 0; i < 4; i++) {
                    p1discard.push(temp1[i]);
                    p1discard.push(cards[i]);
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    p2discard.push(temp1[i]);
                    p2discard.push(cards[i]);
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
    public static void endGameCheck(int cardsNeeded, SocketConnection socket1, SocketConnection socket2) throws IOException {
        String response;
        if (p1hand.length() < cardsNeeded) {
            if ((p1hand.length() + p1discard.length()) < cardsNeeded) {
                // game over
                response = "Game over. P1 ran out of cards and P2 wins!\n";
                System.out.print(response);
                socket1.sendMsgToServer(response);
                socket2.sendMsgToServer(response);
                System.exit(0);
            } else {
                response = "Not enough cards. Reshuffling P1's discard pile into their hand\n";
                socket1.sendMsgToServer(response);
                p1hand.combine(p1discard);
                p1hand.shuffle();
            }

        }
        if (p2hand.length() < cardsNeeded) {
            if ((p2hand.length() + p2discard.length()) < cardsNeeded) {
                // game over
                response = "Game over. P2 ran out of cards and P1 wins!\n";
                System.out.print(response);
                socket1.sendMsgToServer(response);
                socket2.sendMsgToServer(response);
                System.exit(0);
            } else {
                response = "Not enough cards. Reshuffling P1's discard into their hand\n";
                socket2.sendMsgToServer(response);
                p2hand.combine(p2discard);
                p2hand.shuffle();
            }
        }
    }
}
