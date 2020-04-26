import java.io.*;
import java.net.*;
//import java.util.Scanner;
//import java.util.concurrent.TimeUnit;

class Client {

	private static String host = "localhost";
	private static Integer port = 1337;
	private static String msgWelcome = "--- Welcome to the card game 'war'";
	private static String msgRules = "\nRule set:\n - Each player flips one card at a time\n - Whoevers card is the highest value takes both cards \n - "
			+ "If there is a tie, the players 'go to war' meaning: each player puts 3 cards face down and 1 face up\n - The face up card is compared and handled as stated above\n - "
			+ "Once you have no more cards in you hand, the second pile is reshuffled into your hand\n - The game is won once the other player runs out of cards\n";
	private static Socket clientSocket;
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer;
	private static boolean autoFlip = false;// set to false to prompt client to send a card

	public static void main(String args[]) throws Exception {

		String input, response;

		System.out.println(Client.msgWelcome);
		System.out.println(Client.msgRules);

		// initializing the client socket, output to server and input from server
		clientSocket = new Socket(Client.host, Client.port);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		System.out.println("Start the game by pressing enter");
		System.in.read();
		Client.send("Client to server test" + "\n"); // output test
		System.out.print("C2S test sent\n");
		response = Client.recieve(); // input test
		System.out.print("Input from server test: " + response + "\n");

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
				if (!autoFlip) {
					System.in.read();
				}
				Client.send(input);
				System.out.print("\nYour card was successfully transmitted to the server."
						+ " Waiting on result from the server...");
			}

		}
		System.out.print("Client program ended\n");

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

	// asks user if they want their cards to be flipped automatically
	/*
	 * private static boolean getAuto() { Scanner scan = new Scanner(System.in);
	 * char temp; System.out.
	 * print("Would you like to have the cards automatically flipped? (Y/N)"); temp
	 * = Character.toUpperCase(scan.next().charAt(0)); while (temp != 'Y' && temp !=
	 * 'N') { System.out.print("error. please input either 'Y' or 'N'\n"); temp =
	 * Character.toUpperCase(scan.next().charAt(0)); } scan.close(); if (temp ==
	 * 'Y') { return true; } else { return false; }
	 * 
	 * 
	 * }
	 */
}
