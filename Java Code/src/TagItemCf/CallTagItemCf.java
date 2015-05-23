package TagItemCf;

import java.io.IOException;

import validation.measure;

public class CallTagItemCf {

	public static void main(String[] args) throws IOException {
		String tag_itemcf_rec_res_file = "results_analysis/tag_itemcf_rec_res.dat";
		
		
		
		for(int i = 10; i<= 20; i = i + 5){
			System.out.println("Item CF (rec_num = " + i + ") start...");
			
			TagItemCf tic = new TagItemCf(0.2f, 400, i, tag_itemcf_rec_res_file);//Base (float alpha, int k, int rec_num)
			//System.out.println("Tag Item Cf recommending...");
			tic.start_recommend();
			//System.out.println("Tag Item Cf ok");
			measure me = new measure();
			me.call_measure(tag_itemcf_rec_res_file);
			
		}

	}

}
