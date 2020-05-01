import java.io.*;
import java.net.*;
import java.util.Scanner;

class Client {

	private static String host = "localhost";
	private static int port = 1337;
	private static String msgWelcome = "--- Welcome to the card game 'war'\n";
	private static String msgRules = "\nRule set:\n - Each player flips one card at a time\n - Whoevers card is the highest value takes both cards \n - "
			+ "If there is a tie, the players 'go to war' meaning: each player puts 3 cards face down and 1 face up\n - The face up card is compared and handled as stated above\n - "
			+ "Once you have no more cards in you hand, the second pile is reshuffled into your hand\n - The game is won once the other player runs out of cards\n\n";
	private static Socket clientSocket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;
	private static Scanner sc;

	public static void main(String args[]) throws Exception {

		String input, response;

		System.out.print(Client.msgWelcome);
		System.out.print(Client.msgRules);

		sc = new Scanner(System.in);
		Client.host = Client.getHost();
		Client.port = Client.getPort();

		// set to false to prompt client to send a card
		boolean autoFlip = getAuto();

		// initializing the client socket, output to server and input from server
		clientSocket = new Socket("192.168.86.133", Client.port);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		/*
		 * Client.send("Client to server test" + "\n"); // output test
		 * System.out.print("\nClient to server test msg sent\n"); response =
		 * Client.recieve(); // input test
		 * System.out.print("Test msg from server recieved: " + response + "\n");
		 */

		System.out.print("Ready to begin. Press enter to start game");
		System.in.read();

		// Constantly listening for messages from the server
		while ((response = Client.recieve()) != null) {

			// Print response
			System.out.print("\nServer: " + response);

			// Check if game has ended
			if (response.contains("Game over.")) {
				break;
			}
			if (response.contains("press enter")) {
				// Tell server to flip next card
				input = "send card\n";
				if (autoFlip == false) {
					System.in.read();
				}
				Client.send(input);
				System.out.print("\nYour card was successfully transmitted to the server."
						+ " Waiting on result from the server...");
			}

		}
		System.out.print("\nClient program ended\n");

		// Close scanner
		sc.close();

		// Close socket
		clientSocket.close();

	}

	// method to send Strings to the server
	private static void send(String input) throws IOException {
		Client.outToServer.writeBytes(input);
		Client.outToServer.flush();
	}

	// method to receive messages from the server
	private static String recieve() throws IOException {
		return Client.inFromServer.readLine();
	}

	// checks that input port number is valid
	public static boolean validPort(Integer x) {
		return x >= 1 && x <= 65535 ? true : false;
	}

	// gets port from user
	public static int getPort() {
		int input;

		do {
			System.out.print("\nPlease select a port by entering an integer value between 1 and 65535 or\n");
			System.out.print("insert \"0\" in order to continue with the default setting (" + Client.port + "): ");
			input = sc.nextInt();

		} while (input != 0 && !Client.validPort(input));

		return input == 0 ? Client.port : input;
	}

	// gets port from user
	public static String getHost() {
		String input;

		System.out.print("Please enter the servers ip address or\n");
		System.out.print("insert \"0\" to connect to server running on this machine (localhost): ");
		input = sc.next();

		return input == "0" ? Client.host : input;
	}

	public static boolean getAuto() {
		char input;
		boolean result = false;
		do {
			System.out.print("\nDo you want to enable automatic card flipping? (y/n):");
			input = sc.next().charAt(0);
			input = Character.toUpperCase(input);
			if (input == 'Y') {
				result = true;
				System.out.print("Autoflip enabled\n");
			}
			if (input == 'N') {
				result = false;
				System.out.print("Autoflip disabled\n");
			}
		} while (input != 'Y' && input != 'N');
		return result;
	}

}
