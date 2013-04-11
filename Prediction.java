import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Prediction{
	static ArrayList<Integer> SR_DocRatings = new ArrayList<Integer>();
	static ArrayList<Integer> DS_DocRatings = new ArrayList<Integer>();
	static ArrayList<ArrayList<Integer>> SR_ParagraphRatings = new ArrayList<ArrayList<Integer>>();
	static ArrayList<ArrayList<Integer>> DS_ParagraphRatings = new ArrayList<ArrayList<Integer>>();
	static ArrayList<ArrayList<ArrayList<Integer>>> SR_SentenceRatings = new ArrayList<ArrayList<ArrayList<Integer>>>();
	static ArrayList<ArrayList<ArrayList<Integer>>> DS_SentenceRatings = new ArrayList<ArrayList<ArrayList<Integer>>>();

	static ArrayList<Integer> SR_DocRatings2 = new ArrayList<Integer>();
	static ArrayList<Integer> DS_DocRatings2 = new ArrayList<Integer>();
	static ArrayList<ArrayList<Integer>> SR_ParagraphRatings2 = new ArrayList<ArrayList<Integer>>();
	static ArrayList<ArrayList<Integer>> DS_ParagraphRatings2 = new ArrayList<ArrayList<Integer>>();
	static ArrayList<ArrayList<ArrayList<Integer>>> SR_SentenceRatings2 = new ArrayList<ArrayList<ArrayList<Integer>>>();
	static ArrayList<ArrayList<ArrayList<Integer>>> DS_SentenceRatings2 = new ArrayList<ArrayList<ArrayList<Integer>>>();

	static int numReview_R = 0;
	static int numReview_S = 0;

	public static void main(String[] args) throws IOException {
		WeightedAveragePrediction();
	}

	public static void ScottRenshawParagraphSentimentBasedPrediction() throws IOException {
		parse("data/ScottRenshaw_train_complete_first_25pc.txt", true, SR_DocRatings, SR_ParagraphRatings, SR_SentenceRatings);
		
		int[][] rateCounts_firstP = new int[5][11];
		int[][] rateCounts_lastP = new int[5][11];
		int[][][] rateCounts_firstLastP = new int[5][5][11];
		int[][][] rateCounts_lastTwoP = new int[5][5][11];

		int[] predict_firstP = new int[5];
		int[] predict_lastP = new int[5];
		int[][] predict_firstLastP = new int[5][5];
		int[][] predict_lastTwoP = new int[5][5];

		for(int i=0; i<SR_DocRatings.size(); i++){
			int last = SR_ParagraphRatings.get(i).size()-1;
			rateCounts_firstP[SR_ParagraphRatings.get(i).get(0)+2][SR_DocRatings.get(i)]++;
			rateCounts_lastP[SR_ParagraphRatings.get(i).get(last)+2][SR_DocRatings.get(i)]++;
			rateCounts_firstLastP[SR_ParagraphRatings.get(i).get(0)+2][SR_ParagraphRatings.get(i).get(last)+2][SR_DocRatings.get(i)]++;
			rateCounts_lastTwoP[SR_ParagraphRatings.get(i).get(last-1)+2][SR_ParagraphRatings.get(i).get(last)+2][SR_DocRatings.get(i)]++;
		}
		
		for(int i=0; i<rateCounts_firstP.length; i++){
			int max1=0, max2=0;
			predict_firstP[i] = (int)(i*5/2.+0.5);
			predict_lastP[i] = (int)(i*5/2.+0.5);
			for(int j=0; j<rateCounts_firstP[0].length; j++){
				if(rateCounts_firstP[i][j] > max1){
					max1 = rateCounts_firstP[i][j];
					predict_firstP[i] = j;
				}
				if(rateCounts_lastP[i][j] > max2){
					max2 = rateCounts_lastP[i][j];
					predict_lastP[i] = j;
				}
			}
		}
		for(int i=0; i<rateCounts_firstLastP.length; i++){
			for(int j=0; j<rateCounts_firstLastP[0].length; j++){
				int max3=0, max4=0;
				predict_firstLastP[i][j] = (int)((i+j)*5/4.+0.5);
				predict_lastTwoP[i][j] = (int)((i+j)*5/4.+0.5);
				for(int k=0; k<rateCounts_firstLastP[0][0].length; k++){
					if(rateCounts_firstLastP[i][j][k] > max3){
						max3 = rateCounts_firstLastP[i][j][k];
						predict_firstLastP[i][j] = k;
					}
					if(rateCounts_lastTwoP[i][j][k] > max4){
						max4 = rateCounts_lastTwoP[i][j][k];
						predict_lastTwoP[i][j] = k;
					}
				}
			}
		}

		parse("data/ScottRenshaw_train_complete_last_75pc.txt", true, SR_DocRatings2, SR_ParagraphRatings2, SR_SentenceRatings2);
		int error1 = 0, error2 = 0, error3 = 0, error4 = 0, numDocs = SR_DocRatings2.size();
		for(int i=0; i<numDocs; i++){
			int last = SR_ParagraphRatings2.get(i).size()-1;
			int diff1 = SR_DocRatings2.get(i)-predict_firstP[SR_ParagraphRatings2.get(i).get(0)+2];
			int diff2 = SR_DocRatings2.get(i)-predict_lastP[SR_ParagraphRatings2.get(i).get(last)+2];
			int diff3 = SR_DocRatings2.get(i)-predict_firstLastP[SR_ParagraphRatings2.get(i).get(0)+2][SR_ParagraphRatings2.get(i).get(last)+2];
			int diff4 = SR_DocRatings2.get(i)-predict_lastTwoP[SR_ParagraphRatings2.get(i).get(last-1)+2][SR_ParagraphRatings2.get(i).get(last)+2];
			
			error1 += diff1*diff1;
			error2 += diff2*diff2;
			error3 += diff3*diff3;
			error4 += diff4*diff4;
		}
		System.out.println("RMSE for using \nfirst paragraph sentiment: "+Math.sqrt(error1*1.0/numDocs)+"\nsecond paragraph sentiment: "+Math.sqrt(error2*1.0/numDocs)+
				"\nfirst and last paragraph sentiments: "+Math.sqrt(error3*1.0/numDocs)+"\nlast two paragraph sentiments: "+Math.sqrt(error4*1.0/numDocs));
	}

	public static void DennisSchwartzParagraphSentimentBasedPrediction() throws IOException {
		parse("data/DennisSchwartz_train_complete_first_75pc.txt", false, DS_DocRatings, DS_ParagraphRatings, DS_SentenceRatings);
		
		int[][] rateCounts_firstP = new int[5][13];
		int[][] rateCounts_lastP = new int[5][13];
		int[][][] rateCounts_firstLastP = new int[5][5][13];
		int[][][] rateCounts_lastTwoP = new int[5][5][13];

		int[] predict_firstP = new int[5];
		int[] predict_lastP = new int[5];
		int[][] predict_firstLastP = new int[5][5];
		int[][] predict_lastTwoP = new int[5][5];

		for(int i=0; i<DS_DocRatings.size(); i++){
			int last = DS_ParagraphRatings.get(i).size()-1;
			rateCounts_firstP[DS_ParagraphRatings.get(i).get(0)+2][DS_DocRatings.get(i)]++;
			rateCounts_lastP[DS_ParagraphRatings.get(i).get(last)+2][DS_DocRatings.get(i)]++;
			rateCounts_firstLastP[DS_ParagraphRatings.get(i).get(0)+2][DS_ParagraphRatings.get(i).get(last)+2][DS_DocRatings.get(i)]++;
			rateCounts_lastTwoP[DS_ParagraphRatings.get(i).get(last-1)+2][DS_ParagraphRatings.get(i).get(last)+2][DS_DocRatings.get(i)]++;
		}
		
		for(int i=0; i<rateCounts_firstP.length; i++){
			int max1=0, max2=0;
			predict_firstP[i] = (int)(i*3.+0.5);
			predict_lastP[i] = (int)(i*3.+0.5);
			for(int j=0; j<rateCounts_firstP[0].length; j++){
				if(rateCounts_firstP[i][j] > max1){
					max1 = rateCounts_firstP[i][j];
					predict_firstP[i] = j;
				}
				if(rateCounts_lastP[i][j] > max2){
					max2 = rateCounts_lastP[i][j];
					predict_lastP[i] = j;
				}
			}
		}
		for(int i=0; i<rateCounts_firstLastP.length; i++){
			for(int j=0; j<rateCounts_firstLastP[0].length; j++){
				int max3=0, max4=0;
				predict_firstLastP[i][j] = (int)((i+j)*3/2.+0.5);
				predict_lastTwoP[i][j] = (int)((i+j)*3/2.+0.5);
				for(int k=0; k<rateCounts_firstLastP[0][0].length; k++){
					if(rateCounts_firstLastP[i][j][k] > max3){
						max3 = rateCounts_firstLastP[i][j][k];
						predict_firstLastP[i][j] = k;
					}
					if(rateCounts_lastTwoP[i][j][k] > max4){
						max4 = rateCounts_lastTwoP[i][j][k];
						predict_lastTwoP[i][j] = k;
					}
				}
			}
		}

		parse("data/DennisSchwartz_train_complete_last_25pc.txt", false, DS_DocRatings2, DS_ParagraphRatings2, DS_SentenceRatings2);
		int error1 = 0, error2 = 0, error3 = 0, error4 = 0, numDocs = DS_DocRatings2.size();
		for(int i=0; i<numDocs; i++){
			int last = DS_ParagraphRatings2.get(i).size()-1;
			int diff1 = DS_DocRatings2.get(i)-predict_firstP[DS_ParagraphRatings2.get(i).get(0)+2];
			int diff2 = DS_DocRatings2.get(i)-predict_lastP[DS_ParagraphRatings2.get(i).get(last)+2];
			int diff3 = DS_DocRatings2.get(i)-predict_firstLastP[DS_ParagraphRatings2.get(i).get(0)+2][DS_ParagraphRatings2.get(i).get(last)+2];
			int diff4 = DS_DocRatings2.get(i)-predict_lastTwoP[DS_ParagraphRatings2.get(i).get(last-1)+2][DS_ParagraphRatings2.get(i).get(last)+2];
			
			error1 += diff1*diff1;
			error2 += diff2*diff2;
			error3 += diff3*diff3;
			error4 += diff4*diff4;
		}
		System.out.println("RMSE for using \nfirst paragraph sentiment: "+Math.sqrt(error1*25./36/numDocs)+"\nsecond paragraph sentiment: "+Math.sqrt(error2*25./36/numDocs)+
				"\nfirst and last paragraph sentiments: "+Math.sqrt(error3*25./36/numDocs)+"\nlast two paragraph sentiments: "+Math.sqrt(error4*25./36/numDocs));
	}

	public static void WeightedAveragePrediction() throws IOException {
		double[][] cumul_rating_errors = new double[20][20];
		ScottRenshawWeightedAveragePrediction("data/ScottRenshaw_train_complete.txt", cumul_rating_errors);
		DennisSchwartzWeightedAveragePrediction("data/DennisSchwartz_train_complete.txt", cumul_rating_errors);

		int min_i = 0, min_j = 0;
		double min_error = Double.MAX_VALUE;
		cumul_rating_errors[0][0] = Double.MAX_VALUE;
		for(int i=0; i<cumul_rating_errors.length; i++){
			for(int j=0; j<cumul_rating_errors[0].length; j++){
				if(cumul_rating_errors[i][j] < min_error){
					min_i = i;
					min_j = j;
					min_error = cumul_rating_errors[i][j];
				}
			}
		}
		System.out.println("minimum total rating error at " + min_i + " " + min_j + ": " + min_error);
	}

	public static void parse(String fileName, boolean numScore, ArrayList<Integer> doc, 
			ArrayList<ArrayList<Integer>> paragraph, ArrayList<ArrayList<ArrayList<Integer>>> sentence) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));

		String line = br.readLine();
		int numReview = 0;

		while(line != null){
			int ind1 = line.indexOf('/');
			int ind2 = line.indexOf(']');
			ArrayList<Integer> parRatings = null;
			ArrayList<ArrayList<Integer>> sentRatings = null;
			// beginning of a review
			if(ind1 != -1){
				parRatings = new ArrayList<Integer>();
				sentRatings = new ArrayList<ArrayList<Integer>>(); 

				// Scott Renshaw rating in the form of [10184/8]
				if(numScore){
					doc.add(Integer.parseInt(line.substring(ind1+1,ind2)));
				}
				else{
					doc.add(letter_to_score(line.substring(ind1+1,ind2)));
				}
				numReview++;
				line = br.readLine();
				int numLines = 0;
				int numParagraphs = 0;

				ArrayList<Integer> sentences = null;  

				// it is not a beginning of a review 
				while(line != null && line.indexOf('/') == -1){
					int ind3 = line.indexOf('<');

					if(ind3 != -1){
						// beginning of a paragraph
						if(line.charAt(0) == '{'){
							parRatings.add(Integer.parseInt(line.substring(1, line.indexOf('}'))));
							if(sentences != null){
								sentRatings.add(sentences);
							}
							sentences = new ArrayList<Integer>();
						}
						sentences.add(Integer.parseInt(line.substring(ind3+1, line.length()-1)));
						numLines++;
					}
					line = br.readLine();
				}
				sentRatings.add(sentences);
				paragraph.add(parRatings);
			}
			sentence.add(sentRatings);
		}
		br.close();
		System.out.println("done!");
	}

	public static void ScottRenshawWeightedAveragePrediction(String fileName, double[][] cumul_rating_errors) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		int[][] rating_sq_errors = new int[20][20];
		double[][] rmse = new double[20][20];

		String line = br.readLine();
		int numReview = 0;
		// Scott Renshaw rating in the form of [10184/8]
		while(line != null){
			int ind1 = line.indexOf('/');
			int ind2 = line.indexOf(']');
			// beginning of a review
			if(ind1 != -1){
				int actual_rating = Integer.parseInt(line.substring(ind1+1,ind2));
				numReview++;
				line = br.readLine();
				int numLines = 0;
				int numParagraphs = 0;
				int sumSentenceRatings = 0;
				int sumParagraphRatings = 0;
				// it is not a beginning of a review 
				while(line != null && line.indexOf('/') == -1){
					int ind3 = line.indexOf('<');
					if(ind3 != -1){
						// beginning of a paragraph
						if(line.charAt(0) == '{'){
							sumParagraphRatings += Integer.parseInt(line.substring(1, line.indexOf('}')));
							numParagraphs++;
						}
						sumSentenceRatings += Integer.parseInt(line.substring(ind3+1, line.length()-1));
						numLines++;
					}
					line = br.readLine();
				}

				for(int i=0; i<rating_sq_errors.length; i++){
					for(int j=0; j<rating_sq_errors[0].length; j++){
						if(i+j == 0){
							rating_sq_errors[i][j] = Integer.MAX_VALUE;
							rmse[i][j] = Double.MAX_VALUE;
						}
						else{
							int predicted_rating = (int)(0.5+((i*sumSentenceRatings+j*sumParagraphRatings)*1.0/(i*numLines+j*numParagraphs)+2)*5/2);
							rating_sq_errors[i][j] += (predicted_rating-actual_rating)*(predicted_rating-actual_rating);
							rmse[i][j] = Math.sqrt(rating_sq_errors[i][j]*1.0/numReview);
						}
					}
				}
			}
		}
		br.close();

		int min_i = 0, min_j = 0, min_error = Integer.MAX_VALUE;
		for(int i=0; i<rating_sq_errors.length; i++){
			for(int j=0; j<rating_sq_errors[0].length; j++){
				if(rating_sq_errors[i][j] < min_error){
					min_i = i;
					min_j = j;
					min_error = rating_sq_errors[i][j];
				}
				cumul_rating_errors[i][j] += rmse[i][j];
			}
		}
		System.out.println("Scott Renshaw minimum total squared error at " + min_i + " " + min_j + ": " + min_error + ", root mean squared error: " + rmse[min_i][min_j]);//(min_error*1.0/numReview));

	}

	public static void DennisSchwartzWeightedAveragePrediction(String fileName, double[][] cumul_rating_errors) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		int[][] rating_sq_errors = new int[20][20];
		double[][] rmse = new double[20][20];

		String line = br.readLine();
		int numReview = 0;
		// Dennis Schwartz rating in the form of [10184/b+]
		while(line != null){
			int ind1 = line.indexOf('/');
			int ind2 = line.indexOf(']');
			// beginning of a review
			if(ind1 != -1){
				int actual_rating = letter_to_score(line.substring(ind1+1,ind2));
				numReview++;
				line = br.readLine();
				int numLines = 0;
				int numParagraphs = 0;
				int sumSentenceRatings = 0;
				int sumParagraphRatings = 0;
				// it is not a beginning of a review 
				while(line != null && line.indexOf('/') == -1){
					int ind3 = line.indexOf('<');
					if(ind3 != -1){
						// beginning of a paragraph
						if(line.charAt(0) == '{'){
							sumParagraphRatings += Integer.parseInt(line.substring(1, line.indexOf('}')));
							numParagraphs++;
						}
						sumSentenceRatings += Integer.parseInt(line.substring(ind3+1, line.length()-1));
						numLines++;
					}
					line = br.readLine();
				}

				for(int i=0; i<rating_sq_errors.length; i++){
					for(int j=0; j<rating_sq_errors[0].length; j++){
						if(i+j == 0){
							rating_sq_errors[i][j] = Integer.MAX_VALUE;
							rmse[i][j] = Double.MAX_VALUE;
						}
						else{
							int predicted_rating = (int)(0.5+((i*sumSentenceRatings+j*sumParagraphRatings)*1.0/(i*numLines+j*numParagraphs)+2)*3);
							rating_sq_errors[i][j] += (predicted_rating-actual_rating)*(predicted_rating-actual_rating);
							// multiply by 25/36 to change error in 0-12 scale to 0-10 scale
							rmse[i][j] = Math.sqrt(rating_sq_errors[i][j]*25./36./numReview);
						}
					}
				}
			}
		}
		br.close();

		int min_i = 0, min_j = 0, min_error = Integer.MAX_VALUE;
		for(int i=0; i<rating_sq_errors.length; i++){
			for(int j=0; j<rating_sq_errors[0].length; j++){
				if(rating_sq_errors[i][j] < min_error){
					min_i = i;
					min_j = j;
					min_error = rating_sq_errors[i][j];
				}
				cumul_rating_errors[i][j] += rmse[i][j];
			}
		}
		System.out.println("Dennis Schwartz minimum total squared error at " + min_i + " " + min_j + ": " + min_error + ", root mean squared error: " + rmse[min_i][min_j]);
	}

	public static int letter_to_score(String letter){
		switch(letter){
		case "a+":
			return 12;
		case "a":
			return 11;
		case "a-":
			return 10;
		case "b+":
			return 9;
		case "b":
			return 8;
		case "b-":
			return 7;
		case "c+":
			return 6;
		case "c":
			return 5;
		case "c-":
			return 4;
		case "d+":
			return 3;
		case "d":
			return 2;
		case "d-":
			return 1;
		case "f":
			return 0;
		}
		return 0;
	}
}