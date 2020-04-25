import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {

	static Deck master = new Deck(true);
	static Deck p1 = new Deck(false);
	static Deck p2 = new Deck(false);
	static Deck p1discard = new Deck(false);
	static Deck p2discard = new Deck(false);
	static ServerSocket welcomeSocket;
	static Socket client_1;
	static Socket client_2;
	static DataOutputStream outClient_1;
	static BufferedReader inClient_1;
	static DataOutputStream outClient_2;
	static BufferedReader inClient_2;
	private static Integer port = 1337;
	private static String welcomeMsg = "Welcome to the card game 'War'.";

	private static boolean validPort(Integer x) {
		return x >= 1 && x <= 65535 ? true : false;
	}

	private static int getPort() {
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

	public static void main(String[] args) throws IOException, InterruptedException {

		System.out.println(Server.welcomeMsg);

		Server.port = Server.getPort();
		Server.welcomeSocket = new ServerSocket(Server.port);
		System.out.println("\nOk, we're up and running on port " + Server.welcomeSocket.getLocalPort() + " ...");

		for (int i = 0; i < 26; i++) { // deal first half of the deck to p1
			Server.p1.push(Server.master.pop());
		}
		for (int i = 0; i < 26; i++) { // deal first half of the deck to p1
			Server.p2.push(Server.master.pop());
		}

		// Player one set up
		Server.client_1 = Server.welcomeSocket.accept();
		if (Server.client_1.isConnected()) {
			System.out.println("\nPlayer one (" + (Server.client_1.getLocalAddress().toString()).substring(1) + ":"
					+ Server.client_1.getLocalPort() + ") has joined ... waiting for player two ...");
		}
		Server.outClient_1 = new DataOutputStream(Server.client_1.getOutputStream());
		Server.inClient_1 = new BufferedReader(new InputStreamReader(Server.client_1.getInputStream()));

		System.out.println(Server.recieve(1)); // input test
		Server.send("S2C1\n", 1); // output test
		System.out.print("S2C1 sent, P1 ready\n");

		// Player two set up
		Server.client_2 = Server.welcomeSocket.accept();
		if (Server.client_2.isConnected()) {
			System.out.println("Player two (" + (Server.client_2.getLocalAddress().toString()).substring(1) + ":"
					+ Server.client_1.getLocalPort() + ") has joined ... lets start ...");
		}
		Server.outClient_2 = new DataOutputStream(Server.client_2.getOutputStream());
		Server.inClient_2 = new BufferedReader(new InputStreamReader(Server.client_2.getInputStream()));

		System.out.println(Server.recieve(2)); // input test
		Server.send("S2C2\n", 2); // output test
		System.out.print("S2C2 sent, P2 ready\n");

		String temp = "Game starting\n";
		System.out.print(temp);
		Server.send(temp, 3);

		boolean game = true;
		int count = 0;
		String p1Send;
		String p2Send;
		String round;
		
		// start game
		while (game) {
			round = "ROUND #" + count + "\n";
			Server.send(round, 3);
			System.out.print(round);
			count++;
			endGameCheck(1);

			p1Send = "P1, press enter to flip card. You have " + Server.p1.length() + " cards in hand, "
					+ Server.p1discard.length() + " cards in discard pile.\n";

			p2Send = "P2, press enter to flip card. You have " + Server.p2.length() + " cards in hand, "
					+ Server.p2discard.length() + " cards in discard pile.\n";

			// send message to client
			Server.send(p1Send, 1);
			Server.send(p2Send, 2);

			System.out.print(p1Send);
			System.out.print(p2Send);

			// wait for response
			System.out.println(Server.recieve(1));
			System.out.println(Server.recieve(2));

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

	public static boolean whoWins(String p1in, String p2in) throws IOException {

		String response = "P1 flipped " + p1in + " and P2 flipped " + p2in + "\n";
		System.out.print(response);
		Server.send(response, 3);

		int result = Deck.compare(p1in, p2in);

		if (result == -1) {
			response = "P2 wins\n\n";
			System.out.print(response);
			Server.send(response, 3);
			Server.p2discard.push(p1in);
			Server.p2discard.push(p2in);
			return true;
		} else if (result == 1) {
			response = "P1 wins\n\n";
			System.out.print(response);
			Server.send(response, 3);
			Server.p1discard.push(p1in);
			Server.p1discard.push(p2in);
			return false;
		} else {
			response = "Tie! Time for war!\n";
			System.out.print(response);
			Server.send(response, 3);
			endGameCheck(4);
			response = "P1, press enter to start war. You have " + Server.p1.length() + " cards in hand, "
					+ Server.p1discard.length() + " cards in discard pile.\n";
			System.out.print(response);
			Server.send(response, 1);
			response = Server.recieve(1);
			System.out.println(response);
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
			System.out.println(response);
			String[] temp2 = new String[4];
			temp2[0] = Server.p2.pop();
			temp2[1] = Server.p2.pop();
			temp2[2] = Server.p2.pop();
			temp2[3] = Server.p2.pop();
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

	public static void endGameCheck(int n) throws IOException {
		String response;
		if (Server.p1.length() < n) {
			if ((Server.p1.length() + Server.p1discard.length()) < n) {
				// game over
				response = "Game over. P1 ran out of cards and P2 wins! \n";
				System.out.print(response + "press enter to end the program.\n");
				Server.send(response, 3);
				System.in.read();
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
				response = "Game over. P2 ran out of cards and P1 wins! \n";
				System.out.print(response + "press enter to end the program.\n");
				Server.send(response, 3);
				System.in.read();
				System.exit(0);
			} else {
				response = "Not enough cards. Reshuffling P1's discard into thier hand";
				Server.send(response, 2);
				Server.p2.reShuffle(Server.p2discard);
			}
		}
	}

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

	public static String recieve(int n) throws IOException {
		if (n == 1) {
			return Server.inClient_1.readLine();
		} else if (n == 2) {
			return Server.inClient_2.readLine();
		} else {
			return "Error in Server.recieve";
		}
	}

}
