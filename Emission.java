import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class Emission {

	/* Data Structure for the words. Outermost HashMap takes the String of the sentiment ("0", "-2")
	 *  and returns the ArrayList of feature HashMaps (Adjective counts, Adverb counts, and 
	 *  Gerund counts in order of index) */
	private HashMap<String, ArrayList<HashMap<String, Double>>> sentiments= new HashMap<String, ArrayList<HashMap<String, Double>>>();
	/* List of adjective tags. */
	private ArrayList<String> adj= new ArrayList<String>(Arrays.asList("JJ","JJR","JJS"));
	/* List of verb tags. */
	private ArrayList<String> ver= new ArrayList<String>(Arrays.asList("VB","VBD","VBN","VBZ","VBP"));
	/* List of adverb tags. */
	private ArrayList<String> adv= new ArrayList<String>(Arrays.asList("RB","RBR","RBS"));
	/* List of noun tags. */
	private ArrayList<String> nou= new ArrayList<String>(Arrays.asList("NN","NNS", "NNP", "NNPS"));
	/* List of pronoun tags. */ 
	private ArrayList<String> pro= new ArrayList<String>(Arrays.asList("PRP", "PRP$"));
	/* Determiner tag. */
	private String det= "DT";
	/* Gerund tag. */
	private String ger= "VBG";
	/* Coordinating conjunction tag. */
	private String con= "CC";
	/* Subordinating conjunction tag. */
	private String sub= "IN";
	/* Interjection tag. */
	private String inj= "UH";
	/* Values for unseen data */
	private double[] DEFAULT = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	/* HashMap for sense count. */
	private HashMap<String, Double> sentimentCount= new HashMap<String, Double>();

	
	private String author = "";
	private ArrayList<String> scottGood = new ArrayList<String>(Arrays.asList("appealing", "comic", 
			"best", "good", "wonderful", "effective", "perfect"));
	private ArrayList<String> scottBad = new ArrayList<String>(Arrays.asList("few", "big", "moral", 
			"least", "impossible", "dead", "funny", "only", "central", "final", "bad", "such", "real", 
			"worse", "wrong", "absurd", "simple", "hard", "predictable", "little", "silly", "always", 
			"simply", "unfortunately", "yet", "enough", "long", "far", "generally", "maybe", "already", 
			"really", "actually", "almost", "vaguely", "perhaps", "away", "instead", "finds", "lost", 
			"play", "turn", "seem", "imagine", "appears", "feel", "feels", "seems", "want", "turns"));
	private ArrayList<String> dennisGood = new ArrayList<String>(Arrays.asList(
			"noir", "right", "enjoyable", "entertaining", "excellent", "new", "considerable", "superb", 
			"better", "tremendous", "emotional", "worth", "subject", "beautiful", "true", "subtle", "able", 
			"important", "human", "intelligent", "brilliant", "difficult", "american", "best", "young", 
			"original", "dark", "innovative", "natural", "outstanding", "certain", "visual", "modern", 
			"splendid", "perfect", "special", "well", "especially"));
	private List<String> dennisBad = new ArrayList<String>(Arrays.asList( "bad", "political", "really", "almost"));

	
	/* Create an instance of Emission. */
	public Emission(String authorName) {
		author = authorName;
	}
	
	/* Return sentiment data structure. */
	public HashMap<String, ArrayList<HashMap<String, Double>>> getSentiments() {
		return sentiments;
	}

	/* Returns the probability of the specified adjective, adverb, or gerund,
	 *  given the desired sentiment and pos tag. */
	public double calcProb(String sentiment, String pos, String word) {
		int index= 0; 
		//Part of speech weight, to be taken from Constants
		double posWeight= 1;
		
		// weight author specific words
		if (author == "dennis") {
			if ((sentiment == "1") || (sentiment == "2")) {
				if (dennisGood.contains(word)) {
					posWeight = posWeight*Constants.AUTHOR_GOOD;
				}
			}
			if ((sentiment == "-1") || (sentiment == "-2")) {
				if (dennisBad.contains(word)) {
					posWeight = posWeight*Constants.AUTHOR_BAD;
				}
			}
		} else if (author == "scott") {
			if ((sentiment == "1") || (sentiment == "2")) {
				if (scottGood.contains(word)) {
					posWeight = posWeight*Constants.AUTHOR_GOOD;
				}
			}
			if ((sentiment == "-1") || (sentiment == "-2")) {
				if (scottBad.contains(word)) {
					posWeight = posWeight*Constants.AUTHOR_BAD;
				}
			}
		}
		
		
		
		//Determines feature index given the word's pos. If unknown, defaults to 0
		if (adj.contains(pos)) {
			index= 0;
			if (pos=="JJS") {
				//Double-count superlative adjectives (best, coolest, etc.)
				posWeight= Constants.SUPER_MULT;
			} else {
				posWeight= Constants.ADJ_MULT;
			}
		}
		else if (adv.contains(pos)) {
			index= 1;
			posWeight= Constants.ADV_MULT;
		}
		else if (ger.equals(pos)) {
			index= 2;
			posWeight= Constants.GER_MULT;
		}
		else if (ver.contains(pos)) {
			index= 3;
			posWeight= Constants.VER_MULT;
		} 
		else if (nou.contains(pos)) {
			index= 4;
			posWeight= Constants.NOU_MULT;
		}
		else if (pro.contains(pos)) {
			index= 5;
			posWeight= Constants.PRO_MULT;
		}
		else if (det.equals(pos)) {
			index= 6;
			posWeight= Constants.DET_MULT;
		}
		else if (con.equals(pos)) {
			index= 7;
			posWeight= Constants.CON_MULT;
		} 
		else if (sub.equals(pos)) {
			index= 8;
			posWeight= Constants.SUB_MULT;
		}
		else if (inj.equals(pos)) {
			index= 9;
			posWeight= Constants.INJ_MULT;
		}
		else {
			index= 10;
			posWeight= Constants.WTF_MULT;
		}
		double b = sum(sentiments.get(sentiment).get(index).values());
		double a = sentiments.get(sentiment).get(index).containsKey(word) ? 
				sentiments.get(sentiment).get(index).get(word) : DEFAULT[index];
		if(a==0 || b==0){
			return 0;
		}
		//System.out.println(sentiments.get(sentiment).get(index).containsKey(word));
		//System.out.println(sentiment+" : "+pos+" : "+a +" : "+b +" : "+posWeight * a / b);
		return posWeight * a / b;
	}
	
	/* Computes the most likely sentiment given a sentence based on its gathered features. 
	public Double[] findSentiment(ArrayList<String> sentence, ArrayList<String> pos) {
		ArrayList<Double> sentProbs= new ArrayList<Double>(Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0));
		Double bestSentiment=null;
		double bestProbability= 0;
		//Runs through all 5 sentiments
		for(int i= -2; i<=2; i++) {
			//Calculates sentence probability as multiplication of all word probabilities
			for(int j= 0; j<sentence.size(); j++) {
				sentProbs.set(i+2, -Math.log(sentProbs.get(i+2))-Math.log(calcProb(i+"", pos.get(j), sentence.get(j))));
			}
			//Updates best probability
			if(sentProbs.get(i+2)>bestProbability) {
				bestSentiment= (double)i;
				bestProbability= sentProbs.get(i+2);
			}
		}
		Double[] result={bestSentiment, bestProbability};
		return result;
	} */
	
	/* Returns the probability of a sentence being the specified sentiment. */
	public double sentProb(String sentiment, ArrayList<String> sentence, ArrayList<String> pos) {
		double prob= 0;
		for (int i= 0; i<sentence.size(); i++) {
			//prob= 10*prob*calcProb(sentiment, pos.get(i), sentence.get(i));
			prob= prob+ep(calcProb(sentiment, pos.get(i), sentence.get(i)));

		}
		//System.out.println("End Prob: "+prob);
		//System.out.println(sentence);
		return prob;
	}
	
	private double ep(double calcProb) {
		double epsilon = .000001;
		return epsilon+Math.log(calcProb+epsilon);
	}

	/* Sums all values in the collection. */
	private double sum(Collection<Double> values) {
		double sum= 0.0; 
		for (Double d:values){
			sum += d;
		}
		return sum;
	}

	/* Updates pre-existing or creates new data as necessary in the sentiments data structure, 
	 * where sentence and pos are the words and pos of the sentence. */
	public void updTable(String sentiment, ArrayList<String> sentence, ArrayList<String> pos) {
		
		HashMap<String, Double> adjectives= null;
		HashMap<String, Double> adverbs= null;
		HashMap<String, Double> gerunds= null;
		HashMap<String, Double> verbs= null;
		HashMap<String, Double> nouns= null;
		HashMap<String, Double> pronouns= null;
		HashMap<String, Double> determiners= null;
		HashMap<String, Double> conjunctions= null;
		HashMap<String, Double> subordinates= null;
		HashMap<String, Double> interjections= null;
		HashMap<String, Double> remainder= null;
		// Get current or create new feature hashmaps by situation
		if (sentiments.containsKey(sentiment)) {
			//Get current hashmaps of specified sentiment
			adjectives = sentiments.get(sentiment).get(0);
			adverbs = sentiments.get(sentiment).get(1);
			gerunds = sentiments.get(sentiment).get(2);
			verbs= sentiments.get(sentiment).get(3);
			nouns= sentiments.get(sentiment).get(4);
			pronouns= sentiments.get(sentiment).get(5);
			determiners= sentiments.get(sentiment).get(6);
			conjunctions= sentiments.get(sentiment).get(7);
			subordinates= sentiments.get(sentiment).get(8);
			interjections= sentiments.get(sentiment).get(9);
			remainder= sentiments.get(sentiment).get(10);
		}
		else {
			// Create new hashmaps for new sentiment
			adjectives = new HashMap<String, Double>();
			adverbs = new HashMap<String, Double>();
			gerunds = new HashMap<String, Double>();
			verbs= new HashMap<String, Double>();
			nouns= new HashMap<String, Double>();
			pronouns= new HashMap<String, Double>();
			determiners= new HashMap<String, Double>();
			conjunctions= new HashMap<String, Double>();
			subordinates= new HashMap<String, Double>();
			interjections= new HashMap<String, Double>();
			remainder= new HashMap<String, Double>();
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
			} else if(ver.contains(pos.get(i))) {
				if (!verbs.containsKey(sentence.get(i))) {
					verbs.put(sentence.get(i), 1.0);
				} else {
					verbs.put(sentence.get(i), verbs.get(sentence.get(i))+1);
				}
			} else if(nou.contains(pos.get(i))) {
				if (!nouns.containsKey(sentence.get(i))) {
					nouns.put(sentence.get(i), 1.0);
				} else {
					nouns.put(sentence.get(i), nouns.get(sentence.get(i))+1);
				}
			} else if(pro.contains(pos.get(i))) {
				if (!pronouns.containsKey(sentence.get(i))) {
					pronouns.put(sentence.get(i), 1.0);
				} else {
					pronouns.put(sentence.get(i), pronouns.get(sentence.get(i))+1);
				}
			} else if(det.equals(pos.get(i))) {
				if (!determiners.containsKey(sentence.get(i))) {
					determiners.put(sentence.get(i), 1.0);
				} else {
					determiners.put(sentence.get(i), determiners.get(sentence.get(i))+1);
				}
			} else if(con.equals(pos.get(i))) {
				if (!conjunctions.containsKey(sentence.get(i))) {
					conjunctions.put(sentence.get(i), 1.0);
				} else {
					conjunctions.put(sentence.get(i), conjunctions.get(sentence.get(i))+1);
				}
			} else if(sub.equals(pos.get(i))) {
				if (!subordinates.containsKey(sentence.get(i))) {
					subordinates.put(sentence.get(i), 1.0);
				} else {
					subordinates.put(sentence.get(i), subordinates.get(sentence.get(i))+1);
				}
			} else if(inj.equals(pos.get(i))) {
				if (!interjections.containsKey(sentence.get(i))) {
					interjections.put(sentence.get(i), 1.0);
				} else {
					interjections.put(sentence.get(i), interjections.get(sentence.get(i))+1);
				}
			} else {
				if (!remainder.containsKey(sentence.get(i))) {
					remainder.put(sentence.get(i), 1.0);
				} else {
					remainder.put(sentence.get(i), remainder.get(sentence.get(i))+1);
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
		features.add(verbs);
		features.add(nouns);
		features.add(pronouns);
		features.add(determiners);
		features.add(conjunctions);
		features.add(subordinates);
		features.add(interjections);
		features.add(remainder);
		sentiments.put(sentiment, features);
	}

}
