import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {

	private static Deck master;
	private static Deck p1;
	private static Deck p2;
	private static Deck p1discard;
	private static Deck p2discard;
	private static ServerSocket welcomeSocket;
	private static Socket client_1, client_2;
	private static DataOutputStream outClient_1, outClient_2;
	private static BufferedReader inClient_1, inClient_2;
	private static Integer port = 1337;
	private static String welcomeMsg = "Welcome to the card game 'War'.";

	public static void main(String[] args) throws InterruptedException, IOException {

		// initialize the decks
		master = new Deck(true);
		p1 = new Deck(false);
		p2 = new Deck(false);
		p1discard = new Deck(false);
		p2discard = new Deck(false);

		// Printing welcome message
		System.out.println(Server.welcomeMsg);

		// Setting up server socket on specified port
		Server.port = Server.getPort();
		Server.welcomeSocket = new ServerSocket(Server.port);
		System.out.println("\nOk, we're up and running on port " + Server.welcomeSocket.getLocalPort() + " ...");

		// Dealing cards to each player
		for (int i = 0; i < 26; i++) { // deal first half of the deck to p1
			Server.p1.push(Server.master.pop());
		}
		for (int i = 0; i < 26; i++) { // deal first half of the deck to p1
			Server.p2.push(Server.master.pop());
		}

		// Player one socket, data in, and data out set up
		Server.client_1 = Server.welcomeSocket.accept();
		if (Server.client_1.isConnected()) {
			System.out.println("\nPlayer one (" + (Server.client_1.getLocalAddress().toString()).substring(1) + ":"
					+ Server.client_1.getLocalPort() + ") has joined ... waiting for player two ...");
		}
		Server.outClient_1 = new DataOutputStream(Server.client_1.getOutputStream());
		Server.inClient_1 = new BufferedReader(new InputStreamReader(Server.client_1.getInputStream()));

		System.out.println(Server.recieve(1)); // input test
		Server.send("Server to client 1\n", 1); // output test
		System.out.print("S2C1 sent, P1 ready\n");

		// Player two socket, data in, and data out set up
		Server.client_2 = Server.welcomeSocket.accept();
		if (Server.client_2.isConnected()) {
			System.out.println("Player two (" + (Server.client_2.getLocalAddress().toString()).substring(1) + ":"
					+ Server.client_1.getLocalPort() + ") has joined ... lets start ...");
		}
		Server.outClient_2 = new DataOutputStream(Server.client_2.getOutputStream());
		Server.inClient_2 = new BufferedReader(new InputStreamReader(Server.client_2.getInputStream()));

		// Testing input and output to and from client
		System.out.println(Server.recieve(2)); // input test
		Server.send("Server to client 2\n", 2); // output test
		System.out.print("S2C2 sent, P2 ready\n");

		String temp = "Game starting\n";
		System.out.print(temp);
		Server.send(temp, 3);

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

			p1Send = "P1, press enter to flip card. You have " + Server.p1.length() + " cards in hand, "
					+ Server.p1discard.length() + " cards in discard pile.\n";
			p2Send = "P2, press enter to flip card. You have " + Server.p2.length() + " cards in hand, "
					+ Server.p2discard.length() + " cards in discard pile.\n";

			// send message to client
			Server.send(p1Send, 1);
			Server.send(p2Send, 2);

			// print message to server
			System.out.print(p1Send + p2Send);

			// wait for response
			Server.recieve(1);
			Server.recieve(2);

			// flip top card on deck
			String p1in = Server.p1.pop();
			String p2in = Server.p2.pop();

			// compare the cards
			whoWins(p1in, p2in);
		}

		// close sockets
		Server.client_1.close();
		Server.client_2.close();
		Server.welcomeSocket.close();

	}

	// sends strings to client sockets
	// input the msg as String
	// input int n: 1 to send to P1, 2 for P2 and 3 to send to both
	public static void send(String input, int n) throws IOException {
		if (n == 1) {
			Server.outClient_1.writeBytes(input);
			Server.outClient_1.flush();
		} else if (n == 2) {
			Server.outClient_2.writeBytes(input);
			Server.outClient_2.flush();
		} else if (n == 3) {
			Server.send(input, 1);
			Server.send(input, 2);
		} else {
			System.out.print("Error in Server.send");
		}
	}

	// receives input from specified client
	public static String recieve(int n) throws IOException {
		if (n == 1) {
			return Server.inClient_1.readLine();
		} else if (n == 2) {
			return Server.inClient_2.readLine();
		} else {
			return "Error in Server.recieve";
		}
	}

	// function to determine which player wins a round
	// input: 2 strings to represent the cards
	// output: boolean 1 for P1 win and 0 for P2 win
	public static boolean whoWins(String p1in, String p2in) throws IOException {

		// print result to server and both clients
		String response = "P1 flipped " + Deck.getString(p1in) + " and P2 flipped " + Deck.getString(p2in) + "\n";
		System.out.print(response);
		Server.send(response, 3);

		// compare the cards to see which wins
		int result = Deck.compare(p1in, p2in);

		if (result == -1) { // if P2's card is higher value
			response = "P2 wins\n\n";
			System.out.print(response);
			Server.send(response, 3);
			Server.p2discard.push(p1in);
			Server.p2discard.push(p2in);
			return true;
		} else if (result == 1) { // if P2's card is higher value
			response = "P1 wins\n\n";
			System.out.print(response);
			Server.send(response, 3);
			Server.p1discard.push(p1in);
			Server.p1discard.push(p2in);
			return false;
		} else { // if the cards tie then the players enter war
			// print to server and both clients
			response = "Tie! Time for war!\n";
			System.out.print(response);
			Server.send(response, 3);

			endGameCheck(4); // check if both players have enough cards for war

			// prompt players to flip their cards
			response = "P1, press enter to start war. You have " + Server.p1.length() + " cards in hand, "
					+ Server.p1discard.length() + " cards in discard pile.\n";
			System.out.print(response);
			Server.send(response, 1);
			response = Server.recieve(1);
			// flip 4 cards from P1
			String[] temp1 = new String[4];
			temp1[0] = Server.p1.pop();
			temp1[1] = Server.p1.pop();
			temp1[2] = Server.p1.pop();
			temp1[3] = Server.p1.pop();
			response = "P2, press enter to start war. You have " + Server.p2.length() + " cards in hand, "
					+ Server.p2discard.length() + " cards in discard pile.\n";
			System.out.print(response);
			Server.send(response, 2);
			response = Server.recieve(2);
			// flip 4 cards from P1
			String[] temp2 = new String[4];
			temp2[0] = Server.p2.pop();
			temp2[1] = Server.p2.pop();
			temp2[2] = Server.p2.pop();
			temp2[3] = Server.p2.pop();
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

	// checks to see whether both players have enough cards to continue play
	public static void endGameCheck(int n) throws IOException {
		String response;
		if (Server.p1.length() < n) {
			if ((Server.p1.length() + Server.p1discard.length()) < n) {
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
				response = "Not enough cards. Reshuffling P1's discard into thier hand";
				Server.send(response, 1);
				Server.p1.reShuffle(Server.p1discard);
			}

		}
		if (Server.p2.length() < n) {
			if ((Server.p2.length() + Server.p2discard.length()) < n) {
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
				response = "Not enough cards. Reshuffling P1's discard into thier hand";
				Server.send(response, 2);
				Server.p2.reShuffle(Server.p2discard);
			}
		}
	}

	// checks that input port number is valid
	public static boolean validPort(Integer x) {
		return x >= 1 && x <= 65535 ? true : false;
	}

	// gets port from user
	public static int getPort() {
		Integer input;
		Scanner sc = new Scanner(System.in);

		do {
			System.out.print("Please select a port by entering an integer value between 1 and 65535 or\n");
			System.out.print("insert \"0\" in order to continue with the default setting (" + Server.port + "): ");
			input = sc.nextInt();

		} while (input != 0 && !Server.validPort(input));

		sc.close();

		return input == 0 ? Server.port : input;
	}
}
