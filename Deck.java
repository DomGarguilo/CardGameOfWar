import java.util.Collections;
import java.util.LinkedList;

public class Deck {
    private final LinkedList<String> deck;

    /**
     * Create a new empty Deck
     */
    public Deck() {
        this.deck = new LinkedList<>();
    }

    /**
     * @param card card to get value of
     * @return the numeric value of the given card
     */
    public static int getValue(String card) {
        return Integer.parseInt(card.replaceAll("([A-Z])", ""));
    }

    /**
     * Gets a String value from shorthand (e.g. 11H -> Jack of Hearts).
     *
     * @param card the card to return the string value of
     * @return String value of a card
     */
    public static String getString(String card) {
        String suit = String.valueOf(card.charAt(card.length() - 1));
        switch (suit) {
            case "H":
                suit = "Hearts";
                break;
            case "D":
                suit = "Diamonds";
                break;
            case "C":
                suit = "Clubs";
                break;
            case "S":
                suit = "Spades";
                break;
        }

        int num = getValue(card);
        String val = String.valueOf(num);
        switch (num) {
            case 1:
                val = "Ace";
                break;
            case 11:
                val = "Jack";
                break;
            case 12:
                val = "Queen";
                break;
            case 13:
                val = "King";
                break;
        }
        return val + " of " + suit;
    }

    /**
     * Adds a card to this Deck
     *
     * @param input card to add
     */
    public void push(String input) {
        this.deck.add(input);
    }

    /**
     * Removes the top card from this Deck and returns it
     *
     * @return the top card from this Deck
     */
    public String pop() {
        return this.deck.removeFirst();
    }

    /**
     * fill deck with card values 1-13 (Ace-King)
     */
    public void fill() {
        for (int i = 1; i <= 13; i++) {
            this.deck.push(i + "H"); // hearts
            this.deck.push(i + "D"); // diamonds
            this.deck.push(i + "S"); // spades
            this.deck.push(i + "C"); // clubs
        }
    }

    //shuffles a deck
    public void shuffle() {
        Collections.shuffle(this.deck);
    }

    public int length() {
        return this.deck.size();
    }

    /**
     * Adds all cards in other Deck to this Deck
     *
     * @param other Deck to add to this Deck
     */
    public void combine(Deck other) {
        this.deck.addAll(other.deck);
        other.deck.clear();
    }

}
