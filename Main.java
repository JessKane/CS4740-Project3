import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		//HMM schwartz = new HMM("data/DennisSchwartz_train.txt","data/DennisSchwartz_train.txt","data/DennisSchwartz_train_results.txt");
		//System.out.println(KFold.check("data/DennisSchwartz_train.txt", "data/DennisSchwartz_train_results.txt"));
		//HMM renshaw = new HMM("data/ScottRenshaw_train.txt","data/ScottRenshaw_train.txt","data/ScottRenshaw_train_results.txt");
		//System.out.println(KFold.check("data/ScottRenshaw_train.txt", "data/ScottRenshaw_train_results.txt"));
<<<<<<< HEAD

		HMM test = new HMM("data/DennisSchwartz_train.txt","data/DennisSchwartz_test.txt","data/DennisSchwartz_test_results.txt");
		HMM test2 = new HMM("data/ScottRenshaw_train.txt","data/ScottRenshaw_test.txt","data/ScottRenshaw_test_results.txt");

=======
		
		HMM test = new HMM("data/DennisSchwartz_train.txt","data/DennisSchwartz_test.txt","data/DennisSchwartz_test_results.txt");
		HMM test2 = new HMM("data/ScottRenshaw_train.txt","data/ScottRenshaw_test.txt","data/ScottRenshaw_test_results.txt");
		
>>>>>>> 46e06d20f1b58f0da1552555310f2da41f87f211
		//HMM renshaw = new HMM("data/ScottRenshaw_train.txt","data/test.txt","data/test_results.txt");
	}
}
