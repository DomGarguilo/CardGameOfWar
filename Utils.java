import java.util.Scanner;

public class Utils {

    /**
     * Prompts the user for a port number using the given Scanner.
     *
     * @param inputScanner Scanner to use to prompt the user
     * @param defaultPort  a port number the user can default to
     */
    public static int getPort(Scanner inputScanner, int defaultPort) {
        int input;

        do {
            System.out.print("\nPlease select a port by entering an integer value between 1 and 65535 or\n");
            System.out.print("insert \"0\" in order to continue with the default setting (" + defaultPort + "): ");
            input = inputScanner.nextInt();
        } while (input != 0 && !isValidPort(input));

        return input == 0 ? defaultPort : input;
    }

    /**
     * @param port number to check
     * @return true if the given number is a valid port number, else false
     */
    public static boolean isValidPort(int port) {
        return port >= 1 && port <= 65535;
    }

    static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }

}
