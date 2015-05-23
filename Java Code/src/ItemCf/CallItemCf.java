package ItemCf;

import java.io.IOException;

import validation.measure;

public class CallItemCf {

	public static void main(String[] args) throws IOException {
		
		String itemcf_rec_res_file = "results_analysis/itemcf_rec_res.dat";
		
		for(int i = 5; i<= 5; i = i + 5){
			System.out.println("Item CF (rec_num = " + i + ") start...");
			ItemCf ic = new ItemCf(400, i, itemcf_rec_res_file);//Base (int k, int rec_num, itemcf_rec_res_file)
			//System.out.println("Item Cf recommending...");
			ic.start_recommend();
			//System.out.println("Item Cf ok");
			measure me = new measure();
			me.call_measure(itemcf_rec_res_file);
		}

	}

}
