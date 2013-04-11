import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class KFold {

	public static void main(String[] args) throws IOException {
		Double d = 0.0;
		String trainBase = "./validation/ScottRenshaw_train";
		String testBase = "./validation/ScottRenshaw_test";
		//String trainBase = "./validation/DennisSchwartz_train";
		//String testBase = "./validation/DennisSchwartz_test";		
		for (int i = 1; i <= 5; i++) {
			String trainFile = trainBase + i + ".txt";
			String testFile = testBase + i + ".txt";
			new HMM(trainFile, testFile, "./validation/predictions.txt", "scott");
			d += Math.pow(check(testFile, "./validation/predictions.txt"), .5);
		}
		System.out.println(d/5 + " AVG RMSE");
	}

	public static Double check(String originalFile, String predictionsFile) throws IOException {
		Double numLines = 0.0;
		Double error=0.0;
		BufferedReader original = new BufferedReader(new FileReader(originalFile));
		BufferedReader predictions = new BufferedReader(new FileReader(predictionsFile));

		String predictionLine;
		String originalLine;
		while (true) {
			originalLine = original.readLine();
			if (originalLine==null) break;
			if (originalLine.endsWith(">")) {
				predictionLine = predictions.readLine();
				int endBracket = originalLine.lastIndexOf('>');
				int startBracket = originalLine.lastIndexOf('<');
				String correctSentiment = originalLine.substring(startBracket+1, endBracket);
				numLines++;
				error+=Math.pow(Integer.parseInt(correctSentiment)-((int)Double.parseDouble(predictionLine)),2);

			}
		}
		original.close();
		predictions.close();

		return error / numLines;
	}
}
