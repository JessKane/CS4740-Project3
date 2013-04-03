import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class Parse {

	/* Bigram model for transition frequencies */
	private HashMap<String,HashMap<String, Double>> transitions = new HashMap<String,HashMap<String, Double>>();

	/* TODO: Model for emission frequencies */
	private Object emissions;

	/* Sentiment list */
	private ArrayList<String> sentimentList = new ArrayList<String>(Arrays.asList("<-2>", "<-1>", "<0>", "<1>", "<2>"));
	/* Stanford NLP modelling pipeline, used in annotation. */
	protected StanfordCoreNLP pipeline;
	/* Train data location. */
	public static String trainLoc= "./ScottRenshaw_train.txt";

	public static void main(String[] args) {

		Parse pt= new Parse(trainLoc);

	}

	/* Constructs a Parse object with transition frequencies */
	public Parse(String trainLoc) {
		readFile(trainLoc);
	}

	/* Reads through the file and generates a sentiment bigram model */
	/* TODO: POS tagging, etc */
	private void readFile(String fileLoc) {

		ArrayList<String> sentiments = new ArrayList<String>();

		// Lists of sentences and their pos's. If I did this right, ArrayList sentences should be constructed
		// so that sentences.get(i) corresponds to sentiments.get(i). This might be useful for calculating
		// emissions probabilities. If not, feel free to disregard.
		ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> poss = new ArrayList<ArrayList<String>>();

		/* I left this here in case I missed something; should be useless now though
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
		*/

		// Create StanfordCoreNLP object properties, with POS tagging and tokenization
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos");
		// StanfordCoreNLP loads a lot of models, so try to only do this once
		this.pipeline = new StanfordCoreNLP(props);
		// ArrayLists to store the words and pos's on the current line
		ArrayList<String> sentPos= null;
		ArrayList<String> sentence= null;
		BufferedReader br= null;
		try{
			br= new BufferedReader(new FileReader(fileLoc));
			while(true){
				// Read current line, skipping over if a review header [xxxx/xx],
				// and breaking the loop if the end has been reached
				String line= br.readLine();
				if(line.length()==0 || line.charAt(line.length()-1) != '>') line= br.readLine();
				if(line==null) break;
				sentPos= new ArrayList<String>();
				sentence= new ArrayList<String>();
				// Stanford stuff
				Annotation lineAnno= new Annotation(line);
				this.pipeline.annotate(lineAnno);
				List<CoreLabel> tokens = lineAnno.get(TokensAnnotation.class);
				// Iterate over all of the tokens on a line
				for (CoreLabel token: tokens) {
					if (token.value().equals("{}")) {
						// Do nothing
					}
					//Add sentiment rating to sentiments
					if(sentimentList.contains(token.value())) {
						sentiments.add(token.value());
					} else {
						sentPos.add(token.tag());
						sentence.add(token.value());
					}
				}
				sentences.add(sentence);
				poss.add(sentPos);
				//System.out.println(sentPos);
				//System.out.println(sentence);
			}
			br.close();
		} catch(FileNotFoundException f) {
			System.out.println("File not found.");
		} catch(IOException io) {
		}

		// Generate sentiment bigrams
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
