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
	private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> documents = new ArrayList<ArrayList<ArrayList<ArrayList<String>>>>();
	private HashMap<ArrayList<String>, String> sentiments = new HashMap<ArrayList<String>, String>();		// "full sentence":"sentiment"
	private HashMap<ArrayList<String>, ArrayList<String>> POSs = new HashMap<ArrayList<String>, ArrayList<String>>();		// "full sentence":"list of POSs"
	private HashMap<ArrayList<String>, List<CoreLabel>> tokens = new HashMap<ArrayList<String>, List<CoreLabel>>();		// "full sentence":"list of tokens"


	private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> testDocuments = new ArrayList<ArrayList<ArrayList<ArrayList<String>>>>();
	private HashMap<ArrayList<String>, String> testSentiments = new HashMap<ArrayList<String>, String>();		// "full sentence":"sentiment"
	private HashMap<ArrayList<String>, ArrayList<String>> testPOSs = new HashMap<ArrayList<String>, ArrayList<String>>();		// "full sentence":"list of POSs"
	private HashMap<ArrayList<String>, List<CoreLabel>> testTokens = new HashMap<ArrayList<String>, List<CoreLabel>>();		// "full sentence":"list of tokens"


	/* Stanford NLP modelling pipeline, used in annotation. */
	protected StanfordCoreNLP pipeline;

	/* Constructs a Parse object with transition frequencies */
	public HMM(String trainLoc String testLoc) {
		readFile(trainLoc);
		System.out.println("documents size: " + documents.size());
		System.out.println("review 1 # of paragraphs: " + documents.get(0).size());
		sentimentModel();
		System.out.println(emissions.getSentiments().get("-2").get(0).get("nice"));
		System.out.println(emissions.calcProb("-2","JJ","nice"));
		System.out.println();
		int r=1,s=6;
		System.out.println(emissions.sentProb(-2+"",getSentence(documents.get(r),s),POSs.get(getSentence(documents.get(r),s))));
		System.out.println(emissions.sentProb(-1+"",getSentence(documents.get(r),s),POSs.get(getSentence(documents.get(r),s))));
		System.out.println(emissions.sentProb(0+"",getSentence(documents.get(r),s),POSs.get(getSentence(documents.get(r),s))));
		System.out.println(emissions.sentProb(1+"",getSentence(documents.get(r),s),POSs.get(getSentence(documents.get(r),s))));
		System.out.println(emissions.sentProb(2+"",getSentence(documents.get(r),s),POSs.get(getSentence(documents.get(r),s))));

		System.out.println();
		viterbi(documents.get(0));
	}

	public void viterbi(ArrayList<ArrayList<ArrayList<String>>> review){
		double[][] T1 = new double[5][sentenceCount(review)];
		double[][] T2 = new double[5][sentenceCount(review)];


		for (int i=-2; i<=2; i++){
			T1[i+2][0]=findPercent(i+"")*emissions.sentProb(i+"",getSentence(review,0), POSs.get(getSentence(review,0))); //TODO remove nulls, see emails
			T2[i+2][0]=0;
		}

		double innerProb,innerArg,maxProb=-100,maxArg=-100;

		for (int i=1; i<sentenceCount(review); i++){
			for (int j=-2; j<=2; j++){
				maxProb=-100;maxArg=-100;
				for (int k=-2; k<=2; k++){
					innerProb=T1[k+2][i-1]*findPercent(k+"",j+"")*emissions.sentProb(j+"",getSentence(review,i),POSs.get(getSentence(review,i)));
					innerArg=k;

					if (innerProb>maxProb){
						maxProb=innerProb;
						maxArg=innerArg;
					}
				}
				T1[j+2][i]=maxProb;
				T2[j+2][i]=maxArg;
			}
		}

		innerProb=0;maxProb=0;maxArg=-100;innerArg=-100;
		for (int s=-2; s<=2; s++){
			innerProb=T1[s+2][sentenceCount(review)-1];
			if (innerProb>maxProb){
				maxProb=innerProb;
				maxArg=s;
			}
		}
		double[] prediction= new double[sentenceCount(review)];
		prediction[sentenceCount(review)-1]=maxArg;
		
		for (int i=sentenceCount(review)-1;i>1;i--){
			prediction[i-1]=T2[(int) prediction[i]+2][i];
		}
		print(prediction);
	}

	private void print(double[] prediction) {
		for (Double i : prediction)
			System.out.println(i);
	}

	private int sentenceCount(ArrayList<ArrayList<ArrayList<String>>> review) {
		int count=0;
		for (ArrayList<ArrayList<String>> para : review){
			for (ArrayList<String> sent : para){
				count++;
			}
		}
		return count;
	}

	//Get the ith sentence in the document, avoiding paragraph mumbojumbo
	private ArrayList<String> getSentence(ArrayList<ArrayList<ArrayList<String>>> review, int i) {
		int count=0;
		for (ArrayList<ArrayList<String>> para : review){
			for (ArrayList<String> sent : para){
				if (count==i) return sent;
				count++;
			}
		}
		return null;
	}

	/* Reads through the file and generates a sentiment bigram model, and updates
	 *  emission feature counts */
	public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> readFile(String fileLoc) {

		// Lists of paragraphs and sentences
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
			boolean firstParagraph = true;
			while(true){
				// Read current line, skipping over if a review header [xxxx/xx],
				// and breaking the loop if the end has been reached
				String line= br.readLine();
				//				System.out.println(line.length() + " " + line);

				while(line.length()==0 || line.charAt(line.length()-1) != '>') {

					if (line.length()==0) {
						// Do nothing
					}
					// If it's [xxxx/xx], switch documents
					else if (line.charAt(line.length()-1) != '>') {
						if (firstParagraph) {
							firstParagraph = false;
						} else {
							documents.add(paragraphs);
							paragraphs = new ArrayList<ArrayList<ArrayList<String>>>();
							sentences = new ArrayList<ArrayList<String>>();
						}
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
				List<CoreLabel> allTokens = lineAnno.get(TokensAnnotation.class);
				// Iterate over all of the tokens on a line
				for (CoreLabel token: allTokens) {
					if (token.value().equals("-LCB-")) {
						// {} denotes a new paragraph
						if (sentences.size() != 0) {
							paragraphs.add(sentences);
							sentences = new ArrayList<ArrayList<String>>();
						}
					} else if (token.value().equals("-RCB-")) {
						// do nothing...
					} else {
						sentPos.add(token.tag());
						sentence.add(token.value());
					}
				}
				sentiment= sentence.get(sentence.size()-2);
				sentiments.put(sentence, sentiment);
				POSs.put(sentence, sentPos);
				tokens.put(sentence, allTokens);
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
	public void sentimentModel() {
		for (ArrayList<ArrayList<ArrayList<String>>> review : documents) {
			for (int i = 0; i < review.size(); i++) { // traversing review by paragraph
				//				System.out.println("Paragraph " + i);
				for (int j = 0; j < review.get(i).size(); j++) { // traversing paragraph by sentence
					//					System.out.println("Sentence " + j);
					ArrayList<String> currSent = null;
					ArrayList<String> nextSent = null;
					if (i == review.size()-1 && j == review.get(i).size()-1) { // if last sentence of review
						//						System.out.println(review.get(i));
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
	
	private double findPercent(String sentiment) {
		return (double)sum(transitions.get(sentiment).values())/(double)sumInner(transitions);
	}

	private double sumInner( HashMap<String, HashMap<String, Double>> values) {
		double sum= 0.0; 
		for (HashMap<String, Double> i : values.values()){
			for (Double j : i.values()){
				sum = sum + j;
			}
		}
		return sum;
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
		// Lists of paragraphs and sentences
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
			boolean firstParagraph = true;

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

					if (line.length()==0) {
						// do nothing
					}

					// If it's [xxxx/xx], switch documents
					else if (line.charAt(line.length()-1) != '>') {
						if (firstParagraph) {
							firstParagraph = false;
						} else {
							testDocuments.add(paragraphs);
							paragraphs = new ArrayList<ArrayList<ArrayList<String>>>();
							sentences = new ArrayList<ArrayList<String>>();
						}
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

				// Sentiment value for current line
				String sentiment= null;
				// Stanford stuff
				Annotation lineAnno= new Annotation(line);
				this.pipeline.annotate(lineAnno);
				List<CoreLabel> allTokens = lineAnno.get(TokensAnnotation.class);
				// Iterate over all of the tokens on a line
				for (CoreLabel token: allTokens) {
					if (token.value().equals("-LCB-")) {
						// {} denotes a new paragraph
						paragraphs.add(sentences);
						sentences = new ArrayList<ArrayList<String>>();
					} else if (token.value().equals("-RCB-")) {
						// do nothing
					} else {
						sentPos.add(token.tag());
						sentence.add(token.value());
					}
				}

				// TODO: actually predict the sentiment
				//sentiment= sentence.get(sentence.size()-2);
				//testSentiments.put(sentence, sentiment);
				//testPOSs.put(sentence, sentPos);
				//testTokens.put(sentence, allTokens);

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

	public HashMap<ArrayList<String>, String> getSentiments() {
		return sentiments;
	}

	public HashMap<ArrayList<String>, String> getResults() {
		return testSentiments;
	}
}
