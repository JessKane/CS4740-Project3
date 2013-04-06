import java.util.ArrayList;
import java.util.HashMap;


public class KFold {
  
	public static void main(String[] args) {
		HMM original = new HMM("./train/ScottRenshaw_train.txt");
		HashMap<ArrayList<String>, String> correctSentiments = original.getSentiments();
		Double d = 0.0;
		String trainBase = "./validation/ScottRenshaw_train";
		String testBase = ".validation/ScottRenshaw_test";
		for (int i = 1; i <= 5; i++) {
			String trainFile = trainBase + i + ".txt";
			String testFile = testBase + i + ".txt";
			HMM model = new HMM(trainFile);
			model.parseTestData(testFile);
			HashMap<ArrayList<String>, String> predicted = model.getResults();
			d += check(correctSentiments, predicted);
		}
		System.out.println(d/5 + "% accuracy");
	}
	
	public static Double check(HashMap<ArrayList<String>, String> checkedAgainst, HashMap<ArrayList<String>, String> fileToCheck) {
		Double numCorrect = 0.0;
		for (ArrayList<String> sentence : fileToCheck.keySet()) {
			if (fileToCheck.get(sentence).equals(checkedAgainst.get(sentence))) {
				numCorrect = numCorrect + 1.0;
			}
		}
		return numCorrect / fileToCheck.size();
	}

}
