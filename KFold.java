import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class KFold {
	
	public static void main(String[] args) throws IOException {
		Double d = 0.0;
		String trainBase = "./validation/ScottRenshaw_train";
		String testBase = "./validation/ScottRenshaw_test";
		for (int i = 1; i <= 5; i++) {
			String trainFile = trainBase + i + ".txt";
			String testFile = testBase + i + ".txt";
			HMM model = new HMM(trainFile, testFile, "./validation/predictions.txt");
			d += check(testFile, "./validation/predictions.txt");
		}
		System.out.println(d/5 + "% accuracy");
	}
	
	public static Double check(String originalFile, String predictionsFile) throws IOException {
		Double numCorrect = 0.0;
		BufferedReader original = new BufferedReader(new FileReader(originalFile));
		BufferedReader predictions = new BufferedReader(new FileReader(predictionsFile));
		Double numLines = 0.0;
		while (true) {
			String originalLine = original.readLine();
			String prediction = predictions.readLine();
			if (originalLine==null) break;
			if (originalLine.length()!=0) {
				int endBracket = originalLine.length()-1;
				int startBracket = originalLine.lastIndexOf('<');
				String correctSentiment = originalLine.substring(startBracket+1, endBracket);
				numLines++;
				if (correctSentiment.equals(prediction)) {
					numCorrect += 1.0;
				}
			}
			else {
				// Do nothing
			}
			
		}
		original.close();
		predictions.close();

		return numCorrect / numLines;
	}

}
