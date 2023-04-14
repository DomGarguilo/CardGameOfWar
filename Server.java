import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

public class Server {

    private static Deck player1hand;
    private static Deck player2hand;
    private static Deck player1discardPile;
    private static Deck player2discardPile;

    private static final String WELCOME_MSG = "Welcome to the card game 'War'.\n";

    public static void main(String[] args) throws Exception {

        // Printing welcome message
        System.out.println(WELCOME_MSG);

        // initialize the decks
        player1hand = new Deck();
        player2hand = new Deck();

        player1discardPile = new Deck();
        player2discardPile = new Deck();

        final Deck gameDeck = new Deck();
        gameDeck.fill();
        gameDeck.shuffle();

        // Dealing half of the cards to each player
        for (int j = 0; j < 52; j++) {
            if (j % 2 == 0) {
                player1hand.push(gameDeck.pop());
            } else {
                player2hand.push(gameDeck.pop());
            }
        }
        Utils.assertTrue(player1hand.length() == player2hand.length(), "P1 and P2 had unequal hands after dealing");

        printServerIP();

        // Ask user which port they want to start the server on
        int port;
        try (Scanner scanner = new Scanner(System.in)) {
            port = Utils.getPort(scanner, 1337);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("\nServer is running on port: " + serverSocket.getLocalPort() + ". Waiting for clients to connect");

            SocketConnection clientSocket1 = new SocketConnection(serverSocket);
            if (clientSocket1.socketIsConnected()) {
                System.out.println("\nPlayer one (" + (clientSocket1.getAddress()).substring(1)
                        + ") has joined, waiting for player two.");
            }

            SocketConnection clientSocket2 = new SocketConnection(serverSocket);
            if (clientSocket2.socketIsConnected()) {
                System.out.println("Player two (" + (clientSocket2.getAddress()).substring(1)
                        + ") has joined. Starting game.");
            }

            final String startingGame = "Game starting\n";
            System.out.print(startingGame);
            clientSocket1.sendMsgToServer(startingGame);
            clientSocket2.sendMsgToServer(startingGame);

            int roundNumber = 0; // round counter
            try {
                // Main game loop
                while (true) {
                    // print round # to server and both clients
                    String roundMsg = "ROUND #" + roundNumber + "\n";
                    clientSocket1.sendMsgToServer(roundMsg);
                    clientSocket2.sendMsgToServer(roundMsg);
                    System.out.print(roundMsg);
                    roundNumber++;

                    endGameCheck(1, clientSocket1, clientSocket2); // check if end game conditions are met

                    String player1Msg = "P1, press enter to flip card. You have " + player1hand.length() + " cards in hand, "
                            + player1discardPile.length() + " cards in discard pile.\n";
                    String player2Msg = "P2, press enter to flip card. You have " + player2hand.length() + " cards in hand, "
                            + player2discardPile.length() + " cards in discard pile.\n";

                    // send message to client
                    clientSocket1.sendMsgToServer(player1Msg);
                    clientSocket2.sendMsgToServer(player2Msg);

                    // print message to server
                    System.out.print(player1Msg + player2Msg);

                    // wait for response
                    clientSocket1.receiveMsgFromServer();
                    clientSocket2.receiveMsgFromServer();

                    // flip top card on deck
                    String player1Card = player1hand.pop();
                    String player2Card = player2hand.pop();

                    // compare the cards
                    whoWins(player1Card, player2Card, clientSocket1, clientSocket2);
                }
            } catch (GameOverException gameOverException) { // expected to happen when the game ends
                System.out.println(gameOverException.getMessage());
            } finally {
                clientSocket1.close();
                clientSocket2.close();
                System.out.println("Client SocketConnection objects closed");
            }
        } finally {
            System.out.println("ServerSocket closed");
        }

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
     * Determine which player wins a round
     *
     * @param player1Card first player to assess
     * @param player2Card second player to assess
     * @return true if player 2 wins, else false
     * @throws GameOverException when one of the players win
     */
    public static boolean whoWins(String player1Card, String player2Card, SocketConnection socket1, SocketConnection socket2) throws IOException, GameOverException {

        // print result to server and both clients
        String message = "P1 flipped " + Deck.getString(player1Card) + " and P2 flipped " + Deck.getString(player2Card) + "\n";
        System.out.print(message);
        socket1.sendMsgToServer(message);
        socket2.sendMsgToServer(message);

        // compare the cards to see which wins
        int result = Integer.compare(Deck.getValue(player1Card), Deck.getValue(player2Card));

        if (result < 0) { // if P2's card is higher value
            message = "P2 wins\n\n";
            System.out.print(message);
            socket1.sendMsgToServer(message);
            socket2.sendMsgToServer(message);
            player2discardPile.push(player1Card);
            player2discardPile.push(player2Card);
            return true;
        } else if (result > 0) { // if P1's card is higher value
            message = "P1 wins\n\n";
            System.out.print(message);
            socket1.sendMsgToServer(message);
            socket2.sendMsgToServer(message);
            player1discardPile.push(player1Card);
            player1discardPile.push(player2Card);
            return false;
        } else { // if the cards tie then the players enter war
            return handleWar(socket1, socket2);
        }
    }

    private static boolean handleWar(SocketConnection socket1, SocketConnection socket2) throws IOException, GameOverException {
        // print to server and both clients
        String message = "Tie! Time for war!\n";
        System.out.print(message);
        socket1.sendMsgToServer(message);
        socket2.sendMsgToServer(message);

        endGameCheck(4, socket1, socket2); // check if both players have enough cards for war

        // prompt player 1 to flip their cards for war
        message = "P1, press enter to start war. You have " + player1hand.length() + " cards in hand, "
                + player1discardPile.length() + " cards in discard pile.\n";
        System.out.print(message);
        socket1.sendMsgToServer(message);
        socket1.receiveMsgFromServer();

        // flip 4 cards from P1
        String[] player1cards = flipCards(player1hand, 4);

        // prompt player 2 to flip their cards for war
        message = "P2, press enter to start war. You have " + player2hand.length() + " cards in hand, "
                + player2discardPile.length() + " cards in discard pile.\n";
        System.out.print(message);
        socket2.sendMsgToServer(message);
        socket2.receiveMsgFromServer();

        // flip 4 cards from P2
        String[] player2cards = flipCards(player2hand, 4);

        // compare the top card from each player then distribute cards to winner
        boolean whoWins = whoWins(player1cards[3], player2cards[3], socket1, socket2);
        if (!whoWins) {
            for (int i = 0; i < 4; i++) {
                player1discardPile.push(player1cards[i]);
                player1discardPile.push(player2cards[i]);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                player2discardPile.push(player1cards[i]);
                player2discardPile.push(player2cards[i]);
            }
        }
        return whoWins;
    }

    private static String[] flipCards(Deck deck, int count) {
        String[] cards = new String[count];
        for (int i = 0; i < count; i++) {
            cards[i] = deck.pop();
        }
        return cards;
    }

    /**
     * checks to see whether both players have enough cards to continue play
     *
     * @param cardsNeeded the number of cards needed to continue play
     */
    public static void endGameCheck(int cardsNeeded, SocketConnection socket1, SocketConnection socket2) throws IOException, GameOverException {
        String response;
        if (player1hand.length() < cardsNeeded) {
            if ((player1hand.length() + player1discardPile.length()) < cardsNeeded) {
                response = "Game over. P1 ran out of cards and P2 wins!\n";
                socket1.sendMsgToServer(response);
                socket2.sendMsgToServer(response);
                throw new GameOverException(response);
            } else {
                response = "Not enough cards. Reshuffling P1's discard pile into their hand\n";
                socket1.sendMsgToServer(response);
                player1hand.combine(player1discardPile);
                player1hand.shuffle();
            }

        }
        if (player2hand.length() < cardsNeeded) {
            if ((player2hand.length() + player2discardPile.length()) < cardsNeeded) {
                response = "Game over. P2 ran out of cards and P1 wins!\n";
                socket1.sendMsgToServer(response);
                socket2.sendMsgToServer(response);
                throw new GameOverException(response);
            } else {
                response = "Not enough cards. Reshuffling P1's discard into their hand\n";
                socket2.sendMsgToServer(response);
                player2hand.combine(player2discardPile);
                player2hand.shuffle();
            }
        }
    }
}
