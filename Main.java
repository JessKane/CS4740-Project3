import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		//HMM schwartz = new HMM("data/DennisSchwartz_train.txt","data/DennisSchwartz_train.txt","data/DennisSchwartz_train_results.txt", "dennis");
		//System.out.println(KFold.check("data/DennisSchwartz_train.txt", "data/DennisSchwartz_train_results.txt"));
		//HMM renshaw = new HMM("data/ScottRenshaw_train.txt","data/ScottRenshaw_train.txt","data/ScottRenshaw_train_results.txt", "scott");
		//System.out.println(KFold.check("data/ScottRenshaw_train.txt", "data/ScottRenshaw_train_results.txt"));
		
		HMM test = new HMM("data/DennisSchwartz_train.txt","data/DennisSchwartz_test.txt","data/DennisSchwartz_test_results.txt", "dennis");
		HMM test2 = new HMM("data/ScottRenshaw_train.txt","data/ScottRenshaw_test.txt","data/ScottRenshaw_test_results.txt", "scott");
		
		//HMM renshaw = new HMM("data/ScottRenshaw_train.txt","data/test.txt","data/test_results.txt");
	}
}
