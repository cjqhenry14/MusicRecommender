package hybrid;

import java.io.IOException;

import validation.measure;

public class CallHybridRec {
	
	public static void main(String[] args)  throws IOException {
		//float social_w, float tag_w, float bayes_w
		measure me = new measure();
		for(float i = 0.1f; i<= 5.0f; i = i + 0.5f){
			System.out.println("hybrid (so_w: " + i + " tag_w: "+ 1 +") start...");
			HybridRec hr = new HybridRec(i, 1, 0);
			hr.start_recommend();
			me.call_measure("results_analysis/hybrid_res.dat");
		}
	}
}
