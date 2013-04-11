import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
//		HMM renshaw = new HMM("data/ScottRenshaw_train");
		//HMM schwartz = new HMM("data/DennisSchwartz_train.txt");
		HMM schwartz = new HMM("data/DennisSchwartz_train.txt","data/DennisSchwartz_train.txt","data/DennisSchwartz_train_results.txt");
		System.out.println(KFold.check("data/DennisSchwartz_train.txt", "data/DennisSchwartz_train_results.txt"));
		HMM renshaw = new HMM("data/ScottRenshaw_train.txt","data/ScottRenshaw_train.txt","data/ScottRenshaw_train_results.txt");
		System.out.println(KFold.check("data/ScottRenshaw_train.txt", "data/ScottRenshaw_train_results.txt"));
	}
}
