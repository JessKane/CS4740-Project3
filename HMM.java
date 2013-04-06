import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class HMM {

	/* Bigram model for transition frequencies */
	private HashMap<String,HashMap<String, Double>> transitions = new HashMap<String,HashMap<String, Double>>();
	/* Emissions object, still working on this. */
	private Emission emissions= new Emission();

	/* List of strings = sentence. List of sentences = paragraph. List of paragraphs = review. List of reviews = entire document */
	private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> documents = null;
	private HashMap<ArrayList<String>, String> sentiments = null;		// "full sentence":"sentiment"

	private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> testDocuments = null;
	private HashMap<ArrayList<String>, String> testSentiments = null;		// "full sentence":"sentiment"


	/* Stanford NLP modelling pipeline, used in annotation. */
	protected StanfordCoreNLP pipeline;
	/* Train data location. */
	public static String trainLoc= "./ScottRenshaw_train.txt";

	/* Comment. */
	public static void main(String[] args) {

		HMM pt= new HMM(trainLoc);

	}

	/* Constructs a Parse object with transition frequencies */
	public HMM(String trainLoc) {
		readFile(trainLoc);
		sentimentModel(documents);
		System.out.println(emissions.getSentiments().get("-2").get(0).get("nice"));
		System.out.println(emissions.calcProb("-2","JJ","nice"));
	}

	/* Reads through the file and generates a sentiment bigram model, and updates
	 *  emission feature counts */
	public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> readFile(String fileLoc) {

		// Lists of sentences and their pos's. If I did this right, ArrayList sentences should be constructed
		// so that sentences.get(i) corresponds to sentiments.get(i). This might be useful for calculating
		// emissions probabilities. If not, feel free to disregard.
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = new ArrayList<ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();

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

				while(line.length()==0 || line.charAt(line.length()-1) != '>') {

					// If it's [xxxx/xx], switch documents
					if (line.charAt(line.length()-1) != '>') {
						documents.add(paragraphs);
						paragraphs = (ArrayList<ArrayList<ArrayList<String>>>) paragraphs.clone();
						paragraphs.clear();
						sentences = (ArrayList<ArrayList<String>>) sentences.clone();
						sentences.clear();					
					} 
					line= br.readLine();
					if(line==null) break;
				}
				if(line==null) break;
				sentPos= new ArrayList<String>();
				sentence= new ArrayList<String>();
				//Sentiment value for current line
				String sentiment= null;
				// Stanford stuff
				Annotation lineAnno= new Annotation(line);
				this.pipeline.annotate(lineAnno);
				List<CoreLabel> tokens = lineAnno.get(TokensAnnotation.class);
				// Iterate over all of the tokens on a line
				for (CoreLabel token: tokens) {
					if (token.value().equals("{}")) {
						// {} denotes a new paragraph
						paragraphs.add(sentences);
						sentences = (ArrayList<ArrayList<String>>) sentences.clone();
						sentences.clear();	
					} else {
						sentPos.add(token.tag());
						sentence.add(token.value());
					}
				}
				sentiment= sentence.get(sentence.size()-2);
				sentiments.put(sentence, sentiment);
				//Update sentiment tables
				emissions.updTable(sentiment, sentence, sentPos);
				sentences.add(sentence);
			}
			br.close();
		} catch(FileNotFoundException f) {
			System.out.println("File not found.");
		} catch(IOException io) {
		}

		return documents;		
	}
	
	/* Generates the sentiment bigrams, taking care to not treat last sentiment of a review + first sentiment of
	 * the next review as one bigram
	 */
	public void sentimentModel(ArrayList<ArrayList<ArrayList<ArrayList<String>>>> sectionedTxt) {
		for (ArrayList<ArrayList<ArrayList<String>>> review : sectionedTxt) {
			for (int i = 0; i < review.size(); i++) { // traversing review by paragraph
				for (int j = 0; j < review.get(i).size(); j++) { // traversing paragraph by sentence
					ArrayList<String> currSent = null;
					ArrayList<String> nextSent = null;
					if (i == review.size()-1 && j == review.get(i).size()-1) { // if last sentence of review
						continue;
					}
					else if (j != review.get(i).size()-1) { // if not last sentence of a paragraph
						currSent = review.get(i).get(j);
						nextSent = review.get(i).get(j+1);
					}
					else { // if last sentence of a paragraph
						currSent = review.get(i).get(j);
						nextSent = review.get(i+1).get(0); // next sentence is first sentence of next paragraph
					}
					
					String current = sentiments.get(currSent);
					String next = sentiments.get(nextSent);
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

	/**
	 * Parse the test data.
	 */
	public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> parseTestData (String filename) {
		// Lists of sentences and their pos's. If I did this right, ArrayList sentences should be constructed
		// so that sentences.get(i) corresponds to sentiments.get(i). This might be useful for calculating
		// emissions probabilities. If not, feel free to disregard.
		ArrayList<ArrayList<ArrayList<String>>> paragraphs = new ArrayList<ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();

		// ArrayLists to store the words and pos's on the current line
		ArrayList<String> sentPos= null;
		ArrayList<String> sentence= null;

		// Create StanfordCoreNLP object properties, with POS tagging
		Properties props;
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos");

		// StanfordCoreNLP loads a lot of models, so you probably
		// only want to do this once per execution
		this.pipeline = new StanfordCoreNLP(props);
		BufferedReader br = null;
		PrintWriter out = null;

		try {
			br= new BufferedReader(new FileReader(filename));
			out = new PrintWriter(new BufferedWriter(new FileWriter("./results.txt", true)));
		} catch(FileNotFoundException e) {
			System.out.println("That file doesn't exist. Try again.");
		} catch(IOException d) {
			System.out.println(d.getMessage());
		}		

		if (br != null && out != null) {
			// Reads through the test file line by line
			while(true) {

				// Read current line, skipping over if a review header [xxxx/xx],
				// and breaking the loop if the end has been reached
				String line = "";
				try {
					line = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				while(line.length()==0 || line.charAt(line.length()-1) != '>') {

					// If it's [xxxx/xx], switch documents
					if (line.charAt(line.length()-1) != '>') {
						testDocuments.add(paragraphs);
						paragraphs = (ArrayList<ArrayList<ArrayList<String>>>) paragraphs.clone();
						paragraphs.clear();
						sentences = (ArrayList<ArrayList<String>>) sentences.clone();
						sentences.clear();					
					} 

					try {
						line= br.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(line==null) break;
				}
				if(line==null) break;

				sentPos= new ArrayList<String>();
				sentence= new ArrayList<String>();
				//Sentiment value for current line
				String sentiment= null;
				// Stanford stuff
				Annotation lineAnno= new Annotation(line);
				this.pipeline.annotate(lineAnno);
				List<CoreLabel> tokens = lineAnno.get(TokensAnnotation.class);
				// Iterate over all of the tokens on a line
				for (CoreLabel token: tokens) {
					if (token.value().equals("{}")) {
						// {} denotes a new paragraph
						paragraphs.add(sentences);
						sentences = (ArrayList<ArrayList<String>>) sentences.clone();
						sentences.clear();	
					} else {
						sentPos.add(token.tag());
						sentence.add(token.value());
					}
				}
				sentiment= sentence.get(sentence.size()-2);
				testSentiments.put(sentence, sentiment);
				//Update sentiment tables
				sentences.add(sentence);
			}

			//Close input/output stream
			try {
				br.close();
				out.close();
			} catch(FileNotFoundException e) {
				System.out.println("That file doesn't exist. Try again.");
			} catch(IOException d) {
				System.out.println(d.getMessage());
			}	
		}
		return testDocuments;
	}
}
