import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class Parse {
  
	/* Bigram model for transition frequencies */
	private HashMap<String,HashMap<String, Double>> transitions = new HashMap<String,HashMap<String, Double>>();
	
	/* TODO: Model for emission frequencies */
	private Object emissions;
	
	/* Sentiment list */
	private ArrayList<String> sentimentList = new ArrayList<String>(Arrays.asList("<-2>", "<-1>", "<0>", "<1>", "<2>"));
	
	/* Constructs a Parse object with transition frequencies */
	public Parse(String trainLoc) throws IOException {
		readFile(trainLoc);
	}
	
	/* Reads through the file and generates a sentiment bigram model */
	/* TODO: POS tagging, etc */
	private void readFile(String fileLoc) throws IOException {
		
		ArrayList<String> sentiments = new ArrayList<String>();
		
		// List of sentences. If I did this right, ArrayList sentences should be constructed
		// so that sentences.get(i) corresponds to sentiments.get(i). This might be useful for calculating
		// emissions probabilities. If not, feel free to disregard.
		ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();
		
		DocumentPreprocessor doc= new DocumentPreprocessor(fileLoc);
		for(List<HasWord> sent : doc){
			ArrayList<String> sentence = new ArrayList<String>();
			for(HasWord elem : sent) {
				if (elem.word().equals("{}")) {
					// Do nothing
				}
				if(sentimentList.contains(elem.word())) {
					sentiments.add(elem.word());
				} else {
					sentence.add(elem.word());
				}
			}
			sentences.add(sentence);
		}
		
		for (int i=0; i<sentiments.size()-1; i++){
			String current = sentiments.get(i);
			String next = sentiments.get(i+1);
			if (!transitions.containsKey(current)){
				HashMap<String, Double> sub = new HashMap<String,Double>();
				sub.put(next, 1.0);
				transitions.put(current, sub);						
			} else if (!transitions.get(current).containsKey(next)){
				HashMap<String, Double> sub = transitions.get(current);
				sub.put(next, 1.0);
				transitions.put(current, sub);
			} else {
				HashMap<String, Double> sub = transitions.get(current);
				Double count = transitions.get(current).get(next) + 1.0;
				sub.put(next, count);
				transitions.put(current, sub);
			}
		}		
	}

	/* Returns the count for the bigram sentiment1 sentiment2 */
	public double findCount(String sentiment1, String sentiment2) {
		if (transitions.containsKey(sentiment1) && transitions.get(sentiment1).containsKey(sentiment2)) {
			return transitions.get(sentiment1).get(sentiment2);
		}
		else return 0.0;
	}
	
	/* Returns the probability for the bigram sentiment1 sentiment2 */
	public double findPercent(String sentiment1, String sentiment2){
		if (transitions.containsKey(sentiment1) && transitions.get(sentiment1).containsKey(sentiment2)) {
			return (double)transitions.get(sentiment1).get(sentiment2)/(double)sum(transitions.get(sentiment1).values());
		}
		
		else return 0.0; 
	}
	
	/* Sums a collection. */
	private double sum(Collection<Double> values) {
		double sum= 0.0; 
		for (Double i:values)
			sum = sum + i;
		return sum;
	}
}
