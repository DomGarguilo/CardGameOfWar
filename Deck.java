import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class Deck {
	private LinkedList<String> deck;
	
	public Deck(boolean version) {
		if(!version) { 	// false for empty
			this.deck = new LinkedList<String>();
		} else {		// true for full
			this.deck = new LinkedList<String>();
			this.fill();
			this.shuffle();
		}
	}
		
	public void push(String input) {
		this.deck.add(input);
	}
	
	public String pop() {
		return this.deck.removeFirst();
	}
	
	//fill deck with card values 1-14 (1-Ace)
	public void fill() {
		for(int i = 1; i <= 13; i++) {
			this.deck.push(i+"H"); //hearts
			this.deck.push(i+"D"); //diamonds
			this.deck.push(i+"S"); //spades
			this.deck.push(i+"C"); //clubs
		}
	}
	
	public void shuffle() {
		Collections.shuffle(this.deck);
	}
	
	public static int getValue(String card) {
		int result = 0;
		result = Integer.parseInt(card.replaceAll("([A-Z])", ""));
		return result;
	}
	
	public static int compare(String a, String b) {
		if(getValue(a) > getValue(b)) { 			//a>b
			return 1;
		} else if (getValue(a) < getValue(b)) {		//a<b
			return -1;
		} else {									//a==b
			return 0;
		}	
	}

	public int length() {
		return this.deck.size();
	}
	
	public void print() {
		System.out.println(Arrays.toString(this.deck.toArray()));
	}
	
	public void reShuffle(Deck d) {
		int deckLength = d.length();
		for(int i = 0; i < deckLength; i++) {
			this.deck.push(d.pop());
			this.shuffle();
		}
	}

}


