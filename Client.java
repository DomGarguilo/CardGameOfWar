import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

class Client {

	private static String host = "localhost";
	private static Integer port = 1337;
	private static String msgWelcome = "--- Welcome to the card game 'war'";
	private static String msgRules = "\nRule set:\n - Each player flips one card at a time\n - Whoevers card is the highest value takes both cards \n - "
			+ "If there is a tie, the players 'go to war' meaning: each player puts 3 cards face down and 1 face up\n - The face up card is compared and handled as stated above\n - "
			+ "Once you have no more cards in you hand, the second pile is reshuffled into your hand\n - The game is won once the other player runs out of cards\n";
	static Socket clientSocket;
	static DataOutputStream outToServer;
	static BufferedReader inFromServer;

	public static void main(String args[]) throws Exception {

		String input = "";
		String response;

		System.out.println(Client.msgWelcome);
		System.out.println(Client.msgRules);

		clientSocket = new Socket(Client.host, Client.port);
		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		System.out.println("Start the game by pressing enter");
		System.in.read();
		Client.send("C2S" + "\n"); // output test
		System.out.print("C2S sent\n");
		TimeUnit.SECONDS.sleep(1);
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
				// System.in.read();
				Client.send(input);
				System.out.print("\nYour card was successfully transmitted to the server."
						+ " Waiting on result from the server...");
			}

		}
		System.out.print("Client program ended\n");

		// Close socket
		clientSocket.close();

	}

	public static void send(String input) throws IOException {
		Client.outToServer.writeBytes(input);
		Client.outToServer.flush();
	}

	public static String recieve() throws IOException {
		return Client.inFromServer.readLine();
	}
}
