import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;


public class Emission {

	/* Data Structure for the words. Outermost HashMap takes the String of the sentiment ("0", "-2")
	 *  and returns the ArrayList of feature HashMaps (Adjective counts, Adverb counts, and 
	 *  Gerund counts in order of index) */
	private HashMap<String, ArrayList<HashMap<String, Double>>> sentiments= new HashMap<String, ArrayList<HashMap<String, Double>>>();
	/* Stoplist for finding words in the surrounding context. */
	private ArrayList<String> stopList= new ArrayList<String>(Arrays.asList("DT"));
	/* List of adjective tags. */
	private ArrayList<String> adj= new ArrayList<String>(Arrays.asList("JJ","JJR","JJS"));
	/* List of adverb tags. */
	private ArrayList<String> adv= new ArrayList<String>(Arrays.asList("RB","RBR","RBS"));
	/* Gerund tag. */
	private String ger= "VBG";
	/* Values for unseen data */
	private double[] DEFAULT = {0.0, 0.0, 0.0};
	/* HashMap for sense count. */
	private HashMap<String, Double> sentimentCount= new HashMap<String, Double>();

	/* Create an instance of Emission. */
	public Emission() {
		
	}
	
	/* Return sentiment data structure. */
	public HashMap<String, ArrayList<HashMap<String, Double>>> getSentiments() {
		return sentiments;
	}

	/* Returns the probability of the specified adjective, adverb, or gerund,
	 *  given the desired sentiment and pos tag. */
	public double calcProb(String sentiment, String pos, String word) {
		int index= 0;
		//Determines feature index given the word's pos. If unknown, defaults to 0
		if (adj.contains(pos)) index= 0;
		else if (adv.contains(pos)) index= 1;
		else if (ger.contains(pos)) index= 2;
		else return DEFAULT[index];
		double b = sum(sentiments.get(sentiment).get(index).values());
		double a = sentiments.get(sentiment).get(index).containsKey(word) ? 
				sentiments.get(sentiment).get(index).get(word) : DEFAULT[index];
		return a / b;	
	}
	
	/* Sums all values in the collection. */
	private double sum(Collection<Double> values) {
		double sum= 0.0; 
		for (Double d:values)
			sum += d;
		return sum;
	}

	/* Updates pre-existing or creates new data as necessary in the sentiments data structure, 
	 * where sentence and pos are the words and pos of the sentence. */
	public void updTable(String sentiment, ArrayList<String> sentence, ArrayList<String> pos) {
		
		HashMap<String, Double> adjectives= null;
		HashMap<String, Double> adverbs= null;
		HashMap<String, Double> gerunds= null;
		// Get current or create new feature hashmaps by situation
		if (sentiments.containsKey(sentiment)) {
			//Get current hashmaps of specified sentiment
			adjectives = sentiments.get(sentiment).get(0);
			adverbs = sentiments.get(sentiment).get(1);
			gerunds = sentiments.get(sentiment).get(2);
		}
		else {
			// Create new hashmaps for new sentiment
			adjectives = new HashMap<String, Double>();
			adverbs = new HashMap<String, Double>();
			gerunds = new HashMap<String, Double>();
		}
		ArrayList<HashMap<String,Double>> features = new ArrayList<HashMap<String,Double>>();
		
		//Update or create new hashmap information as necessary
		for (int i= 0; i<pos.size(); i++) {
			if(adj.contains(pos.get(i))) {
				if (!adjectives.containsKey(sentence.get(i))) {
					adjectives.put(sentence.get(i), 1.0);
				} else {
					adjectives.put(sentence.get(i), adjectives.get(sentence.get(i))+1);
				}
			} else if(adv.contains(pos.get(i))) {
				if (!adverbs.containsKey(sentence.get(i))) {
					adverbs.put(sentence.get(i), 1.0);
				} else {
					adverbs.put(sentence.get(i), adverbs.get(sentence.get(i))+1);
				}
			} else if(ger==pos.get(i)) {
				if (!gerunds.containsKey(sentence.get(i))) {
					gerunds.put(sentence.get(i), 1.0);
				} else {
					gerunds.put(sentence.get(i), gerunds.get(sentence.get(i))+1);
				}
			}
		}
		// Update sentiment count
		if(sentimentCount.containsKey(sentiment)) {
			sentimentCount.put(sentiment, sentimentCount.get(sentiment)+1.0);
		} else {
			sentimentCount.put(sentiment, 1.0);
		}

		//assemble feature vector
		features.add(adjectives);
		features.add(adverbs);
		features.add(gerunds);
		sentiments.put(sentiment, features);
	}

}
